package com.novatech.terratech.core.infrastructure.di

import com.novatech.terratech.BuildConfig
import com.novatech.terratech.core.infrastructure.remote.TerraApi
import com.novatech.terratech.iam.domain.repository.AccountRepository
import com.novatech.terratech.iam.infrastructure.remote.AccountApi
import com.novatech.terratech.iam.infrastructure.remote.SessionAuthorizationInterceptor
import com.novatech.terratech.monitoring.infrastructure.remote.MonitoringApi
import com.novatech.terratech.profile.infrastructure.remote.ProfileApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Provider
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
  @Provides
  @Singleton
  fun api(account: Provider<AccountRepository>): TerraApi {
    val client =
      OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(SessionAuthorizationInterceptor(account))
        .build()
    return Retrofit.Builder()
      .baseUrl(BuildConfig.API_URL)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(TerraApi::class.java)
  }

  @Provides fun accountApi(api: TerraApi): AccountApi = api

  @Provides fun profileApi(api: TerraApi): ProfileApi = api

  @Provides fun monitoringApi(api: TerraApi): MonitoringApi = api
}
