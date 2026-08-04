package com.restaurant.restaurantservice.service

import com.restaurant.restaurantservice.dto.RestaurantRequest
import com.restaurant.restaurantservice.dto.RestaurantResponse
import com.restaurant.restaurantservice.entity.Restaurant
import com.restaurant.restaurantservice.exception.DuplicateResourceException
import com.restaurant.restaurantservice.exception.ResourceNotFoundException
import com.restaurant.restaurantservice.mapper.toEntity
import com.restaurant.restaurantservice.mapper.toResponse
import com.restaurant.restaurantservice.repository.FranchiseRepository
import com.restaurant.restaurantservice.repository.RestaurantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface RestaurantService {
    fun getAllRestaurants(): List<RestaurantResponse>
    fun createRestaurant(request: RestaurantRequest): RestaurantResponse
    fun createRestaurant(request: RestaurantRequest, chefUserId: String?): RestaurantResponse
    fun getRestaurantsByFranchise(franchiseId: Long): List<RestaurantResponse>
    fun getRestaurantById(id: Long): RestaurantResponse
}

@Service
class RestaurantServiceImpl(
    private val restaurantRepository: RestaurantRepository,
    private val franchiseRepository: FranchiseRepository
) : RestaurantService {

    @Transactional(readOnly = true)
    override fun getAllRestaurants(): List<RestaurantResponse> =
        restaurantRepository.findAll().map(Restaurant::toResponse)

    @Transactional
    override fun createRestaurant(request: RestaurantRequest): RestaurantResponse =
        createRestaurant(request, null)

    @Transactional
    override fun createRestaurant(request: RestaurantRequest, chefUserId: String?): RestaurantResponse {
        val normalizedAddress = request.address.trim()
        if (restaurantRepository.existsByAddressIgnoreCase(normalizedAddress)) {
            throw DuplicateResourceException("A restaurant with the address '$normalizedAddress' already exists")
        }

        val franchise = request.franchiseId?.let { id ->
            franchiseRepository.findById(id).orElseThrow { ResourceNotFoundException("Franchise not found: $id") }
        }
        val restaurant = request.toEntity(franchise).apply {
            this.chefUserId = chefUserId
        }
        return restaurantRepository.save(restaurant).toResponse()
    }

    @Transactional(readOnly = true)
    override fun getRestaurantsByFranchise(franchiseId: Long): List<RestaurantResponse> {
        return restaurantRepository.findByFranchiseId(franchiseId).map(Restaurant::toResponse)
    }

    @Transactional(readOnly = true)
    override fun getRestaurantById(id: Long): RestaurantResponse {
        val restaurant = restaurantRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Restaurant not found: $id") }
        return restaurant.toResponse()
    }
}
