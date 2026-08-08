package com.restaurant.restaurantservice.controller

import com.restaurant.restaurantservice.dto.AuthRequestDto
import com.restaurant.restaurantservice.service.CognitoAuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth/cognito")
class AuthController(
    private val cognitoAuthService: CognitoAuthService
) {

    @PostMapping("/callback")
    fun handleCallback(@RequestBody request: AuthRequestDto): ResponseEntity<Any> {
        val tokens = cognitoAuthService.exchangeCodeForTokens(
            code = request.code,
            codeVerifier = request.codeVerifier,
            customRedirectUri = request.redirectUri
        )

        return if (tokens != null) {
            ResponseEntity.ok(tokens)
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "No se pudo intercambiar el código por tokens en Cognito"))
        }
    }
}