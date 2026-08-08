package com.restaurant.restaurantservice.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class CognitoAuthService(
    @Value("\${cognito.domain-base:\${cognito.domain:}}") private val cognitoDomain: String,
    @Value("\${cognito.app-client-id:\${cognito.client-id:}}") private val clientId: String,
    @Value("\${cognito.app-client-secret:}") private val clientSecret: String,
    @Value("\${cognito.redirect-uri:}") private val defaultRedirectUri: String
) {
    private val restTemplate = RestTemplate()

    fun exchangeCodeForTokens(code: String, codeVerifier: String? = null, customRedirectUri: String? = null): Map<String, Any>? {
        val url = "$cognitoDomain/oauth2/token"
        val effectiveRedirectUri = customRedirectUri?.takeIf { it.isNotBlank() } ?: defaultRedirectUri

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        // If an app client secret is configured, include HTTP Basic auth
        if (!clientSecret.isNullOrBlank()) {
            headers.setBasicAuth(clientId, clientSecret)
        }

        val body = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", clientId)
            add("code", code)
            add("redirect_uri", effectiveRedirectUri)
            
            // Requisito clave para resolver el error 401 por PKCE
            if (!codeVerifier.isNullOrBlank()) {
                add("code_verifier", codeVerifier)
            }
        }

        val request = HttpEntity(body, headers)
        
        return try {
            val response = restTemplate.postForEntity(url, request, Map::class.java)
            @Suppress("UNCHECKED_CAST")
            response.body as? Map<String, Any>
        } catch (e: Exception) {
            println("Error al intercambiar tokens con Cognito: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}