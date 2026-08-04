package com.restaurant.restaurantservice.dto

data class TableRequest(
    val number: Int,
    val capacity: Int
)

data class TableResponse(
    val id: Long?,
    val number: Int,
    val capacity: Int,
    val restaurantId: Long
)
