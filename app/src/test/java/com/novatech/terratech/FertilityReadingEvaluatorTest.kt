
package com.novatech.terratech

import com.novatech.terratech.monitoring.domain.entity.Reading
import com.novatech.terratech.monitoring.domain.valueobject.FertilityLevel
import com.novatech.terratech.monitoring.domain.valueobject.NpkRanges
import com.novatech.terratech.monitoring.domain.valueobject.NutrientRanges
import com.novatech.terratech.monitoring.domain.valueobject.evaluateReadingFertility
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FertilityReadingEvaluatorTest {

    private val now = Instant.parse("2026-10-28T12:00:00Z")

    private val ranges = NpkRanges(
        nitrogen = NutrientRanges(10.0, 20.0, 40.0),
        phosphorus = NutrientRanges(7.0, 15.0, 30.0),
        potassium = NutrientRanges(15.0, 30.0, 50.0),
    )

    private fun reading(
        moisture: Double = 75.0,
        nitrogen: Double = 30.0,
        phosphorus: Double = 20.0,
        potassium: Double = 40.0,
        recordedAt: Instant = now,
    ): Reading {
        return Reading(
            id = 1,
            deviceId = 1,
            recordedAt = recordedAt,
            moisturePercent = moisture,
            soilTemperatureC = 22.0,
            nitrogenPpm = nitrogen,
            phosphorusPpm = phosphorus,
            potassiumPpm = potassium,
            source = "SIMULATED",
        )
    }

    @Test
    fun optimalReadingReturnsOptimal() {
        assertEquals(
            FertilityLevel.OPTIMAL,
            evaluateReadingFertility(reading(), now, ranges),
        )
    }

    @Test
    fun moderateMoistureReturnsModerate() {
        assertEquals(
            FertilityLevel.MODERATE,
            evaluateReadingFertility(
                reading(moisture = 45.0),
                now,
                ranges,
            ),
        )
    }

    @Test
    fun lowNitrogenReturnsCritical() {
        assertEquals(
            FertilityLevel.CRITICAL,
            evaluateReadingFertility(
                reading(nitrogen = 5.0),
                now,
                ranges,
            ),
        )
    }

    @Test
    fun missingRangesCannotBeEvaluated() {
        assertNull(
            evaluateReadingFertility(reading(), now, null)
        )
    }

    @Test
    fun staleReadingCannotBeEvaluated() {
        assertNull(
            evaluateReadingFertility(
                reading(
                    recordedAt = now.minusSeconds(31 * 60L)
                ),
                now,
                ranges,
            )
        )
    }

    @Test
    fun futureReadingCannotBeEvaluated() {
        assertNull(
            evaluateReadingFertility(
                reading(
                    recordedAt = now.plusSeconds(60L)
                ),
                now,
                ranges,
            )
        )
    }

    @Test
    fun nutrientAboveConfiguredRangeIsUnknown() {
        assertNull(
            evaluateReadingFertility(
                reading(nitrogen = 45.0),
                now,
                ranges,
            )
        )
    }
}
