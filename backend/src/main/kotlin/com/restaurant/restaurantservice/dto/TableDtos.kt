package com.restaurant.restaurantservice.dto

data class TableRequest(
    val reference: String,
    val capacity: Int
)

data class TableResponse(
    val id: Long?,
    val reference: String,
    val capacity: Int,
    val restaurantId: Long
)
