package com.novatech.terratech.core.infrastructure.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.novatech.terratech.monitoring.infrastructure.local.entity.DownloadRow
import com.novatech.terratech.monitoring.infrastructure.local.entity.FieldRow
import com.novatech.terratech.monitoring.infrastructure.local.entity.ReadingRow
import com.novatech.terratech.monitoring.infrastructure.local.entity.SensorRow
import com.novatech.terratech.profile.infrastructure.local.entity.ProfileRow

@Database(
  entities =
    [ProfileRow::class, FieldRow::class, SensorRow::class, ReadingRow::class, DownloadRow::class],
  version = 1,
  exportSchema = true,
)
abstract class TerraDatabase : RoomDatabase() {
  abstract fun dao(): TerraDao
}
