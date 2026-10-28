
package com.novatech.terratech

import com.novatech.terratech.monitoring.domain.valueobject.FertilityLevel
import com.novatech.terratech.monitoring.domain.valueobject.NutrientLevel
import com.novatech.terratech.monitoring.domain.valueobject.classifyFertility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FertilityLevelTest {

    @Test
    fun criticalMoistureMeansCriticalFertility() {
        assertEquals(
            FertilityLevel.CRITICAL,
            classifyFertility(25.0, null, null, null),
        )
    }

    @Test
    fun lowNutrientMeansCriticalFertility() {
        assertEquals(
            FertilityLevel.CRITICAL,
            classifyFertility(
                75.0,
                NutrientLevel.LOW,
                NutrientLevel.OPTIMAL,
                NutrientLevel.OPTIMAL,
            ),
        )
    }

    @Test
    fun moderateMoistureMeansModerateFertility() {
        assertEquals(
            FertilityLevel.MODERATE,
            classifyFertility(
                45.0,
                NutrientLevel.OPTIMAL,
                NutrientLevel.OPTIMAL,
                NutrientLevel.OPTIMAL,
            ),
        )
    }

    @Test
    fun mediumNutrientMeansModerateFertility() {
        assertEquals(
            FertilityLevel.MODERATE,
            classifyFertility(
                75.0,
                NutrientLevel.OPTIMAL,
                NutrientLevel.MEDIUM,
                NutrientLevel.OPTIMAL,
            ),
        )
    }

    @Test
    fun allOptimalMeansOptimalFertility() {
        assertEquals(
            FertilityLevel.OPTIMAL,
            classifyFertility(
                75.0,
                NutrientLevel.OPTIMAL,
                NutrientLevel.OPTIMAL,
                NutrientLevel.OPTIMAL,
            ),
        )
    }

    @Test
    fun missingNutrientMeansUnknownFertility() {
        assertNull(
            classifyFertility(
                75.0,
                null,
                NutrientLevel.OPTIMAL,
                NutrientLevel.OPTIMAL,
            )
        )
    }

    @Test
    fun invalidMoistureCannotBeClassified() {
        assertNull(
            classifyFertility(
                110.0,
                NutrientLevel.OPTIMAL,
                NutrientLevel.OPTIMAL,
                NutrientLevel.OPTIMAL,
            )
        )
    }
}
