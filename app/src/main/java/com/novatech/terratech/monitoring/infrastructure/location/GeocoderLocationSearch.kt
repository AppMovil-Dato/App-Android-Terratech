package com.novatech.terratech.monitoring.infrastructure.location

import android.content.Context
import android.location.Geocoder
import com.novatech.terratech.monitoring.domain.repository.LocationSearch
import com.novatech.terratech.monitoring.domain.valueobject.Coordinates
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeocoderLocationSearch @Inject constructor(@ApplicationContext private val context: Context) :
  LocationSearch {
  @Suppress("DEPRECATION")
  override suspend fun find(query: String): Coordinates? =
    withContext(Dispatchers.IO) {
      if (!Geocoder.isPresent()) return@withContext null
      Geocoder(context, Locale.getDefault()).getFromLocationName(query, 1)?.firstOrNull()?.let {
        Coordinates.of(it.latitude, it.longitude)
      }
    }
}
