package com.yosebooster.racingrush.domain.models

data class UserPreferences(
    val highscore: Int = 0,
    val coins: Int = 0,
    val unlockedCars: Set<String> = setOf("car_1"),
    val selectedCarId: String = "car_1",
    val movementInput: String = "SwipeGestures",
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)
