package com.novatech.terratech.iam.domain.valueobject

import com.novatech.terratech.core.domain.Failure

@JvmInline
value class Password private constructor(val value: String) {
    companion object {
        fun of(value: String): Password {
            if (value.length !in 6..128) throw Failure("INVALID_PASSWORD")
            return Password(value)
        }
    }
}
