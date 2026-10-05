package com.novatech.terratech.core.presentation.state

import com.novatech.terratech.profile.presentation.state.ProfileState

fun ProfileState.forUser(user: Int): ProfileState =
    if (userId == user) this else ProfileState(userId = user, busy = true)
