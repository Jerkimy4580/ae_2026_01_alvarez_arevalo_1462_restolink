package com.restaurant.restaurantservice.service

import com.restaurant.restaurantservice.dto.FranchiseRequest
import com.restaurant.restaurantservice.entity.Franchise
import com.restaurant.restaurantservice.exception.DuplicateResourceException
import com.restaurant.restaurantservice.exception.ResourceNotFoundException
import com.restaurant.restaurantservice.repository.FranchiseRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class FranchiseServiceTest {

    @Mock
    private lateinit var franchiseRepository: FranchiseRepository

    @InjectMocks
    private lateinit var franchiseService: FranchiseServiceImpl

    @Test
    fun `getAllFranchises should return all franchises`() {
        // Arrange
        val franchise = Franchise(id = 1L, name = "Franquicia Uno")
        Mockito.`when`(franchiseRepository.findAll()).thenReturn(listOf(franchise))

        // Act
        val result = franchiseService.getAllFranchises()

        // Assert
        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
        assertEquals("Franquicia Uno", result[0].name)
    }

    @Test
    fun `createFranchise should throw when name duplicate`() {
        // Arrange
        val request = FranchiseRequest(name = "Franquicia Uno")
        Mockito.`when`(franchiseRepository.existsByNameIgnoreCase("Franquicia Uno")).thenReturn(true)

        // Act & Assert
        assertThrows(DuplicateResourceException::class.java) {
            franchiseService.createFranchise(request)
        }
    }

    @Test
    fun `createFranchise should save franchise when name is not duplicate`() {
        // Arrange
        val request = FranchiseRequest(name = "Franquicia Nueva")
        Mockito.`when`(franchiseRepository.existsByNameIgnoreCase("Franquicia Nueva")).thenReturn(false)
        Mockito.`when`(franchiseRepository.save(Mockito.any(Franchise::class.java))).thenAnswer { invocation ->
            invocation.getArgument<Franchise>(0).also { it.id = 3L }
        }

        // Act
        val response = franchiseService.createFranchise(request)

        // Assert
        assertEquals(3L, response.id)
        assertEquals("Franquicia Nueva", response.name)
    }

    @Test
    fun `getFranchiseById should throw when franchise not found`() {
        // Arrange
        Mockito.`when`(franchiseRepository.findById(5L)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows(ResourceNotFoundException::class.java) {
            franchiseService.getFranchiseById(5L)
        }
    }

    @Test
    fun `getFranchiseById should return franchise response when found`() {
        // Arrange
        val franchise = Franchise(id = 2L, name = "Otra Franquicia")
        Mockito.`when`(franchiseRepository.findById(2L)).thenReturn(Optional.of(franchise))

        // Act
        val response = franchiseService.getFranchiseById(2L)

        // Assert
        assertEquals(2L, response.id)
        assertEquals("Otra Franquicia", response.name)
        assertTrue(response.restaurantIds.isEmpty())
    }
}
