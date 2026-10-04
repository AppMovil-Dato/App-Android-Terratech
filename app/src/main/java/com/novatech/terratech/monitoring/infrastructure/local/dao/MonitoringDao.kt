package com.novatech.terratech.monitoring.infrastructure.local.dao

import androidx.room.*
import com.novatech.terratech.monitoring.infrastructure.local.entity.DownloadRow
import com.novatech.terratech.monitoring.infrastructure.local.entity.FieldRow
import com.novatech.terratech.monitoring.infrastructure.local.entity.ReadingRow
import com.novatech.terratech.monitoring.infrastructure.local.entity.SensorRow
import kotlinx.coroutines.flow.Flow

@Dao
interface MonitoringDao {
  @Query("SELECT * FROM fields WHERE userId=:user ORDER BY name")
  fun fields(user: Int): Flow<List<FieldRow>>

  @Query("SELECT * FROM sensors WHERE userId=:user ORDER BY name")
  fun sensors(user: Int): Flow<List<SensorRow>>

  @Query(
    "SELECT * FROM readings WHERE userId=:user AND deviceId=:device ORDER BY julianday(recordedAt),id"
  )
  fun readings(user: Int, device: Int): Flow<List<ReadingRow>>

  @Query("SELECT * FROM downloads WHERE userId=:user AND deviceId=:device AND days=:days")
  fun download(user: Int, device: Int, days: Int): Flow<DownloadRow?>

  @Upsert suspend fun putFields(rows: List<FieldRow>)

  @Upsert suspend fun putSensors(rows: List<SensorRow>)

  @Upsert suspend fun putReadings(rows: List<ReadingRow>)

  @Upsert suspend fun putDownload(row: DownloadRow)

  @Query("DELETE FROM fields WHERE userId=:user") suspend fun deleteFields(user: Int)

  @Query("DELETE FROM sensors WHERE userId=:user AND fieldId=:field")
  suspend fun deleteSensors(user: Int, field: Int)

  @Query("DELETE FROM fields") suspend fun clearFields()

  @Query("DELETE FROM sensors") suspend fun clearSensors()

  @Query("DELETE FROM readings") suspend fun clearReadings()

  @Query("DELETE FROM downloads") suspend fun clearDownloads()
}
