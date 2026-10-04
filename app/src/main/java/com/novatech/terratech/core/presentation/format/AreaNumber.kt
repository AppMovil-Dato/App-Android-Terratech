package com.novatech.terratech.core.presentation.format

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import com.novatech.terratech.ui.theme.*

fun areaNumber(value: Double) = java.text.DecimalFormat("0.####").format(value)
