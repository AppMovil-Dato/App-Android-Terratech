package com.novatech.terratech.core.presentation.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.novatech.terratech.monitoring.presentation.navigation.CreateFieldDestination
import com.novatech.terratech.monitoring.presentation.navigation.FieldSensorsDestination
import com.novatech.terratech.monitoring.presentation.navigation.HistoryDestination
import com.novatech.terratech.monitoring.presentation.navigation.ReadingDestination
import com.novatech.terratech.monitoring.presentation.navigation.RegisterSensorDestination
import com.novatech.terratech.monitoring.presentation.navigation.SensorDestination

class AppNavigator(private val controller: NavHostController) {
    fun topLevel(destination: TopLevelDestination) {
        if (destination.contains(controller.currentDestination)) {
            controller.navigate(destination.start) {
                popUpTo(controller.getBackStackEntry(destination.graph).destination.id)
                launchSingleTop = true
            }
        } else {
            controller.navigate(destination.graph) {
                popUpTo(controller.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    fun back() {
        controller.popBackStack()
    }

    fun createField(hasProfile: Boolean) {
        if (hasProfile) controller.navigate(CreateFieldDestination)
        else topLevel(TopLevelDestination.PROFILE)
    }

    fun sensors(fieldId: Int) {
        controller.navigate(FieldSensorsDestination(fieldId))
    }

    fun registerSensor(fieldId: Int?) {
        if (fieldId == null) topLevel(TopLevelDestination.FIELDS)
        else controller.navigate(RegisterSensorDestination(fieldId))
    }

    fun sensor(deviceId: Int?) {
        if (deviceId == null) topLevel(TopLevelDestination.FIELDS)
        else controller.navigate(SensorDestination(deviceId))
    }

    fun history(deviceId: Int?) {
        if (deviceId == null) topLevel(TopLevelDestination.FIELDS)
        else controller.navigate(HistoryDestination(deviceId))
    }

    fun reading(deviceId: Int, readingId: Int) {
        controller.navigate(ReadingDestination(deviceId, readingId))
    }

    fun fieldCreated(fieldId: Int) {
        controller.navigate(FieldSensorsDestination(fieldId)) {
            popUpTo<CreateFieldDestination> { inclusive = true }
            launchSingleTop = true
        }
    }

    fun sensorCreated(deviceId: Int) {
        controller.navigate(SensorDestination(deviceId)) {
            popUpTo<RegisterSensorDestination> { inclusive = true }
            launchSingleTop = true
        }
    }
}
