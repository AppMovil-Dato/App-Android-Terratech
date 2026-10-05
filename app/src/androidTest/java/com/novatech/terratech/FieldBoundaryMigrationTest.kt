package com.novatech.terratech

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.JsonParser
import com.novatech.terratech.core.infrastructure.local.FieldBoundaryMigration
import com.novatech.terratech.core.infrastructure.local.TerraDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FieldBoundaryMigrationTest {
    @Test
    fun oldFieldsSurviveMigrationWithEmptyBoundary() = runBlocking {
        val name = "boundary-migration-test.db"
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(name)
        val schema =
            InstrumentationRegistry.getInstrumentation()
                .context
                .assets
                .open("com.novatech.terratech.core.infrastructure.local.TerraDatabase/1.json")
                .bufferedReader()
                .use { JsonParser.parseReader(it).asJsonObject.getAsJsonObject("database") }
        context.getDatabasePath(name).parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(name), null).use { old ->
            schema.getAsJsonArray("entities").forEach { element ->
                val entity = element.asJsonObject
                val table = entity["tableName"].asString
                old.execSQL(entity["createSql"].asString.replace("\${TABLE_NAME}", table))
                entity.getAsJsonArray("indices")?.forEach {
                    old.execSQL(
                        it.asJsonObject["createSql"].asString.replace("\${TABLE_NAME}", table)
                    )
                }
            }
            schema.getAsJsonArray("setupQueries").forEach { old.execSQL(it.asString) }
            old.execSQL(
                "INSERT INTO fields (userId,id,profileId,name,sizeM2,soilType,latitude,longitude,cropName) VALUES (91,5,2,'North',10000,'Loam',-12,-77,'Potato')"
            )
            old.version = 1
        }
        val db =
            Room.databaseBuilder(context, TerraDatabase::class.java, name)
                .addMigrations(FieldBoundaryMigration)
                .build()
        try {
            val field = db.dao().fields(91).first().single()
            assertEquals("North", field.name)
            assertEquals("[]", field.boundaryJson)
            assertTrue(db.dao().fields(92).first().isEmpty())
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }
}
