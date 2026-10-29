
package com.novatech.terratech.monitoring.presentation.navigation

import com.novatech.terratech.monitoring.presentation.viewmodel.MonitoringViewModel

fun MonitoringViewModel.navigationActions() =
    MonitoringNavigationActions(
        selectField = ::selectField,
        selectSensor = ::selectSensor,
        createField = ::createField,
        registerSensor = ::registerSensor,
        changeDays = ::days,
        readDetail = ::detail,
        refresh = ::refresh,
        clearMessage = ::clearMessage,

        // US18: conectar las acciones al ViewModel
        loadZoneThresholds = ::loadZoneThresholds,
        saveZoneThresholds = ::saveZoneThresholds,
        resetZoneThresholds = ::resetZoneThresholds,
        closeZoneThresholds = ::closeZoneThresholds,

        // Acciones de clima e imagenes satelitales
        refreshWeather = ::refreshWeather,
        loadSatelliteByDate = ::loadSatelliteByDate,
        toggleSatelliteMode = ::toggleSatelliteMode,
        compareVegetation = ::compareVegetation,
    )
