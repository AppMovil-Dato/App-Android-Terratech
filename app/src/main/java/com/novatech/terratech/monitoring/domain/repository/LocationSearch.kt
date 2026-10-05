package com.novatech.terratech.monitoring.domain.repository

import com.novatech.terratech.monitoring.domain.valueobject.Coordinates

interface LocationSearch {
  suspend fun find(query: String): Coordinates?
}
