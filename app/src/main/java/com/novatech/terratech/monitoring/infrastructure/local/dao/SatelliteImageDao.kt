package com.novatech.terratech.monitoring.infrastructure.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.novatech.terratech.monitoring.infrastructure.local.entity.SatelliteImageRow
import kotlinx.coroutines.flow.Flow

@Dao
interface SatelliteImageDao {
    @Query("SELECT * FROM satellite_images WHERE fieldId = :fieldId ORDER BY capturedAt DESC LIMIT 1")
    fun latestImage(fieldId: Int): Flow<SatelliteImageRow?>

    @Query("SELECT * FROM satellite_images WHERE fieldId = :fieldId ORDER BY capturedAt DESC LIMIT 1")
    suspend fun findLatestImage(fieldId: Int): SatelliteImageRow?

    @Query("SELECT * FROM satellite_images WHERE fieldId = :fieldId AND capturedAt LIKE :datePrefix || '%' ORDER BY capturedAt DESC LIMIT 1")
    suspend fun findImageByDate(fieldId: Int, datePrefix: String): SatelliteImageRow?

    @Query("SELECT * FROM satellite_images WHERE fieldId = :fieldId ORDER BY capturedAt DESC")
    fun imagesForField(fieldId: Int): Flow<List<SatelliteImageRow>>

    @Upsert
    suspend fun putImage(row: SatelliteImageRow)

    @Query("DELETE FROM satellite_images WHERE fieldId = :fieldId")
    suspend fun deleteImages(fieldId: Int)

    @Query("DELETE FROM satellite_images")
    suspend fun clearSatelliteImages()
}
