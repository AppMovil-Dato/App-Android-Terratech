package com.novatech.terratech.iam.domain.entity

import java.time.Instant

data class Session(
    val userId: Int,
    val email: String,
    val fullName: String,
    val token: String,
    val expiresAt: Instant,
) {
    fun expired(now: Instant = Instant.now()) = !expiresAt.isAfter(now)

    override fun toString() = "Session(userId=$userId)"
}
