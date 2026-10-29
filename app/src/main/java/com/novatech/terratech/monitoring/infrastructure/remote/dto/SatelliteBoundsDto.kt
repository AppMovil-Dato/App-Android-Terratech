package com.novatech.terratech.monitoring.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class SatelliteBoundsDto(
    @SerializedName("north") val north: Double,
    @SerializedName("south") val south: Double,
    @SerializedName("east") val east: Double,
    @SerializedName("west") val west: Double,
)
