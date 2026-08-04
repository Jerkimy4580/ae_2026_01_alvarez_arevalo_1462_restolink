package com.restaurant.restaurantservice.repository

import com.restaurant.restaurantservice.entity.RestaurantWaiter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RestaurantWaiterRepository : JpaRepository<RestaurantWaiter, Long> {
    fun findByWaiterUserId(waiterUserId: String): Optional<RestaurantWaiter>
    fun existsByWaiterUserId(waiterUserId: String): Boolean
}
