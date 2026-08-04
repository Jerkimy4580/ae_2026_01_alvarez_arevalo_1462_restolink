package com.restaurant.restaurantservice.controller

import com.restaurant.restaurantservice.dto.RestaurantRequest
import com.restaurant.restaurantservice.dto.RestaurantResponse
import com.restaurant.restaurantservice.service.RestaurantService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/restaurants", "/api/v1/restaurants"])
class RestaurantsController(
    private val restaurantService: RestaurantService
) {

    @GetMapping
    fun getRestaurants(): List<RestaurantResponse> = restaurantService.getAllRestaurants()

    @PostMapping
    @PreAuthorize("hasRole('CHEF')")
    @ResponseStatus(HttpStatus.CREATED)
    fun createRestaurant(
        @RequestBody request: RestaurantRequest,
        authentication: Authentication
    ): RestaurantResponse {
        val chefUserId = extractUserId(authentication)
        return restaurantService.createRestaurant(request, chefUserId)
    }

    @GetMapping("/franchise/{franchiseId}")
    fun getRestaurantsByFranchise(@PathVariable franchiseId: Long): List<RestaurantResponse> = restaurantService.getRestaurantsByFranchise(franchiseId)

    @GetMapping("/{id}")
    fun getRestaurant(@PathVariable id: Long): RestaurantResponse = restaurantService.getRestaurantById(id)

    private fun extractUserId(authentication: Authentication): String {
        val principal = authentication.principal
        return when (principal) {
            is Jwt -> (principal.claims["username"] as? String ?: principal.subject ?: authentication.name)
            else -> authentication.name
        }
    }
}
