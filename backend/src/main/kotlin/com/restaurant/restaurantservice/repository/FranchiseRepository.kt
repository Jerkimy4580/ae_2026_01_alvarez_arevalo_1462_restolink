package com.restaurant.restaurantservice.repository

import com.restaurant.restaurantservice.entity.Franchise
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FranchiseRepository : JpaRepository<Franchise, Long> {
    fun existsByNameIgnoreCase(name: String): Boolean
}
