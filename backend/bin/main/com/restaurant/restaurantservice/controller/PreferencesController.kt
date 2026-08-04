package com.restaurant.restaurantservice.controller

import com.restaurant.restaurantservice.dto.ClientPreferenceRequest
import com.restaurant.restaurantservice.dto.ClientPreferenceResponse
import com.restaurant.restaurantservice.service.PreferenceService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/preferences")
class PreferencesController(
    private val preferenceService: PreferenceService
) {

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    @ResponseStatus(HttpStatus.CREATED)
    fun createPreferences(
        @RequestBody request: ClientPreferenceRequest,
        authentication: Authentication
    ): ClientPreferenceResponse {
        val username = extractUsername(authentication)
        return preferenceService.createPreferences(username, request)
    }

    @PutMapping
    @PreAuthorize("hasRole('CLIENT')")
    fun updatePreferences(
        @RequestBody request: ClientPreferenceRequest,
        authentication: Authentication
    ): ClientPreferenceResponse {
        val username = extractUsername(authentication)
        return preferenceService.updatePreferences(username, request)
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    fun getMyPreferences(authentication: Authentication): ClientPreferenceResponse {
        val username = extractUsername(authentication)
        return preferenceService.getMyPreferences(username)
    }

    @GetMapping("/client/{username}")
    @PreAuthorize("hasAnyRole('CHEF', 'WAITER')")
    fun getClientPreferences(@PathVariable username: String): ClientPreferenceResponse {
        return preferenceService.getPreferencesForClient(username)
    }

    private fun extractUsername(authentication: Authentication): String {
        val principal = authentication.principal
        return when (principal) {
            is Jwt -> (principal.claims["username"] as? String ?: principal.subject ?: authentication.name)
            else -> authentication.name
        }
    }
}