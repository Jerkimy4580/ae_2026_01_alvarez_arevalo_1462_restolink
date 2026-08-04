package com.restaurant.restaurantservice.mapper

import com.restaurant.restaurantservice.dto.FranchiseRequest
import com.restaurant.restaurantservice.dto.FranchiseResponse
import com.restaurant.restaurantservice.entity.Franchise

fun FranchiseRequest.toEntity(): Franchise = Franchise(name = name)

fun Franchise.toResponse(): FranchiseResponse = FranchiseResponse(
    id = id,
    name = name,
    restaurantIds = restaurants.mapNotNull { it.id }
)
