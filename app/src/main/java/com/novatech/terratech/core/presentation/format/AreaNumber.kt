package com.novatech.terratech.core.presentation.format

import java.text.DecimalFormat

fun areaNumber(value: Double) = DecimalFormat("0.####").format(value)
