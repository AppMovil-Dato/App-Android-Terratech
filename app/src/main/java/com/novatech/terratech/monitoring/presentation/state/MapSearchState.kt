package com.novatech.terratech.monitoring.presentation.state

import com.novatech.terratech.monitoring.domain.valueobject.Coordinates

data class MapSearchState(
  val searching: Boolean = false,
  val result: Coordinates? = null,
  val error: Boolean = false,
)
