package com.restaurant.restaurantservice.dto

data class ClientPreferenceRequest(
    val allergies: String = "",
    val favoriteIngredients: String = "",
    val dislikedIngredients: String = "",
    val notes: String = ""
)

data class ClientPreferenceResponse(
    val username: String,
    val allergies: String,
    val favoriteIngredients: String,
    val dislikedIngredients: String,
    val notes: String,
    val updatedAt: String
)