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
  val boundary: List<Coordinates>,
) {
  companion object {
    fun of(
      name: String,
      crop: String,
      hectares: Double,
      soil: String,
      latitude: Double,
      longitude: Double,
      boundary: List<Coordinates> = emptyList(),
    ): FieldDraft {
      if (crop.trim().length !in 1..100 || soil.trim().length !in 1..50)
        throw Failure("REQUIRED_FIELDS")
      val polygon = if (boundary.isEmpty()) null else ParcelBoundary.of(boundary)
      return FieldDraft(
        FieldName.of(name),
        crop.trim(),
        AreaM2.fromHectares(polygon?.areaM2?.div(10000) ?: hectares),
        soil.trim(),
        polygon?.center ?: Coordinates.of(latitude, longitude),
        polygon?.points.orEmpty(),
      )
    }
  }
}
