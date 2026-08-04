package com.restaurant.restaurantservice.dto

data class RestaurantRequest(
    val name: String,
    val address: String,
    val franchiseId: Long? = null
)

data class RestaurantResponse(
    val id: Long?,
    val name: String,
    val address: String,
    val franchiseId: Long?,
    val chefUserId: String?,
    val tableCount: Int,
    val dishCount: Int
)
