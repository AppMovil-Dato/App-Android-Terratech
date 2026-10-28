package com.novatech.terratech.monitoring.infrastructure.remote

import com.novatech.terratech.monitoring.infrastructure.remote.dto.CreateFieldDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.FieldDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.FieldWeatherDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.HistoryDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.LatestDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.ReadingDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.RegisterSensorDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.SatelliteImageDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.SensorDto
import retrofit2.Response
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

    @GET("api/v1/fields/{fieldId}/weather")
    suspend fun getFieldWeather(@Path("fieldId") fieldId: Int): Response<FieldWeatherDto>

    @GET("api/v1/fields/{fieldId}/satellite-images/latest")
    suspend fun getLatestSatelliteImage(@Path("fieldId") fieldId: Int): Response<SatelliteImageDto>

    @GET("api/v1/fields/{fieldId}/satellite-images")
    suspend fun getSatelliteImageByDate(
        @Path("fieldId") fieldId: Int,
        @Query("targetDate") targetDate: String,
    ): Response<SatelliteImageDto>
}
