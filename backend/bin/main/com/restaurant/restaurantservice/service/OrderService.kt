package com.restaurant.restaurantservice.service

import com.restaurant.restaurantservice.dto.OrderRequest
import com.restaurant.restaurantservice.dto.OrderResponse
import com.restaurant.restaurantservice.dto.UpdateOrderStatusRequest
import com.restaurant.restaurantservice.entity.Order
import com.restaurant.restaurantservice.entity.OrderItem
import com.restaurant.restaurantservice.entity.OrderStatus
import com.restaurant.restaurantservice.exception.ForbiddenAccessException
import com.restaurant.restaurantservice.exception.InvalidOrderStatusException
import com.restaurant.restaurantservice.exception.ResourceNotFoundException
import com.restaurant.restaurantservice.mapper.toResponse
import com.restaurant.restaurantservice.repository.DishRepository
import com.restaurant.restaurantservice.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

interface OrderService {
    fun createOrder(request: OrderRequest, username: String?): OrderResponse
    fun updateOrderStatus(id: Long, request: UpdateOrderStatusRequest): OrderResponse
    fun getOrdersForCurrentClient(username: String): List<OrderResponse>
    fun getOrdersForWaiter(): List<OrderResponse>
    fun getKitchenOrders(): List<OrderResponse>
}

@Service
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val dishRepository: DishRepository
) : OrderService {

    @Transactional
    override fun createOrder(request: OrderRequest, username: String?): OrderResponse {
        val dishesById = dishRepository.findAllById(request.items.map { it.dishId }).associateBy { it.id }

        val order = Order(
            tableNumber = request.tableNumber,
            clientUser = username,
            status = OrderStatus.PENDING,
            totalAmount = BigDecimal.ZERO
        )

        val orderItems = request.items.map { item ->
            val dish = dishesById[item.dishId] ?: throw ResourceNotFoundException("Dish not found: ${item.dishId}")
            if (!dish.isAvailable) {
                throw IllegalArgumentException("Dish is not available: ${dish.name}")
            }
            OrderItem(order = order, dish = dish, quantity = item.quantity)
        }

        order.items.addAll(orderItems)
        order.totalAmount = orderItems.sumOf { it.dish.price.multiply(BigDecimal(it.quantity)) }

        return orderRepository.save(order).toResponse()
    }

    @Transactional
    override fun updateOrderStatus(id: Long, request: UpdateOrderStatusRequest): OrderResponse {
        val order = orderRepository.findById(id).orElseThrow { ResourceNotFoundException("Order not found: $id") }
        val normalizedStatus = runCatching { OrderStatus.fromInput(request.status) }
            .getOrElse { throw InvalidOrderStatusException(it.message ?: "Invalid order status") }
        order.status = normalizedStatus
        return orderRepository.save(order).toResponse()
    }

    @Transactional(readOnly = true)
    override fun getOrdersForCurrentClient(username: String): List<OrderResponse> {
        val orders = orderRepository.findByClientUser(username)
        return orders.map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    override fun getOrdersForWaiter(): List<OrderResponse> {
        return orderRepository.findAllByOrderByCreatedAtDesc().map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    override fun getKitchenOrders(): List<OrderResponse> {
        return orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.PENDING).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getOrderByIdForOwnershipCheck(id: Long): Order {
        return orderRepository.findById(id).orElseThrow { ResourceNotFoundException("Order not found: $id") }
    }

    fun validateOwnership(order: Order, username: String) {
        if (order.clientUser != username) {
            throw ForbiddenAccessException("You do not have access to this order")
        }
    }
}
