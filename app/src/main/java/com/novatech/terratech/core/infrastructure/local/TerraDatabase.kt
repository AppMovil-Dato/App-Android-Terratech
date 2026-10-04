package com.novatech.terratech.core.infrastructure.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "profiles", primaryKeys = ["userId"])
data class ProfileRow(
  val userId: Int,
  val id: Int,
  val fullName: String,
  val email: String,
  val fundoName: String,
  val contactPhone: String,
  val location: String,
  val sizeM2: Double?,
  val minimumMoisture: Double,
)

@Entity(tableName = "fields", primaryKeys = ["userId", "id"])
data class FieldRow(
  val userId: Int,
  val id: Int,
  val profileId: Int,
  val name: String,
  val sizeM2: Double,
  val soilType: String,
  val latitude: Double,
  val longitude: Double,
  val cropName: String?,
)

@Entity(
  tableName = "sensors",
  primaryKeys = ["userId", "id"],
  indices = [Index(value = ["userId", "fieldId"])],
)
data class SensorRow(
  val userId: Int,
  val id: Int,
  val fieldId: Int,
  val name: String?,
  val sensorCode: String?,
  val macAddress: String,
  val status: String,
)

@Entity(
  tableName = "readings",
  primaryKeys = ["userId", "deviceId", "id"],
  indices = [Index(value = ["userId", "deviceId", "recordedAt"])],
)
data class ReadingRow(
  val userId: Int,
  val deviceId: Int,
  val id: Int,
  val recordedAt: String,
  val moisturePercent: Double,
  val soilTemperatureC: Double,
  val nitrogenPpm: Double,
  val phosphorusPpm: Double,
  val potassiumPpm: Double,
  val source: String,
)

@Entity(tableName = "downloads", primaryKeys = ["userId", "deviceId", "days"])
data class DownloadRow(
  val userId: Int,
  val deviceId: Int,
  val days: Int,
  val downloadedAt: String,
  val fromUtc: String?,
  val toUtc: String?,
  val minimumMoisture: Double?,
)

@Dao
interface TerraDao {
  @Query("SELECT * FROM profiles WHERE userId=:user") fun profile(user: Int): Flow<ProfileRow?>

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

  @Upsert suspend fun putProfile(row: ProfileRow)

  @Upsert suspend fun putFields(rows: List<FieldRow>)

  @Upsert suspend fun putSensors(rows: List<SensorRow>)

  @Upsert suspend fun putReadings(rows: List<ReadingRow>)

  @Upsert suspend fun putDownload(row: DownloadRow)

  @Query("DELETE FROM fields WHERE userId=:user") suspend fun deleteFields(user: Int)

  @Query("DELETE FROM sensors WHERE userId=:user AND fieldId=:field")
  suspend fun deleteSensors(user: Int, field: Int)

  @Query("DELETE FROM profiles") suspend fun clearProfiles()

  @Query("DELETE FROM fields") suspend fun clearFields()

  @Query("DELETE FROM sensors") suspend fun clearSensors()

  @Query("DELETE FROM readings") suspend fun clearReadings()

  @Query("DELETE FROM downloads") suspend fun clearDownloads()

  @Transaction
  suspend fun clearPrivateData() {
    clearProfiles()
    clearFields()
    clearSensors()
    clearReadings()
    clearDownloads()
  }
}

@Database(
  entities =
    [ProfileRow::class, FieldRow::class, SensorRow::class, ReadingRow::class, DownloadRow::class],
  version = 1,
  exportSchema = true,
)
abstract class TerraDatabase : RoomDatabase() {
  abstract fun dao(): TerraDao
}
