package com.restaurant.restaurantservice.dto

data class AuthResponseDto(
    val accessToken: String,
    val idToken: String,
    val refreshToken: String?,
    val user: UserProfileDto
)
