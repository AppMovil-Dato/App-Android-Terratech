package com.novatech.terratech.core.infrastructure.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RecommendationsMigration : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `profiles` ADD COLUMN `registeredAt` TEXT")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `recommendations` (
                `userId` INTEGER NOT NULL, `deviceId` INTEGER NOT NULL,
                `readingId` INTEGER NOT NULL, `type` TEXT NOT NULL,
                `severity` TEXT NOT NULL, `title` TEXT NOT NULL,
                `message` TEXT NOT NULL, `actionSuggested` TEXT,
                `createdAt` TEXT NOT NULL,
                PRIMARY KEY(`userId`, `deviceId`, `readingId`, `type`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_recommendations_userId_deviceId_createdAt` ON `recommendations` (`userId`, `deviceId`, `createdAt`)")
    }
}
