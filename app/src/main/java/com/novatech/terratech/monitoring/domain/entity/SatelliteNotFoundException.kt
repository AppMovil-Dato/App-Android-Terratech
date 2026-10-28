package com.novatech.terratech.monitoring.domain.entity

class SatelliteNotFoundException(
    message: String = "No satellite image available for the requested date",
) : Exception(message)
