package com.novatech.terratech.monitoring.presentation.navigation

import com.novatech.terratech.monitoring.domain.valueobject.FieldDraft

data class MonitoringNavigationActions(
    val selectField: (Int) -> Unit,
    val selectSensor: (Int) -> Unit,
    val createField: (Int, FieldDraft) -> Unit,
    val registerSensor: (String, String) -> Unit,
    val changeDays: (Int) -> Unit,
    val readDetail: (Int) -> Unit,
    val refresh: () -> Unit,
    val clearMessage: () -> Unit,
)
