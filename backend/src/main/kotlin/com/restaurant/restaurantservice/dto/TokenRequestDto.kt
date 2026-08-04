package com.restaurant.restaurantservice.dto

import jakarta.validation.constraints.NotBlank

data class TokenRequestDto(
    @field:NotBlank(message = "Authorization code is required")
    val code: String
)
