package com.novatech.terratech.iam.infrastructure.remote.dto

data class RegisterDto(
    val fullName: String,
    val emailAddress: String,
    val password: String,
    val confirmPassword: String,
)
