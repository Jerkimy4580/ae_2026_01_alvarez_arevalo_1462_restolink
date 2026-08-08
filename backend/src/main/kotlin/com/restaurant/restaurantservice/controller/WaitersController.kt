package com.restaurant.restaurantservice.controller

import com.restaurant.restaurantservice.dto.RestaurantResponse
import com.restaurant.restaurantservice.dto.WaiterAssignmentRequest
import com.restaurant.restaurantservice.service.WaiterService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/waiters", "/api/v1/waiters"])
class WaitersController(
    private val waiterService: WaiterService
) {

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun assignToRestaurant(
        @Valid @RequestBody request: WaiterAssignmentRequest
    ): RestaurantResponse {
        return waiterService.assignWaiterToRestaurant(request.restaurantId, request.waiterUserId)
    }

    @GetMapping("/me/restaurant")
    @PreAuthorize("hasRole('WAITER')")
    fun getMyRestaurant(authentication: Authentication): RestaurantResponse {
        val waiterUserId = extractUserId(authentication)
        return waiterService.getMyRestaurant(waiterUserId)
    }

    private fun extractUserId(authentication: Authentication): String {
        val principal = authentication.principal
        return when (principal) {
            is Jwt -> (principal.claims["username"] as? String ?: principal.subject ?: authentication.name)
            else -> authentication.name
        }
    }
}
