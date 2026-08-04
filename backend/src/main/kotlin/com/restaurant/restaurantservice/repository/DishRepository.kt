package com.restaurant.restaurantservice.repository

import com.restaurant.restaurantservice.entity.Dish
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.Optional

@Repository
interface DishRepository : JpaRepository<Dish, Long> {
    fun findByRestaurantIdAndIsDeletedFalse(restaurantId: Long): List<Dish>
    fun findByRestaurantIdAndIdAndIsDeletedFalse(restaurantId: Long, id: Long): Optional<Dish>
    fun existsByRestaurantIdAndNameIgnoreCaseAndPriceAndIsDeletedFalse(restaurantId: Long, name: String, price: BigDecimal): Boolean
    fun existsByRestaurantIdAndNameIgnoreCaseAndPriceAndIdNotAndIsDeletedFalse(restaurantId: Long, name: String, price: BigDecimal, id: Long?): Boolean
    fun findByRestaurantId(restaurantId: Long): List<Dish>
    fun findByRestaurantIdAndId(restaurantId: Long, id: Long): Optional<Dish>
    fun existsByRestaurantIdAndNameIgnoreCaseAndPrice(restaurantId: Long, name: String, price: BigDecimal): Boolean
    fun existsByRestaurantIdAndNameIgnoreCaseAndPriceAndIdNot(restaurantId: Long, name: String, price: BigDecimal, id: Long?): Boolean
}
