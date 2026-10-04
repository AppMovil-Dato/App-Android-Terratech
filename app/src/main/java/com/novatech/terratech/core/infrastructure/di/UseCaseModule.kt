package com.novatech.terratech.core.infrastructure.di

import com.novatech.terratech.iam.application.usecase.AccountActions
import com.novatech.terratech.iam.domain.repository.AccountRepository
import com.novatech.terratech.monitoring.application.usecase.MonitoringActions
import com.novatech.terratech.monitoring.domain.repository.MonitoringRepository
import com.novatech.terratech.profile.application.usecase.ProfileActions
import com.novatech.terratech.profile.domain.repository.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
  @Provides fun accountActions(repository: AccountRepository) = AccountActions(repository)

  @Provides fun profileActions(repository: ProfileRepository) = ProfileActions(repository)

  @Provides fun monitoringActions(repository: MonitoringRepository) = MonitoringActions(repository)
}
