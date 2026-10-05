package com.novatech.terratech.core.presentation.state

import com.novatech.terratech.monitoring.presentation.state.MonitoringState

fun MonitoringState.forUser(user: Int): MonitoringState =
    if (userId == user) this else MonitoringState(userId = user, busy = true)
