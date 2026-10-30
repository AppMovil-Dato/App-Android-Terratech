package com.novatech.terratech.iam.infrastructure.remote

import com.novatech.terratech.iam.infrastructure.remote.dto.LoginDto
import com.novatech.terratech.iam.infrastructure.remote.dto.LoginResponse
import com.novatech.terratech.iam.infrastructure.remote.dto.RegisterDto
import com.novatech.terratech.iam.infrastructure.remote.dto.UserDto
import com.novatech.terratech.iam.infrastructure.remote.dto.AccountEmailRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AccountApi {
    @POST("api/v1/authentication/email-verifications")
    suspend fun requestVerification(@Body body: AccountEmailRequestDto)

    @POST("api/v1/authentication/password-reset-requests")
    suspend fun requestPasswordReset(@Body body: AccountEmailRequestDto)

    @POST("api/v1/authentication/sign-up") suspend fun register(@Body body: RegisterDto): UserDto

    @POST("api/v1/authentication/sign-in") suspend fun login(@Body body: LoginDto): LoginResponse
}
