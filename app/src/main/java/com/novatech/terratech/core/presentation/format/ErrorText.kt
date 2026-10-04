package com.novatech.terratech.core.presentation.format

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.novatech.terratech.R
import com.novatech.terratech.ui.theme.*

@Composable
fun errorText(code: String): String =
  stringResource(
    when (code) {
      "INVALID_EMAIL" -> R.string.error_email
      "INVALID_PASSWORD" -> R.string.error_password
      "INVALID_NAME" -> R.string.error_name
      "PASSWORD_CONFIRMATION_MISMATCH" -> R.string.error_confirmation
      "EMAIL_EXISTS" -> R.string.error_duplicate
      "INVALID_CREDENTIALS" -> R.string.error_credentials
      "INVALID_SENSOR_CODE",
      "SENSOR_NOT_FOUND",
      "UNKNOWN_SENSOR" -> R.string.error_sensor
      "SENSOR_OCCUPIED" -> R.string.error_occupied
      "INVALID_AREA" -> R.string.error_area
      "INVALID_COORDINATES" -> R.string.error_coords
      "REQUIRED_FIELDS",
      "VALIDATION_ERROR",
      "INVALID_INPUT" -> R.string.error_required
      "UNAUTHENTICATED" -> R.string.session_expired
      "OFFLINE" -> R.string.offline
      "FIELD_NOT_FOUND",
      "DEVICE_NOT_FOUND",
      "READING_NOT_FOUND" -> R.string.error_missing
      else -> R.string.error_generic
    }
  )
