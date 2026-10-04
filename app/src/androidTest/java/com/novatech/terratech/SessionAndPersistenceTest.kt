package com.novatech.terratech

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.novatech.terratech.core.infrastructure.local.TerraDatabase
import com.novatech.terratech.iam.domain.entity.Session
import com.novatech.terratech.iam.infrastructure.local.SessionStore
import com.novatech.terratech.monitoring.infrastructure.local.entity.FieldRow
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionAndPersistenceTest {
  @Test
  fun diskCacheSurvivesDatabaseReopen() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val name = "cache-persistence-test.db"
    context.deleteDatabase(name)
    val db = Room.databaseBuilder(context, TerraDatabase::class.java, name).build()
    db.dao().putFields(listOf(FieldRow(91, 5, 2, "North", 10000.0, "Loam", -12.0, -77.0, "Potato")))
    db.close()
    val reopened = Room.databaseBuilder(context, TerraDatabase::class.java, name).build()
    try {
      assertEquals("North", reopened.dao().fields(91).first().single().name)
      assertTrue(reopened.dao().fields(92).first().isEmpty())
    } finally {
      reopened.close()
      context.deleteDatabase(name)
    }
  }

  @Test
  fun encryptedSessionAndSelectionRestoreAndLogoutClear() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val store = SessionStore(context)
    store.clear()
    val session =
      Session(
        91,
        "test@example.com",
        "Test",
        "test-private-token",
        Instant.parse("2026-10-04T00:00:00Z"),
      )
    store.save(session)
    store.select(91, 5, 7)
    assertEquals(session, store.load())
    assertEquals(5 to 7, store.selection(91).first())
    assertEquals(null to null, store.selection(92).first())
    val file = java.io.File(context.filesDir, "datastore/session.preferences_pb")
    assertFalse(file.readBytes().toString(Charsets.ISO_8859_1).contains(session.token))
    store.clear()
    assertNull(store.load())
    assertEquals(null to null, store.selection(91).first())
  }

  @Test
  fun reauthenticationKeepsOwnCacheButSwitchingAccountClearsIt() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val store = SessionStore(context)
    store.clear()
    val previous =
      Session(
        91,
        "test@example.com",
        "Test",
        "expired-test-token",
        Instant.parse("2026-01-01T00:00:00Z"),
      )
    store.save(previous)
    val db = Room.inMemoryDatabaseBuilder(context, TerraDatabase::class.java).build()
    db.dao().putFields(listOf(FieldRow(91, 5, 2, "North", 10000.0, "Loam", -12.0, -77.0, "Potato")))
    val server = okhttp3.mockwebserver.MockWebServer()
    server.start()
    val api =
      retrofit2.Retrofit.Builder()
        .baseUrl(server.url("/"))
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()
        .create(com.novatech.terratech.core.infrastructure.remote.TerraApi::class.java)
    val scope =
      kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO
      )
    val repo =
      com.novatech.terratech.iam.infrastructure.implementation.AccountRepositoryImpl(
        api,
        store,
        db,
        scope,
      )
    try {
      repo.restored.first { it }
      fun response(id: Int) =
        okhttp3.mockwebserver
          .MockResponse()
          .setBody(
            """{"id":$id,"emailAddress":"test@example.com","fullName":"Test","token":"new-test-token","expiresAt":"2030-01-01T00:00:00Z"}"""
          )
      server.enqueue(response(91))
      repo.login("test@example.com", "test-password")
      assertEquals("North", db.dao().fields(91).first().single().name)
      server.enqueue(response(92))
      repo.login("other@example.com", "test-password")
      assertTrue(db.dao().fields(91).first().isEmpty())
      assertEquals(92, repo.session.value!!.userId)
    } finally {
      scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
      store.clear()
      db.close()
      server.shutdown()
    }
  }
}
