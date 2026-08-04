package com.restaurant.restaurantservice.controller

import com.restaurant.restaurantservice.service.CognitoAuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class CognitoAuthController(
    private val cognitoAuthService: CognitoAuthService
) {

    @PostMapping("/cognito/callback")
    fun handleCognitoCallback(@RequestBody payload: Map<String, String>): ResponseEntity<*> {
        val code = payload["code"] ?: return ResponseEntity.badRequest().body("Code is required")
        
        // CORRECCIÓN: Usar 'exchangeCodeForTokens' (plural)
        val tokens = cognitoAuthService.exchangeCodeForTokens(code)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to exchange code")

        return ResponseEntity.ok(tokens)
    }
}