package com.yosebooster.racingrush.domain.usecase

import com.yosebooster.racingrush.domain.repo.UserPreferencesRepository

class SelectCarUseCase(
    private val repository: UserPreferencesRepository
) {
    suspend fun execute(carId: String) {
        repository.selectCar(carId)
    }
}
