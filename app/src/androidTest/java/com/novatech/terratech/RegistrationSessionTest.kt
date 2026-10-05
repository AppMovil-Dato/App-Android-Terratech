package com.novatech.terratech

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.core.infrastructure.local.TerraDatabase
import com.novatech.terratech.core.infrastructure.remote.TerraApi
import com.novatech.terratech.iam.infrastructure.implementation.AccountRepositoryImpl
import com.novatech.terratech.iam.infrastructure.local.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(AndroidJUnit4::class)
class RegistrationSessionTest {
    @Test fun signupSessionIsSavedWithoutAnotherLoginRequest() = scenario("session")

    @Test fun legacySignupAutomaticallyLogsInWithExactEnteredCredentials() = scenario("legacy")

    @Test fun failedLegacyLoginReportsCreatedAccountAndDoesNotRepeatSignup() = scenario("failure")

    private fun scenario(mode: String) = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = SessionStore(context)
        store.clear()
        val db = Room.inMemoryDatabaseBuilder(context, TerraDatabase::class.java).build()
        val server = MockWebServer()
        server.start()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val api =
            Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TerraApi::class.java)
        val repo = AccountRepositoryImpl(api, store, db, scope)
        val identity = """"id":91,"emailAddress":"ana@example.com","fullName":"Ana Torres""""
        val session =
            """{$identity,"token":"signup-test-token","expiresAt":"2030-01-01T00:00:00Z"}"""
        try {
            repo.restored.first { it }
            server.enqueue(
                MockResponse()
                    .setResponseCode(201)
                    .setBody(if (mode == "session") session else "{$identity}")
            )
            if (mode != "session")
                server.enqueue(
                    if (mode == "legacy") MockResponse().setBody(session)
                    else MockResponse().setResponseCode(503).setBody("""{"code":"UNAVAILABLE"}""")
                )
            if (mode == "failure") {
                try {
                    repo.register(
                        "Ana Torres",
                        "ana@example.com",
                        " exact password ",
                        " exact password ",
                    )
                    fail("Expected a login recovery error")
                } catch (e: Failure) {
                    assertEquals("ACCOUNT_CREATED_LOGIN_REQUIRED", e.code)
                }
                assertNull(repo.session.value)
                assertNull(store.load())
            } else {
                repo.register(
                    "Ana Torres",
                    "ana@example.com",
                    " exact password ",
                    " exact password ",
                )
                assertEquals(91, repo.session.value!!.userId)
                assertEquals(repo.session.value, store.load())
            }
            assertEquals("/api/v1/authentication/sign-up", server.takeRequest().path)
            assertEquals(if (mode == "session") 1 else 2, server.requestCount)
            if (mode != "session") {
                val login = server.takeRequest()
                assertEquals("/api/v1/authentication/sign-in", login.path)
                val body =
                    com.google.gson.JsonParser.parseString(login.body.readUtf8()).asJsonObject
                assertEquals(" exact password ", body["password"].asString)
                assertEquals("ana@example.com", body["emailAddress"].asString)
            }
        } finally {
            scope.cancel()
            store.clear()
            db.close()
            server.shutdown()
        }
    }
}
