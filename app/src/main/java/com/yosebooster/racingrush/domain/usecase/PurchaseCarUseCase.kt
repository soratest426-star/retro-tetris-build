package com.yosebooster.racingrush.domain.usecase

import com.yosebooster.racingrush.domain.models.Car
import com.yosebooster.racingrush.domain.repo.UserPreferencesRepository
import kotlinx.coroutines.flow.first

class PurchaseCarUseCase(
    private val repository: UserPreferencesRepository
) {
    suspend fun execute(car: Car): Boolean {
        val prefs = repository.getUserPreferences().first()
        if (prefs.coins >= car.price && car.id !in prefs.unlockedCars) {
            repository.addCoins(-car.price)
            repository.unlockCar(car.id)
            return true
        }
        return false
    }
}
