package com.novatech.terratech.monitoring.domain.valueobject

import com.novatech.terratech.core.domain.Failure

@JvmInline
value class FieldName private constructor(val value: String) {
  companion object {
    fun of(value: String): FieldName {
      val name = value.trim()
      if (name.length !in 1..100) throw Failure("REQUIRED_FIELDS")
      return FieldName(name)
    }
  }
}
