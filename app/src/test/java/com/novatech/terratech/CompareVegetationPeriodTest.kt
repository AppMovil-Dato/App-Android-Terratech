package com.novatech.terratech

import com.novatech.terratech.monitoring.application.usecase.CompareVegetationPeriod
import com.novatech.terratech.monitoring.domain.entity.SatelliteBounds
import com.novatech.terratech.monitoring.domain.entity.SatelliteImage
import org.junit.Assert.assertEquals
import org.junit.Test

class CompareVegetationPeriodTest {
    private val bounds =
        SatelliteBounds(
            north = -11.501,
            south = -11.505,
            east = -77.201,
            west = -77.205,
        )

    @Test
    fun vegetationComparisonCalculatesPositiveNdviDeltaCorrectly() {
        val useCase = CompareVegetationPeriod()
        val imageA =
            SatelliteImage(
                fieldId = 1,
                capturedAt = "2026-10-01T06:00:00Z",
                imageUrl = "https://example.com/a.png",
                ndviAverage = 0.50,
                cloudCoveragePercent = 2.0,
                bounds = bounds,
            )
        val imageB =
            SatelliteImage(
                fieldId = 1,
                capturedAt = "2026-10-15T06:00:00Z",
                imageUrl = "https://example.com/b.png",
                ndviAverage = 0.75,
                cloudCoveragePercent = 1.0,
                bounds = bounds,
            )

        val result = useCase(imageA, imageB)

        assertEquals("2026-10-01T06:00:00Z", result.earlier.capturedAt)
        assertEquals("2026-10-15T06:00:00Z", result.later.capturedAt)
        assertEquals(0.25, result.ndviDelta, 0.001)
        assertEquals(50.0, result.percentageChange, 0.01)
    }

    @Test
    fun vegetationComparisonOrdersDatesChronologically() {
        val useCase = CompareVegetationPeriod()
        val imageRecent =
            SatelliteImage(
                fieldId = 1,
                capturedAt = "2026-10-20T06:00:00Z",
                imageUrl = "https://example.com/recent.png",
                ndviAverage = 0.40,
                cloudCoveragePercent = 5.0,
                bounds = bounds,
            )
        val imageOld =
            SatelliteImage(
                fieldId = 1,
                capturedAt = "2026-09-20T06:00:00Z",
                imageUrl = "https://example.com/old.png",
                ndviAverage = 0.60,
                cloudCoveragePercent = 0.0,
                bounds = bounds,
            )

        val result = useCase(imageRecent, imageOld)

        assertEquals("2026-09-20T06:00:00Z", result.earlier.capturedAt)
        assertEquals("2026-10-20T06:00:00Z", result.later.capturedAt)
        assertEquals(-0.20, result.ndviDelta, 0.001)
        assertEquals(-33.33, result.percentageChange, 0.02)
    }
}
