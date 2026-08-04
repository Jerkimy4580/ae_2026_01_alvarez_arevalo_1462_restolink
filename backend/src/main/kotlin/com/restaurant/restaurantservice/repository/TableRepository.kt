package com.restaurant.restaurantservice.repository

import com.restaurant.restaurantservice.entity.TableEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TableRepository : JpaRepository<TableEntity, Long> {
    fun findByRestaurantId(restaurantId: Long): List<TableEntity>
}
