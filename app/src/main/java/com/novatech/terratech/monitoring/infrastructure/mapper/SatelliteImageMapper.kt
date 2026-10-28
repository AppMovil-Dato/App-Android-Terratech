package com.novatech.terratech.monitoring.infrastructure.mapper

import com.novatech.terratech.monitoring.domain.entity.SatelliteBounds
import com.novatech.terratech.monitoring.domain.entity.SatelliteImage
import com.novatech.terratech.monitoring.infrastructure.local.entity.SatelliteImageRow
import com.novatech.terratech.monitoring.infrastructure.remote.dto.SatelliteBoundsDto
import com.novatech.terratech.monitoring.infrastructure.remote.dto.SatelliteImageDto

fun SatelliteImageDto.row(downloadedAt: Long): SatelliteImageRow =
    SatelliteImageRow(
        fieldId = fieldId,
        capturedAt = capturedAt,
        imageUrl = imageUrl,
        ndviAverage = ndviAverage,
        cloudCoveragePercent = cloudCoveragePercent,
        boundsNorth = bounds.north,
        boundsSouth = bounds.south,
        boundsEast = bounds.east,
        boundsWest = bounds.west,
        downloadedAt = downloadedAt,
    )

fun SatelliteImageDto.domain(downloadedAt: Long = 0L, isOffline: Boolean = false): SatelliteImage =
    SatelliteImage(
        fieldId = fieldId,
        capturedAt = capturedAt,
        imageUrl = imageUrl,
        ndviAverage = ndviAverage,
        cloudCoveragePercent = cloudCoveragePercent,
        bounds = bounds.domain(),
        downloadedAt = downloadedAt,
        isOffline = isOffline,
    )

fun SatelliteBoundsDto.domain(): SatelliteBounds =
    SatelliteBounds(
        north = north,
        south = south,
        east = east,
        west = west,
    )

fun SatelliteImageRow.domain(isOffline: Boolean = true): SatelliteImage =
    SatelliteImage(
        fieldId = fieldId,
        capturedAt = capturedAt,
        imageUrl = imageUrl,
        ndviAverage = ndviAverage,
        cloudCoveragePercent = cloudCoveragePercent,
        bounds =
            SatelliteBounds(
                north = boundsNorth,
                south = boundsSouth,
                east = boundsEast,
                west = boundsWest,
            ),
        downloadedAt = downloadedAt,
        isOffline = isOffline,
    )
