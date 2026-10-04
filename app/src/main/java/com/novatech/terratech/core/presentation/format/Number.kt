package com.novatech.terratech.core.presentation.format

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import com.novatech.terratech.ui.theme.*
import java.util.Locale

fun number(value: Double) = String.format(Locale.getDefault(), "%.1f", value)
