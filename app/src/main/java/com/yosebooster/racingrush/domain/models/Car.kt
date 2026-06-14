package com.yosebooster.racingrush.domain.models

data class Car(
    val id: String,
    val name: String,
    val price: Int,
    val imageRes: Int,
    val speedMultiplier: Float = 1.0f
)

val availableCars = listOf(
    Car("car_1", "Speedy", 0, com.yosebooster.racingrush.R.drawable.ic_car),
    Car("car_2", "Night Rider", 500, com.yosebooster.racingrush.R.drawable.ic_car_night),
    Car("car_3", "Braker", 1000, com.yosebooster.racingrush.R.drawable.ic_car_brakes),
    Car("car_4", "Flash", 2500, com.yosebooster.racingrush.R.drawable.ic_car_lights),
    Car("car_5", "Interceptor", 5000, com.yosebooster.racingrush.R.drawable.ic_car) // Reuse for now
)
