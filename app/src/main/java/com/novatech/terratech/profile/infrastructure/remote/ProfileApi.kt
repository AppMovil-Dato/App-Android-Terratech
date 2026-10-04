package com.novatech.terratech.profile.infrastructure.remote

import com.novatech.terratech.profile.infrastructure.remote.dto.ProfileDto
import com.novatech.terratech.profile.infrastructure.remote.dto.SaveProfileDto
import retrofit2.http.*

interface ProfileApi {
  @GET("api/v1/profiles/me") suspend fun profile(): ProfileDto

  @PUT("api/v1/profiles/me") suspend fun saveProfile(@Body body: SaveProfileDto): ProfileDto
}
