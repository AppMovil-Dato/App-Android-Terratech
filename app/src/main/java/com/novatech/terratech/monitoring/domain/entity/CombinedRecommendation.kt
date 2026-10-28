package com.novatech.terratech.monitoring.domain.entity

data class CombinedRecommendation(
    val suspensionRecommended: Boolean,
    val estimatedWaterSavingsLiters: Int,
    val message: String,
)
