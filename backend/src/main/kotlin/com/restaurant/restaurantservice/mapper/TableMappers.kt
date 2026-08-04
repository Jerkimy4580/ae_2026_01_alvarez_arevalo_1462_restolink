package com.restaurant.restaurantservice.mapper

import com.restaurant.restaurantservice.dto.TableRequest
import com.restaurant.restaurantservice.dto.TableResponse
import com.restaurant.restaurantservice.entity.Restaurant
import com.restaurant.restaurantservice.entity.TableEntity

fun TableRequest.toEntity(restaurant: Restaurant): TableEntity = TableEntity(
    number = number,
    capacity = capacity,
    restaurant = restaurant
)

fun TableEntity.toResponse(): TableResponse = TableResponse(
    id = id,
    number = number,
    capacity = capacity,
    restaurantId = restaurant.id ?: -1
)
