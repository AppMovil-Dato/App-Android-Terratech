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
    )
