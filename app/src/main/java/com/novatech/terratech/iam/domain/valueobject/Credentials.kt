package com.novatech.terratech.iam.domain.valueobject

import com.novatech.terratech.core.domain.Failure

@JvmInline
value class Email private constructor(val value: String) {
  companion object {
    fun of(value: String): Email {
      val normalized = value.trim().lowercase(java.util.Locale.ROOT)
      if (normalized.length > 255 || !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(normalized))
        throw Failure("INVALID_EMAIL")
      return Email(normalized)
    }
  }
}

@JvmInline
value class Password private constructor(val value: String) {
  companion object {
    fun of(value: String): Password {
      if (value.length !in 6..128) throw Failure("INVALID_PASSWORD")
      return Password(value)
    }
  }
}
