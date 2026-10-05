package com.novatech.terratech.iam.infrastructure.remote.dto

data class UserDto(
  val id: Int,
  val emailAddress: String,
  val fullName: String?,
  val token: String? = null,
  val expiresAt: String? = null,
)
