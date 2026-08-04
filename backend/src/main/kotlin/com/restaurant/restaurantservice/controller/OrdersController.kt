package com.restaurant.restaurantservice.controller

import com.restaurant.restaurantservice.dto.OrderRequest
import com.restaurant.restaurantservice.dto.OrderResponse
import com.restaurant.restaurantservice.dto.UpdateOrderStatusRequest
import com.restaurant.restaurantservice.service.OrderService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/orders")
class OrdersController(
    private val orderService: OrderService
) {

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'WAITER')")
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrder(
        @PathVariable restaurantId: Long,
        @RequestBody request: OrderRequest,
        authentication: Authentication
    ): OrderResponse {
        val username = extractUsername(authentication)
        return orderService.createOrder(restaurantId, request, username)
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('WAITER', 'CHEF')")
    fun updateOrderStatus(
        @PathVariable restaurantId: Long,
        @PathVariable id: Long,
        @RequestBody request: UpdateOrderStatusRequest
    ): OrderResponse = orderService.updateOrderStatus(restaurantId, id, request)

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'WAITER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteOrder(
        @PathVariable restaurantId: Long,
        @PathVariable id: Long,
        authentication: Authentication
    ) {
        val username = extractUsername(authentication)
        val isWaiter = authentication.authorities.any { it.authority == "ROLE_WAITER" }
        orderService.deleteOrder(restaurantId, id, username, isWaiter)
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    fun getMyOrders(@PathVariable restaurantId: Long, authentication: Authentication): List<OrderResponse> {
        val username = extractUsername(authentication)
        return orderService.getOrdersForCurrentClient(restaurantId, username)
    }

    @GetMapping("/waiter")
    @PreAuthorize("hasRole('WAITER')")
    fun getWaiterOrders(@PathVariable restaurantId: Long): List<OrderResponse> = orderService.getOrdersForWaiter(restaurantId)

    @GetMapping("/kitchen")
    @PreAuthorize("hasRole('CHEF')")
    fun getKitchenOrders(@PathVariable restaurantId: Long): List<OrderResponse> = orderService.getKitchenOrders(restaurantId)

    private fun extractUsername(authentication: Authentication): String {
        val principal = authentication.principal
        return when (principal) {
            is Jwt -> (principal.claims["username"] as? String ?: principal.subject ?: authentication.name)
            else -> authentication.name
        }
    }
}