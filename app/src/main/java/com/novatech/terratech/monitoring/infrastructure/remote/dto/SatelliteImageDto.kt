package com.novatech.terratech.monitoring.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class SatelliteImageDto(
    @SerializedName("fieldId") val fieldId: Int,
    @SerializedName("capturedAt") val capturedAt: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("ndviAverage") val ndviAverage: Double,
    @SerializedName("cloudCoveragePercent") val cloudCoveragePercent: Double,
    @SerializedName("bounds") val bounds: SatelliteBoundsDto,
)
