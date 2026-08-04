package com.restaurant.restaurantservice.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class DishResponse(
    val id: Long?,
    val name: String,
    val price: BigDecimal,
    @param:JsonProperty("isAvailable")
    @get:JsonProperty("isAvailable")
    val isAvailable: Boolean,
    val allergens: List<String> = emptyList()
)

data class DishRequest(
    val name: String,
    val price: BigDecimal,
    @param:JsonProperty("isAvailable")
    @get:JsonProperty("isAvailable")
    val isAvailable: Boolean = true,
    val allergens: List<String> = emptyList()
)

data class UpdateDishAvailabilityRequest(
    @param:JsonProperty("isAvailable")
    @get:JsonProperty("isAvailable")
    val isAvailable: Boolean = false // ✅ Se le agrega un valor por defecto para que Jackson pueda generar el constructor no-arg
)