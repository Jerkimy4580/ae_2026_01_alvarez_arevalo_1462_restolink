package com.restaurant.restaurantservice.service

import com.restaurant.restaurantservice.dto.DishRequest
import com.restaurant.restaurantservice.dto.DishResponse
import com.restaurant.restaurantservice.dto.UpdateDishAvailabilityRequest
import com.restaurant.restaurantservice.entity.Dish
import com.restaurant.restaurantservice.exception.ResourceNotFoundException
import com.restaurant.restaurantservice.mapper.toEntity
import com.restaurant.restaurantservice.mapper.toResponse
import com.restaurant.restaurantservice.repository.DishRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface DishService {
    fun getAllDishes(): List<DishResponse>
    fun createDish(request: DishRequest): DishResponse
    fun updateAvailability(id: Long, request: UpdateDishAvailabilityRequest): DishResponse
    fun deleteDish(id: Long)
}

@Service
class DishServiceImpl(
    private val dishRepository: DishRepository
) : DishService {

    @Transactional(readOnly = true)
    override fun getAllDishes(): List<DishResponse> =
        dishRepository.findAll().map(Dish::toResponse)

    @Transactional
    override fun createDish(request: DishRequest): DishResponse {
        val dish = request.toEntity()
        return dishRepository.save(dish).toResponse()
    }

    @Transactional
    override fun updateAvailability(id: Long, request: UpdateDishAvailabilityRequest): DishResponse {
        val dish = dishRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Dish not found: $id") }
        dish.isAvailable = request.isAvailable
        return dishRepository.save(dish).toResponse()
    }

    @Transactional
    override fun deleteDish(id: Long) {
        if (!dishRepository.existsById(id)) {
            throw ResourceNotFoundException("Dish not found: $id")
        }
        dishRepository.deleteById(id)
    }
}
