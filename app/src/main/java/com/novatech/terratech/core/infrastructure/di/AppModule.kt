package com.novatech.terratech.core.infrastructure.di

import android.content.Context
import androidx.room.Room
import com.novatech.terratech.BuildConfig
import com.novatech.terratech.core.infrastructure.local.*
import com.novatech.terratech.core.infrastructure.remote.*
import com.novatech.terratech.iam.application.usecase.AccountActions
import com.novatech.terratech.iam.domain.repository.AccountRepository
import com.novatech.terratech.iam.infrastructure.implementation.AccountRepositoryImpl
import com.novatech.terratech.iam.infrastructure.local.SessionStore
import com.novatech.terratech.monitoring.application.usecase.MonitoringActions
import com.novatech.terratech.monitoring.domain.repository.MonitoringRepository
import com.novatech.terratech.monitoring.infrastructure.implementation.MonitoringRepositoryImpl
import com.novatech.terratech.profile.application.usecase.ProfileActions
import com.novatech.terratech.profile.domain.repository.ProfileRepository
import com.novatech.terratech.profile.infrastructure.implementation.ProfileRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
  @Provides
  @Singleton
  fun database(@ApplicationContext context: Context) =
    Room.databaseBuilder(context, TerraDatabase::class.java, "terratech.db").build()

  @Provides @Singleton fun store(@ApplicationContext context: Context) = SessionStore(context)

  @Provides @Singleton fun scope() = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  @Provides
  @Singleton
  fun api(account: Provider<AccountRepository>): TerraApi {
    val client =
      OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor { chain ->
          val request = chain.request()
          val session = account.get().session.value
          val auth = request.url.encodedPath.contains("/authentication/")
          if (!auth && session?.expired() == true) throw java.io.IOException("SESSION_EXPIRED")
          chain.proceed(
            request
              .newBuilder()
              .apply {
                if (!auth && session != null) header("Authorization", "Bearer ${session.token}")
              }
              .build()
          )
        }
        .build()
    return Retrofit.Builder()
      .baseUrl(BuildConfig.API_URL)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(TerraApi::class.java)
  }

  @Provides
  @Singleton
  fun accounts(
    api: TerraApi,
    store: SessionStore,
    db: TerraDatabase,
    scope: CoroutineScope,
  ): AccountRepository = AccountRepositoryImpl(api, store, db, scope)

  @Provides
  @Singleton
  fun profiles(api: TerraApi, db: TerraDatabase): ProfileRepository = ProfileRepositoryImpl(api, db)

  @Provides
  @Singleton
  fun monitoring(api: TerraApi, db: TerraDatabase, store: SessionStore): MonitoringRepository =
    MonitoringRepositoryImpl(api, db, store)

  @Provides fun accountActions(repository: AccountRepository) = AccountActions(repository)

  @Provides fun profileActions(repository: ProfileRepository) = ProfileActions(repository)

  @Provides fun monitoringActions(repository: MonitoringRepository) = MonitoringActions(repository)
}
