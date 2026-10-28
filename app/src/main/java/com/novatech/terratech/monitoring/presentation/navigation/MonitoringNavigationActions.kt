
package com.novatech.terratech.monitoring.presentation.navigation

import com.novatech.terratech.monitoring.domain.entity.FertilityZoneThresholds
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

    // US18: acciones de configuracion de umbrales
    val loadZoneThresholds: (Int) -> Unit,
    val saveZoneThresholds: (FertilityZoneThresholds) -> Unit,
    val resetZoneThresholds: () -> Unit,
    val closeZoneThresholds: () -> Unit,
)
