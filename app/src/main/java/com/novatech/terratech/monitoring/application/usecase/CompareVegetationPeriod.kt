package com.novatech.terratech.monitoring.application.usecase

import com.novatech.terratech.monitoring.domain.entity.SatelliteImage
import com.novatech.terratech.monitoring.domain.entity.VegetationComparison

class CompareVegetationPeriod {
    operator fun invoke(
        imageA: SatelliteImage,
        imageB: SatelliteImage,
    ): VegetationComparison {
        val (earlier, later) =
            if (imageA.capturedAt <= imageB.capturedAt) {
                imageA to imageB
            } else {
                imageB to imageA
            }
        val delta = later.ndviAverage - earlier.ndviAverage
        val percentageChange =
            if (earlier.ndviAverage > 0.0) {
                (delta / earlier.ndviAverage) * 100.0
            } else {
                0.0
            }
        return VegetationComparison(
            earlier = earlier,
            later = later,
            ndviDelta = delta,
            percentageChange = percentageChange,
        )
    }
}
