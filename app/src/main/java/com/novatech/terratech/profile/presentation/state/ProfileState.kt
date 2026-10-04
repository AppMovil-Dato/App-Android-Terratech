package com.novatech.terratech.profile.presentation.state

import com.novatech.terratech.profile.domain.entity.FarmProfile

data class ProfileState(
  val userId: Int? = null,
  val profile: FarmProfile? = null,
  val busy: Boolean = false,
  val checked: Boolean = false,
  val error: String? = null,
  val saved: Boolean = false,
)
