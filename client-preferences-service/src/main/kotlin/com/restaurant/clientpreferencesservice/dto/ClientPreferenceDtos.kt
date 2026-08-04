package com.restaurant.clientpreferencesservice.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ClientPreferenceRequest(
    val username: String? = null, // Opcional por si viene del Body o del Token
    val allergens: List<String> = emptyList()
)

data class ClientPreferenceResponse(
    val id: Long? = null,
    val username: String = "",
    val allergens: List<String> = emptyList()
)