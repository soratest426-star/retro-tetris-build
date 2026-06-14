package com.yosebooster.racingrush.domain.repo

import com.yosebooster.racingrush.domain.models.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun saveHighscore(score: Int)
    suspend fun addCoins(amount: Int)
    suspend fun unlockCar(carId: String)
    suspend fun selectCar(carId: String)
    suspend fun updateMovementInput(input: String)
    suspend fun updateSoundEnabled(enabled: Boolean)
    suspend fun updateVibrationEnabled(enabled: Boolean)
}
