package com.novatech.terratech

import com.novatech.terratech.core.infrastructure.remote.TerraApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ExternalServicesContractTest {
    private suspend fun server(block: suspend (MockWebServer, TerraApi) -> Unit) {
        val server = MockWebServer()
        server.start()
        try {
            val api =
                Retrofit.Builder()
                    .baseUrl(server.url("/"))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(TerraApi::class.java)
            block(server, api)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun weatherForecastReturns200WithCombinedRecommendation() =
        runTest {
            server { mockServer, api ->
                val json =
                    """
                    {
                      "fieldId": 1,
                      "fieldName": "Parcela Norte",
                      "forecastTimestamp": "2026-10-28T06:00:00Z",
                      "currentTemperatureC": 22.4,
                      "relativeHumidityPercent": 68.0,
                      "windSpeedKmh": 14.1,
                      "precipitationProbability24h": 85.0,
                      "expectedPrecipitationMm": 12.5,
                      "combinedRecommendation": {
                        "suspensionRecommended": true,
                        "estimatedWaterSavingsLiters": 500,
                        "message": "Se recomienda suspender el riego."
                      }
                    }
                    """.trimIndent()
                mockServer.enqueue(MockResponse().setResponseCode(200).setBody(json))

                val response = api.getFieldWeather(1)
                assertTrue(response.isSuccessful)
                val body = response.body()
                assertNotNull(body)
                assertEquals(1, body?.fieldId)
                assertEquals(22.4, body?.currentTemperatureC ?: 0.0, 0.01)
                assertEquals(85.0, body?.precipitationProbability24h ?: 0.0, 0.01)
                assertTrue(body?.combinedRecommendation?.suspensionRecommended == true)
                assertEquals(500, body?.combinedRecommendation?.estimatedWaterSavingsLiters)
            }
        }

    @Test
    fun latestSatelliteImageReturns200WithBoundsAndNdvi() =
        runTest {
            server { mockServer, api ->
                val json =
                    """
                    {
                      "fieldId": 1,
                      "capturedAt": "2026-10-26T06:00:00Z",
                      "imageUrl": "https://storage.googleapis.com/terratech/fields/1/latest.png",
                      "ndviAverage": 0.74,
                      "cloudCoveragePercent": 3.2,
                      "bounds": {
                        "north": -11.501,
                        "south": -11.505,
                        "east": -77.201,
                        "west": -77.205
                      }
                    }
                    """.trimIndent()
                mockServer.enqueue(MockResponse().setResponseCode(200).setBody(json))

                val response = api.getLatestSatelliteImage(1)
                assertTrue(response.isSuccessful)
                val body = response.body()
                assertNotNull(body)
                assertEquals("2026-10-26T06:00:00Z", body?.capturedAt)
                assertEquals(0.74, body?.ndviAverage ?: 0.0, 0.01)
                assertEquals(3.2, body?.cloudCoveragePercent ?: 0.0, 0.01)
                assertEquals(-11.501, body?.bounds?.north ?: 0.0, 0.001)
            }
        }

    @Test
    fun weatherServiceReturns502WhenProviderUnavailable() =
        runTest {
            server { mockServer, api ->
                mockServer.enqueue(
                    MockResponse()
                        .setResponseCode(502)
                        .setBody("{\"status\":502,\"code\":\"WEATHER_SERVICE_UNAVAILABLE\"}"),
                )
                val response = api.getFieldWeather(1)
                assertFalse(response.isSuccessful)
                assertEquals(502, response.code())
            }
        }

    @Test
    fun satelliteImageByDateReturns404WhenNotFound() =
        runTest {
            server { mockServer, api ->
                mockServer.enqueue(
                    MockResponse()
                        .setResponseCode(404)
                        .setBody("{\"status\":404,\"code\":\"SATELLITE_IMAGE_NOT_FOUND\"}"),
                )
                val response = api.getSatelliteImageByDate(1, "2026-01-01")
                assertFalse(response.isSuccessful)
                assertEquals(404, response.code())
            }
        }
}
