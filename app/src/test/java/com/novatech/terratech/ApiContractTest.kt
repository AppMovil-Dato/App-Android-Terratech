package com.novatech.terratech

import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.core.infrastructure.remote.TerraApi
import com.novatech.terratech.core.infrastructure.remote.apiCall
import com.novatech.terratech.iam.infrastructure.remote.dto.RegisterDto
import com.novatech.terratech.monitoring.infrastructure.mapper.*
import com.novatech.terratech.monitoring.infrastructure.remote.dto.FieldDto
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiContractTest {
  private suspend fun server(block: suspend (MockWebServer, TerraApi) -> Unit) {
    val server = MockWebServer()
    server.start()
    try {
      block(
        server,
        Retrofit.Builder()
          .baseUrl(server.url("/"))
          .addConverterFactory(GsonConverterFactory.create())
          .build()
          .create(TerraApi::class.java),
      )
    } finally {
      server.shutdown()
    }
  }

  @Test
  fun registrationIncludesExactConfirmation() = runTest {
    server { s, api ->
      s.enqueue(
        MockResponse().setBody("{\"id\":1,\"emailAddress\":\"a@example.com\",\"fullName\":\"Ana\"}")
      )
      api.register(RegisterDto("Ana", "a@example.com", " secret ", " secret "))
      val req = s.takeRequest()
      assertEquals("/api/v1/authentication/sign-up", req.path)
      val json = com.google.gson.JsonParser.parseString(req.body.readUtf8()).asJsonObject
      assertEquals(" secret ", json["confirmPassword"].asString)
    }
  }

  @Test
  fun sensorSelectionUsesFieldRoute() = runTest {
    server { s, api ->
      s.enqueue(MockResponse().setBody("[]"))
      assertTrue(api.sensors(7).isEmpty())
      assertEquals("/api/v1/fields/7/devices", s.takeRequest().path)
    }
  }

  @Test
  fun problemDetailsPreservesConflictAndNoDataCodes() = runTest {
    server { s, api ->
      for ((status, code) in
        listOf(
          409 to "SENSOR_OCCUPIED",
          404 to "NO_READINGS",
          401 to "UNAUTHENTICATED",
          400 to "PASSWORD_CONFIRMATION_MISMATCH",
        )) {
        s.enqueue(MockResponse().setResponseCode(status).setBody("{\"code\":\"$code\"}"))
        try {
          apiCall { api.latest(1) }
          fail()
        } catch (e: Failure) {
          assertEquals(status, e.status)
          assertEquals(code, e.code)
        }
      }
    }
  }

  @Test
  fun historyAndDetailShareUnitsAndIdentity() = runTest {
    server { s, api ->
      val body =
        """{"id":5,"deviceId":2,"recordedAt":"2026-10-04T12:00:00Z","moisturePercent":42,"soilTemperatureC":23,"nitrogenPpm":35,"phosphorusPpm":18,"potassiumPpm":60,"source":"SIMULATED"}"""
      s.enqueue(MockResponse().setBody(body))
      val reading = api.detail(2, 5).row(9).domain()
      assertEquals("/api/v1/devices/2/readings/5", s.takeRequest().path)
      assertEquals(5, reading.id)
      assertEquals(42.0, reading.moisturePercent, 0.0)
      assertEquals(23.0, reading.soilTemperatureC, 0.0)
      assertEquals(35.0, reading.nitrogenPpm, 0.0)
      assertEquals(18.0, reading.phosphorusPpm, 0.0)
      assertEquals(60.0, reading.potassiumPpm, 0.0)
      assertEquals("SIMULATED", reading.source)
    }
  }

  @Test
  fun noInternetIsNotAnEmptyCollection() = runTest {
    try {
      apiCall<List<FieldDto>> { throw java.io.IOException("network") }
      fail()
    } catch (e: Failure) {
      assertEquals("OFFLINE", e.code)
    }
  }

  @Test
  fun expiredSessionIsNotMislabelledOffline() = runTest {
    try {
      apiCall<Unit> { throw java.io.IOException("SESSION_EXPIRED") }
      fail()
    } catch (e: Failure) {
      assertEquals("UNAUTHENTICATED", e.code)
    }
  }
}
