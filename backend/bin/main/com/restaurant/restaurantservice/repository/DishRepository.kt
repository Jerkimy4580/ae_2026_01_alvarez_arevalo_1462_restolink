package com.restaurant.restaurantservice.repository

import com.restaurant.restaurantservice.entity.Dish
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DishRepository : JpaRepository<Dish, Long>
