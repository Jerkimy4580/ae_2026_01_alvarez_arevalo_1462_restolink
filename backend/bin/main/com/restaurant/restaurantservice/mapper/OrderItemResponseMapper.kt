package com.restaurant.restaurantservice.mapper

import com.restaurant.restaurantservice.dto.OrderItemResponse
import com.restaurant.restaurantservice.entity.OrderItem

fun OrderItem.toResponse(): OrderItemResponse = OrderItemResponse(
    id = id,
    dishId = dish.id!!,
    dishName = dish.name,
    quantity = quantity,
    unitPrice = dish.price
)
