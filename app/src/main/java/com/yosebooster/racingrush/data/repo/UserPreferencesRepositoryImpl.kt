package com.yosebooster.racingrush.data.repo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.yosebooster.racingrush.domain.models.UserPreferences
import com.yosebooster.racingrush.domain.repo.UserPreferencesRepository
import com.yosebooster.racingrush.utils.SecurityUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    override fun getUserPreferences(): Flow<UserPreferences> {
        return dataStore.data.map { preferences ->
            val highscore = preferences[HIGHSCORE_KEY] ?: 0
            val coins = preferences[COINS_KEY] ?: 0
            val unlockedCars = preferences[UNLOCKED_CARS_KEY] ?: setOf("car_1")
            val storedSignature = preferences[SIGNATURE_KEY]

            // Integrity check / Anti-Fraud verification
            val isTampered = if (storedSignature == null) {
                // If there's no stored signature but the values are abnormally high, we treat it as fraud.
                // Otherwise, we allow it (e.g., first-run or migrating older data safely).
                highscore > 100000 || coins > 50000
            } else {
                val expectedSignature = SecurityUtils.generateDataSignature(highscore, coins, unlockedCars)
                storedSignature != expectedSignature
            }

            if (isTampered) {
                // Return default clean state to neutralize hacking of files
                UserPreferences(
                    highscore = 0,
                    coins = 0,
                    unlockedCars = setOf("car_1"),
                    selectedCarId = "car_1",
                    movementInput = preferences[MOVEMENT_INPUT_KEY] ?: "SwipeGestures",
                    soundEnabled = preferences[SOUND_ENABLED_KEY] ?: true,
                    vibrationEnabled = preferences[VIBRATION_ENABLED_KEY] ?: true
                )
            } else {
                UserPreferences(
                    highscore = highscore,
                    coins = coins,
                    unlockedCars = unlockedCars,
                    selectedCarId = preferences[SELECTED_CAR_KEY] ?: "car_1",
                    movementInput = preferences[MOVEMENT_INPUT_KEY] ?: "SwipeGestures",
                    soundEnabled = preferences[SOUND_ENABLED_KEY] ?: true,
                    vibrationEnabled = preferences[VIBRATION_ENABLED_KEY] ?: true
                )
            }
        }
    }

    override suspend fun saveHighscore(score: Int) {
        dataStore.edit { preferences ->
            val currentHighscore = preferences[HIGHSCORE_KEY] ?: 0
            val finalHighscore = if (score > currentHighscore) score else currentHighscore
            preferences[HIGHSCORE_KEY] = finalHighscore

            // Recalculate signature with updated highscore
            val coins = preferences[COINS_KEY] ?: 0
            val unlockedCars = preferences[UNLOCKED_CARS_KEY] ?: setOf("car_1")
            preferences[SIGNATURE_KEY] = SecurityUtils.generateDataSignature(finalHighscore, coins, unlockedCars)
        }
    }

    override suspend fun addCoins(amount: Int) {
        dataStore.edit { preferences ->
            val highscore = preferences[HIGHSCORE_KEY] ?: 0
            val currentCoins = preferences[COINS_KEY] ?: 0
            val finalCoins = currentCoins + amount
            preferences[COINS_KEY] = finalCoins

            // Recalculate signature with updated coins
            val unlockedCars = preferences[UNLOCKED_CARS_KEY] ?: setOf("car_1")
            preferences[SIGNATURE_KEY] = SecurityUtils.generateDataSignature(highscore, finalCoins, unlockedCars)
        }
    }

    override suspend fun unlockCar(carId: String) {
        dataStore.edit { preferences ->
            val highscore = preferences[HIGHSCORE_KEY] ?: 0
            val coins = preferences[COINS_KEY] ?: 0
            val currentCars = preferences[UNLOCKED_CARS_KEY] ?: setOf("car_1")
            val finalCars = currentCars + carId
            preferences[UNLOCKED_CARS_KEY] = finalCars

            // Recalculate signature with updated cars
            preferences[SIGNATURE_KEY] = SecurityUtils.generateDataSignature(highscore, coins, finalCars)
        }
    }

    override suspend fun selectCar(carId: String) {
        dataStore.edit { preferences ->
            preferences[SELECTED_CAR_KEY] = carId
        }
    }

    override suspend fun updateMovementInput(input: String) {
        dataStore.edit { preferences ->
            preferences[MOVEMENT_INPUT_KEY] = input
        }
    }

    override suspend fun updateSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SOUND_ENABLED_KEY] = enabled
        }
    }

    override suspend fun updateVibrationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED_KEY] = enabled
        }
    }

    companion object {
        private val HIGHSCORE_KEY = intPreferencesKey("highscore")
        private val COINS_KEY = intPreferencesKey("coins")
        private val UNLOCKED_CARS_KEY = stringSetPreferencesKey("unlocked_cars")
        private val SELECTED_CAR_KEY = stringPreferencesKey("selected_car")
        private val MOVEMENT_INPUT_KEY = stringPreferencesKey("movement_input")
        private val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
        private val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")
        private val SIGNATURE_KEY = stringPreferencesKey("data_signature")
    }
}
