package com.novatech.terratech.core.infrastructure.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.novatech.terratech.monitoring.infrastructure.local.entity.DownloadRow
import com.novatech.terratech.monitoring.infrastructure.local.entity.FieldRow
import com.novatech.terratech.monitoring.infrastructure.local.entity.FieldWeatherRow
import com.novatech.terratech.monitoring.infrastructure.local.entity.ReadingRow
import com.novatech.terratech.monitoring.infrastructure.local.entity.RecommendationRow
import com.novatech.terratech.monitoring.infrastructure.local.entity.SatelliteImageRow
import com.novatech.terratech.monitoring.infrastructure.local.entity.SensorRow
import com.novatech.terratech.profile.infrastructure.local.entity.ProfileRow

@Database(
    entities =
        [
            ProfileRow::class,
            FieldRow::class,
            SensorRow::class,
            ReadingRow::class,
            DownloadRow::class,
            FieldWeatherRow::class,
            SatelliteImageRow::class,
            RecommendationRow::class,
        ],
    version = 5,
    exportSchema = true,
)
abstract class TerraDatabase : RoomDatabase() {
    abstract fun dao(): TerraDao
}
