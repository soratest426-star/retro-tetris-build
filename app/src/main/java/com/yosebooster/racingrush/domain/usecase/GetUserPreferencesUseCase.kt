package com.yosebooster.racingrush.domain.usecase

import com.yosebooster.racingrush.domain.models.UserPreferences
import com.yosebooster.racingrush.domain.repo.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetUserPreferencesUseCase(
    private val repository: UserPreferencesRepository
) {
    fun execute(): Flow<UserPreferences> = repository.getUserPreferences()
}
