package com.novatech.terratech.iam.infrastructure.local

internal data class StoredSession(
    val userId: Int,
    val email: String,
    val fullName: String,
    val token: String,
    val expiresAt: String,
)
