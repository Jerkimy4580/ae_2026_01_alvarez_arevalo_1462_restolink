package com.restaurant.restaurantservice.service

import com.restaurant.restaurantservice.dto.DishRequest
import com.restaurant.restaurantservice.dto.RestaurantRequest
import com.restaurant.restaurantservice.entity.Dish
import com.restaurant.restaurantservice.entity.Restaurant
import com.restaurant.restaurantservice.repository.DishRepository
import com.restaurant.restaurantservice.repository.FranchiseRepository
import com.restaurant.restaurantservice.repository.RestaurantRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import java.util.Optional

class RestaurantServiceTest {

    private val restaurantRepository: RestaurantRepository = mock()
    private val franchiseRepository: FranchiseRepository = mock()
    private val dishRepository: DishRepository = mock()

    @Test
    fun `createRestaurant assigns chef id from authenticated user`() {
        val service = RestaurantServiceImpl(restaurantRepository, franchiseRepository)

        `when`(restaurantRepository.existsByAddressIgnoreCase("123 Main St"))
            .thenReturn(false)
        `when`(restaurantRepository.save(any(Restaurant::class.java)))
            .thenAnswer { it.arguments[0] as Restaurant }

        val request = RestaurantRequest(name = "La Casa", address = "123 Main St")
        val response = service.createRestaurant(request, "chef-123")

        assertEquals("chef-123", response.chefUserId)
        assertEquals("La Casa", response.name)
    }

    @Test
    fun `deleteDish marks dish as deleted instead of removing it`() {
        val service = DishServiceImpl(dishRepository, restaurantRepository)
        val dish = Dish(
            name = "Burger",
            price = BigDecimal("10.50"),
            restaurant = Restaurant(name = "Resto", address = "Street")
        )

        `when`(dishRepository.findByRestaurantIdAndIdAndIsDeletedFalse(1L, 2L))
            .thenReturn(Optional.of(dish))
        `when`(dishRepository.save(any(Dish::class.java)))
            .thenAnswer { it.arguments[0] as Dish }

        service.deleteDish(1L, 2L)

        assertTrue(dish.isDeleted)
        verify(dishRepository).save(dish)
    }
}
