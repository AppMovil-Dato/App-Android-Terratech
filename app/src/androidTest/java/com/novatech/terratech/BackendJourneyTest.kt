package com.novatech.terratech

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.UUID
import org.junit.*
import org.junit.runner.RunWith

/**
 * Explicitly selected by scripts/verify-backend-journey.py against an isolated Development backend.
 */
@RunWith(AndroidJUnit4::class)
class BackendJourneyTest {
  @get:Rule val compose = createAndroidComposeRule<MainActivity>()

  private fun text(id: Int) = compose.activity.getString(id)

  private fun scroll(node: SemanticsNodeInteraction) {
    try {
      node.performScrollTo()
    } catch (e: AssertionError) {
      if (!e.message.orEmpty().contains("no parent layout")) throw e
    }
  }

  private fun input(id: Int, value: String) {
    val node = compose.onNodeWithText(text(id))
    scroll(node)
    node.performTextReplacement(value)
  }

  private fun click(id: Int) {
    val matcher = hasText(text(id)) and hasClickAction()
    if (compose.onAllNodes(matcher).fetchSemanticsNodes().isEmpty())
      compose.onAllNodes(hasScrollToIndexAction()).onLast().performScrollToNode(matcher)
    val node = compose.onNode(matcher)
    scroll(node)
    compose.waitUntil(15000) { runCatching { node.assertIsEnabled() }.isSuccess }
    node.performClick()
  }

  private fun visible(id: Int, timeout: Long = 15000) {
    compose.waitUntil(timeout) {
      compose.onAllNodesWithText(text(id)).fetchSemanticsNodes().isNotEmpty()
    }
  }

  private fun shell(command: String) {
    InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command).use {
      java.io.FileInputStream(it.fileDescriptor).readBytes()
    }
  }

  private fun capture(name: String) {
    compose.waitForIdle()
    shell("screencap -p /sdcard/Download/terratech-$name.png")
  }

  @Test
  fun completeJourneyWithOfflineAndLogout() {
    val args = InstrumentationRegistry.getArguments()
    Assume.assumeTrue(
      "Run the isolated backend harness for this integration",
      args.getString("tb1BackendJourney") == "true",
    )
    val email = "android-${UUID.randomUUID()}@test.example"
    val password = "Tb1-test-${UUID.randomUUID()}"
    visible(R.string.welcome)
    capture("login")
    click(R.string.no_account)
    input(R.string.full_name, "Ana Torres")
    input(R.string.email, email)
    input(R.string.password, password)
    input(R.string.confirmation, "wrong-confirmation")
    click(R.string.register)
    visible(R.string.error_confirmation)
    input(R.string.confirmation, password)
    click(R.string.register)
    visible(R.string.registered)
    click(R.string.no_account)
    input(R.string.password, password)
    input(R.string.confirmation, password)
    click(R.string.register)
    visible(R.string.error_duplicate)
    click(R.string.have_account)
    input(R.string.email, email)
    input(R.string.password, "wrong-password")
    click(R.string.login)
    visible(R.string.error_credentials)
    input(R.string.password, password)
    click(R.string.login)
    visible(R.string.use_profile)
    click(R.string.use_profile)
    visible(R.string.farm_name)
    input(R.string.farm_name, "Fundo Android")
    input(R.string.phone, "999888777")
    input(R.string.location, "Huaral, Lima")
    input(R.string.area_ha, "1.25")
    click(R.string.save)
    visible(R.string.profile_saved)
    click(R.string.edit_profile)
    input(R.string.full_name, "Ana Torres Vega")
    input(R.string.area_ha, "2.25")
    click(R.string.save)
    visible(R.string.profile_saved)
    compose.onNodeWithText("Ana Torres Vega").assertExists()
    capture("profile")
    click(R.string.fields)
    visible(R.string.new_field)
    click(R.string.new_field)
    input(R.string.field_name, "Parcela Norte")
    input(R.string.crop, "Papa")
    input(R.string.area_ha, "0.5")
    input(R.string.soil, "Franco")
    input(R.string.latitude, "-11.5")
    input(R.string.longitude, "-77.2")
    click(R.string.save)
    visible(R.string.my_fields)
    compose.waitUntil(10000) {
      compose.onAllNodesWithText("Parcela Norte").fetchSemanticsNodes().isNotEmpty()
    }
    capture("fields")
    click(R.string.choose_field)
    visible(R.string.associate)
    click(R.string.associate)
    input(R.string.sensor_code, "BAD")
    input(R.string.sensor_name, "Sensor Norte")
    click(R.string.associate)
    visible(R.string.error_sensor)
    input(R.string.sensor_code, "TT-000000")
    click(R.string.associate)
    visible(R.string.error_sensor)
    input(R.string.sensor_code, "TT-ZZZ001")
    click(R.string.associate)
    visible(R.string.view_sensor)
    click(R.string.associate)
    input(R.string.sensor_code, "TT-ZZZ001")
    input(R.string.sensor_name, "Repeated sensor")
    click(R.string.associate)
    visible(R.string.error_occupied)
    click(R.string.back)
    click(R.string.view_sensor)
    // The harness notices the association and generates readings through the explicit backend CLI.
    compose.waitUntil(60000) {
      compose.onAllNodesWithText("SIMULATED").fetchSemanticsNodes().isNotEmpty().also { found ->
        if (!found)
          compose
            .onAllNodesWithText(text(R.string.retry))
            .fetchSemanticsNodes()
            .firstOrNull()
            ?.let { compose.onNodeWithText(text(R.string.retry)).performClick() }
      }
    }
    capture("sensor")
    click(R.string.view_history)
    visible(R.string.days_30)
    click(R.string.days_30)
    compose.waitUntil(30000) {
      compose
        .onAllNodes(hasText("%", substring = true) and hasClickAction())
        .fetchSemanticsNodes()
        .isNotEmpty()
    }
    compose.waitUntil(30000) {
      compose
        .onAllNodesWithText(compose.activity.getString(R.string.reading_count, 720))
        .fetchSemanticsNodes()
        .isNotEmpty()
    }
    capture("history")
    compose.onAllNodes(hasText("%", substring = true) and hasClickAction()).onFirst().performClick()
    visible(R.string.reading_detail)
    capture("reading-detail")
    shell("svc wifi disable")
    shell("svc data disable")
    try {
      click(R.string.home)
      click(R.string.retry)
      visible(R.string.offline, 30000)
      capture("offline")
      compose.activityRule.scenario.recreate()
      visible(R.string.offline, 30000)
      compose.waitUntil(15000) {
        compose.onAllNodesWithText("SIMULATED").fetchSemanticsNodes().isNotEmpty()
      }
      capture("offline-recreated")
    } finally {
      shell("svc wifi enable")
      shell("svc data enable")
    }
    click(R.string.profile)
    click(R.string.logout)
    visible(R.string.logout_title)
    compose.onAllNodesWithText(text(R.string.logout)).onLast().performClick()
    visible(R.string.welcome)
    // A second account must start with neither fields nor profile from the first account.
    click(R.string.no_account)
    input(R.string.full_name, "Bruno Rojas")
    input(R.string.email, "android-${UUID.randomUUID()}@test.example")
    input(R.string.password, password)
    input(R.string.confirmation, password)
    click(R.string.register)
    visible(R.string.registered)
    input(R.string.password, password)
    click(R.string.login)
    visible(R.string.use_profile)
    click(R.string.fields)
    visible(R.string.no_fields)
    compose.onAllNodesWithText("Parcela Norte").assertCountEquals(0)
    capture("second-account")
    // Leave the first account fully downloaded for the host's actual process restart without
    // network.
    click(R.string.profile)
    click(R.string.logout)
    visible(R.string.logout_title)
    compose.onAllNodesWithText(text(R.string.logout)).onLast().performClick()
    visible(R.string.welcome)
    input(R.string.email, email)
    input(R.string.password, password)
    click(R.string.login)
    compose.waitUntil(30000) {
      compose.onAllNodesWithText("SIMULATED").fetchSemanticsNodes().isNotEmpty()
    }
    click(R.string.view_history)
    click(R.string.days_30)
    compose.waitUntil(30000) {
      compose
        .onAllNodesWithText(compose.activity.getString(R.string.reading_count, 720))
        .fetchSemanticsNodes()
        .isNotEmpty()
    }
    click(R.string.home)
    capture("home")
  }
}
