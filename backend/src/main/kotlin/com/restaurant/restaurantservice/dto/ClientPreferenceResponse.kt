package com.restaurant.restaurantservice.dto

data class ClientPreferenceResponse(
    val username: String,
    val allergens: List<String>
)
