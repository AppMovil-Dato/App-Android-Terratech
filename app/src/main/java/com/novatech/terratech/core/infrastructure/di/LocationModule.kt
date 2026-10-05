package com.novatech.terratech.core.infrastructure.di

import com.novatech.terratech.monitoring.domain.repository.LocationSearch
import com.novatech.terratech.monitoring.infrastructure.location.GeocoderLocationSearch
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {
  @Binds abstract fun location(implementation: GeocoderLocationSearch): LocationSearch
}
