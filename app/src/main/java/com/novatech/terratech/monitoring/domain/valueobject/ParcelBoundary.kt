package com.novatech.terratech.monitoring.domain.valueobject

import com.novatech.terratech.core.domain.Failure
import kotlin.math.*

data class ParcelBoundary private constructor(val points: List<Coordinates>) {
  val areaM2: Double
    get() = area(points)

  val center: Coordinates
    get() =
      Coordinates.of(points.map { it.latitude }.average(), points.map { it.longitude }.average())

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
            (points[1].longitude - points[0].longitude) * (p.latitude - points[0].latitude) -
              (points[1].latitude - points[0].latitude) * (p.longitude - points[0].longitude)
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
        val a = points[i]
        val b = points[(i + 1) % points.size]
        var delta = Math.toRadians(b.longitude - a.longitude)
        if (delta > PI) delta -= 2 * PI
        if (delta < -PI) delta += 2 * PI
        sum += delta * (2 + sin(Math.toRadians(a.latitude)) + sin(Math.toRadians(b.latitude)))
      }
      return abs(sum * 6371009.0 * 6371009.0 / 2)
    }

    private fun intersects(
      a: Coordinates,
      b: Coordinates,
      c: Coordinates,
      d: Coordinates,
    ): Boolean {
      fun cross(p: Coordinates, q: Coordinates, r: Coordinates) =
        (q.longitude - p.longitude) * (r.latitude - p.latitude) -
          (q.latitude - p.latitude) * (r.longitude - p.longitude)
      fun on(p: Coordinates, q: Coordinates, r: Coordinates) =
        abs(cross(p, q, r)) < 1e-12 &&
          r.latitude in min(p.latitude, q.latitude)..max(p.latitude, q.latitude) &&
          r.longitude in min(p.longitude, q.longitude)..max(p.longitude, q.longitude)
      return cross(a, b, c) * cross(a, b, d) < 0 && cross(c, d, a) * cross(c, d, b) < 0 ||
        on(a, b, c) ||
        on(a, b, d) ||
        on(c, d, a) ||
        on(c, d, b)
    }
  }
}
