package com.novatech.terratech.iam.domain.valueobject

import com.novatech.terratech.core.domain.Failure
import java.util.Locale

@JvmInline
value class Email private constructor(val value: String) {
    companion object {
        fun of(value: String): Email {
            val normalized = value.trim().lowercase(Locale.ROOT)
            if (
                normalized.length > 255 ||
                    !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(normalized)
            )
                throw Failure("INVALID_EMAIL")
            return Email(normalized)
        }
    }
}
