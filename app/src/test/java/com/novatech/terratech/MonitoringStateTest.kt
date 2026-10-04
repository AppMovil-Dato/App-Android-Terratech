package com.novatech.terratech

import androidx.lifecycle.ViewModelStore
import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.iam.domain.entity.Session
import com.novatech.terratech.monitoring.application.usecase.MonitoringActions
import com.novatech.terratech.monitoring.domain.entity.*
import com.novatech.terratech.monitoring.domain.repository.MonitoringRepository
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

  class Repository : MonitoringRepository {
    val plots =
      MutableStateFlow(listOf(Field(1, 1, "North", 5000.0, "Loam", -12.0, -77.0, "Potato")))
    val devices =
      MutableStateFlow(
        listOf(Sensor(11, 1, "North sensor", "TT-ZZZ001", "02:00:00:00:00:01", "OFFLINE"))
      )
    val samples =
      MutableStateFlow(
        listOf(Reading(9, 11, Instant.now(), 42.0, 23.0, 35.0, 18.0, 60.0, "SIMULATED"))
      )
    var offline = false
    var registerCalls = 0
    var occupied = false
    var selected: Pair<Int?, Int?> = null to null

    private fun connected() {
      if (offline) throw Failure("OFFLINE")
    }

    override fun selection(user: Int) = flowOf(selected)

    override suspend fun select(user: Int, field: Int?, device: Int?) {
      selected = field to device
    }

    override fun fields(user: Int) = if (user == 1) plots else flowOf(emptyList())

    override fun sensors(user: Int) = if (user == 1) devices else flowOf(emptyList())

    override fun readings(user: Int, device: Int) =
      if (user == 1 && device == 11) samples else flowOf(emptyList())

    override fun downloadState(user: Int, device: Int, days: Int) =
      flowOf(DownloadState(Instant.now(), null, null, 30.0))

    override suspend fun refreshFields(user: Int) {
      connected()
    }

    override suspend fun refreshSensors(user: Int, field: Int) {
      connected()
    }

    override suspend fun refreshReadings(user: Int, device: Int, days: Int) {
      connected()
    }

    override suspend fun refreshDetail(user: Int, device: Int, reading: Int) {
      connected()
    }

    override suspend fun createField(
      user: Int,
      profile: Int,
      name: String,
      crop: String,
      area: Double,
      soil: String,
      latitude: Double,
      longitude: Double,
    ) {
      connected()
      plots.value = plots.value + Field(2, profile, name, area, soil, latitude, longitude, crop)
    }

    override suspend fun registerSensor(user: Int, field: Int, code: String, name: String) {
      connected()
      registerCalls++
      if (occupied) throw Failure("SENSOR_OCCUPIED", 409)
    }
  }

  private fun vm(repo: Repository, account: ApplicationActionsTest.Accounts): MonitoringViewModel {
    account.session.value = session(1)
    return MonitoringViewModel(MonitoringActions(repo), account).also {
      store.put("monitoring", it)
    }
  }

  @Test
  fun offlineKeepsCachedValuesAndDoesNotLookEmpty() =
    runTest(dispatcher) {
      try {
        val repo = Repository().apply { offline = true }
        val vm = vm(repo, ApplicationActionsTest.Accounts())
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
        val account = ApplicationActionsTest.Accounts()
        val vm = vm(Repository(), account)
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
        val vm = vm(Repository(), ApplicationActionsTest.Accounts())
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
        val repo = Repository()
        val vm = vm(repo, ApplicationActionsTest.Accounts())
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
        val repo = Repository().apply { occupied = true }
        val vm = vm(repo, ApplicationActionsTest.Accounts())
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
        val repo = Repository()
        val vm = vm(repo, ApplicationActionsTest.Accounts())
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
        val repo = Repository().apply { offline = true }
        val vm = vm(repo, ApplicationActionsTest.Accounts())
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
