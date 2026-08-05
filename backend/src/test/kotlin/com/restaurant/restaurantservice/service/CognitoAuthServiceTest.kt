package com.restaurant.restaurantservice.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.client.RestTemplate

class CognitoAuthServiceTest {

    @Test
    fun `exchangeCodeForTokens should return token map when response is successful`() {
        // Arrange
        val authService = CognitoAuthService("https://cognito.example.com", "client-id", "http://redirect")
        val mockRestTemplate = Mockito.mock(RestTemplate::class.java)
        ReflectionTestUtils.setField(authService, "restTemplate", mockRestTemplate)

        val responseBody = mapOf("access_token" to "token123", "expires_in" to 3600)
        Mockito.`when`(mockRestTemplate.postForEntity(Mockito.anyString(), Mockito.any(), Mockito.eq(Map::class.java)))
            .thenReturn(ResponseEntity(responseBody, HttpStatus.OK))

        // Act
        val result = authService.exchangeCodeForTokens("code123")

        // Assert
        assertNotNull(result)
        assertEquals("token123", result?.get("access_token"))
    }

    @Test
    fun `exchangeCodeForTokens should return null when response body cannot be cast to map`() {
        // Arrange
        val authService = CognitoAuthService("https://cognito.example.com", "client-id", "http://redirect")
        val mockRestTemplate = Mockito.mock(RestTemplate::class.java)
        ReflectionTestUtils.setField(authService, "restTemplate", mockRestTemplate)

        Mockito.`when`(mockRestTemplate.postForEntity(Mockito.anyString(), Mockito.any(), Mockito.eq(Map::class.java)))
            .thenReturn(ResponseEntity<Map<*, *>>(null, HttpStatus.OK))

        // Act
        val result = authService.exchangeCodeForTokens("code123")

        // Assert
        assertNull(result)
    }

    @Test
    fun `exchangeCodeForTokens should return null on exception`() {
        // Arrange
        val authService = CognitoAuthService("https://cognito.example.com", "client-id", "http://redirect")
        val mockRestTemplate = Mockito.mock(RestTemplate::class.java)
        ReflectionTestUtils.setField(authService, "restTemplate", mockRestTemplate)

        Mockito.`when`(mockRestTemplate.postForEntity(Mockito.anyString(), Mockito.any(), Mockito.eq(Map::class.java)))
            .thenThrow(RuntimeException("failure"))

        // Act
        val result = authService.exchangeCodeForTokens("code123")

        // Assert
        assertNull(result)
    }
}