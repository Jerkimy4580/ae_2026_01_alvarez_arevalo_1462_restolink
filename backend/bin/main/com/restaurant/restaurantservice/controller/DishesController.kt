package com.restaurant.restaurantservice.controller

import com.restaurant.restaurantservice.dto.DishRequest
import com.restaurant.restaurantservice.dto.DishResponse
import com.restaurant.restaurantservice.dto.UpdateDishAvailabilityRequest
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
@RequestMapping("/api/v1/dishes")
class DishesController(
    private val dishService: DishService
) {

    @GetMapping
    fun getDishes(): List<DishResponse> = dishService.getAllDishes()

    @PostMapping
    @PreAuthorize("hasRole('CHEF')")
    @ResponseStatus(HttpStatus.CREATED)
    fun createDish(@RequestBody request: DishRequest): DishResponse = dishService.createDish(request)

    @PutMapping("/{id}/availability")
    @PreAuthorize("hasRole('CHEF')")
    fun updateAvailability(
        @PathVariable id: Long,
        @RequestBody request: UpdateDishAvailabilityRequest
    ): DishResponse = dishService.updateAvailability(id, request)

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CHEF')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDish(@PathVariable id: Long) {
        dishService.deleteDish(id)
    }
}