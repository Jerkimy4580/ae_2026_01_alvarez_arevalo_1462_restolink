package com.restaurant.restaurantservice.repository

import com.restaurant.restaurantservice.entity.Order
import com.restaurant.restaurantservice.entity.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findByClientUser(clientUser: String): List<Order>
    fun findAllByOrderByCreatedAtDesc(): List<Order>
    fun findByStatusOrderByCreatedAtAsc(status: OrderStatus): List<Order>
}
