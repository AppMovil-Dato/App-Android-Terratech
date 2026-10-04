package com.novatech.terratech.profile.domain.valueobject

import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.iam.domain.valueobject.FullName

data class FarmDetails
private constructor(
  val name: FullName,
  val farm: String,
  val phone: String,
  val location: String,
  val area: AreaM2,
) {
  companion object {
    fun of(
      name: String,
      farm: String,
      phone: String,
      location: String,
      hectares: Double,
    ): FarmDetails {
      if (
        farm.trim().length !in 1..100 ||
          phone.trim().length !in 1..30 ||
          location.trim().length !in 1..250
      )
        throw Failure("REQUIRED_FIELDS")
      return FarmDetails(
        FullName.of(name),
        farm.trim(),
        phone.trim(),
        location.trim(),
        AreaM2.fromHectares(hectares),
      )
    }
  }
}
