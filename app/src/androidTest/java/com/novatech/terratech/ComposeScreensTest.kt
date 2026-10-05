package com.novatech.terratech

import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.novatech.terratech.iam.presentation.state.AccountState
import com.novatech.terratech.iam.presentation.ui.AccountScreen
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.monitoring.presentation.ui.*
import com.novatech.terratech.ui.theme.TerraTechTheme
import java.util.Locale
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeScreensTest {
    @get:Rule val compose = createComposeRule()

    @Before
    fun accessibility() {
        compose.enableAccessibilityChecks()
    }

    @Test
    fun accountScreenSubmitsExactPassword() {
        var received: Pair<String, String>? = null
        compose.setContent {
            TerraTechTheme {
                AccountScreen(AccountState(), { e, p -> received = e to p }, { _, _, _, _ -> }, {})
            }
        }
        compose.onNodeWithText("Email").performTextInput("a@example.com")
        compose.onNodeWithText("Password").performTextInput(" secret ")
        compose.onAllNodes(hasText("Sign in") and hasClickAction()).onLast().performClick()
        compose.runOnIdle { assertEquals("a@example.com" to " secret ", received) }
    }

    @Test
    fun emptyFieldsExplainsFirstStep() {
        compose.setContent { TerraTechTheme { FieldsScreen(MonitoringState(), false, {}, {}, {}) } }
        compose.onNodeWithText("Your next harvest starts here").assertIsDisplayed()
        compose.onNodeWithText("Complete your farm profile").assertIsDisplayed()
    }

    @Test
    fun historySwitchesThirtyDaysWithoutFakeReadings() {
        var selected = 7
        compose.setContent {
            TerraTechTheme { HistoryScreen(MonitoringState(), { selected = it }, {}, {}) }
        }
        compose.onNodeWithText("30 days").performClick()
        compose.runOnIdle { assertEquals(30, selected) }
        compose.onNodeWithText("No readings in this range").assertIsDisplayed()
    }

    @Test
    fun spanishResourcesAreRendered() {
        val original =
            androidx.test.core.app.ApplicationProvider.getApplicationContext<
                android.content.Context
            >()
        val config =
            Configuration(original.resources.configuration).apply { setLocale(Locale("es", "PE")) }
        val localized = original.createConfigurationContext(config)
        compose.setContent {
            CompositionLocalProvider(
                LocalContext provides localized,
                LocalConfiguration provides config,
            ) {
                TerraTechTheme { FieldsScreen(MonitoringState(), false, {}, {}, {}) }
            }
        }
        compose.onNodeWithText("Mis parcelas").assertIsDisplayed()
        compose.onNodeWithText("Completa el perfil de tu fundo").assertIsDisplayed()
    }

    @Test
    fun largeTextStillAllowsFieldSelection() {
        var selection: Int? = null
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.8f)) {
                TerraTechTheme {
                    FieldsScreen(
                        MonitoringState(
                            fields =
                                listOf(
                                    com.novatech.terratech.monitoring.domain.entity.Field(
                                        1,
                                        1,
                                        "North",
                                        5000.0,
                                        "Loam",
                                        -12.0,
                                        -77.0,
                                        "Potato",
                                    )
                                )
                        ),
                        true,
                        { selection = it },
                        {},
                        {},
                    )
                }
            }
        }
        compose.onNodeWithText("Open field").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, selection) }
    }

    @Test
    fun sensorDoesNotInventThresholdWithoutDownloadedReference() {
        val reading =
            com.novatech.terratech.monitoring.domain.entity.Reading(
                1,
                2,
                java.time.Instant.now(),
                28.0,
                23.0,
                35.0,
                18.0,
                60.0,
                "SIMULATED",
            )
        compose.setContent {
            TerraTechTheme { SensorScreen(MonitoringState(readings = listOf(reading)), {}) }
        }
        compose.onAllNodesWithText("Below the reference").assertCountEquals(0)
        compose.onAllNodesWithText("At or above the reference").assertCountEquals(0)
        compose.onNodeWithText("SIMULATED").assertIsDisplayed()
    }
}
