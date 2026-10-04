package com.novatech.terratech.core.presentation.state

import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.profile.presentation.state.ProfileState

// Gate private presentation state during asynchronous account transitions.
fun MonitoringState.forUser(user: Int): MonitoringState =
  if (userId == user) this else MonitoringState(userId = user, busy = true)

fun ProfileState.forUser(user: Int): ProfileState =
  if (userId == user) this else ProfileState(userId = user, busy = true)
