package com.novatech.terratech

import com.novatech.terratech.monitoring.domain.entity.Reading
import com.novatech.terratech.monitoring.domain.entity.Recommendation
import com.novatech.terratech.monitoring.domain.valueobject.RecommendationSeverity
import com.novatech.terratech.monitoring.domain.valueobject.RecommendationType
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationVisibilityTest {
    private val now = Instant.parse("2026-10-29T20:00:00Z")

    private fun reading(id: Int, time: Instant) =
        Reading(id, 11, time, 42.0, 23.0, 35.0, 18.0, 60.0, "SIMULATED")

    private fun recommendation(reading: Reading, device: Int = reading.deviceId) =
        Recommendation(
            deviceId = device,
            readingId = reading.id,
            type = RecommendationType.WATERING,
            severity = RecommendationSeverity.INFO,
            title = "Current status",
            message = "No watering required",
            actionSuggested = null,
            createdAt = reading.recordedAt,
        )

    @Test
    fun onlyTheLatestReadingOfTheSelectedDeviceIsShown() {
        val previous = reading(1, now.minusSeconds(60))
        val current = reading(2, now)
        val expected = recommendation(current)
        val state = MonitoringState(
            deviceId = 11,
            now = now,
            readings = listOf(previous, current),
            recommendations = listOf(recommendation(previous), expected, recommendation(current, 12)),
        )
        assertEquals(listOf(expected), state.deviceRecommendations)
    }

    @Test
    fun expiredOrFutureReadingsHideRecommendations() {
        for (time in listOf(now.minusSeconds(1801), now.plusSeconds(1))) {
            val sample = reading(1, time)
            val state = MonitoringState(
                deviceId = 11, now = now, readings = listOf(sample),
                recommendations = listOf(recommendation(sample)),
            )
            assertTrue(state.deviceRecommendations.isEmpty())
        }
    }

    @Test
    fun missingReadingsHideCachedRecommendations() {
        val state = MonitoringState(
            deviceId = 11, now = now,
            recommendations = listOf(recommendation(reading(1, now))),
        )
        assertTrue(state.deviceRecommendations.isEmpty())
    }
}
