package com.restaurant.restaurantservice.dto

import com.restaurant.restaurantservice.entity.OrderStatus
import java.math.BigDecimal
import java.time.Instant

data class OrderRequest(
    val restaurantId: Long,
    val tableId: Long? = null,
    val items: List<OrderItemRequest>,
    val force: Boolean = false
)

data class OrderItemRequest(
    val dishId: Long,
    val quantity: Int
)

data class OrderResponse(
    val id: Long?,
    val restaurantId: Long,
    val tableId: Long?,
    val clientUser: String?,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val createdAt: Instant,
    val items: List<OrderItemResponse>
)

data class OrderItemResponse(
    val dishId: Long,
    val dishName: String,
    val quantity: Int,
    val unitPrice: BigDecimal
)

data class UpdateOrderStatusRequest(
    val status: String
)
