package com.restaurant.restaurantservice.repository

import com.restaurant.restaurantservice.entity.Order
import com.restaurant.restaurantservice.entity.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findByRestaurantId(restaurantId: Long): List<Order>
    fun findByRestaurantIdAndClientUser(restaurantId: Long, clientUser: String): List<Order>
    fun findByRestaurantIdAndStatusOrderByCreatedAtAsc(restaurantId: Long, status: OrderStatus): List<Order>
    fun findByRestaurantIdAndStatusInOrderByCreatedAtAsc(restaurantId: Long, statuses: List<OrderStatus>): List<Order>
    fun findByRestaurantIdOrderByCreatedAtDesc(restaurantId: Long): List<Order>
    fun findByRestaurantIdAndId(restaurantId: Long, id: Long): Optional<Order>

    @Query("select o from Order o where o.clientUser = :clientUser and o.restaurant.id = :restaurantId and o.status not in :finishedStatuses order by o.createdAt desc")
    fun findLatestActiveOrderByClientUser(
        @Param("clientUser") clientUser: String,
        @Param("restaurantId") restaurantId: Long,
        @Param("finishedStatuses") finishedStatuses: List<OrderStatus>
    ): Optional<Order>
}
