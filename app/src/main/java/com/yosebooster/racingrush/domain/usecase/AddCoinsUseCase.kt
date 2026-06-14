package com.yosebooster.racingrush.domain.usecase

import com.yosebooster.racingrush.domain.repo.UserPreferencesRepository

class AddCoinsUseCase(
    private val repository: UserPreferencesRepository
) {
    suspend fun execute(amount: Int) {
        if (amount > 0) {
            repository.addCoins(amount)
        }
    }
}
