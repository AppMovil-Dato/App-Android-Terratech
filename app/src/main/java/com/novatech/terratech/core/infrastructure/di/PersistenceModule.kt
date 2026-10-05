package com.novatech.terratech.core.infrastructure.di

import android.content.Context
import androidx.room.Room
import com.novatech.terratech.core.infrastructure.local.TerraDatabase
import com.novatech.terratech.iam.infrastructure.local.SessionStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.*

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, TerraDatabase::class.java, "terratech.db")
            .addMigrations(com.novatech.terratech.core.infrastructure.local.FieldBoundaryMigration)
            .build()

    @Provides @Singleton fun store(@ApplicationContext context: Context) = SessionStore(context)

    @Provides @Singleton fun scope() = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
