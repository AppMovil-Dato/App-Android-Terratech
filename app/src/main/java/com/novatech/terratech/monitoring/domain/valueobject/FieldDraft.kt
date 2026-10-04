package com.novatech.terratech.monitoring.domain.valueobject

import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.profile.domain.valueobject.AreaM2

@JvmInline
value class FieldName private constructor(val value: String) {
  companion object {
    fun of(value: String): FieldName {
      val name = value.trim()
      if (name.length !in 1..100) throw Failure("REQUIRED_FIELDS")
      return FieldName(name)
    }
  }
}

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

data class FieldDraft
private constructor(
  val name: FieldName,
  val crop: String,
  val area: AreaM2,
  val soil: String,
  val coordinates: Coordinates,
) {
  companion object {
    fun of(
      name: String,
      crop: String,
      hectares: Double,
      soil: String,
      latitude: Double,
      longitude: Double,
    ): FieldDraft {
      if (crop.trim().length !in 1..100 || soil.trim().length !in 1..50)
        throw Failure("REQUIRED_FIELDS")
      return FieldDraft(
        FieldName.of(name),
        crop.trim(),
        AreaM2.fromHectares(hectares),
        soil.trim(),
        Coordinates.of(latitude, longitude),
      )
    }
  }
}
