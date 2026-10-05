package com.novatech.terratech.monitoring.infrastructure.remote

import com.novatech.terratech.monitoring.infrastructure.remote.dto.CreateFieldDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.FieldDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.HistoryDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.LatestDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.ReadingDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.RegisterSensorDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.SensorDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MonitoringApi {
    @GET("api/v1/fields") suspend fun fields(): List<FieldDto>

    @GET("api/v1/devices") suspend fun allSensors(): List<SensorDto>

    @POST("api/v1/fields") suspend fun createField(@Body body: CreateFieldDto): FieldDto

    @GET("api/v1/fields/{id}/devices") suspend fun sensors(@Path("id") field: Int): List<SensorDto>

    @POST("api/v1/devices/register")
    suspend fun registerSensor(@Body body: RegisterSensorDto): SensorDto

    @GET("api/v1/devices/{id}/readings/latest")
    suspend fun latest(@Path("id") device: Int): LatestDto

    @GET("api/v1/devices/{id}/readings")
    suspend fun history(@Path("id") device: Int, @Query("days") days: Int): HistoryDto

    @GET("api/v1/devices/{device}/readings/{id}")
    suspend fun detail(@Path("device") device: Int, @Path("id") id: Int): ReadingDto
}
