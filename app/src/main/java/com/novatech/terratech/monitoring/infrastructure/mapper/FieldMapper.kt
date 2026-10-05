package com.novatech.terratech.monitoring.infrastructure.mapper

import com.google.gson.Gson
import com.novatech.terratech.monitoring.domain.entity.Field
import com.novatech.terratech.monitoring.domain.valueobject.Coordinates
import com.novatech.terratech.monitoring.infrastructure.local.entity.FieldRow
import com.novatech.terratech.monitoring.infrastructure.remote.dto.FieldDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.FieldVertexDto

fun FieldDto.row(user: Int) =
  FieldRow(
    user,
    id,
    profileId,
    name,
    sizeM2,
    soilType,
    latitude,
    longitude,
    cropName,
    Gson().toJson(boundary.orEmpty()),
  )

fun FieldRow.domain() =
  Field(
    id,
    profileId,
    name,
    sizeM2,
    soilType,
    latitude,
    longitude,
    cropName,
    Gson().fromJson(boundaryJson, Array<FieldVertexDto>::class.java).map {
      Coordinates.of(it.latitude, it.longitude)
    },
  )
