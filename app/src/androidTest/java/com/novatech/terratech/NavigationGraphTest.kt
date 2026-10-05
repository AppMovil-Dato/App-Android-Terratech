package com.novatech.terratech

import android.os.Bundle
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import com.novatech.terratech.core.presentation.navigation.AppDestination
import com.novatech.terratech.core.presentation.navigation.AppNavigator
import com.novatech.terratech.core.presentation.navigation.HomeDestination
import com.novatech.terratech.core.presentation.navigation.TopLevelDestination
import com.novatech.terratech.core.presentation.ui.SignedInContent
import com.novatech.terratech.core.presentation.ui.TerraTechNavHost
import com.novatech.terratech.iam.domain.entity.Session
import com.novatech.terratech.monitoring.domain.entity.Field
import com.novatech.terratech.monitoring.domain.entity.Reading
import com.novatech.terratech.monitoring.domain.entity.Sensor
import com.novatech.terratech.monitoring.presentation.navigation.FieldSensorsDestination
import com.novatech.terratech.monitoring.presentation.navigation.FieldsDestination
import com.novatech.terratech.monitoring.presentation.navigation.HistoryDestination
import com.novatech.terratech.monitoring.presentation.navigation.MonitoringNavigationActions
import com.novatech.terratech.monitoring.presentation.navigation.ReadingDestination
import com.novatech.terratech.monitoring.presentation.navigation.SensorDestination
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.profile.domain.entity.FarmProfile
import com.novatech.terratech.profile.presentation.navigation.ProfileDestination
import com.novatech.terratech.profile.presentation.navigation.ProfileNavigationActions
import com.novatech.terratech.profile.presentation.state.ProfileState
import com.novatech.terratech.ui.theme.TerraTechTheme
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NavigationGraphTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var controller: TestNavHostController
    private lateinit var navigator: AppNavigator
    private var userId by mutableIntStateOf(7)
    private var navigationVersion by mutableIntStateOf(0)
    private var savedNavigation: Bundle? = null
    private var monitoringState by mutableStateOf(sampleMonitoringState())
    private var profileState by mutableStateOf(sampleProfileState())

    private fun launch() {
        val monitoringActions =
            MonitoringNavigationActions(
                selectField = { id -> monitoringState = monitoringState.copy(fieldId = id) },
                selectSensor = { id ->
                    val sensor = monitoringState.sensors.first { it.id == id }
                    monitoringState = monitoringState.copy(fieldId = sensor.fieldId, deviceId = id)
                },
                createField = { _, _ -> },
                registerSensor = { _, _ -> },
                changeDays = { days -> monitoringState = monitoringState.copy(days = days) },
                readDetail = {},
                refresh = {},
                clearMessage = {},
            )
        val profileActions = ProfileNavigationActions({ _, _, _, _, _ -> }, {}, {})
        compose.setContent {
            key(userId) {
                val context = LocalContext.current
                controller =
                    remember(navigationVersion) {
                        TestNavHostController(context).apply {
                            navigatorProvider.addNavigator(ComposeNavigator())
                            savedNavigation?.let(::restoreState)
                        }
                    }
                navigator = remember(controller) { AppNavigator(controller) }
                val entry by controller.currentBackStackEntryAsState()
                val selectedTab =
                    TopLevelDestination.entries.firstOrNull { it.contains(entry?.destination) }
                        ?: TopLevelDestination.HOME
                TerraTechTheme {
                    SignedInContent(
                        destination = AppDestination.from(entry?.destination),
                        selectedTab = selectedTab,
                        monitoringState = monitoringState,
                        profileState = profileState,
                        snackbarHostState = remember { SnackbarHostState() },
                        sessionExpired = false,
                        onNavigate = navigator::topLevel,
                        onBack = navigator::back,
                        onRefresh = {},
                        onSignIn = {},
                    ) {
                        TerraTechNavHost(
                            navController = controller,
                            navigator = navigator,
                            session =
                                Session(
                                    userId,
                                    "nav@test.example",
                                    "Test Farmer",
                                    "test-only",
                                    Instant.now().plusSeconds(3600),
                                ),
                            profileState = profileState,
                            monitoringState = monitoringState,
                            monitoringActions = monitoringActions,
                            profileActions = profileActions,
                        )
                    }
                }
            }
        }
    }

    @Test
    fun startsAtHomeAndRoutesFieldAndSensorClicksWithIds() {
        launch()
        compose.runOnIdle {
            assertTrue(controller.currentDestination!!.hasRoute<HomeDestination>())
        }
        compose.onNodeWithTag("tab-fields").performClick()
        compose.runOnIdle {
            assertTrue(controller.currentDestination!!.hasRoute<FieldsDestination>())
        }
        compose.onNodeWithText("Open field").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(
                10,
                controller.currentBackStackEntry!!.toRoute<FieldSensorsDestination>().fieldId,
            )
        }
        compose.onNodeWithText("View readings").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(
                11,
                controller.currentBackStackEntry!!.toRoute<SensorDestination>().deviceId,
            )
        }
        compose.onNodeWithTag("tab-fields").assertIsSelected()
        compose.runOnIdle { navigator.back() }
        compose.runOnIdle {
            assertTrue(controller.currentDestination!!.hasRoute<FieldSensorsDestination>())
        }
    }

    @Test
    fun switchingTabsRestoresMonitoringHistoryWithoutOpeningItUnderProfile() {
        launch()
        compose.runOnIdle { navigator.history(11) }
        compose.onNodeWithTag("tab-fields").assertIsSelected()
        compose.onNodeWithTag("tab-profile").performClick()
        compose.runOnIdle {
            assertTrue(controller.currentDestination!!.hasRoute<ProfileDestination>())
        }
        compose.onNodeWithTag("tab-profile").assertIsSelected()
        compose.onNodeWithTag("tab-fields").performClick()
        compose.runOnIdle {
            assertEquals(
                11,
                controller.currentBackStackEntry!!.toRoute<HistoryDestination>().deviceId,
            )
        }
        compose.onNodeWithTag("tab-fields").assertIsSelected()
    }

    @Test
    fun reselectingFieldsReturnsToItsRootAndBackReturnsHome() {
        launch()
        compose.runOnIdle {
            navigator.topLevel(TopLevelDestination.FIELDS)
            navigator.history(11)
        }
        compose.onNodeWithTag("tab-fields").performClick()
        compose.runOnIdle {
            assertTrue(controller.currentDestination!!.hasRoute<FieldsDestination>())
        }
        compose.runOnIdle { navigator.back() }
        compose.runOnIdle {
            assertTrue(controller.currentDestination!!.hasRoute<HomeDestination>())
        }
    }

    @Test
    fun readingKeepsDeviceAndReadingIdsAndBackReturnsHistory() {
        launch()
        compose.runOnIdle {
            navigator.history(11)
            navigator.reading(11, 720)
        }
        compose.runOnIdle {
            val route = controller.currentBackStackEntry!!.toRoute<ReadingDestination>()
            assertEquals(11, route.deviceId)
            assertEquals(720, route.readingId)
            assertEquals(AppDestination.READING, AppDestination.from(controller.currentDestination))
        }
        compose.runOnIdle { navigator.back() }
        compose.runOnIdle {
            assertTrue(controller.currentDestination!!.hasRoute<HistoryDestination>())
        }
    }

    @Test
    fun creatingFieldWithoutProfileOpensProfileAndNewAccountResetsPrivateStack() {
        profileState = ProfileState(userId = 7, checked = true)
        launch()
        compose.runOnIdle { navigator.createField(false) }
        compose.runOnIdle {
            assertTrue(controller.currentDestination!!.hasRoute<ProfileDestination>())
        }
        compose.runOnIdle {
            userId = 8
            monitoringState = MonitoringState(userId = 8)
            profileState = ProfileState(userId = 8, checked = true)
        }
        compose.runOnIdle {
            assertTrue(controller.currentDestination!!.hasRoute<HomeDestination>())
        }
        compose.onNodeWithTag("tab-home").assertIsSelected()
    }

    @Test
    fun restoredNavigationKeepsTypedReadingArguments() {
        launch()
        compose.runOnIdle {
            navigator.history(11)
            navigator.reading(11, 720)
        }
        compose.runOnIdle {
            savedNavigation = controller.saveState()
            navigationVersion++
        }
        compose.runOnIdle {
            val route = controller.currentBackStackEntry!!.toRoute<ReadingDestination>()
            assertEquals(11, route.deviceId)
            assertEquals(720, route.readingId)
        }
        compose.onNodeWithTag("tab-fields").assertIsSelected()
    }

    private fun sampleProfileState() =
        ProfileState(
            userId = 7,
            profile =
                FarmProfile(
                    1,
                    7,
                    "Test Farmer",
                    "nav@test.example",
                    "Test Farm",
                    "999888777",
                    "Lima",
                    10000.0,
                ),
            checked = true,
        )

    private fun sampleMonitoringState() =
        MonitoringState(
            userId = 7,
            fields = listOf(Field(10, 1, "North", 5000.0, "SANDY", -12.0, -77.0, "Potato")),
            sensors =
                listOf(Sensor(11, 10, "Soil One", "TT-ABC123", "02:00:00:00:00:11", "ACTIVE")),
            fieldId = 10,
            deviceId = 11,
            readings =
                listOf(Reading(720, 11, Instant.now(), 42.0, 23.0, 35.0, 18.0, 60.0, "SIMULATED")),
        )
}
