package com.novatech.terratech.core.infrastructure.local

import androidx.room.Dao
import androidx.room.Transaction
import com.novatech.terratech.monitoring.infrastructure.local.dao.MonitoringDao
import com.novatech.terratech.profile.infrastructure.local.dao.ProfileDao

@Dao
interface TerraDao : ProfileDao, MonitoringDao {
    @Transaction
    suspend fun clearPrivateData() {
        clearProfiles()
        clearFields()
        clearSensors()
        clearReadings()
        clearDownloads()
    }
}
