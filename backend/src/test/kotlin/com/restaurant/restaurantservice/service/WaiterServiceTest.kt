package com.restaurant.restaurantservice.service

import com.restaurant.restaurantservice.entity.Restaurant
import com.restaurant.restaurantservice.entity.RestaurantWaiter
import com.restaurant.restaurantservice.exception.ResourceNotFoundException
import com.restaurant.restaurantservice.repository.RestaurantRepository
import com.restaurant.restaurantservice.repository.RestaurantWaiterRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class WaiterServiceTest {

    @Mock
    private lateinit var restaurantWaiterRepository: RestaurantWaiterRepository

    @Mock
    private lateinit var restaurantRepository: RestaurantRepository

    @InjectMocks
    private lateinit var waiterService: WaiterServiceImpl

    @Test
    fun `assignWaiterToRestaurant should save assignment when waiter is free`() {
        // Arrange
        val restaurant = Restaurant(id = 1L, name = "Restaurante", address = "Calle X")
        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(restaurantWaiterRepository.findByWaiterUserId("waiter1")).thenReturn(Optional.empty())
        Mockito.`when`(restaurantWaiterRepository.save(Mockito.any(RestaurantWaiter::class.java))).thenAnswer { invocation -> invocation.getArgument<RestaurantWaiter>(0) }

        // Act
        val response = waiterService.assignWaiterToRestaurant(1L, "waiter1")

        // Assert
        assertEquals(1L, response.id)
        assertEquals("Restaurante", response.name)
    }

    @Test
    fun `assignWaiterToRestaurant should update assignment when waiter is already assigned`() {
        // Arrange
        val oldRestaurant = Restaurant(id = 2L, name = "Restaurante Viejo", address = "Calle Y")
        val newRestaurant = Restaurant(id = 1L, name = "Restaurante Nuevo", address = "Calle X")
        val existingAssignment = RestaurantWaiter(id = 10L, waiterUserId = "waiter1", restaurant = oldRestaurant)

        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(newRestaurant))
        Mockito.`when`(restaurantWaiterRepository.findByWaiterUserId("waiter1")).thenReturn(Optional.of(existingAssignment))
        Mockito.`when`(restaurantWaiterRepository.save(Mockito.any(RestaurantWaiter::class.java))).thenAnswer { invocation -> invocation.getArgument<RestaurantWaiter>(0) }

        // Act
        val response = waiterService.assignWaiterToRestaurant(1L, "waiter1")

        // Assert
        assertEquals(1L, response.id)
        assertEquals("Restaurante Nuevo", response.name)
        assertEquals(newRestaurant, existingAssignment.restaurant)
    }

    @Test
    fun `assignWaiterToRestaurant should throw when restaurant not found`() {
        // Arrange
        Mockito.`when`(restaurantRepository.findById(9L)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows(ResourceNotFoundException::class.java) {
            waiterService.assignWaiterToRestaurant(9L, "waiter4")
        }
    }

    @Test
    fun `getMyRestaurant should return restaurant when waiter exists`() {
        // Arrange
        val restaurant = Restaurant(id = 2L, name = "Restaurante B", address = "Calle Y")
        val assignment = RestaurantWaiter(id = 1L, waiterUserId = "waiter2", restaurant = restaurant)
        Mockito.`when`(restaurantWaiterRepository.findByWaiterUserId("waiter2")).thenReturn(Optional.of(assignment))

        // Act
        val response = waiterService.getMyRestaurant("waiter2")

        // Assert
        assertEquals(2L, response.id)
        assertEquals("Restaurante B", response.name)
    }

    @Test
    fun `getMyRestaurant should throw when waiter not assigned`() {
        // Arrange
        Mockito.`when`(restaurantWaiterRepository.findByWaiterUserId("waiter3")).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows(ResourceNotFoundException::class.java) {
            waiterService.getMyRestaurant("waiter3")
        }
    }
}