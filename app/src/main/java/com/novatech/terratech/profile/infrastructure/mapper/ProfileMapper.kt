package com.novatech.terratech.profile.infrastructure.mapper

import com.novatech.terratech.core.infrastructure.local.ProfileRow
import com.novatech.terratech.core.infrastructure.remote.ProfileDto
import com.novatech.terratech.profile.domain.entity.FarmProfile

fun ProfileDto.row(user: Int) =
  ProfileRow(
    user,
    id,
    fullName.orEmpty(),
    emailAddress,
    fundoName,
    contactPhone,
    location.orEmpty(),
    sizeM2,
    moistureThreshold ?: 30.0,
  )

fun ProfileRow.domain() =
  FarmProfile(
    id,
    userId,
    fullName,
    email,
    fundoName,
    contactPhone,
    location,
    sizeM2,
    minimumMoisture,
  )
