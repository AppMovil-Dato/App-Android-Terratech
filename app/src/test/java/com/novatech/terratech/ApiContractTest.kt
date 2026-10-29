package com.novatech.terratech

import com.google.gson.JsonParser
import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.core.infrastructure.remote.TerraApi
import com.novatech.terratech.core.infrastructure.remote.apiCall
import com.novatech.terratech.iam.infrastructure.remote.dto.RegisterDto
import com.novatech.terratech.monitoring.infrastructure.mapper.domain
import com.novatech.terratech.monitoring.infrastructure.mapper.row
import com.novatech.terratech.monitoring.infrastructure.remote.dto.FieldDto
import com.novatech.terratech.profile.infrastructure.mapper.domain
import com.novatech.terratech.profile.infrastructure.mapper.row
import com.novatech.terratech.profile.infrastructure.remote.dto.SaveProfileDto
import java.io.IOException
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
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
                MockResponse()
                    .setBody("{\"id\":1,\"emailAddress\":\"a@example.com\",\"fullName\":\"Ana\"}")
            )
            api.register(RegisterDto("Ana", "a@example.com", " secret ", " secret "))
            val req = s.takeRequest()
            assertEquals("/api/v1/authentication/sign-up", req.path)
            val json = JsonParser.parseString(req.body.readUtf8()).asJsonObject
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
                } catch (exception: Failure) {
                    assertEquals(status, exception.status)
                    assertEquals(code, exception.code)
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
    fun profileThresholdsSerializedAndDeserializedAccurately() = runTest {
        server { s, api ->
            val body =
                """{"id":1,"userId":10,"fullName":"Carlos Silva","emailAddress":"carlos@terratech.com","fundoName":"Fundo Los Pinos","contactPhone":"+51987654321","location":"Ica, Peru","sizeM2":15000.0,"moistureThreshold":35.5,"tempThreshold":32.0}"""
            s.enqueue(MockResponse().setBody(body))
            val profile = api.profile().row(10).domain()
            val req = s.takeRequest()
            assertEquals("/api/v1/profiles/me", req.path)
            assertEquals(35.5, profile.minimumMoisture, 0.0)
            assertEquals(32.0, profile.temperatureThreshold, 0.0)

            s.enqueue(MockResponse().setBody(body))
            api.saveProfile(
                SaveProfileDto(
                    fullName = "Carlos Silva",
                    fundoName = "Fundo Los Pinos",
                    contactPhone = "+51987654321",
                    location = "Ica, Peru",
                    sizeM2 = 15000.0,
                    moistureThreshold = 35.5,
                    tempThreshold = 32.0,
                ),
            )
            val putReq = s.takeRequest()
            assertEquals("PUT", putReq.method)
            assertEquals("/api/v1/profiles/me", putReq.path)
            val json = JsonParser.parseString(putReq.body.readUtf8()).asJsonObject
            assertEquals(35.5, json["moistureThreshold"].asDouble, 0.0)
            assertEquals(32.0, json["tempThreshold"].asDouble, 0.0)
        }
    }

    @Test
    fun noInternetIsNotAnEmptyCollection() = runTest {
        try {
            apiCall<List<FieldDto>> { throw IOException("network") }
            fail()
        } catch (exception: Failure) {
            assertEquals("OFFLINE", exception.code)
        }
    }

    @Test
    fun expiredSessionIsNotMislabelledOffline() = runTest {
        try {
            apiCall<Unit> { throw IOException("SESSION_EXPIRED") }
            fail()
        } catch (exception: Failure) {
            assertEquals("UNAUTHENTICATED", exception.code)
        }
    }
}
