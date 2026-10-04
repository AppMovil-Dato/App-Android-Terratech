package com.novatech.terratech.profile.domain.repository

import com.novatech.terratech.profile.domain.entity.FarmProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
  fun observe(userId: Int): Flow<FarmProfile?>

  suspend fun refresh(userId: Int)

  suspend fun save(
    userId: Int,
    name: String,
    farm: String,
    phone: String,
    location: String,
    sizeM2: Double,
  )
}
