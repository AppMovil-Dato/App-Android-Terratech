package com.novatech.terratech.monitoring.domain.valueobject

import com.novatech.terratech.core.domain.Failure

@JvmInline
value class SensorCode private constructor(val value: String) {
  companion object {
    fun of(value: String): SensorCode {
      val code = value.trim().uppercase(java.util.Locale.ROOT)
      if (!Regex("TT-[A-Z0-9]{6}").matches(code)) throw Failure("INVALID_SENSOR_CODE")
      return SensorCode(code)
    }
  }
}
