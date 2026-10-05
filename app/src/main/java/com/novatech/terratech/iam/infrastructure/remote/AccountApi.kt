package com.novatech.terratech.iam.infrastructure.remote

import com.novatech.terratech.iam.infrastructure.remote.dto.LoginDto
import com.novatech.terratech.iam.infrastructure.remote.dto.LoginResponse
import com.novatech.terratech.iam.infrastructure.remote.dto.RegisterDto
import com.novatech.terratech.iam.infrastructure.remote.dto.UserDto
import retrofit2.http.*

interface AccountApi {
    @POST("api/v1/authentication/sign-up") suspend fun register(@Body body: RegisterDto): UserDto

    @POST("api/v1/authentication/sign-in") suspend fun login(@Body body: LoginDto): LoginResponse
}
