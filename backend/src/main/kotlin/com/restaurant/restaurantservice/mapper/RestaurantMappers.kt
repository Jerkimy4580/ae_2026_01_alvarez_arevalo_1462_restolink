package com.restaurant.restaurantservice.mapper

import com.restaurant.restaurantservice.dto.RestaurantRequest
import com.restaurant.restaurantservice.dto.RestaurantResponse
import com.restaurant.restaurantservice.entity.Franchise
import com.restaurant.restaurantservice.entity.Restaurant

fun RestaurantRequest.toEntity(franchise: Franchise?): Restaurant = Restaurant(
    name = name,
    address = address,
    franchise = franchise
)

fun Restaurant.toResponse(): RestaurantResponse = RestaurantResponse(
    id = id,
    name = name,
    address = address,
    franchiseId = franchise?.id,
    chefUserId = chefUserId,
    tableCount = tables.size,
    dishCount = dishes.count { !it.isDeleted }
)
