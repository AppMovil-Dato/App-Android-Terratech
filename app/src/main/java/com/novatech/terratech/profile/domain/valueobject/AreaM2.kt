package com.novatech.terratech.profile.domain.valueobject

import com.novatech.terratech.core.domain.Failure

@JvmInline
value class AreaM2 private constructor(val value: Double) {
    val hectares: Double
        get() = value / 10000

    companion object {
        fun of(value: Double): AreaM2 {
            if (!value.isFinite() || value !in 0.01..999999999.0) throw Failure("INVALID_AREA")
            return AreaM2(value)
        }

        fun fromHectares(value: Double) = of(value * 10000)
    }
}
