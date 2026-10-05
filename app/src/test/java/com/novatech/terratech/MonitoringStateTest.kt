package com.novatech.terratech

import androidx.lifecycle.ViewModelStore
import com.novatech.terratech.iam.domain.entity.Session
import com.novatech.terratech.monitoring.application.usecase.MonitoringActions
import com.novatech.terratech.monitoring.domain.entity.*
import com.novatech.terratech.monitoring.presentation.viewmodel.MonitoringViewModel
import java.time.Instant
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class MonitoringStateTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun teardown() {
        store.clear()
        Dispatchers.resetMain()
    }

    private fun session(user: Int) =
        Session(user, "user$user@example.com", "Farmer", "private", Instant.now().plusSeconds(3600))

    private fun vm(
        repo: FakeMonitoringRepository,
        account: FakeAccountRepository,
    ): MonitoringViewModel {
        account.session.value = session(1)
        return MonitoringViewModel(MonitoringActions(repo), account).also {
            store.put("monitoring", it)
        }
    }

    @Test
    fun offlineKeepsCachedValuesAndDoesNotLookEmpty() =
        runTest(dispatcher) {
            try {
                val repo = FakeMonitoringRepository().apply { offline = true }
                val vm = vm(repo, FakeAccountRepository())
                runCurrent()
                assertTrue(vm.state.value.offline)
                assertEquals("OFFLINE", vm.state.value.error)
                assertEquals(42.0, vm.state.value.latest!!.moisturePercent, 0.0)
                assertEquals("North", vm.state.value.fields.single().name)
                assertFalse(vm.state.value.busy)
            } finally {
                store.clear()
            }
        }

    @Test
    fun secondAccountClearsSelectionAndCachedReadingsImmediately() =
        runTest(dispatcher) {
            try {
                val account = FakeAccountRepository()
                val vm = vm(FakeMonitoringRepository(), account)
                runCurrent()
                assertNotNull(vm.state.value.latest)
                account.session.value = session(2)
                runCurrent()
                assertTrue(vm.state.value.fields.isEmpty())
                assertTrue(vm.state.value.sensors.isEmpty())
                assertNull(vm.state.value.latest)
                assertNull(vm.state.value.fieldId)
                assertNull(vm.state.value.deviceId)
            } finally {
                store.clear()
            }
        }

    @Test
    fun creationEventSurvivesAutomaticRefreshUntilAcknowledged() =
        runTest(dispatcher) {
            try {
                val vm = vm(FakeMonitoringRepository(), FakeAccountRepository())
                runCurrent()
                vm.createField(1, "South", "Grapes", 1.25, "Loam", -12.1, -77.1)
                runCurrent()
                vm.refresh()
                runCurrent()
                assertTrue(vm.state.value.created)
                assertEquals(12500.0, vm.state.value.fields.last().sizeM2, 0.0)
                vm.clearMessage()
                assertFalse(vm.state.value.created)
            } finally {
                store.clear()
            }
        }

    @Test
    fun invalidSensorNeverReachesRepository() =
        runTest(dispatcher) {
            try {
                val repo = FakeMonitoringRepository()
                val vm = vm(repo, FakeAccountRepository())
                runCurrent()
                vm.registerSensor("INVALID", "North")
                runCurrent()
                assertEquals("INVALID_SENSOR_CODE", vm.state.value.error)
                assertEquals(0, repo.registerCalls)
            } finally {
                store.clear()
            }
        }

    @Test
    fun occupiedSensorRetainsSelectionAndShowsConflict() =
        runTest(dispatcher) {
            try {
                val repo = FakeMonitoringRepository().apply { occupied = true }
                val vm = vm(repo, FakeAccountRepository())
                runCurrent()
                vm.registerSensor("TT-ZZZ002", "Other")
                runCurrent()
                assertEquals("SENSOR_OCCUPIED", vm.state.value.error)
                assertEquals(11, vm.state.value.deviceId)
            } finally {
                store.clear()
            }
        }

    @Test
    fun emptyParcelClearsPreviousDeviceReadings() =
        runTest(dispatcher) {
            try {
                val repo = FakeMonitoringRepository()
                val vm = vm(repo, FakeAccountRepository())
                runCurrent()
                vm.createField(1, "South", "Grapes", 1.0, "Loam", -12.1, -77.1)
                runCurrent()
                vm.selectField(2)
                runCurrent()
                assertNull(vm.state.value.deviceId)
                assertNull(vm.state.value.latest)
                assertTrue(vm.state.value.fieldSensors.isEmpty())
            } finally {
                store.clear()
            }
        }

    @Test
    fun recoveryClearsOfflineFlagAfterSuccessfulRefresh() =
        runTest(dispatcher) {
            try {
                val repo = FakeMonitoringRepository().apply { offline = true }
                val vm = vm(repo, FakeAccountRepository())
                runCurrent()
                repo.offline = false
                vm.refresh()
                runCurrent()
                assertFalse(vm.state.value.offline)
                assertNull(vm.state.value.error)
            } finally {
                store.clear()
            }
        }
}
