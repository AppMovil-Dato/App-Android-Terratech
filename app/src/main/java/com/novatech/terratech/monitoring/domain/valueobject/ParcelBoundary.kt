package com.novatech.terratech.monitoring.domain.valueobject

import com.novatech.terratech.core.domain.Failure
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

data class ParcelBoundary private constructor(val points: List<Coordinates>) {
    val areaM2: Double
        get() = area(points)

    val center: Coordinates
        get() =
            Coordinates.of(
                points.map { it.latitude }.average(),
                points.map { it.longitude }.average(),
            )

    companion object {
        fun of(vertices: List<Coordinates>): ParcelBoundary {
            val points =
                if (vertices.size > 1 && vertices.first() == vertices.last()) vertices.dropLast(1)
                else vertices.toList()
            if (points.size !in 3..100 || points.distinct().size != points.size)
                throw Failure("INVALID_BOUNDARY")
            for (i in points.indices) for (j in i + 1 until points.size) {
                if (j == i + 1 || i == 0 && j == points.lastIndex) continue
                if (
                    intersects(
                        points[i],
                        points[(i + 1) % points.size],
                        points[j],
                        points[(j + 1) % points.size],
                    )
                )
                    throw Failure("INVALID_BOUNDARY")
            }
            if (
                points.drop(2).all { p ->
                    abs(
                        (points[1].longitude - points[0].longitude) *
                            (p.latitude - points[0].latitude) -
                            (points[1].latitude - points[0].latitude) *
                                (p.longitude - points[0].longitude)
                    ) < 1e-12
                }
            )
                throw Failure("INVALID_BOUNDARY")
            val area = area(points)
            if (!area.isFinite() || area !in 0.01..9999999.0) throw Failure("INVALID_BOUNDARY")
            return ParcelBoundary(points)
        }

        private fun area(points: List<Coordinates>): Double {
            var sum = 0.0
            for (i in points.indices) {
                val currentPoint = points[i]
                val nextPoint = points[(i + 1) % points.size]
                var delta = Math.toRadians(nextPoint.longitude - currentPoint.longitude)
                if (delta > PI) delta -= 2 * PI
                if (delta < -PI) delta += 2 * PI
                sum +=
                    delta *
                        (2 +
                            sin(Math.toRadians(currentPoint.latitude)) +
                            sin(Math.toRadians(nextPoint.latitude)))
            }
            return abs(sum * 6371009.0 * 6371009.0 / 2)
        }

        private fun intersects(
            firstStart: Coordinates,
            firstEnd: Coordinates,
            secondStart: Coordinates,
            secondEnd: Coordinates,
        ): Boolean {
            fun signedArea(segmentStart: Coordinates, segmentEnd: Coordinates, point: Coordinates) =
                (segmentEnd.longitude - segmentStart.longitude) *
                    (point.latitude - segmentStart.latitude) -
                    (segmentEnd.latitude - segmentStart.latitude) *
                        (point.longitude - segmentStart.longitude)
            fun isOnSegment(
                segmentStart: Coordinates,
                segmentEnd: Coordinates,
                point: Coordinates,
            ) =
                abs(signedArea(segmentStart, segmentEnd, point)) < 1e-12 &&
                    point.latitude in
                        min(segmentStart.latitude, segmentEnd.latitude)..max(
                                segmentStart.latitude,
                                segmentEnd.latitude,
                            ) &&
                    point.longitude in
                        min(segmentStart.longitude, segmentEnd.longitude)..max(
                                segmentStart.longitude,
                                segmentEnd.longitude,
                            )
            return signedArea(firstStart, firstEnd, secondStart) *
                signedArea(firstStart, firstEnd, secondEnd) < 0 &&
                signedArea(secondStart, secondEnd, firstStart) *
                    signedArea(secondStart, secondEnd, firstEnd) < 0 ||
                isOnSegment(firstStart, firstEnd, secondStart) ||
                isOnSegment(firstStart, firstEnd, secondEnd) ||
                isOnSegment(secondStart, secondEnd, firstStart) ||
                isOnSegment(secondStart, secondEnd, firstEnd)
        }
    }
}
