package com.restaurant.restaurantservice.mapper

import com.restaurant.restaurantservice.dto.DishRequest
import com.restaurant.restaurantservice.entity.Dish

fun DishRequest.toEntity(): Dish = Dish(
    name = name,
    price = price,
    isAvailable = isAvailable
)
