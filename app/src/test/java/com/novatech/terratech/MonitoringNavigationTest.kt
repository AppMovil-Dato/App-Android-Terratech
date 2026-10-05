package com.novatech.terratech

import androidx.lifecycle.viewModelScope
import com.novatech.terratech.iam.domain.entity.Session
import com.novatech.terratech.monitoring.application.usecase.MonitoringActions
import com.novatech.terratech.monitoring.domain.entity.Field
import com.novatech.terratech.monitoring.domain.entity.Sensor
import com.novatech.terratech.monitoring.presentation.viewmodel.MonitoringViewModel
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MonitoringNavigationTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun restoringSensorRouteSelectsItsFieldAndPersistsThePair() =
        runTest(dispatcher) {
            val account = FakeAccountRepository()
            account.session.value =
                Session(
                    1,
                    "nav@test.example",
                    "Test Farmer",
                    "test-only",
                    Instant.now().plusSeconds(3600),
                )
            val repository = FakeMonitoringRepository()
            repository.plots.value += Field(2, 1, "South", 5000.0, "Loam", -12.0, -77.0, "Potato")
            repository.devices.value +=
                Sensor(22, 2, "South sensor", "TT-ZZZ002", "02:00:00:00:00:02", "OFFLINE")
            val viewModel = MonitoringViewModel(MonitoringActions(repository), account)
            try {
                runCurrent()
                assertEquals(1, viewModel.state.value.fieldId)
                viewModel.selectSensor(22)
                runCurrent()
                assertEquals(2, viewModel.state.value.fieldId)
                assertEquals(22, viewModel.state.value.deviceId)
                assertEquals(2 to 22, repository.selected)
                viewModel.selectSensor(999)
                runCurrent()
                assertEquals(2 to 22, repository.selected)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }
}
