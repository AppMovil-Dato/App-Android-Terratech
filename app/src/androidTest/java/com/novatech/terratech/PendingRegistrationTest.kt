package com.novatech.terratech

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.novatech.terratech.core.infrastructure.local.TerraDatabase
import com.novatech.terratech.iam.infrastructure.implementation.AccountRepositoryImpl
import com.novatech.terratech.iam.infrastructure.local.SessionStore
import com.novatech.terratech.iam.infrastructure.remote.AccountApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(AndroidJUnit4::class)
class PendingRegistrationTest {
    @Test
    fun verificationRequiredDoesNotLoginOrPersistASession() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = SessionStore(context)
        store.clear()
        val db = Room.inMemoryDatabaseBuilder(context, TerraDatabase::class.java).build()
        val server = MockWebServer()
        server.start()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val api = Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(AccountApi::class.java)
            val repository = AccountRepositoryImpl(api, store, db, scope)
            repository.restored.first { it }
            server.enqueue(MockResponse().setResponseCode(201).setBody("""{"id":91,"emailAddress":"test@example.com","fullName":"Test","token":null,"expiresAt":null,"emailVerificationRequired":true}"""))
            repository.register("Test", "test@example.com", "password123", "password123")
            assertEquals(1, server.requestCount)
            assertNull(repository.session.value)
            assertNull(store.load())
        } finally {
            scope.cancel()
            store.clear()
            db.close()
            server.shutdown()
        }
    }
}
