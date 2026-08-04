package com.restaurant.restaurantservice.mapper

import com.restaurant.restaurantservice.dto.DishRequest
import com.restaurant.restaurantservice.entity.Allergen
import com.restaurant.restaurantservice.entity.Dish
import com.restaurant.restaurantservice.entity.Restaurant

fun DishRequest.toEntity(restaurant: Restaurant): Dish = Dish(
    name = name,
    price = price,
    isAvailable = isAvailable,
    allergens = allergens.map { rawValue ->
        Allergen.fromInput(rawValue) ?: throw IllegalArgumentException("Unsupported allergen: $rawValue")
    }.toMutableSet(),
    restaurant = restaurant
)
