package com.novatech.terratech.profile.domain.entity

data class FarmProfile(
    val id: Int,
    val userId: Int,
    val fullName: String,
    val email: String,
    val fundoName: String,
    val contactPhone: String,
    val location: String,
    val sizeM2: Double?,
    val minimumMoisture: Double = 30.0,
)
