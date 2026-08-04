package com.restaurant.restaurantservice.repository

import com.restaurant.restaurantservice.entity.Restaurant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RestaurantRepository : JpaRepository<Restaurant, Long> {
    fun findByFranchiseId(franchiseId: Long): List<Restaurant>
    fun existsByAddressIgnoreCase(address: String): Boolean
}
