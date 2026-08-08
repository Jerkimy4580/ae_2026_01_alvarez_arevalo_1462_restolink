package com.restaurant.clientpreferencesservice.controller

import com.restaurant.clientpreferencesservice.dto.ClientPreferenceRequest
import com.restaurant.clientpreferencesservice.dto.ClientPreferenceResponse
import com.restaurant.clientpreferencesservice.service.ClientPreferenceService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/preferences")
class ClientPreferenceController(
    private val clientPreferenceService: ClientPreferenceService
) {

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    @ResponseStatus(HttpStatus.CREATED)
    fun savePreferences(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: ClientPreferenceRequest
    ): ClientPreferenceResponse {
        val username = request.username 
            ?: jwt.getClaimAsString("username") 
            ?: jwt.getClaimAsString("cognito:username") 
            ?: jwt.subject
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Username could not be determined from token")

        return clientPreferenceService.saveOrUpdatePreferences(username, request)
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN') or hasRole('WAITER')")
    fun getPreferences(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable username: String
    ): ClientPreferenceResponse {
        val authenticatedUsername = jwt.getClaimAsString("username")
            ?: jwt.getClaimAsString("cognito:username")
            ?: jwt.subject
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User could not be determined from token")

        // Safe-call (?.) y lista vacía por defecto si authentication es null
        val authorities = SecurityContextHolder.getContext().authentication?.authorities ?: emptyList()
        val isClient = authorities.any { it.authority == "ROLE_CLIENT" }
        val isOwner = authenticatedUsername == username

        // Si es ROL_CLIENTE y no es el dueño de la cuenta, denegar acceso
        if (isClient && !isOwner) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own preferences")
        }

        return clientPreferenceService.getPreferencesByUsername(username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Preferences not found for user: $username")
    }
}