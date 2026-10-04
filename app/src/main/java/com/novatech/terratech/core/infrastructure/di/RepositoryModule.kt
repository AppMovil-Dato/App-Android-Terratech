package com.novatech.terratech.core.infrastructure.di

import com.novatech.terratech.core.infrastructure.local.TerraDatabase
import com.novatech.terratech.iam.domain.repository.AccountRepository
import com.novatech.terratech.iam.infrastructure.implementation.AccountRepositoryImpl
import com.novatech.terratech.iam.infrastructure.local.SessionStore
import com.novatech.terratech.iam.infrastructure.remote.AccountApi
import com.novatech.terratech.monitoring.domain.repository.MonitoringRepository
import com.novatech.terratech.monitoring.infrastructure.implementation.MonitoringRepositoryImpl
import com.novatech.terratech.monitoring.infrastructure.remote.MonitoringApi
import com.novatech.terratech.profile.domain.repository.ProfileRepository
import com.novatech.terratech.profile.infrastructure.implementation.ProfileRepositoryImpl
import com.novatech.terratech.profile.infrastructure.remote.ProfileApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.*

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
  @Provides
  @Singleton
  fun accounts(
    api: AccountApi,
    store: SessionStore,
    db: TerraDatabase,
    scope: CoroutineScope,
  ): AccountRepository = AccountRepositoryImpl(api, store, db, scope)

  @Provides
  @Singleton
  fun profiles(api: ProfileApi, db: TerraDatabase): ProfileRepository =
    ProfileRepositoryImpl(api, db)

  @Provides
  @Singleton
  fun monitoring(api: MonitoringApi, db: TerraDatabase, store: SessionStore): MonitoringRepository =
    MonitoringRepositoryImpl(api, db, store)
}
