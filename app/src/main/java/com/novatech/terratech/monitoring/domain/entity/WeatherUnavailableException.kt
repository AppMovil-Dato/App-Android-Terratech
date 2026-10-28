package com.novatech.terratech.monitoring.domain.entity

class WeatherUnavailableException(
    message: String = "Weather service temporarily unavailable",
) : Exception(message)
