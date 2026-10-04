package com.novatech.terratech.monitoring.domain.valueobject

import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.profile.domain.valueobject.AreaM2

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
