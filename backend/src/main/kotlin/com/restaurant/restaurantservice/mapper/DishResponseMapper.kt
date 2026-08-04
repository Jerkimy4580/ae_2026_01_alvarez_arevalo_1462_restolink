package com.restaurant.restaurantservice.mapper

import com.restaurant.restaurantservice.dto.DishResponse
import com.restaurant.restaurantservice.entity.Dish

fun Dish.toResponse(): DishResponse = DishResponse(
    id = id,
    name = name,
    price = price,
    isAvailable = isAvailable,
    allergens = allergens.map { it.name }
)
