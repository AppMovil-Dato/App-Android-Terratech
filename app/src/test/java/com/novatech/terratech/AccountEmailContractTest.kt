package com.novatech.terratech

import com.novatech.terratech.iam.infrastructure.remote.AccountApi
import com.novatech.terratech.iam.infrastructure.remote.dto.AccountEmailRequestDto
import com.novatech.terratech.iam.infrastructure.remote.dto.RegisterDto
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AccountEmailContractTest {
    @Test
    fun pendingRegistrationHasNoSessionAndEmailActionsUseDedicatedRoutes() = runTest {
        val server = MockWebServer()
        server.start()
        try {
            val api = Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(AccountApi::class.java)
            server.enqueue(MockResponse().setResponseCode(201).setBody("""{"id":1,"emailAddress":"test@example.com","fullName":"Test","token":null,"expiresAt":null,"emailVerificationRequired":true}"""))
            val response = api.register(RegisterDto("Test", "test@example.com", "secret123", "secret123"))
            assertTrue(response.emailVerificationRequired)
            assertNull(response.token)
            server.takeRequest()
            server.enqueue(MockResponse().setResponseCode(202).setBody("{}"))
            api.requestVerification(AccountEmailRequestDto("test@example.com"))
            assertEquals("/api/v1/authentication/email-verifications", server.takeRequest().path)
            server.enqueue(MockResponse().setResponseCode(202).setBody("{}"))
            api.requestPasswordReset(AccountEmailRequestDto("test@example.com"))
            assertEquals("/api/v1/authentication/password-reset-requests", server.takeRequest().path)
        } finally { server.shutdown() }
    }
}
