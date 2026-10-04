package com.novatech.terratech.core.presentation.state

import com.novatech.terratech.monitoring.presentation.state.MonitoringState

// Gate private presentation state during asynchronous account transitions.
fun MonitoringState.forUser(user: Int): MonitoringState =
  if (userId == user) this else MonitoringState(userId = user, busy = true)
