package com.novatech.terratech.monitoring.presentation.ui

internal fun String.decimal() = replace(',', '.').toDoubleOrNull() ?: Double.NaN
