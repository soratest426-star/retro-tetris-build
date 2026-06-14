package com.yosebooster.racingrush.di

import android.content.Context
import com.yosebooster.racingrush.data.repo.UserPreferencesRepositoryImpl
import com.yosebooster.racingrush.data.source.highscoreDataStore
import com.yosebooster.racingrush.domain.repo.UserPreferencesRepository
import com.yosebooster.racingrush.utils.AdManager
import com.yosebooster.racingrush.utils.SoundRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun providesUserPreferencesRepository(
        @ApplicationContext context: Context
    ): UserPreferencesRepository {
        return UserPreferencesRepositoryImpl(context.highscoreDataStore)
    }

    @Provides
    @Singleton
    fun providesSoundManager(
        @ApplicationContext context: Context
    ): SoundRepository {
        return SoundRepository(context)
    }

    @Provides
    @Singleton
    fun providesAdManager(
        @ApplicationContext context: Context
    ): AdManager {
        return AdManager(context)
    }

}
