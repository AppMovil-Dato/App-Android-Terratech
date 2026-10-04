package com.novatech.terratech.core.presentation.format

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import com.novatech.terratech.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun timestamp(value: Instant) =
  DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm", Locale.getDefault())
    .withZone(ZoneId.systemDefault())
    .format(value)
