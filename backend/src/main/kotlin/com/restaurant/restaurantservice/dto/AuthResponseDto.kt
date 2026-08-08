package com.restaurant.restaurantservice.dto

data class AuthRequestDto(
    val code: String,
    val codeVerifier: String? = null,
    val redirectUri: String? = null
)

data class AuthResponseDto(
    val accessToken: String,
    val idToken: String,
    val refreshToken: String?,
    val user: UserProfileDto? = null
)