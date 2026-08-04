package com.restaurant.restaurantservice.mapper

import com.restaurant.restaurantservice.dto.ClientPreferenceRequest
import com.restaurant.restaurantservice.dto.ClientPreferenceResponse
import com.restaurant.restaurantservice.entity.ClientPreference

fun ClientPreferenceRequest.toEntity(username: String): ClientPreference = ClientPreference(
    username = username,
    allergies = allergies.trim(),
    favoriteIngredients = favoriteIngredients.trim(),
    dislikedIngredients = dislikedIngredients.trim(),
    notes = notes.trim()
)

fun ClientPreference.applyRequest(request: ClientPreferenceRequest): ClientPreference {
    allergies = request.allergies.trim()
    favoriteIngredients = request.favoriteIngredients.trim()
    dislikedIngredients = request.dislikedIngredients.trim()
    notes = request.notes.trim()
    return this
}

fun ClientPreference.toResponse(): ClientPreferenceResponse = ClientPreferenceResponse(
    username = username,
    allergies = allergies,
    favoriteIngredients = favoriteIngredients,
    dislikedIngredients = dislikedIngredients,
    notes = notes,
    updatedAt = updatedAt.toString()
)