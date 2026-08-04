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
    @Value("\${cognito.domain:}") private val cognitoDomain: String,
    @Value("\${cognito.client-id:}") private val clientId: String,
    @Value("\${cognito.redirect-uri:}") private val redirectUri: String
) {
    private val restTemplate = RestTemplate()

    fun exchangeCodeForTokens(code: String): Map<String, Any>? {
        val url = "$cognitoDomain/oauth2/token"

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        val body = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", clientId)
            add("code", code)
            add("redirect_uri", redirectUri)
        }

        val request = HttpEntity(body, headers)
        
        return try {
            val response = restTemplate.postForEntity(url, request, Map::class.java)
            @Suppress("UNCHECKED_CAST")
            response.body as? Map<String, Any>
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}