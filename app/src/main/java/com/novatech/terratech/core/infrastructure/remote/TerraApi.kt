package com.novatech.terratech.core.infrastructure.remote

import retrofit2.http.*

data class RegisterDto(
  val fullName: String,
  val emailAddress: String,
  val password: String,
  val confirmPassword: String,
)

data class LoginDto(val emailAddress: String, val password: String)

data class UserDto(val id: Int, val emailAddress: String, val fullName: String?)

data class LoginResponse(
  val id: Int,
  val emailAddress: String,
  val fullName: String?,
  val token: String,
  val expiresAt: String,
)

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

data class SaveProfileDto(
  val fullName: String,
  val fundoName: String,
  val contactPhone: String,
  val location: String,
  val sizeM2: Double,
)

data class FieldDto(
  val id: Int,
  val profileId: Int,
  val name: String,
  val sizeM2: Double,
  val soilType: String,
  val latitude: Double,
  val longitude: Double,
  val cropName: String?,
)

data class CreateFieldDto(
  val profileId: Int,
  val name: String,
  val sizeM2: Double,
  val soilType: String,
  val latitude: Double,
  val longitude: Double,
  val cropName: String,
)

data class SensorDto(
  val id: Int,
  val fieldId: Int,
  val name: String?,
  val sensorCode: String?,
  val macAddress: String,
  val status: String,
)

data class RegisterSensorDto(val sensorCode: String, val fieldId: Int, val name: String)

data class ReadingDto(
  val id: Int,
  val deviceId: Int,
  val recordedAt: String,
  val moisturePercent: Double,
  val soilTemperatureC: Double,
  val nitrogenPpm: Double,
  val phosphorusPpm: Double,
  val potassiumPpm: Double,
  val source: String,
)

data class LatestDto(val reading: ReadingDto, val isStale: Boolean)

data class HistoryDto(
  val deviceId: Int,
  val fromUtc: String,
  val toUtc: String,
  val minimumMoisturePercent: Double,
  val readings: List<ReadingDto>,
)

interface TerraApi {
  @POST("api/v1/authentication/sign-up") suspend fun register(@Body body: RegisterDto): UserDto

  @POST("api/v1/authentication/sign-in") suspend fun login(@Body body: LoginDto): LoginResponse

  @GET("api/v1/profiles/me") suspend fun profile(): ProfileDto

  @PUT("api/v1/profiles/me") suspend fun saveProfile(@Body body: SaveProfileDto): ProfileDto

  @GET("api/v1/fields") suspend fun fields(): List<FieldDto>

  @POST("api/v1/fields") suspend fun createField(@Body body: CreateFieldDto): FieldDto

  @GET("api/v1/fields/{id}/devices") suspend fun sensors(@Path("id") field: Int): List<SensorDto>

  @POST("api/v1/devices/register")
  suspend fun registerSensor(@Body body: RegisterSensorDto): SensorDto

  @GET("api/v1/devices/{id}/readings/latest") suspend fun latest(@Path("id") device: Int): LatestDto

  @GET("api/v1/devices/{id}/readings")
  suspend fun history(@Path("id") device: Int, @Query("days") days: Int): HistoryDto

  @GET("api/v1/devices/{device}/readings/{id}")
  suspend fun detail(@Path("device") device: Int, @Path("id") id: Int): ReadingDto
}
