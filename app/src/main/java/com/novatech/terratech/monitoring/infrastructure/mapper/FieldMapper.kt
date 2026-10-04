package com.novatech.terratech.monitoring.infrastructure.mapper

import com.novatech.terratech.monitoring.domain.entity.*
import com.novatech.terratech.monitoring.infrastructure.local.entity.FieldRow
import com.novatech.terratech.monitoring.infrastructure.remote.dto.FieldDto

fun FieldDto.row(user: Int) =
  FieldRow(user, id, profileId, name, sizeM2, soilType, latitude, longitude, cropName)

fun FieldRow.domain() = Field(id, profileId, name, sizeM2, soilType, latitude, longitude, cropName)
