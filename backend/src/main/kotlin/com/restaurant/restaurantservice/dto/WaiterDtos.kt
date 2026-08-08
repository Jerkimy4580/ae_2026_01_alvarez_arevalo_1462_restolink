package com.restaurant.restaurantservice.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class WaiterAssignmentRequest(
    @field:NotNull
    @field:Positive
    val restaurantId: Long,

    @field:NotBlank
    val waiterUserId: String
)
