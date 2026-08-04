package com.restaurant.restaurantservice.mapper

import com.restaurant.restaurantservice.dto.OrderResponse
import com.restaurant.restaurantservice.entity.Order

fun Order.toResponse(): OrderResponse = OrderResponse(
    id = id,
    restaurantId = restaurant.id ?: -1,
    tableId = table?.id,
    clientUser = clientUser,
    status = status,
    totalAmount = totalAmount,
    createdAt = createdAt,
    items = items.map { it.toResponse() }
)
