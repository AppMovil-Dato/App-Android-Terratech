package com.novatech.terratech.monitoring.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.novatech.terratech.core.presentation.ui.*
import com.novatech.terratech.ui.theme.*

internal fun String.decimal() = replace(',', '.').toDoubleOrNull() ?: Double.NaN
