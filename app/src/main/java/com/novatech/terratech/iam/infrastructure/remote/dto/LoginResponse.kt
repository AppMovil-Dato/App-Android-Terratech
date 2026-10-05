package com.novatech.terratech.iam.infrastructure.remote.dto

data class LoginResponse(
    val id: Int,
    val emailAddress: String,
    val fullName: String?,
    val token: String,
    val expiresAt: String,
)
