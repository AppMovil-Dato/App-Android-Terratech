package com.novatech.terratech.profile.infrastructure.remote.dto

data class SaveProfileDto(
    val fullName: String,
    val fundoName: String,
    val contactPhone: String,
    val location: String,
    val sizeM2: Double,
)
