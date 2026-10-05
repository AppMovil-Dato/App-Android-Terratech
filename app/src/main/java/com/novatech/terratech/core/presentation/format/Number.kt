package com.novatech.terratech.core.presentation.format

import java.util.Locale

fun number(value: Double) = String.format(Locale.getDefault(), "%.1f", value)
