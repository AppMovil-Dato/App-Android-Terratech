package com.novatech.terratech.core.infrastructure.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object FieldBoundaryMigration : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE fields ADD COLUMN boundaryJson TEXT NOT NULL DEFAULT '[]'")
    }
}
