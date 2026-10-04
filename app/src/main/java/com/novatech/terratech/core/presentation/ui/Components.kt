package com.novatech.terratech.core.presentation.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.novatech.terratech.R
import com.novatech.terratech.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FarmCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
  Card(
    modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
  ) {
    Column(
      Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      content = content,
    )
  }
}

@Composable
fun Pill(text: String, warning: Boolean = false) {
  Surface(color = if (warning) AmberLight else LeafLight, shape = RoundedCornerShape(50.dp)) {
    Text(
      text,
      Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
      color = if (warning) Color(0xFF8F4B00) else FarmGreen,
      style = MaterialTheme.typography.labelMedium,
    )
  }
}

@Composable
fun PageTitle(title: String, subtitle: String? = null) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(title, style = MaterialTheme.typography.headlineMedium)
    subtitle?.let { Text(it, color = Muted) }
  }
}

@Composable
fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
  Button(
    onClick = onClick,
    enabled = enabled,
    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
    shape = RoundedCornerShape(14.dp),
  ) {
    Text(text)
  }
}

@Composable
fun EmptyCard(title: String, body: String? = null, action: (@Composable () -> Unit)? = null) {
  FarmCard {
    Text(title, style = MaterialTheme.typography.titleMedium)
    body?.let { Text(it, color = Muted) }
    action?.invoke()
  }
}

@Composable
fun Notice(error: String?, retry: (() -> Unit)? = null) {
  if (error == null) return
  val warning = error == "OFFLINE" || error == "UNAUTHENTICATED"
  Surface(
    color = if (warning) AmberLight else MaterialTheme.colorScheme.errorContainer,
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(errorText(error))
      retry?.let { TextButton(onClick = it) { Text(stringResource(R.string.retry)) } }
    }
  }
}

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

fun areaNumber(value: Double) = java.text.DecimalFormat("0.####").format(value)

fun number(value: Double) = String.format(Locale.getDefault(), "%.1f", value)

fun timestamp(value: Instant) =
  DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm", Locale.getDefault())
    .withZone(ZoneId.systemDefault())
    .format(value)
