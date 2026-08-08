package com.restaurant.restaurantservice.controller

import com.restaurant.restaurantservice.dto.DishRequest
import com.restaurant.restaurantservice.dto.DishResponse
import com.restaurant.restaurantservice.dto.UpdateDishAvailabilityRequest
import com.restaurant.restaurantservice.entity.Allergen
import com.restaurant.restaurantservice.service.DishService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/dishes")
class DishesController(
    private val dishService: DishService
) {

    @GetMapping
    fun getDishes(@PathVariable restaurantId: Long): List<DishResponse> = dishService.getAllDishes(restaurantId)

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun createDish(@PathVariable restaurantId: Long, @RequestBody request: DishRequest): DishResponse =
        dishService.createDish(restaurantId, request)

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateDish(
        @PathVariable restaurantId: Long,
        @PathVariable id: Long,
        @RequestBody request: DishRequest
    ): DishResponse = dishService.updateDish(restaurantId, id, request)

    @PutMapping("/{id}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateAvailability(
        @PathVariable restaurantId: Long,
        @PathVariable id: Long,
        @RequestBody request: UpdateDishAvailabilityRequest
    ): DishResponse = dishService.updateAvailability(restaurantId, id, request)

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDish(@PathVariable restaurantId: Long, @PathVariable id: Long) {
        dishService.deleteDish(restaurantId, id)
    }

    @GetMapping("/allergens")
    fun getAllergens(): List<String> = Allergen.entries.map { it.name }
}