package com.novatech.terratech.monitoring.infrastructure.remote.dto

data class LatestDto(val reading: ReadingDto, val isStale: Boolean)
