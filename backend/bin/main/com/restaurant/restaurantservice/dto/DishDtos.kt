package com.restaurant.restaurantservice.dto

import java.math.BigDecimal

data class DishResponse(
    val id: Long?,
    val name: String,
    val price: BigDecimal,
    val isAvailable: Boolean
)

data class DishRequest(
    val name: String,
    val price: BigDecimal,
    val isAvailable: Boolean = true
)

data class UpdateDishAvailabilityRequest(
    val isAvailable: Boolean
)
