package com.restaurant.restaurantservice.service

import com.restaurant.restaurantservice.dto.FranchiseRequest
import com.restaurant.restaurantservice.dto.RestaurantRequest
import com.restaurant.restaurantservice.dto.TableRequest
import com.restaurant.restaurantservice.entity.Franchise
import com.restaurant.restaurantservice.entity.Restaurant
import com.restaurant.restaurantservice.entity.TableEntity
import com.restaurant.restaurantservice.exception.DuplicateResourceException
import com.restaurant.restaurantservice.repository.FranchiseRepository
import com.restaurant.restaurantservice.repository.RestaurantRepository
import com.restaurant.restaurantservice.repository.TableRepository
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.Optional

class EntityValidationServiceTest {

    @Mock
    private lateinit var franchiseRepository: FranchiseRepository

    @Mock
    private lateinit var restaurantRepository: RestaurantRepository

    @Mock
    private lateinit var tableRepository: TableRepository

    @InjectMocks
    private lateinit var franchiseService: FranchiseServiceImpl

    @InjectMocks
    private lateinit var restaurantService: RestaurantServiceImpl

    @InjectMocks
    private lateinit var tableService: TableServiceImpl

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `createFranchise should reject duplicate names`() {
        `when`(franchiseRepository.existsByNameIgnoreCase("La Casa del Sabor")).thenReturn(true)

        val request = FranchiseRequest(name = "La Casa del Sabor")

        assertThrows<DuplicateResourceException> {
            franchiseService.createFranchise(request)
        }

        verify(franchiseRepository).existsByNameIgnoreCase("La Casa del Sabor")
        verify(franchiseRepository, never()).save(any())
    }

    @Test
    fun `createRestaurant should reject duplicate addresses`() {
        val franchise = Franchise(id = 7L, name = "La Casa del Sabor")
        `when`(franchiseRepository.findById(7L)).thenReturn(Optional.of(franchise))
        `when`(restaurantRepository.existsByAddressIgnoreCase("Av. Siempre Viva 742")).thenReturn(true)

        val request = RestaurantRequest(
            name = "Sushi Express",
            address = "Av. Siempre Viva 742",
            franchiseId = 7L
        )

        assertThrows<DuplicateResourceException> {
            restaurantService.createRestaurant(request)
        }

        verify(restaurantRepository).existsByAddressIgnoreCase("Av. Siempre Viva 742")
        verify(restaurantRepository, never()).save(any())
    }

    @Test
    fun `createTable should reject a second table for the same restaurant`() {
        val restaurant = Restaurant(id = 10L, name = "Sushi Express", address = "Av. Siempre Viva 742")
        `when`(restaurantRepository.findById(10L)).thenReturn(Optional.of(restaurant))
        `when`(tableRepository.findByRestaurantId(10L)).thenReturn(listOf(TableEntity(number = 1, capacity = 4, restaurant = restaurant)))

        val request = TableRequest(number = 2, capacity = 6)

        assertThrows<DuplicateResourceException> {
            tableService.createTable(10L, request)
        }

        verify(tableRepository).findByRestaurantId(10L)
        verify(tableRepository, never()).save(any())
    }
}
