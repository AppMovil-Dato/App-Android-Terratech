package com.novatech.terratech

import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.monitoring.domain.valueobject.Coordinates
import com.novatech.terratech.monitoring.domain.valueobject.FieldDraft
import com.novatech.terratech.monitoring.domain.valueobject.ParcelBoundary
import org.junit.Assert.*
import org.junit.Test

class ParcelBoundaryTest {
  private val rectangle =
    listOf(
      Coordinates.of(-14.0, -75.0),
      Coordinates.of(-14.0, -74.999),
      Coordinates.of(-14.001, -74.999),
      Coordinates.of(-14.001, -75.0),
    )

  @Test
  fun areaAndCenterUseGeographicCoordinates() {
    val polygon = ParcelBoundary.of(rectangle)
    assertEquals(12000.0, polygon.areaM2, 10.0)
    assertEquals(-14.0005, polygon.center.latitude, .000001)
    assertEquals(-74.9995, polygon.center.longitude, .000001)
  }

  @Test
  fun closingPointIsNormalizedAndReversingPreservesArea() {
    assertEquals(rectangle, ParcelBoundary.of(rectangle + rectangle.first()).points)
    assertEquals(
      ParcelBoundary.of(rectangle).areaM2,
      ParcelBoundary.of(rectangle.reversed()).areaM2,
      .000001,
    )
  }

  @Test
  fun rejectsCrossedDuplicateTooFewAndDegenerateBoundaries() {
    for (points in
      listOf(
        listOf(rectangle[0], rectangle[2], rectangle[1], rectangle[3]),
        rectangle.take(2),
        listOf(rectangle[0], rectangle[1], rectangle[0], rectangle[2]),
        listOf(Coordinates.of(1.0, 1.0), Coordinates.of(2.0, 2.0), Coordinates.of(3.0, 3.0)),
      )) {
      assertEquals(
        "INVALID_BOUNDARY",
        assertThrows(Failure::class.java) { ParcelBoundary.of(points) }.code,
      )
    }
  }

  @Test
  fun fieldDraftUsesBoundaryInsteadOfManualAreaAndPoint() {
    val draft = FieldDraft.of("North", "Potato", 50.0, "Loam", 0.0, 0.0, rectangle)
    assertEquals(ParcelBoundary.of(rectangle).areaM2, draft.area.value, .000001)
    assertEquals(ParcelBoundary.of(rectangle).center, draft.coordinates)
    assertEquals(rectangle, draft.boundary)
  }
}
