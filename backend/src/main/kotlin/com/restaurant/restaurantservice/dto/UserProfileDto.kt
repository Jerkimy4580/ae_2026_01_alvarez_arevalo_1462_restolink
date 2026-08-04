package com.restaurant.restaurantservice.dto

data class UserProfileDto(
    val sub: String,
    val email: String,
    val name: String,
    val groups: List<String>
)
