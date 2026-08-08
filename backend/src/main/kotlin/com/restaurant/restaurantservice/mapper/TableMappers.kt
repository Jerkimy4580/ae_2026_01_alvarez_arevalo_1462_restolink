package com.restaurant.restaurantservice.mapper

import com.restaurant.restaurantservice.dto.TableRequest
import com.restaurant.restaurantservice.dto.TableResponse
import com.restaurant.restaurantservice.entity.Restaurant
import com.restaurant.restaurantservice.entity.TableEntity

fun TableRequest.toEntity(restaurant: Restaurant): TableEntity = TableEntity(
    reference = reference,
    capacity = capacity,
    restaurant = restaurant
)

fun TableEntity.toResponse(): TableResponse = TableResponse(
    id = id,
    reference = reference,
    capacity = capacity,
    restaurantId = restaurant.id ?: -1
)
