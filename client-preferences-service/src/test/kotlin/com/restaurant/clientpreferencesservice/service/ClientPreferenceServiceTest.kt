package com.restaurant.clientpreferencesservice.service

import com.restaurant.clientpreferencesservice.dto.ClientPreferenceRequest
import com.restaurant.clientpreferencesservice.entity.Allergen
import com.restaurant.clientpreferencesservice.entity.ClientPreference
import com.restaurant.clientpreferencesservice.repository.ClientPreferenceRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ClientPreferenceServiceTest {

    @Mock
    private lateinit var clientPreferenceRepository: ClientPreferenceRepository

    @InjectMocks
    private lateinit var clientPreferenceService: ClientPreferenceServiceImpl

    @Test
    fun `saveOrUpdatePreferences should create and save new preferences when user does not exist`() {
        // Arrange
        val username = "user1"
        val request = ClientPreferenceRequest(username = username, allergens = listOf("gluten", "lactose"))

        Mockito.`when`(clientPreferenceRepository.findByUsername(username)).thenReturn(Optional.empty())
        Mockito.`when`(clientPreferenceRepository.save(Mockito.any(ClientPreference::class.java))).thenAnswer { invocation ->
            invocation.getArgument<ClientPreference>(0).also { it.id = 1L }
        }

        // Act
        val response = clientPreferenceService.saveOrUpdatePreferences(username, request)

        // Assert
        assertNotNull(response)
        assertEquals(1L, response.id)
        assertEquals(username, response.username)
        assertEquals(listOf("GLUTEN", "LACTOSE"), response.allergens)
    }

    @Test
    fun `saveOrUpdatePreferences should update existing preferences with new allergens`() {
        // Arrange
        val username = "user2"
        val existingPreference = ClientPreference(
            id = 2L,
            username = username,
            allergens = mutableSetOf(Allergen.NUTS)
        )
        val request = ClientPreferenceRequest(username = username, allergens = listOf("cacahuetes", "mariscos"))

        Mockito.`when`(clientPreferenceRepository.findByUsername(username)).thenReturn(Optional.of(existingPreference))
        Mockito.`when`(clientPreferenceRepository.save(Mockito.any(ClientPreference::class.java))).thenAnswer { invocation ->
            invocation.getArgument<ClientPreference>(0)
        }

        // Act
        val response = clientPreferenceService.saveOrUpdatePreferences(username, request)

        // Assert
        assertNotNull(response)
        assertEquals(2L, response.id)
        assertEquals(username, response.username)
        assertEquals(listOf("PEANUTS", "SHELLFISH"), response.allergens)
    }

    @Test
    fun `saveOrUpdatePreferences should replace old allergens entirely, not merge them`() {
        // Arrange: el usuario ya tenía NUTS y SOY, y ahora manda solo GLUTEN.
        // Cubre explícitamente que preference.allergens.clear() se ejecuta antes del addAll.
        val username = "user6"
        val existingPreference = ClientPreference(
            id = 6L,
            username = username,
            allergens = mutableSetOf(Allergen.NUTS, Allergen.SOY)
        )
        val request = ClientPreferenceRequest(username = username, allergens = listOf("gluten"))

        Mockito.`when`(clientPreferenceRepository.findByUsername(username)).thenReturn(Optional.of(existingPreference))
        Mockito.`when`(clientPreferenceRepository.save(Mockito.any(ClientPreference::class.java))).thenAnswer { invocation ->
            invocation.getArgument<ClientPreference>(0)
        }

        // Act
        val response = clientPreferenceService.saveOrUpdatePreferences(username, request)

        // Assert
        assertEquals(listOf("GLUTEN"), response.allergens)
        assertTrue(!response.allergens.contains("NUTS"))
        assertTrue(!response.allergens.contains("SOY"))
    }

    @Test
    fun `saveOrUpdatePreferences should save empty allergens when request list is empty`() {
        // Arrange: cubre el caso en que allergens llega vacío (map sobre lista vacía)
        val username = "user7"
        val existingPreference = ClientPreference(
            id = 7L,
            username = username,
            allergens = mutableSetOf(Allergen.GLUTEN)
        )
        val request = ClientPreferenceRequest(username = username, allergens = emptyList())

        Mockito.`when`(clientPreferenceRepository.findByUsername(username)).thenReturn(Optional.of(existingPreference))
        Mockito.`when`(clientPreferenceRepository.save(Mockito.any(ClientPreference::class.java))).thenAnswer { invocation ->
            invocation.getArgument<ClientPreference>(0)
        }

        // Act
        val response = clientPreferenceService.saveOrUpdatePreferences(username, request)

        // Assert
        assertTrue(response.allergens.isEmpty())
    }

    @Test
    fun `saveOrUpdatePreferences should throw IllegalArgumentException for unsupported allergen`() {
        // Arrange
        val username = "user3"
        val request = ClientPreferenceRequest(username = username, allergens = listOf("unknown-allergen"))

        Mockito.`when`(clientPreferenceRepository.findByUsername(username)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows(IllegalArgumentException::class.java) {
            clientPreferenceService.saveOrUpdatePreferences(username, request)
        }
    }

    @Test
    fun `saveOrUpdatePreferences should throw on first unsupported allergen even if others are valid`() {
        // Arrange: mezcla de alérgeno válido e inválido, cubre que el map corta en el primer fallo
        val username = "user8"
        val request = ClientPreferenceRequest(username = username, allergens = listOf("gluten", "invalido"))

        Mockito.`when`(clientPreferenceRepository.findByUsername(username)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows(IllegalArgumentException::class.java) {
            clientPreferenceService.saveOrUpdatePreferences(username, request)
        }
        Mockito.verify(clientPreferenceRepository, Mockito.never()).save(Mockito.any(ClientPreference::class.java))
    }

    @Test
    fun `getPreferencesByUsername should return response when preference exists`() {
        // Arrange
        val username = "user4"
        val existingPreference = ClientPreference(
            id = 3L,
            username = username,
            allergens = mutableSetOf(Allergen.SOY, Allergen.SESAME)
        )

        Mockito.`when`(clientPreferenceRepository.findByUsername(username)).thenReturn(Optional.of(existingPreference))

        // Act
        val response = clientPreferenceService.getPreferencesByUsername(username)

        // Assert
        assertNotNull(response)
        assertEquals(3L, response!!.id)
        assertEquals(username, response.username)
        assertEquals(listOf("SOY", "SESAME"), response.allergens)
    }

    @Test
    fun `getPreferencesByUsername should return null when preference does not exist`() {
        // Arrange
        val username = "user5"

        Mockito.`when`(clientPreferenceRepository.findByUsername(username)).thenReturn(Optional.empty())

        // Act
        val response = clientPreferenceService.getPreferencesByUsername(username)

        // Assert
        assertNull(response)
    }
}