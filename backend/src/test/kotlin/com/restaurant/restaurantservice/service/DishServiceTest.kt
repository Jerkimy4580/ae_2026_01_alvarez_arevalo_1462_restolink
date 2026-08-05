package com.restaurant.restaurantservice.service

import com.restaurant.restaurantservice.dto.DishRequest
import com.restaurant.restaurantservice.dto.UpdateDishAvailabilityRequest
import com.restaurant.restaurantservice.entity.Allergen
import com.restaurant.restaurantservice.entity.Dish
import com.restaurant.restaurantservice.entity.Restaurant
import com.restaurant.restaurantservice.exception.ResourceNotFoundException
import com.restaurant.restaurantservice.repository.DishRepository
import com.restaurant.restaurantservice.repository.RestaurantRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class DishServiceTest {

    @Mock
    private lateinit var dishRepository: DishRepository

    @Mock
    private lateinit var restaurantRepository: RestaurantRepository

    @InjectMocks
    private lateinit var dishService: DishServiceImpl

    @Test
    fun `getAllDishes should return dish responses`() {
        // Arrange
        val restaurant = Restaurant(id = 1L, name = "Restaurante", address = "Calle X")
        val dish = Dish(id = 1L, name = "Taco", price = BigDecimal("12.50"), isAvailable = true, restaurant = restaurant)
        Mockito.`when`(dishRepository.findByRestaurantIdAndIsDeletedFalse(1L)).thenReturn(listOf(dish))

        // Act
        val result = dishService.getAllDishes(1L)

        // Assert
        assertEquals(1, result.size)
        assertEquals("Taco", result[0].name)
    }

    @Test
    fun `createDish should throw when restaurant not found`() {
        // Arrange
        val request = DishRequest(name = "Taco", price = BigDecimal("10.00"), isAvailable = true)
        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows(ResourceNotFoundException::class.java) {
            dishService.createDish(1L, request)
        }
    }

    @Test
    fun `createDish should throw when duplicate dish exists`() {
        // Arrange
        val restaurant = Restaurant(id = 1L, name = "Restaurante", address = "Calle X")
        val request = DishRequest(name = "Taco", price = BigDecimal("10.00"), isAvailable = true)
        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(dishRepository.existsByRestaurantIdAndNameIgnoreCaseAndPriceAndIsDeletedFalse(1L, "Taco", BigDecimal("10.00"))).thenReturn(true)

        // Act & Assert
        assertThrows(Exception::class.java) {
            dishService.createDish(1L, request)
        }
    }

    @Test
    fun `createDish should save dish when no duplicate exists`() {
        // Arrange
        val restaurant = Restaurant(id = 1L, name = "Restaurante", address = "Calle X")
        val request = DishRequest(name = "Taco", price = BigDecimal("10.00"), isAvailable = true, allergens = listOf("gluten"))
        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(dishRepository.existsByRestaurantIdAndNameIgnoreCaseAndPriceAndIsDeletedFalse(1L, "Taco", BigDecimal("10.00"))).thenReturn(false)
        Mockito.`when`(dishRepository.save(Mockito.any(Dish::class.java))).thenAnswer { invocation ->
            invocation.getArgument<Dish>(0).also { it.id = 5L }
        }

        // Act
        val response = dishService.createDish(1L, request)

        // Assert
        assertEquals(5L, response.id)
        assertEquals("Taco", response.name)
        assertTrue(response.allergens.contains("GLUTEN"))
    }

    @Test
    fun `updateDish should persist updated dish and allergens`() {
        // Arrange
        val restaurant = Restaurant(id = 1L, name = "Restaurante", address = "Calle X")
        val existingDish = Dish(id = 2L, name = "Antiguo", price = BigDecimal("8.00"), isAvailable = false, restaurant = restaurant)
        existingDish.allergens.add(Allergen.NUTS)
        val request = DishRequest(name = "Nuevo", price = BigDecimal("9.00"), isAvailable = true, allergens = listOf("gluten"))
        Mockito.`when`(dishRepository.findByRestaurantIdAndIdAndIsDeletedFalse(1L, 2L)).thenReturn(Optional.of(existingDish))
        Mockito.`when`(dishRepository.existsByRestaurantIdAndNameIgnoreCaseAndPriceAndIdNotAndIsDeletedFalse(1L, "Nuevo", BigDecimal("9.00"), 2L)).thenReturn(false)
        Mockito.`when`(dishRepository.save(Mockito.any(Dish::class.java))).thenAnswer { invocation -> invocation.getArgument<Dish>(0) }

        // Act
        val response = dishService.updateDish(1L, 2L, request)

        // Assert
        assertEquals(2L, response.id)
        assertEquals("Nuevo", response.name)
        assertEquals(BigDecimal("9.00"), response.price)
        assertTrue(response.allergens.contains("GLUTEN"))
    }

    @Test
    fun `updateDish should throw when dish not found`() {
        // Arrange
        val request = DishRequest(name = "Nuevo", price = BigDecimal("9.00"), isAvailable = true)
        Mockito.`when`(dishRepository.findByRestaurantIdAndIdAndIsDeletedFalse(1L, 2L)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows(ResourceNotFoundException::class.java) {
            dishService.updateDish(1L, 2L, request)
        }
    }

    @Test
    fun `updateDish should throw when duplicate dish exists`() {
        // Arrange
        val restaurant = Restaurant(id = 1L, name = "Restaurante", address = "Calle X")
        val existingDish = Dish(id = 2L, name = "Antiguo", price = BigDecimal("8.00"), isAvailable = false, restaurant = restaurant)
        val request = DishRequest(name = "Nuevo", price = BigDecimal("9.00"), isAvailable = true)
        Mockito.`when`(dishRepository.findByRestaurantIdAndIdAndIsDeletedFalse(1L, 2L)).thenReturn(Optional.of(existingDish))
        Mockito.`when`(dishRepository.existsByRestaurantIdAndNameIgnoreCaseAndPriceAndIdNotAndIsDeletedFalse(1L, "Nuevo", BigDecimal("9.00"), 2L)).thenReturn(true)

        // Act & Assert
        assertThrows(Exception::class.java) {
            dishService.updateDish(1L, 2L, request)
        }
    }

    @Test
    fun `updateDish should throw when allergen is unsupported`() {
        // Arrange
        val restaurant = Restaurant(id = 1L, name = "Restaurante", address = "Calle X")
        val existingDish = Dish(id = 2L, name = "Antiguo", price = BigDecimal("8.00"), isAvailable = false, restaurant = restaurant)
        val request = DishRequest(name = "Nuevo", price = BigDecimal("9.00"), isAvailable = true, allergens = listOf("no-existe"))
        Mockito.`when`(dishRepository.findByRestaurantIdAndIdAndIsDeletedFalse(1L, 2L)).thenReturn(Optional.of(existingDish))
        Mockito.`when`(dishRepository.existsByRestaurantIdAndNameIgnoreCaseAndPriceAndIdNotAndIsDeletedFalse(1L, "Nuevo", BigDecimal("9.00"), 2L)).thenReturn(false)

        // Act & Assert
        assertThrows(IllegalArgumentException::class.java) {
            dishService.updateDish(1L, 2L, request)
        }
    }

    @Test
    fun `updateAvailability should throw when dish not found`() {
        // Arrange
        val request = UpdateDishAvailabilityRequest(isAvailable = false)
        Mockito.`when`(dishRepository.findByRestaurantIdAndIdAndIsDeletedFalse(1L, 3L)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows(ResourceNotFoundException::class.java) {
            dishService.updateAvailability(1L, 3L, request)
        }
    }

    @Test
    fun `updateAvailability should update dish availability`() {
        // Arrange
        val restaurant = Restaurant(id = 1L, name = "Restaurante", address = "Calle X")
        val existingDish = Dish(id = 3L, name = "Sopa", price = BigDecimal("5.00"), isAvailable = true, restaurant = restaurant)
        val request = UpdateDishAvailabilityRequest(isAvailable = false)
        Mockito.`when`(dishRepository.findByRestaurantIdAndIdAndIsDeletedFalse(1L, 3L)).thenReturn(Optional.of(existingDish))
        Mockito.`when`(dishRepository.save(Mockito.any(Dish::class.java))).thenAnswer { invocation -> invocation.getArgument<Dish>(0) }

        // Act
        val response = dishService.updateAvailability(1L, 3L, request)

        // Assert
        assertEquals(false, response.isAvailable)
    }

    @Test
    fun `deleteDish should mark dish as deleted`() {
        // Arrange
        val restaurant = Restaurant(id = 1L, name = "Restaurante", address = "Calle X")
        val existingDish = Dish(id = 4L, name = "Ensalada", price = BigDecimal("7.00"), isAvailable = true, restaurant = restaurant)
        Mockito.`when`(dishRepository.findByRestaurantIdAndIdAndIsDeletedFalse(1L, 4L)).thenReturn(Optional.of(existingDish))
        Mockito.`when`(dishRepository.save(Mockito.any(Dish::class.java))).thenAnswer { invocation -> invocation.getArgument<Dish>(0) }

        // Act
        dishService.deleteDish(1L, 4L)

        // Assert
        assertTrue(existingDish.isDeleted)
    }

    @Test
    fun `deleteDish should throw when dish not found`() {
        // Arrange
        Mockito.`when`(dishRepository.findByRestaurantIdAndIdAndIsDeletedFalse(1L, 4L)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows(ResourceNotFoundException::class.java) {
            dishService.deleteDish(1L, 4L)
        }
    }
}
