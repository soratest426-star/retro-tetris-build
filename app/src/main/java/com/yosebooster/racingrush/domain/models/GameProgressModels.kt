package com.yosebooster.racingrush.domain.models

enum class PowerUpType {
    SHIELD, MULTIPLIER, TURBO
}

data class PowerUpState(
    val type: PowerUpType,
    val isActive: Boolean = false,
    val remainingTimeMillis: Long = 0
)

data class Mission(
    val id: String,
    val description: String,
    val targetValue: Int,
    val currentValue: Int = 0,
    val isCompleted: Boolean = false,
    val rewardCoins: Int
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val targetValue: Int,
    val currentValue: Int = 0,
    val isUnlocked: Boolean = false
)
