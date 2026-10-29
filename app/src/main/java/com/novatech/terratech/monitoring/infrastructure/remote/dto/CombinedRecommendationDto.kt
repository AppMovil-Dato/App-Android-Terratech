package com.novatech.terratech.monitoring.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class CombinedRecommendationDto(
    @SerializedName("suspensionRecommended") val suspensionRecommended: Boolean,
    @SerializedName("estimatedWaterSavingsLiters") val estimatedWaterSavingsLiters: Int,
    @SerializedName("message") val message: String,
)
