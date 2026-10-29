
package com.novatech.terratech

import com.novatech.terratech.monitoring.domain.valueobject.NutrientLevel
import com.novatech.terratech.monitoring.domain.valueobject.NutrientRanges
import com.novatech.terratech.monitoring.domain.valueobject.classifyNutrient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NutrientRangesTest {

    private val ranges = NutrientRanges(
        criticalBelowPpm = 10.0,
        optimalMinPpm = 20.0,
        optimalMaxPpm = 40.0,
    )

    @Test
    fun nutrientBelowCriticalLimitIsLow() {
        assertEquals(
            NutrientLevel.LOW,
            classifyNutrient(8.0, ranges),
        )
    }

    @Test
    fun nutrientBetweenCriticalAndOptimalIsMedium() {
        assertEquals(
            NutrientLevel.MEDIUM,
            classifyNutrient(15.0, ranges),
        )
    }

    @Test
    fun nutrientWithinOptimalRangeIsOptimal() {
        assertEquals(
            NutrientLevel.OPTIMAL,
            classifyNutrient(20.0, ranges),
        )

        assertEquals(
            NutrientLevel.OPTIMAL,
            classifyNutrient(35.0, ranges),
        )

        assertEquals(
            NutrientLevel.OPTIMAL,
            classifyNutrient(40.0, ranges),
        )
    }

    @Test
    fun nutrientAboveRangeIsNotClassified() {
        assertNull(classifyNutrient(45.0, ranges))
    }

    @Test
    fun invalidNutrientIsNotClassified() {
        assertNull(classifyNutrient(-1.0, ranges))
        assertNull(classifyNutrient(Double.NaN, ranges))
        assertNull(
            classifyNutrient(
                Double.POSITIVE_INFINITY,
                ranges,
            )
        )
    }
}
