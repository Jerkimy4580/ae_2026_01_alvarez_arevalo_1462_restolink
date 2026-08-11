package com.restaurant.restaurantservice.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

class CognitoAuthServiceTest {

    @Test
    fun `exchangeCodeForTokens should return token map and set basic auth when clientSecret is present`() {
        // Arrange
        val authService = CognitoAuthService(
            "https://cognito.example.com",
            "client-id",
            "client-secret",
            "http://redirect"
        )
        val mockRestTemplate = Mockito.mock(RestTemplate::class.java)
        ReflectionTestUtils.setField(authService, "restTemplate", mockRestTemplate)

        val responseBody: Map<String, Any> = mapOf("access_token" to "token123", "expires_in" to 3600)
        val captor = ArgumentCaptor.forClass(HttpEntity::class.java)

        Mockito.`when`(
            mockRestTemplate.postForEntity(
                any(String::class.java) ?: "",
                captor.capture(),
                eq(Map::class.java)
            )
        ).thenReturn(ResponseEntity(responseBody, HttpStatus.OK))

        // Act
        val result = authService.exchangeCodeForTokens("code123")

        // Assert
        assertNotNull(result)
        assertEquals("token123", result?.get("access_token"))

        val capturedRequest = captor.value
        assertNotNull(capturedRequest.headers.getFirst("Authorization"))
        assertTrue(capturedRequest.headers.getFirst("Authorization")!!.startsWith("Basic "))
    }

    @Test
    fun `exchangeCodeForTokens should fallback to defaultRedirectUri when customRedirectUri is blank`() {
        // Arrange
        val authService = CognitoAuthService(
            "https://cognito.example.com",
            "client-id",
            "client-secret",
            "http://redirect"
        )
        val mockRestTemplate = Mockito.mock(RestTemplate::class.java)
        ReflectionTestUtils.setField(authService, "restTemplate", mockRestTemplate)

        val captor = ArgumentCaptor.forClass(HttpEntity::class.java)

        Mockito.`when`(
            mockRestTemplate.postForEntity(
                any(String::class.java) ?: "",
                captor.capture(),
                eq(Map::class.java)
            )
        ).thenReturn(ResponseEntity(mapOf("access_token" to "token_blank"), HttpStatus.OK))

        // Act
        val result = authService.exchangeCodeForTokens("code123", customRedirectUri = "   ")

        // Assert
        assertNotNull(result)
        val capturedRequest = captor.value
        @Suppress("UNCHECKED_CAST")
        val body = capturedRequest.body as MultiValueMap<String, String>
        assertEquals("http://redirect", body.getFirst("redirect_uri"))
    }

    @Test
    fun `exchangeCodeForTokens with codeVerifier, customRedirectUri and empty clientSecret`() {
        // Arrange
        val authService = CognitoAuthService(
            "https://cognito.example.com",
            "client-id",
            "", // Cubre la rama de clientSecret como cadena vacía ""
            "http://redirect"
        )
        val mockRestTemplate = Mockito.mock(RestTemplate::class.java)
        ReflectionTestUtils.setField(authService, "restTemplate", mockRestTemplate)

        val responseBody: Map<String, Any> = mapOf("access_token" to "token456")
        val captor = ArgumentCaptor.forClass(HttpEntity::class.java)

        Mockito.`when`(
            mockRestTemplate.postForEntity(
                any(String::class.java) ?: "",
                captor.capture(),
                eq(Map::class.java)
            )
        ).thenReturn(ResponseEntity(responseBody, HttpStatus.OK))

        // Act
        val result = authService.exchangeCodeForTokens(
            code = "code123",
            codeVerifier = "verifier123",
            customRedirectUri = "http://custom-redirect"
        )

        // Assert
        assertNotNull(result)
        assertEquals("token456", result?.get("access_token"))

        val capturedRequest = captor.value
        @Suppress("UNCHECKED_CAST")
        val body = capturedRequest.body as MultiValueMap<String, String>
        assertEquals("verifier123", body.getFirst("code_verifier"))
        assertEquals("http://custom-redirect", body.getFirst("redirect_uri"))
        assertNull(capturedRequest.headers.getFirst("Authorization"))
    }

    @Test
    fun `exchangeCodeForTokens when clientSecret contains only spaces or is null`() {
        // 1. Probar clientSecret con espacios en blanco "   "
        val authServiceSpaces = CognitoAuthService(
            "https://cognito.example.com",
            "client-id",
            "   ",
            "http://redirect"
        )
        val mockRestTemplate1 = Mockito.mock(RestTemplate::class.java)
        ReflectionTestUtils.setField(authServiceSpaces, "restTemplate", mockRestTemplate1)

        val captor1 = ArgumentCaptor.forClass(HttpEntity::class.java)
        Mockito.`when`(
            mockRestTemplate1.postForEntity(
                any(String::class.java) ?: "",
                captor1.capture(),
                eq(Map::class.java)
            )
        ).thenReturn(ResponseEntity(mapOf("access_token" to "token_spaces"), HttpStatus.OK))

        val result1 = authServiceSpaces.exchangeCodeForTokens("code123")
        assertNotNull(result1)
        assertNull(captor1.value.headers.getFirst("Authorization"))

        // 2. Probar clientSecret nulo (cubriendo la rama null de isNullOrBlank)
        val authServiceNull = CognitoAuthService(
            "https://cognito.example.com",
            "client-id",
            "dummy",
            "http://redirect"
        )
        ReflectionTestUtils.setField(authServiceNull, "clientSecret", null)
        val mockRestTemplate2 = Mockito.mock(RestTemplate::class.java)
        ReflectionTestUtils.setField(authServiceNull, "restTemplate", mockRestTemplate2)

        val captor2 = ArgumentCaptor.forClass(HttpEntity::class.java)
        Mockito.`when`(
            mockRestTemplate2.postForEntity(
                any(String::class.java) ?: "",
                captor2.capture(),
                eq(Map::class.java)
            )
        ).thenReturn(ResponseEntity(mapOf("access_token" to "token_null"), HttpStatus.OK))

        val result2 = authServiceNull.exchangeCodeForTokens("code123")
        assertNotNull(result2)
        assertNull(captor2.value.headers.getFirst("Authorization"))
    }

    @Test
    fun `exchangeCodeForTokens should return null when response body cannot be cast to map`() {
        // Arrange
        val authService = CognitoAuthService(
            "https://cognito.example.com",
            "client-id",
            "client-secret",
            "http://redirect"
        )
        val mockRestTemplate = Mockito.mock(RestTemplate::class.java)
        ReflectionTestUtils.setField(authService, "restTemplate", mockRestTemplate)

        Mockito.`when`(
            mockRestTemplate.postForEntity(
                any(String::class.java) ?: "",
                any(),
                eq(Map::class.java)
            )
        ).thenReturn(ResponseEntity(null, HttpStatus.OK))

        // Act
        val result = authService.exchangeCodeForTokens("code123")

        // Assert
        assertNull(result)
    }

    @Test
    fun `exchangeCodeForTokens should return null on exception`() {
        // Arrange
        val authService = CognitoAuthService(
            "https://cognito.example.com",
            "client-id",
            "client-secret",
            "http://redirect"
        )
        val mockRestTemplate = Mockito.mock(RestTemplate::class.java)
        ReflectionTestUtils.setField(authService, "restTemplate", mockRestTemplate)

        Mockito.`when`(
            mockRestTemplate.postForEntity(
                any(String::class.java) ?: "",
                any(),
                eq(Map::class.java)
            )
        ).thenThrow(RuntimeException("failure"))

        // Act
        val result = authService.exchangeCodeForTokens("code123")

        // Assert
        assertNull(result)
    }
}