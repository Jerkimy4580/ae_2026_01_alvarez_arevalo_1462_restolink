package com.restaurant.restaurantservice.service

import com.restaurant.restaurantservice.dto.RestaurantResponse
import com.restaurant.restaurantservice.entity.RestaurantWaiter
import com.restaurant.restaurantservice.exception.DuplicateResourceException
import com.restaurant.restaurantservice.exception.ResourceNotFoundException
import com.restaurant.restaurantservice.mapper.toResponse
import com.restaurant.restaurantservice.repository.RestaurantRepository
import com.restaurant.restaurantservice.repository.RestaurantWaiterRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface WaiterService {
    fun assignWaiterToRestaurant(restaurantId: Long, waiterUserId: String): RestaurantResponse
    fun getMyRestaurant(waiterUserId: String): RestaurantResponse
}

@Service
class WaiterServiceImpl(
    private val restaurantWaiterRepository: RestaurantWaiterRepository,
    private val restaurantRepository: RestaurantRepository
) : WaiterService {

    @Transactional
    override fun assignWaiterToRestaurant(restaurantId: Long, waiterUserId: String): RestaurantResponse {
        if (restaurantWaiterRepository.existsByWaiterUserId(waiterUserId)) {
            throw DuplicateResourceException("Waiter '$waiterUserId' is already assigned to a restaurant")
        }

        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { ResourceNotFoundException("Restaurant not found: $restaurantId") }

        restaurantWaiterRepository.save(
            RestaurantWaiter(
                waiterUserId = waiterUserId,
                restaurant = restaurant
            )
        )

        return restaurant.toResponse()
    }

    @Transactional(readOnly = true)
    override fun getMyRestaurant(waiterUserId: String): RestaurantResponse {
        val assignment = restaurantWaiterRepository.findByWaiterUserId(waiterUserId)
            .orElseThrow { ResourceNotFoundException("No restaurant assigned for waiter '$waiterUserId'") }
        return assignment.restaurant.toResponse()
    }
}
