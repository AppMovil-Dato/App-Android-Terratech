package com.novatech.terratech

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.novatech.terratech.core.infrastructure.local.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomCacheTest {
  private lateinit var db: TerraDatabase

  @Before
  fun setup() {
    db =
      Room.inMemoryDatabaseBuilder(
          ApplicationProvider.getApplicationContext(),
          TerraDatabase::class.java,
        )
        .build()
  }

  @After
  fun close() {
    db.close()
  }

  private fun field(user: Int) =
    FieldRow(user, 1, 1, "North", 10000.0, "Loam", -12.0, -77.0, "Potato")

  private fun reading(user: Int, id: Int, at: String) =
    ReadingRow(user, 2, id, at, 42.0, 23.0, 35.0, 18.0, 60.0, "SIMULATED")

  @Test
  fun userScopedIdsNeverLeakBetweenAccounts() = runBlocking {
    db.dao().putFields(listOf(field(1), field(2).copy(name = "South")))
    assertEquals("North", db.dao().fields(1).first().single().name)
    assertEquals("South", db.dao().fields(2).first().single().name)
    assertTrue(db.dao().fields(3).first().isEmpty())
  }

  @Test
  fun readingsUpsertWithoutDuplicatesAndKeepLongerHistory() = runBlocking {
    val old = reading(1, 1, "2026-09-05T00:00:00Z")
    val recent = reading(1, 2, "2026-10-04T00:00:00Z")
    db.dao().putReadings(listOf(old, recent))
    db.dao().putReadings(listOf(recent.copy(moisturePercent = 50.0)))
    val rows = db.dao().readings(1, 2).first()
    assertEquals(2, rows.size)
    assertEquals(1, rows.first().id)
    assertEquals(50.0, rows.last().moisturePercent, 0.0)
    assertTrue(db.dao().readings(2, 2).first().isEmpty())
  }

  @Test
  fun rangeDownloadMetadataIsSeparateForSevenAndThirty() = runBlocking {
    db.dao().putDownload(DownloadRow(1, 2, 30, "2026-10-04T00:00:00Z", null, null, 30.0))
    assertNull(db.dao().download(1, 2, 7).first())
    assertEquals(30.0, db.dao().download(1, 2, 30).first()!!.minimumMoisture!!, 0.0)
  }

  @Test
  fun logoutClearsAllPrivateTables() = runBlocking {
    db.dao().putFields(listOf(field(1)))
    db.dao().putReadings(listOf(reading(1, 1, "2026-10-04T00:00:00Z")))
    db
      .dao()
      .putProfile(ProfileRow(1, 1, "Ana", "a@example.com", "Farm", "999", "Lima", 10000.0, 30.0))
    db.dao().clearPrivateData()
    assertTrue(db.dao().fields(1).first().isEmpty())
    assertTrue(db.dao().readings(1, 2).first().isEmpty())
    assertNull(db.dao().profile(1).first())
  }

  @Test
  fun chronologicalOrderHandlesMixedIsoUtcPrecision() = runBlocking {
    db
      .dao()
      .putReadings(
        listOf(reading(1, 2, "2026-10-04T00:00:00.123Z"), reading(1, 1, "2026-10-04T00:00:00Z"))
      )
    assertEquals(listOf(1, 2), db.dao().readings(1, 2).first().map { it.id })
  }
}
