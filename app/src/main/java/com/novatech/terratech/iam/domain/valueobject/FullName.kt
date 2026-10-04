package com.novatech.terratech.iam.domain.valueobject

import com.novatech.terratech.core.domain.Failure

@JvmInline
value class FullName private constructor(val value: String) {
  companion object {
    fun of(value: String): FullName {
      val name = value.trim()
      if (name.length !in 2..150) throw Failure("INVALID_NAME")
      return FullName(name)
    }
  }
}
