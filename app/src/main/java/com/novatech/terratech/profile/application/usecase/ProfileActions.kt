package com.novatech.terratech.profile.application.usecase

import com.novatech.terratech.profile.domain.repository.ProfileRepository
import com.novatech.terratech.profile.domain.valueobject.FarmDetails

class ProfileActions(private val repository: ProfileRepository) {
  fun observe(user: Int) = repository.observe(user)

  suspend fun refresh(user: Int) = repository.refresh(user)

  suspend fun save(
    user: Int,
    name: String,
    farm: String,
    phone: String,
    location: String,
    hectares: Double,
  ) {
    val details = FarmDetails.of(name, farm, phone, location, hectares)
    repository.save(
      user,
      details.name.value,
      details.farm,
      details.phone,
      details.location,
      details.area.value,
    )
  }
}
