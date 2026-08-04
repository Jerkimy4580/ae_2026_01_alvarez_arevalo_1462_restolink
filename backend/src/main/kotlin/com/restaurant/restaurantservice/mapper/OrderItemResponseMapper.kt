package com.restaurant.restaurantservice.mapper

import com.restaurant.restaurantservice.dto.OrderItemResponse
import com.restaurant.restaurantservice.entity.OrderItem

fun OrderItem.toResponse(): OrderItemResponse = OrderItemResponse(
    dishId = dishId,
    dishName = dishName,
    quantity = quantity,
    unitPrice = unitPrice
)
