package com.yosebooster.racingrush.di

import com.yosebooster.racingrush.domain.repo.UserPreferencesRepository
import com.yosebooster.racingrush.domain.usecase.AddCoinsUseCase
import com.yosebooster.racingrush.domain.usecase.GetUserPreferencesUseCase
import com.yosebooster.racingrush.domain.usecase.PurchaseCarUseCase
import com.yosebooster.racingrush.domain.usecase.SaveHighscoreUseCase
import com.yosebooster.racingrush.domain.usecase.SelectCarUseCase
import com.yosebooster.racingrush.domain.usecase.UpdateSettingsUseCase
import com.yosebooster.racingrush.utils.SoundRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun providesGetUserPreferencesUseCase(
        repository: UserPreferencesRepository
    ): GetUserPreferencesUseCase {
        return GetUserPreferencesUseCase(repository)
    }

    @Provides
    @Singleton
    fun providesSaveHighscoreUseCase(
        repository: UserPreferencesRepository,
        soundRepository: SoundRepository,
    ): SaveHighscoreUseCase {
        return SaveHighscoreUseCase(repository, soundRepository)
    }

    @Provides
    @Singleton
    fun providesAddCoinsUseCase(
        repository: UserPreferencesRepository
    ): AddCoinsUseCase {
        return AddCoinsUseCase(repository)
    }

    @Provides
    @Singleton
    fun providesUpdateSettingsUseCase(
        repository: UserPreferencesRepository
    ): UpdateSettingsUseCase {
        return UpdateSettingsUseCase(repository)
    }

    @Provides
    @Singleton
    fun providesPurchaseCarUseCase(
        repository: UserPreferencesRepository
    ): PurchaseCarUseCase {
        return PurchaseCarUseCase(repository)
    }

    @Provides
    @Singleton
    fun providesSelectCarUseCase(
        repository: UserPreferencesRepository
    ): SelectCarUseCase {
        return SelectCarUseCase(repository)
    }
}
