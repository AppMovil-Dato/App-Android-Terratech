package com.novatech.terratech.monitoring.infrastructure.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.novatech.terratech.monitoring.infrastructure.local.entity.FieldWeatherRow
import kotlinx.coroutines.flow.Flow

@Dao
interface FieldWeatherDao {
    @Query("SELECT * FROM field_weather WHERE fieldId = :fieldId LIMIT 1")
    fun weather(fieldId: Int): Flow<FieldWeatherRow?>

    @Query("SELECT * FROM field_weather WHERE fieldId = :fieldId LIMIT 1")
    suspend fun findWeather(fieldId: Int): FieldWeatherRow?

    @Upsert
    suspend fun putWeather(row: FieldWeatherRow)

    @Query("DELETE FROM field_weather WHERE fieldId = :fieldId")
    suspend fun deleteWeather(fieldId: Int)

    @Query("DELETE FROM field_weather")
    suspend fun clearWeather()
}
