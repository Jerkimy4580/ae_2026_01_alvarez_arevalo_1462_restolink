package com.restaurant.restaurantservice.service

import com.restaurant.restaurantservice.dto.TableRequest
import com.restaurant.restaurantservice.entity.Restaurant
import com.restaurant.restaurantservice.entity.TableEntity
import com.restaurant.restaurantservice.exception.DuplicateResourceException
import com.restaurant.restaurantservice.exception.ResourceNotFoundException
import com.restaurant.restaurantservice.repository.RestaurantRepository
import com.restaurant.restaurantservice.repository.TableRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class TableServiceTest {

    @Mock
    private lateinit var tableRepository: TableRepository

    @Mock
    private lateinit var restaurantRepository: RestaurantRepository

    @InjectMocks
    private lateinit var tableService: TableServiceImpl

    @Test
    fun `getTablesByRestaurant should return table responses`() {
        // Arrange
        val restaurant = Restaurant(id = 1L, name = "Restaurante", address = "Calle X")
        val table = TableEntity(id = 1L, number = 10, capacity = 4, restaurant = restaurant)
        Mockito.`when`(tableRepository.findByRestaurantId(1L)).thenReturn(listOf(table))

        // Act
        val result = tableService.getTablesByRestaurant(1L)

        // Assert
        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
    }

    @Test
    fun `createTable should save table when none exists yet`() {
        // Arrange
        val restaurant = Restaurant(id = 1L, name = "Restaurante", address = "Calle X")
        val request = TableRequest(number = 5, capacity = 2)
        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(tableRepository.findByRestaurantId(1L)).thenReturn(emptyList())
        Mockito.`when`(tableRepository.save(Mockito.any(TableEntity::class.java))).thenAnswer { invocation ->
            invocation.getArgument<TableEntity>(0).also { it.id = 7L }
        }

        // Act
        val response = tableService.createTable(1L, request)

        // Assert
        assertEquals(7L, response.id)
        assertEquals(5, response.number)
        assertEquals(2, response.capacity)
    }

    @Test
    fun `createTable should throw when restaurant not found`() {
        // Arrange
        val request = TableRequest(number = 5, capacity = 2)
        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows(ResourceNotFoundException::class.java) {
            tableService.createTable(1L, request)
        }
    }

    @Test
    fun `createTable should throw when table already exists`() {
        // Arrange
        val restaurant = Restaurant(id = 1L, name = "Restaurante", address = "Calle X")
        val request = TableRequest(number = 5, capacity = 2)
        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(tableRepository.findByRestaurantId(1L)).thenReturn(listOf(TableEntity(id = 2L, number = 3, capacity = 2, restaurant = restaurant)))

        // Act & Assert
        assertThrows(DuplicateResourceException::class.java) {
            tableService.createTable(1L, request)
        }
    }

    @Test
    fun `updateTable should save updated values`() {
        // Arrange
        val restaurant = Restaurant(id = 1L, name = "Restaurante", address = "Calle X")
        val existingTable = TableEntity(id = 3L, number = 1, capacity = 4, restaurant = restaurant)
        val request = TableRequest(number = 2, capacity = 6)
        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(tableRepository.findById(3L)).thenReturn(Optional.of(existingTable))
        Mockito.`when`(tableRepository.save(Mockito.any(TableEntity::class.java))).thenAnswer { invocation -> invocation.getArgument<TableEntity>(0) }

        // Act
        val response = tableService.updateTable(1L, 3L, request)

        // Assert
        assertEquals(2, response.number)
        assertEquals(6, response.capacity)
    }

    @Test
    fun `updateTable should throw when restaurant not found`() {
        // Arrange
        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows(ResourceNotFoundException::class.java) {
            tableService.updateTable(1L, 3L, TableRequest(number = 2, capacity = 6))
        }
    }

    @Test
    fun `updateTable should throw when table not found`() {
        // Arrange
        val restaurant = Restaurant(id = 1L, name = "Restaurante", address = "Calle X")
        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(tableRepository.findById(3L)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows(ResourceNotFoundException::class.java) {
            tableService.updateTable(1L, 3L, TableRequest(number = 2, capacity = 6))
        }
    }

    @Test
    fun `updateTable should throw when table belongs to different restaurant`() {
        // Arrange
        val restaurant = Restaurant(id = 1L, name = "Restaurante", address = "Calle X")
        val otherRestaurant = Restaurant(id = 2L, name = "Otro", address = "Calle Y")
        val existingTable = TableEntity(id = 4L, number = 1, capacity = 4, restaurant = otherRestaurant)
        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(tableRepository.findById(4L)).thenReturn(Optional.of(existingTable))

        // Act & Assert
        assertThrows(ResourceNotFoundException::class.java) {
            tableService.updateTable(1L, 4L, TableRequest(number = 7, capacity = 3))
        }
    }
}
