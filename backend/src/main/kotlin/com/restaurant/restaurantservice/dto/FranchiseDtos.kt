package com.restaurant.restaurantservice.dto

data class FranchiseRequest(
    val name: String
)

data class FranchiseResponse(
    val id: Long?,
    val name: String,
    val restaurantIds: List<Long>
)
