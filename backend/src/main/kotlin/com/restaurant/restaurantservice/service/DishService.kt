package com.restaurant.restaurantservice.service

import com.restaurant.restaurantservice.dto.DishRequest
import com.restaurant.restaurantservice.dto.DishResponse
import com.restaurant.restaurantservice.dto.UpdateDishAvailabilityRequest
import com.restaurant.restaurantservice.entity.Allergen
import com.restaurant.restaurantservice.entity.Dish
import com.restaurant.restaurantservice.exception.ResourceNotFoundException
import com.restaurant.restaurantservice.mapper.toEntity
import com.restaurant.restaurantservice.mapper.toResponse
import com.restaurant.restaurantservice.repository.DishRepository
import com.restaurant.restaurantservice.repository.RestaurantRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

interface DishService {
    fun getAllDishes(restaurantId: Long): List<DishResponse>
    fun createDish(restaurantId: Long, request: DishRequest): DishResponse
    fun updateDish(restaurantId: Long, id: Long, request: DishRequest): DishResponse
    fun updateAvailability(restaurantId: Long, id: Long, request: UpdateDishAvailabilityRequest): DishResponse
    fun deleteDish(restaurantId: Long, id: Long)
}

@Service
class DishServiceImpl(
    private val dishRepository: DishRepository,
    private val restaurantRepository: RestaurantRepository
) : DishService {

    @Transactional(readOnly = true)
    override fun getAllDishes(restaurantId: Long): List<DishResponse> =
        dishRepository.findByRestaurantIdAndIsDeletedFalse(restaurantId).map(Dish::toResponse)

    @Transactional
    override fun createDish(restaurantId: Long, request: DishRequest): DishResponse {
        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { ResourceNotFoundException("Restaurant not found: $restaurantId") }
        validateDuplicateDish(restaurantId, request.name, request.price, null)
        val dish = request.toEntity(restaurant)
        return dishRepository.save(dish).toResponse()
    }

    @Transactional
    override fun updateDish(restaurantId: Long, id: Long, request: DishRequest): DishResponse {
        val dish = dishRepository.findByRestaurantIdAndIdAndIsDeletedFalse(restaurantId, id)
            .orElseThrow { ResourceNotFoundException("Dish not found: $id") }

        validateDuplicateDish(restaurantId, request.name, request.price, id)

        dish.name = request.name
        dish.price = request.price
        dish.isAvailable = request.isAvailable
        dish.allergens.clear()
        dish.allergens.addAll(request.allergens.map { rawValue ->
            Allergen.fromInput(rawValue) ?: throw IllegalArgumentException("Unsupported allergen: $rawValue")
        })

        return dishRepository.save(dish).toResponse()
    }

    @Transactional
    override fun updateAvailability(restaurantId: Long, id: Long, request: UpdateDishAvailabilityRequest): DishResponse {
        val dish = dishRepository.findByRestaurantIdAndIdAndIsDeletedFalse(restaurantId, id)
            .orElseThrow { ResourceNotFoundException("Dish not found: $id") }
        dish.isAvailable = request.isAvailable
        return dishRepository.save(dish).toResponse()
    }

    @Transactional
    override fun deleteDish(restaurantId: Long, id: Long) {
        val dish = dishRepository.findByRestaurantIdAndIdAndIsDeletedFalse(restaurantId, id)
            .orElseThrow { ResourceNotFoundException("Dish not found: $id") }
        dish.isDeleted = true
        dishRepository.save(dish)
    }

    private fun validateDuplicateDish(restaurantId: Long, name: String, price: BigDecimal, currentDishId: Long?) {
        val exists = if (currentDishId == null) {
            dishRepository.existsByRestaurantIdAndNameIgnoreCaseAndPriceAndIsDeletedFalse(restaurantId, name.trim(), price)
        } else {
            dishRepository.existsByRestaurantIdAndNameIgnoreCaseAndPriceAndIdNotAndIsDeletedFalse(restaurantId, name.trim(), price, currentDishId)
        }

        if (exists) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "A dish with the same name and price already exists in this restaurant"
            )
        }
    }
}
