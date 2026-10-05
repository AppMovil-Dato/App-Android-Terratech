package com.novatech.terratech

import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.iam.domain.entity.Session
import com.novatech.terratech.iam.domain.valueobject.Email
import com.novatech.terratech.iam.domain.valueobject.Password
import com.novatech.terratech.monitoring.domain.entity.Reading
import com.novatech.terratech.monitoring.domain.valueobject.Coordinates
import com.novatech.terratech.monitoring.domain.valueobject.SensorCode
import com.novatech.terratech.profile.domain.valueobject.AreaM2
import com.novatech.terratech.profile.domain.valueobject.FarmDetails
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class DomainRulesTest {
    private val now = Instant.parse("2026-10-04T12:00:00Z")

    private fun reading(at: Instant) = Reading(1, 2, at, 42.0, 23.0, 35.0, 18.0, 60.0, "SIMULATED")

    private fun invalid(code: String, block: () -> Unit) {
        try {
            block()
            fail("Expected validation failure")
        } catch (exception: Failure) {
            assertEquals(code, exception.code)
        }
    }

    @Test
    fun emailNormalizesWithoutChangingPassword() {
        assertEquals("ana@example.com", Email.of(" Ana@Example.com ").value)
        assertEquals(" secret ", Password.of(" secret ").value)
    }

    @Test
    fun invalidEmailsFail() {
        listOf("", "ana", "ana@", "a b@c.com", "a@b").forEach {
            invalid("INVALID_EMAIL") { Email.of(it) }
        }
    }

    @Test
    fun passwordLimits() {
        invalid("INVALID_PASSWORD") { Password.of("short") }
        invalid("INVALID_PASSWORD") { Password.of("a".repeat(129)) }
        assertEquals(6, Password.of("123456").value.length)
    }

    @Test
    fun sensorCodeNormalizationAndValidation() {
        assertEquals("TT-ZZZ001", SensorCode.of(" tt-zzz001 ").value)
        listOf("", "TT-123", "BAD-123456", "TT-12345!").forEach {
            invalid("INVALID_SENSOR_CODE") { SensorCode.of(it) }
        }
    }

    @Test
    fun hectaresConvertPreciselyToBackendUnits() {
        assertEquals(12500.0, AreaM2.fromHectares(1.25).value, 0.00001)
        assertEquals(1.25, AreaM2.of(12500.0).hectares, 0.00001)
    }

    @Test
    fun invalidAreasFail() {
        listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY).forEach {
            invalid("INVALID_AREA") { AreaM2.of(it) }
        }
    }

    @Test
    fun readingStaleBoundaryIncludesFractions() {
        assertFalse(reading(now.minusSeconds(1800)).isStale(now))
        assertTrue(reading(now.minusSeconds(1800).minusMillis(1)).isStale(now))
        assertFalse(reading(now.minusSeconds(1799)).isStale(now))
    }

    @Test
    fun rangesIncludeUtcEndpointsAndExcludeFuture() {
        for (days in listOf(7, 30)) {
            assertTrue(reading(now.minusSeconds(days * 86400L)).inRange(days, now))
            assertFalse(reading(now.minusSeconds(days * 86400L).minusMillis(1)).inRange(days, now))
            assertTrue(reading(now).inRange(days, now))
            assertFalse(reading(now.plusMillis(1)).inRange(days, now))
        }
    }

    @Test
    fun tokenExpirationIsStrictAndNeverPrinted() {
        val s = Session(1, "a@example.com", "Ana", "sensitive-token", now)
        assertTrue(s.expired(now))
        assertFalse(s.expired(now.minusMillis(1)))
        assertFalse(s.toString().contains("sensitive-token"))
    }

    @Test
    fun coordinatesRejectInvalidOrNonFiniteValues() {
        invalid("INVALID_COORDINATES") { Coordinates.of(91.0, 0.0) }
        invalid("INVALID_COORDINATES") { Coordinates.of(0.0, Double.NaN) }
        assertEquals(-77.0, Coordinates.of(-12.0, -77.0).longitude, 0.0)
    }

    @Test
    fun profileValidationLivesInDomainAndPreservesTerrainUnits() {
        val details = FarmDetails.of(" Ana ", " Farm ", "999", "Lima", 2.25)
        assertEquals("Ana", details.name.value)
        assertEquals(22500.0, details.area.value, 0.0)
        invalid("REQUIRED_FIELDS") { FarmDetails.of("Ana", "Farm", "999", "", 2.25) }
    }
}
