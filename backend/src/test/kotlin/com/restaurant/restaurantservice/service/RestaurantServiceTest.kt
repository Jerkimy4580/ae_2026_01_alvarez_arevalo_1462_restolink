package com.restaurant.restaurantservice.service

import com.restaurant.restaurantservice.dto.RestaurantRequest
import com.restaurant.restaurantservice.entity.Franchise
import com.restaurant.restaurantservice.entity.Restaurant
import com.restaurant.restaurantservice.exception.DuplicateResourceException
import com.restaurant.restaurantservice.exception.ResourceNotFoundException
import com.restaurant.restaurantservice.repository.FranchiseRepository
import com.restaurant.restaurantservice.repository.RestaurantRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class RestaurantServiceTest {

    @Mock
    private lateinit var restaurantRepository: RestaurantRepository

    @Mock
    private lateinit var franchiseRepository: FranchiseRepository

    @InjectMocks
    private lateinit var restaurantService: RestaurantServiceImpl

    @Test
    fun `getAllRestaurants should return all restaurants`() {
        // Arrange
        val restaurant = Restaurant(id = 1L, name = "La Mesa", address = "Calle 123")
        Mockito.`when`(restaurantRepository.findAll()).thenReturn(listOf(restaurant))

        // Act
        val result = restaurantService.getAllRestaurants()

        // Assert
        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
        assertEquals("La Mesa", result[0].name)
        assertEquals("Calle 123", result[0].address)
    }

    @Test
    fun `createRestaurant should throw when duplicate address exists`() {
        // Arrange
        val request = RestaurantRequest(name = "Test", address = "Calle 123")
        Mockito.`when`(restaurantRepository.existsByAddressIgnoreCase("Calle 123")).thenReturn(true)

        // Act & Assert
        assertThrows(DuplicateResourceException::class.java) {
            restaurantService.createRestaurant(request)
        }
    }

    @Test
    fun `createRestaurant should save restaurant without franchise`() {
        // Arrange
        val request = RestaurantRequest(name = "Nuevo", address = "Avenida 1")
        Mockito.`when`(restaurantRepository.existsByAddressIgnoreCase("Avenida 1")).thenReturn(false)
        Mockito.`when`(restaurantRepository.save(Mockito.any(Restaurant::class.java))).thenAnswer { invocation ->
            invocation.getArgument<Restaurant>(0).also { it.id = 2L }
        }

        // Act
        val response = restaurantService.createRestaurant(request)

        // Assert
        assertNotNull(response)
        assertEquals(2L, response.id)
        assertEquals("Nuevo", response.name)
        assertEquals("Avenida 1", response.address)
        assertNull(response.franchiseId)
    }

    @Test
    fun `createRestaurant should save restaurant with franchise when franchiseId is provided`() {
        // Arrange
        val franchise = Franchise(id = 3L, name = "Franquicia")
        val request = RestaurantRequest(name = "Nuevo", address = "Avenida 2", franchiseId = 3L)
        Mockito.`when`(restaurantRepository.existsByAddressIgnoreCase("Avenida 2")).thenReturn(false)
        Mockito.`when`(franchiseRepository.findById(3L)).thenReturn(Optional.of(franchise))
        Mockito.`when`(restaurantRepository.save(Mockito.any(Restaurant::class.java))).thenAnswer { invocation ->
            invocation.getArgument<Restaurant>(0).also { it.id = 4L }
        }

        // Act
        val response = restaurantService.createRestaurant(request, "chef1")

        // Assert
        assertEquals(4L, response.id)
        assertEquals(3L, response.franchiseId)
        assertEquals("chef1", response.chefUserId)
    }

    @Test
    fun `createRestaurant should throw when franchise not found`() {
        // Arrange
        val request = RestaurantRequest(name = "Nuevo", address = "Avenida 3", franchiseId = 8L)
        Mockito.`when`(restaurantRepository.existsByAddressIgnoreCase("Avenida 3")).thenReturn(false)
        Mockito.`when`(franchiseRepository.findById(8L)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows(ResourceNotFoundException::class.java) {
            restaurantService.createRestaurant(request)
        }
    }

    @Test
    fun `getRestaurantById should return restaurant when found`() {
        // Arrange
        val restaurant = Restaurant(id = 11L, name = "Restaurante C", address = "Calle Z")
        Mockito.`when`(restaurantRepository.findById(11L)).thenReturn(Optional.of(restaurant))

        // Act
        val response = restaurantService.getRestaurantById(11L)

        // Assert
        assertEquals(11L, response.id)
        assertEquals("Restaurante C", response.name)
    }

    @Test
    fun `getRestaurantById should throw when restaurant not found`() {
        // Arrange
        Mockito.`when`(restaurantRepository.findById(10L)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows(ResourceNotFoundException::class.java) {
            restaurantService.getRestaurantById(10L)
        }
    }

    @Test
    fun `getRestaurantsByFranchise should return restaurants for franchise`() {
        // Arrange
        val franchise = Franchise(id = 5L, name = "Franquicia")
        val restaurant = Restaurant(id = 6L, name = "Restaurante", address = "Calle B", franchise = franchise)
        Mockito.`when`(restaurantRepository.findByFranchiseId(5L)).thenReturn(listOf(restaurant))

        // Act
        val result = restaurantService.getRestaurantsByFranchise(5L)

        // Assert
        assertEquals(1, result.size)
        assertEquals(6L, result[0].id)
        assertEquals(5L, result[0].franchiseId)
    }
}
