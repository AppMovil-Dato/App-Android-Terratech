
package com.novatech.terratech

import com.novatech.terratech.monitoring.domain.valueobject.MoistureLevel
import com.novatech.terratech.monitoring.domain.valueobject.classifyMoisture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoistureLevelTest {

    @Test
    fun moistureBelowThirtyIsCritical() {
        assertEquals(
            MoistureLevel.CRITICAL,
            classifyMoisture(0.0),
        )

        assertEquals(
            MoistureLevel.CRITICAL,
            classifyMoisture(25.0),
        )

        assertEquals(
            MoistureLevel.CRITICAL,
            classifyMoisture(29.99),
        )
    }

    @Test
    fun moistureFromThirtyToSixtyIsModerate() {
        assertEquals(
            MoistureLevel.MODERATE,
            classifyMoisture(30.0),
        )

        assertEquals(
            MoistureLevel.MODERATE,
            classifyMoisture(45.0),
        )

        assertEquals(
            MoistureLevel.MODERATE,
            classifyMoisture(60.0),
        )
    }

    @Test
    fun moistureAboveSixtyIsOptimal() {
        assertEquals(
            MoistureLevel.OPTIMAL,
            classifyMoisture(60.01),
        )

        assertEquals(
            MoistureLevel.OPTIMAL,
            classifyMoisture(75.0),
        )

        assertEquals(
            MoistureLevel.OPTIMAL,
            classifyMoisture(100.0),
        )
    }

    @Test
    fun invalidMoistureCannotBeClassified() {
        assertNull(classifyMoisture(-1.0))
        assertNull(classifyMoisture(101.0))
        assertNull(classifyMoisture(Double.NaN))
        assertNull(classifyMoisture(Double.POSITIVE_INFINITY))
        assertNull(classifyMoisture(Double.NEGATIVE_INFINITY))
    }
}
