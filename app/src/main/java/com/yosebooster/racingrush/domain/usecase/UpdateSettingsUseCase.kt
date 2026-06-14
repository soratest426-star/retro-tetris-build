package com.yosebooster.racingrush.domain.usecase

import com.yosebooster.racingrush.domain.repo.UserPreferencesRepository

class UpdateSettingsUseCase(
    private val repository: UserPreferencesRepository
) {
    suspend fun updateMovementInput(input: String) = repository.updateMovementInput(input)
    suspend fun updateSoundEnabled(enabled: Boolean) = repository.updateSoundEnabled(enabled)
    suspend fun updateVibrationEnabled(enabled: Boolean) = repository.updateVibrationEnabled(enabled)
}
