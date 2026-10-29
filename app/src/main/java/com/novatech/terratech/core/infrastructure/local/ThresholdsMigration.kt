package com.novatech.terratech.core.infrastructure.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object ThresholdsMigration : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE profiles ADD COLUMN temperatureThreshold REAL NOT NULL DEFAULT 35.0")
    }
}
