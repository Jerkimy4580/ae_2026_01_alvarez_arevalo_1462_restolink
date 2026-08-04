package com.restaurant.restaurantservice.dto

data class CognitoTokenResponseDto(
    val access_token: String,
    val id_token: String,
    val refresh_token: String?,
    val expires_in: Long,
    val token_type: String
)
