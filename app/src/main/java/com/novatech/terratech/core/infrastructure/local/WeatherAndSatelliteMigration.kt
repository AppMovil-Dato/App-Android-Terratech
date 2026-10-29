package com.novatech.terratech.core.infrastructure.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object WeatherAndSatelliteMigration : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `field_weather` (
                `fieldId` INTEGER NOT NULL,
                `fieldName` TEXT NOT NULL,
                `forecastTimestamp` TEXT NOT NULL,
                `currentTemperatureC` REAL NOT NULL,
                `relativeHumidityPercent` REAL NOT NULL,
                `windSpeedKmh` REAL NOT NULL,
                `precipitationProbability24h` REAL NOT NULL,
                `expectedPrecipitationMm` REAL NOT NULL,
                `suspensionRecommended` INTEGER NOT NULL,
                `estimatedWaterSavingsLiters` INTEGER NOT NULL,
                `recommendationMessage` TEXT NOT NULL,
                `downloadedAt` INTEGER NOT NULL,
                PRIMARY KEY(`fieldId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `satellite_images` (
                `fieldId` INTEGER NOT NULL,
                `capturedAt` TEXT NOT NULL,
                `imageUrl` TEXT NOT NULL,
                `ndviAverage` REAL NOT NULL,
                `cloudCoveragePercent` REAL NOT NULL,
                `boundsNorth` REAL NOT NULL,
                `boundsSouth` REAL NOT NULL,
                `boundsEast` REAL NOT NULL,
                `boundsWest` REAL NOT NULL,
                `downloadedAt` INTEGER NOT NULL,
                PRIMARY KEY(`fieldId`, `capturedAt`)
            )
            """.trimIndent(),
        )
    }
}
