package com.novatech.terratech.profile.infrastructure.remote.dto

data class ProfileDto(
    val id: Int,
    val userId: Int,
    val fullName: String?,
    val emailAddress: String,
    val fundoName: String,
    val contactPhone: String,
    val location: String?,
    val sizeM2: Double?,
    val moistureThreshold: Double?,
)
