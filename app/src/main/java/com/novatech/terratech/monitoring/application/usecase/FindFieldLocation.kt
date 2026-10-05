package com.novatech.terratech.monitoring.application.usecase

import com.novatech.terratech.monitoring.domain.repository.LocationSearch
import javax.inject.Inject

class FindFieldLocation @Inject constructor(private val search: LocationSearch) {
  suspend operator fun invoke(query: String) =
    if (query.trim().length in 2..200) search.find(query.trim()) else null
}
