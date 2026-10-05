package com.novatech.terratech.monitoring.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable data class ReadingDestination(val deviceId: Int, val readingId: Int)
