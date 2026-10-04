package com.novatech.terratech.monitoring.domain.valueobject

import com.novatech.terratech.core.domain.Failure

data class Coordinates private constructor(val latitude: Double, val longitude: Double) {
  companion object {
    fun of(latitude: Double, longitude: Double): Coordinates {
      if (
        !latitude.isFinite() ||
          latitude !in -90.0..90.0 ||
          !longitude.isFinite() ||
          longitude !in -180.0..180.0
      )
        throw Failure("INVALID_COORDINATES")
      return Coordinates(latitude, longitude)
    }
  }
}
