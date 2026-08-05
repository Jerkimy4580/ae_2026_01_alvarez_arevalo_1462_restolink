package com.restaurant.restaurantservice.service

import com.restaurant.restaurantservice.dto.ClientPreferenceResponse
import com.restaurant.restaurantservice.dto.OrderItemRequest
import com.restaurant.restaurantservice.dto.OrderRequest
import com.restaurant.restaurantservice.dto.UpdateOrderStatusRequest
import com.restaurant.restaurantservice.entity.Allergen
import com.restaurant.restaurantservice.entity.Dish
import com.restaurant.restaurantservice.entity.Order
import com.restaurant.restaurantservice.entity.OrderItem
import com.restaurant.restaurantservice.entity.OrderStatus
import com.restaurant.restaurantservice.entity.Restaurant
import com.restaurant.restaurantservice.entity.TableEntity
import com.restaurant.restaurantservice.exception.AllergenConflictException
import com.restaurant.restaurantservice.exception.ForbiddenAccessException
import com.restaurant.restaurantservice.exception.InvalidOrderStatusException
import com.restaurant.restaurantservice.exception.ResourceNotFoundException
import com.restaurant.restaurantservice.repository.DishRepository
import com.restaurant.restaurantservice.repository.OrderRepository
import com.restaurant.restaurantservice.repository.RestaurantRepository
import com.restaurant.restaurantservice.repository.TableRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class OrderServiceTest {

    @Mock
    private lateinit var orderRepository: OrderRepository

    @Mock
    private lateinit var dishRepository: DishRepository

    @Mock
    private lateinit var restaurantRepository: RestaurantRepository

    @Mock
    private lateinit var tableRepository: TableRepository

    private lateinit var orderService: OrderServiceImpl

    @BeforeEach
    fun setUp() {
        orderService = OrderServiceImpl(
            orderRepository,
            dishRepository,
            restaurantRepository,
            tableRepository,
            "http://localhost"
        )
    }

    private fun createRestaurant(id: Long?): Restaurant {
        val restaurant = Restaurant(
            name = "Test Restaurant",
            address = "123 Main St"
        )
        id?.let { ReflectionTestUtils.setField(restaurant, "id", it) }
        return restaurant
    }

    private fun createTable(id: Long?, restaurant: Restaurant): TableEntity {
        val table = TableEntity(
            number = 1,
            capacity = 4,
            restaurant = restaurant
        )
        id?.let { ReflectionTestUtils.setField(table, "id", it) }
        return table
    }

    private fun createDish(id: Long?, name: String, price: BigDecimal, isAvailable: Boolean, restaurant: Restaurant): Dish {
        return Dish(
            id = id,
            name = name,
            price = price,
            isAvailable = isAvailable,
            restaurant = restaurant
        )
    }

    private fun createOrder(id: Long?, restaurant: Restaurant, clientUser: String?, status: OrderStatus): Order {
        val order = Order(
            restaurant = restaurant,
            table = null,
            clientUser = clientUser,
            status = status,
            totalAmount = BigDecimal.ZERO
        )
        id?.let { ReflectionTestUtils.setField(order, "id", it) }
        return order
    }

    private fun mockWebClientCall(responseMono: Mono<ClientPreferenceResponse>): WebClient {
        val mockWebClient = Mockito.mock(WebClient::class.java)
        @Suppress("UNCHECKED_CAST")
        val requestHeadersUriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec::class.java) as WebClient.RequestHeadersUriSpec<*>
        @Suppress("UNCHECKED_CAST")
        val requestHeadersSpec = Mockito.mock(WebClient.RequestHeadersSpec::class.java) as WebClient.RequestHeadersSpec<*>
        val responseSpec = Mockito.mock(WebClient.ResponseSpec::class.java)

        Mockito.`when`(mockWebClient.get()).thenReturn(requestHeadersUriSpec)
        Mockito.`when`(
            requestHeadersUriSpec.uri(Mockito.any<java.util.function.Function<org.springframework.web.util.UriBuilder, java.net.URI>>())
        ).thenReturn(requestHeadersSpec)
        Mockito.`when`(requestHeadersSpec.headers(Mockito.any())).thenReturn(requestHeadersSpec)
        Mockito.`when`(requestHeadersSpec.retrieve()).thenReturn(responseSpec)
        Mockito.`when`(responseSpec.bodyToMono(ClientPreferenceResponse::class.java)).thenReturn(responseMono)

        return mockWebClient
    }

    @Test
    fun `createOrder should save a new order when user is anonymous`() {
        val restaurant = createRestaurant(1L)
        val table = createTable(2L, restaurant)
        val dish = createDish(3L, "Pizza", BigDecimal("8.00"), true, restaurant)

        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(tableRepository.findById(2L)).thenReturn(Optional.of(table))
        Mockito.`when`(dishRepository.findByRestaurantId(1L)).thenReturn(listOf(dish))
        Mockito.`when`(orderRepository.save(Mockito.any(Order::class.java))).thenAnswer { invocation ->
            invocation.getArgument<Order>(0).also { ReflectionTestUtils.setField(it, "id", 10L) }
        }

        val request = OrderRequest(restaurantId = 1L, tableId = 2L, items = listOf(OrderItemRequest(dishId = 3L, quantity = 2)))

        val response = orderService.createOrder(1L, request, null)

        assertEquals(10L, response.id)
        assertEquals(BigDecimal("16.00"), response.totalAmount)
        assertEquals(1, response.items.size)
    }

    @Test
    fun `createOrder should throw when restaurant not found`() {
        val request = OrderRequest(restaurantId = 5L, items = emptyList())
        Mockito.`when`(restaurantRepository.findById(5L)).thenReturn(Optional.empty())

        assertThrows(ResourceNotFoundException::class.java) {
            orderService.createOrder(5L, request, null)
        }
    }

    @Test
    fun `createOrder should throw when restaurant id is null`() {
        val restaurant = createRestaurant(null)
        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))

        val request = OrderRequest(restaurantId = 1L, items = emptyList())

        assertThrows(IllegalStateException::class.java) {
            orderService.createOrder(1L, request, "user")
        }
    }

    @Test
    fun `createOrder should append to existing active order for authenticated user`() {
        val restaurant = createRestaurant(1L)
        val table = createTable(2L, restaurant)
        val dish = createDish(3L, "Pizza", BigDecimal("8.00"), true, restaurant)
        val existingOrder = createOrder(20L, restaurant, "user", OrderStatus.PENDING)
        existingOrder.addItem(OrderItem(dishId = 3L, dishName = "Pizza", unitPrice = BigDecimal("8.00"), quantity = 1))

        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(tableRepository.findById(2L)).thenReturn(Optional.of(table))
        Mockito.`when`(dishRepository.findByRestaurantId(1L)).thenReturn(listOf(dish))
        Mockito.`when`(orderRepository.findLatestActiveOrderByClientUser("user", 1L, listOf(OrderStatus.PAID, OrderStatus.RETURNED)))
            .thenReturn(Optional.of(existingOrder))

        val mockWebClient = mockWebClientCall(Mono.empty())
        ReflectionTestUtils.setField(orderService, "clientPreferencesWebClient", mockWebClient)

        Mockito.`when`(orderRepository.save(Mockito.any(Order::class.java))).thenAnswer { invocation -> invocation.getArgument<Order>(0) }

        val request = OrderRequest(restaurantId = 1L, tableId = 2L, items = listOf(OrderItemRequest(dishId = 3L, quantity = 2)))

        val response = orderService.createOrder(1L, request, "user")

        assertEquals(20L, response.id)
        assertEquals(BigDecimal("24.00"), response.totalAmount)
        assertEquals(1, response.items.size)
        assertEquals(3, response.items[0].quantity)
    }

    @Test
    fun `createOrder should create new order for authenticated user when no active order exists`() {
        val restaurant = createRestaurant(1L)
        val table = createTable(2L, restaurant)
        val dish = createDish(3L, "Pizza", BigDecimal("8.00"), true, restaurant)

        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(tableRepository.findById(2L)).thenReturn(Optional.of(table))
        Mockito.`when`(dishRepository.findByRestaurantId(1L)).thenReturn(listOf(dish))
        Mockito.`when`(orderRepository.findLatestActiveOrderByClientUser("user", 1L, listOf(OrderStatus.PAID, OrderStatus.RETURNED)))
            .thenReturn(Optional.empty())

        val mockWebClient = mockWebClientCall(Mono.empty())
        ReflectionTestUtils.setField(orderService, "clientPreferencesWebClient", mockWebClient)

        Mockito.`when`(orderRepository.save(Mockito.any(Order::class.java))).thenAnswer { invocation ->
            invocation.getArgument<Order>(0).also { ReflectionTestUtils.setField(it, "id", 21L) }
        }

        val request = OrderRequest(restaurantId = 1L, tableId = 2L, items = listOf(OrderItemRequest(dishId = 3L, quantity = 1)))

        val response = orderService.createOrder(1L, request, "user")

        assertEquals(21L, response.id)
        assertEquals(BigDecimal("8.00"), response.totalAmount)
    }

    @Test
    fun `createOrder should throw when dish not found`() {
        val restaurant = createRestaurant(1L)
        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(dishRepository.findByRestaurantId(1L)).thenReturn(emptyList())

        val request = OrderRequest(restaurantId = 1L, items = listOf(OrderItemRequest(dishId = 99L, quantity = 1)))

        assertThrows(ResourceNotFoundException::class.java) {
            orderService.createOrder(1L, request, null)
        }
    }

    @Test
    fun `createOrder should throw when dish is unavailable`() {
        val restaurant = createRestaurant(1L)
        val dish = createDish(3L, "Pizza", BigDecimal("8.00"), false, restaurant)

        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(dishRepository.findByRestaurantId(1L)).thenReturn(listOf(dish))

        val request = OrderRequest(restaurantId = 1L, items = listOf(OrderItemRequest(dishId = 3L, quantity = 1)))

        assertThrows(IllegalArgumentException::class.java) {
            orderService.createOrder(1L, request, null)
        }
    }

    @Test
    fun `createOrder should throw when table not found`() {
        val restaurant = createRestaurant(1L)
        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(tableRepository.findById(2L)).thenReturn(Optional.empty())

        val request = OrderRequest(restaurantId = 1L, tableId = 2L, items = listOf(OrderItemRequest(dishId = 3L, quantity = 1)))

        assertThrows(ResourceNotFoundException::class.java) {
            orderService.createOrder(1L, request, null)
        }
    }

    @Test
    fun `createOrder should throw when table does not belong to restaurant`() {
        val restaurant = createRestaurant(1L)
        val otherRestaurant = createRestaurant(2L)
        val table = createTable(2L, otherRestaurant)

        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(tableRepository.findById(2L)).thenReturn(Optional.of(table))

        val request = OrderRequest(restaurantId = 1L, tableId = 2L, items = listOf(OrderItemRequest(dishId = 3L, quantity = 1)))

        assertThrows(IllegalArgumentException::class.java) {
            orderService.createOrder(1L, request, null)
        }
    }

    @Test
    fun `createOrder should create order without table when tableId is null`() {
        val restaurant = createRestaurant(1L)
        val dish = createDish(3L, "Pizza", BigDecimal("8.00"), true, restaurant)
        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(dishRepository.findByRestaurantId(1L)).thenReturn(listOf(dish))
        Mockito.`when`(orderRepository.save(Mockito.any(Order::class.java))).thenAnswer { invocation ->
            invocation.getArgument<Order>(0).also { ReflectionTestUtils.setField(it, "id", 22L) }
        }

        val request = OrderRequest(restaurantId = 1L, tableId = null, items = listOf(OrderItemRequest(dishId = 3L, quantity = 1)))

        val response = orderService.createOrder(1L, request, null)

        assertEquals(22L, response.id)
        assertNull(response.tableId)
    }

    @Test
    fun `createOrder should pass authorization header from RequestContextHolder to WebClient`() {
        val mockServletRequest = MockHttpServletRequest()
        mockServletRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer test-token")
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(mockServletRequest))

        try {
            val restaurant = createRestaurant(1L)
            val dish = createDish(3L, "Pizza", BigDecimal("8.00"), true, restaurant)

            Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
            Mockito.`when`(dishRepository.findByRestaurantId(1L)).thenReturn(listOf(dish))
            Mockito.`when`(orderRepository.findLatestActiveOrderByClientUser("user", 1L, listOf(OrderStatus.PAID, OrderStatus.RETURNED))).thenReturn(Optional.empty())
            Mockito.`when`(orderRepository.save(Mockito.any(Order::class.java))).thenAnswer { invocation -> invocation.getArgument<Order>(0) }

            val mockWebClient = mockWebClientCall(Mono.empty())
            ReflectionTestUtils.setField(orderService, "clientPreferencesWebClient", mockWebClient)

            val request = OrderRequest(restaurantId = 1L, tableId = null, items = listOf(OrderItemRequest(dishId = 3L, quantity = 1)))
            orderService.createOrder(1L, request, "user")

            Mockito.verify(mockWebClient).get()
        } finally {
            RequestContextHolder.resetRequestAttributes()
        }
    }

    @Test
    fun `createOrder should throw AllergenConflictException on allergen match`() {
        val restaurant = createRestaurant(1L)
        val dish = createDish(3L, "Pizza", BigDecimal("8.00"), true, restaurant)
        dish.allergens.add(Allergen.GLUTEN)

        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(dishRepository.findByRestaurantId(1L)).thenReturn(listOf(dish))
        Mockito.`when`(dishRepository.findById(3L)).thenReturn(Optional.of(dish))
        Mockito.`when`(orderRepository.findLatestActiveOrderByClientUser("user", 1L, listOf(OrderStatus.PAID, OrderStatus.RETURNED))).thenReturn(Optional.empty())

        val mockWebClient = mockWebClientCall(Mono.just(ClientPreferenceResponse(username = "user", allergens = listOf("GLUTEN"))))
        ReflectionTestUtils.setField(orderService, "clientPreferencesWebClient", mockWebClient)

        val request = OrderRequest(restaurantId = 1L, items = listOf(OrderItemRequest(dishId = 3L, quantity = 1)))

        assertThrows(AllergenConflictException::class.java) {
            orderService.createOrder(1L, request, "user")
        }
    }

    @Test
    fun `createOrder should handle missing dish in allergen lookup gracefully`() {
        val restaurant = createRestaurant(1L)
        val dish = createDish(3L, "Pizza", BigDecimal("8.00"), true, restaurant)

        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(dishRepository.findByRestaurantId(1L)).thenReturn(listOf(dish))
        Mockito.`when`(dishRepository.findById(3L)).thenReturn(Optional.empty())
        Mockito.`when`(orderRepository.findLatestActiveOrderByClientUser("user", 1L, listOf(OrderStatus.PAID, OrderStatus.RETURNED))).thenReturn(Optional.empty())
        Mockito.`when`(orderRepository.save(Mockito.any(Order::class.java))).thenAnswer { invocation -> invocation.getArgument<Order>(0) }

        val mockWebClient = mockWebClientCall(Mono.just(ClientPreferenceResponse(username = "user", allergens = listOf("GLUTEN"))))
        ReflectionTestUtils.setField(orderService, "clientPreferencesWebClient", mockWebClient)

        val request = OrderRequest(restaurantId = 1L, items = listOf(OrderItemRequest(dishId = 3L, quantity = 1)))
        val response = orderService.createOrder(1L, request, "user")

        assertNotNull(response)
    }

    @Test
    fun `createOrder should handle WebClientResponseException gracefully`() {
        val restaurant = createRestaurant(1L)
        val dish = createDish(3L, "Pizza", BigDecimal("8.00"), true, restaurant)

        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(dishRepository.findByRestaurantId(1L)).thenReturn(listOf(dish))
        Mockito.`when`(orderRepository.findLatestActiveOrderByClientUser("user", 1L, listOf(OrderStatus.PAID, OrderStatus.RETURNED))).thenReturn(Optional.empty())
        Mockito.`when`(orderRepository.save(Mockito.any(Order::class.java))).thenAnswer { invocation -> invocation.getArgument<Order>(0) }

        val webClientException = Mockito.mock(WebClientResponseException::class.java)
        val mockWebClient = mockWebClientCall(Mono.error(webClientException))
        ReflectionTestUtils.setField(orderService, "clientPreferencesWebClient", mockWebClient)

        val request = OrderRequest(restaurantId = 1L, items = listOf(OrderItemRequest(dishId = 3L, quantity = 1)))
        val response = orderService.createOrder(1L, request, "user")

        assertNotNull(response)
    }

    @Test
    fun `createOrder should handle generic Exception during WebClient fetch gracefully`() {
        val restaurant = createRestaurant(1L)
        val dish = createDish(3L, "Pizza", BigDecimal("8.00"), true, restaurant)

        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(dishRepository.findByRestaurantId(1L)).thenReturn(listOf(dish))
        Mockito.`when`(orderRepository.findLatestActiveOrderByClientUser("user", 1L, listOf(OrderStatus.PAID, OrderStatus.RETURNED))).thenReturn(Optional.empty())
        Mockito.`when`(orderRepository.save(Mockito.any(Order::class.java))).thenAnswer { invocation -> invocation.getArgument<Order>(0) }

        val mockWebClient = mockWebClientCall(Mono.error(RuntimeException("Connection failed")))
        ReflectionTestUtils.setField(orderService, "clientPreferencesWebClient", mockWebClient)

        val request = OrderRequest(restaurantId = 1L, items = listOf(OrderItemRequest(dishId = 3L, quantity = 1)))
        val response = orderService.createOrder(1L, request, "user")

        assertNotNull(response)
    }

    @Test
    fun `createOrder should return null preferences when service responds NotFound`() {
        val restaurant = createRestaurant(1L)
        val dish = createDish(3L, "Pizza", BigDecimal("8.00"), true, restaurant)

        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(dishRepository.findByRestaurantId(1L)).thenReturn(listOf(dish))
        Mockito.`when`(orderRepository.findLatestActiveOrderByClientUser("user", 1L, listOf(OrderStatus.PAID, OrderStatus.RETURNED))).thenReturn(Optional.empty())
        Mockito.`when`(orderRepository.save(Mockito.any(Order::class.java))).thenAnswer { invocation -> invocation.getArgument<Order>(0) }

        val notFoundException = Mockito.mock(WebClientResponseException.NotFound::class.java)
        val mockWebClient = mockWebClientCall(Mono.error(notFoundException))
        ReflectionTestUtils.setField(orderService, "clientPreferencesWebClient", mockWebClient)

        val request = OrderRequest(restaurantId = 1L, items = listOf(OrderItemRequest(dishId = 3L, quantity = 1)))
        val response = orderService.createOrder(1L, request, "user")

        assertNotNull(response)
    }

    @Test
    fun `createOrder should throw ForbiddenAccessException when preferences service responds Unauthorized`() {
        val restaurant = createRestaurant(1L)
        val dish = createDish(3L, "Pizza", BigDecimal("8.00"), true, restaurant)

        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(dishRepository.findByRestaurantId(1L)).thenReturn(listOf(dish))
        Mockito.`when`(orderRepository.findLatestActiveOrderByClientUser("user", 1L, listOf(OrderStatus.PAID, OrderStatus.RETURNED))).thenReturn(Optional.empty())

        val unauthorizedException = Mockito.mock(WebClientResponseException.Unauthorized::class.java)
        val mockWebClient = mockWebClientCall(Mono.error(unauthorizedException))
        ReflectionTestUtils.setField(orderService, "clientPreferencesWebClient", mockWebClient)

        val request = OrderRequest(restaurantId = 1L, items = listOf(OrderItemRequest(dishId = 3L, quantity = 1)))

        assertThrows(ForbiddenAccessException::class.java) {
            orderService.createOrder(1L, request, "user")
        }
    }

    @Test
    fun `createOrder should add new item without merging when existing items differ`() {
        val restaurant = createRestaurant(1L)
        val dishA = createDish(3L, "Pizza", BigDecimal("8.00"), true, restaurant)
        val existingOrder = createOrder(40L, restaurant, "user", OrderStatus.PENDING)
        existingOrder.addItem(OrderItem(dishId = 99L, dishName = "Sushi", unitPrice = BigDecimal("12.00"), quantity = 1))

        Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
        Mockito.`when`(dishRepository.findByRestaurantId(1L)).thenReturn(listOf(dishA))
        Mockito.`when`(orderRepository.findLatestActiveOrderByClientUser("user", 1L, listOf(OrderStatus.PAID, OrderStatus.RETURNED)))
            .thenReturn(Optional.of(existingOrder))

        val mockWebClient = mockWebClientCall(Mono.empty())
        ReflectionTestUtils.setField(orderService, "clientPreferencesWebClient", mockWebClient)

        Mockito.`when`(orderRepository.save(Mockito.any(Order::class.java))).thenAnswer { invocation -> invocation.getArgument<Order>(0) }

        val request = OrderRequest(restaurantId = 1L, items = listOf(OrderItemRequest(dishId = 3L, quantity = 1)))

        val response = orderService.createOrder(1L, request, "user")

        assertEquals(2, response.items.size)
        assertTrue(response.items.any { it.dishId == 3L && it.quantity == 1 })
        assertTrue(response.items.any { it.dishId == 99L && it.quantity == 1 })
    }

    @Test
    fun `createOrder should skip authorization header when request has none`() {
        val mockServletRequest = MockHttpServletRequest()
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(mockServletRequest))

        try {
            val restaurant = createRestaurant(1L)
            val dish = createDish(3L, "Pizza", BigDecimal("8.00"), true, restaurant)

            Mockito.`when`(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant))
            Mockito.`when`(dishRepository.findByRestaurantId(1L)).thenReturn(listOf(dish))
            Mockito.`when`(orderRepository.findLatestActiveOrderByClientUser("user", 1L, listOf(OrderStatus.PAID, OrderStatus.RETURNED))).thenReturn(Optional.empty())
            Mockito.`when`(orderRepository.save(Mockito.any(Order::class.java))).thenAnswer { invocation -> invocation.getArgument<Order>(0) }

            val mockWebClient = mockWebClientCall(Mono.empty())
            ReflectionTestUtils.setField(orderService, "clientPreferencesWebClient", mockWebClient)

            val request = OrderRequest(restaurantId = 1L, items = listOf(OrderItemRequest(dishId = 3L, quantity = 1)))
            val response = orderService.createOrder(1L, request, "user")

            assertNotNull(response)
            Mockito.verify(mockWebClient).get()
        } finally {
            RequestContextHolder.resetRequestAttributes()
        }
    }

    @Test
    fun `getOrderByIdForOwnershipCheck should return order when found`() {
        val restaurant = createRestaurant(1L)
        val order = createOrder(30L, restaurant, "user", OrderStatus.PENDING)
        Mockito.`when`(orderRepository.findByRestaurantIdAndId(1L, 30L)).thenReturn(Optional.of(order))

        val result = orderService.getOrderByIdForOwnershipCheck(1L, 30L)

        assertEquals(30L, result.id)
    }

    @Test
    fun `getOrderByIdForOwnershipCheck should throw when order not found`() {
        Mockito.`when`(orderRepository.findByRestaurantIdAndId(1L, 31L)).thenReturn(Optional.empty())

        assertThrows(ResourceNotFoundException::class.java) {
            orderService.getOrderByIdForOwnershipCheck(1L, 31L)
        }
    }

    @Test
    fun `validateOwnership should not throw when username matches order owner`() {
        val restaurant = createRestaurant(1L)
        val order = createOrder(32L, restaurant, "user", OrderStatus.PENDING)

        orderService.validateOwnership(order, "user")
    }

    @Test
    fun `validateOwnership should throw when username does not match order owner`() {
        val restaurant = createRestaurant(1L)
        val order = createOrder(33L, restaurant, "user", OrderStatus.PENDING)

        assertThrows(ForbiddenAccessException::class.java) {
            orderService.validateOwnership(order, "otherUser")
        }
    }

    @Test
    fun `updateOrderStatus should save new order status`() {
        val restaurant = createRestaurant(1L)
        val order = createOrder(11L, restaurant, null, OrderStatus.PENDING)
        Mockito.`when`(orderRepository.findByRestaurantIdAndId(1L, 11L)).thenReturn(Optional.of(order))
        Mockito.`when`(orderRepository.save(Mockito.any(Order::class.java))).thenAnswer { invocation -> invocation.getArgument<Order>(0) }

        val response = orderService.updateOrderStatus(1L, 11L, UpdateOrderStatusRequest(status = "DELIVERED"))

        assertEquals(OrderStatus.DELIVERED, response.status)
    }

    @Test
    fun `updateOrderStatus should throw when order not found`() {
        Mockito.`when`(orderRepository.findByRestaurantIdAndId(1L, 99L)).thenReturn(Optional.empty())

        assertThrows(ResourceNotFoundException::class.java) {
            orderService.updateOrderStatus(1L, 99L, UpdateOrderStatusRequest(status = "DELIVERED"))
        }
    }

    @Test
    fun `updateOrderStatus should throw when status is invalid`() {
        val restaurant = createRestaurant(1L)
        val order = createOrder(11L, restaurant, null, OrderStatus.PENDING)
        Mockito.`when`(orderRepository.findByRestaurantIdAndId(1L, 11L)).thenReturn(Optional.of(order))

        assertThrows(InvalidOrderStatusException::class.java) {
            orderService.updateOrderStatus(1L, 11L, UpdateOrderStatusRequest(status = "INVALID_STATUS"))
        }
    }

    @Test
    fun `deleteOrder should remove order when waiter deletes`() {
        val restaurant = createRestaurant(1L)
        val order = createOrder(12L, restaurant, "user", OrderStatus.PENDING)
        Mockito.`when`(orderRepository.findByRestaurantIdAndId(1L, 12L)).thenReturn(Optional.of(order))

        orderService.deleteOrder(1L, 12L, username = null, isWaiter = true)

        Mockito.verify(orderRepository).delete(order)
    }

    @Test
    fun `deleteOrder should remove order when client owns pending order`() {
        val restaurant = createRestaurant(1L)
        val order = createOrder(13L, restaurant, "user", OrderStatus.PENDING)
        Mockito.`when`(orderRepository.findByRestaurantIdAndId(1L, 13L)).thenReturn(Optional.of(order))

        orderService.deleteOrder(1L, 13L, username = "user", isWaiter = false)

        Mockito.verify(orderRepository).delete(order)
    }

    @Test
    fun `deleteOrder should throw when client does not own order`() {
        val restaurant = createRestaurant(1L)
        val order = createOrder(19L, restaurant, "someoneElse", OrderStatus.PENDING)
        Mockito.`when`(orderRepository.findByRestaurantIdAndId(1L, 19L)).thenReturn(Optional.of(order))

        assertThrows(ForbiddenAccessException::class.java) {
            orderService.deleteOrder(1L, 19L, username = "user", isWaiter = false)
        }
    }

    @Test
    fun `deleteOrder should throw when unauthenticated client attempts delete`() {
        val restaurant = createRestaurant(1L)
        val order = createOrder(17L, restaurant, "user", OrderStatus.PENDING)
        Mockito.`when`(orderRepository.findByRestaurantIdAndId(1L, 17L)).thenReturn(Optional.of(order))

        assertThrows(ForbiddenAccessException::class.java) {
            orderService.deleteOrder(1L, 17L, username = null, isWaiter = false)
        }
    }

    @Test
    fun `deleteOrder should throw when order is not pending`() {
        val restaurant = createRestaurant(1L)
        val order = createOrder(18L, restaurant, "user", OrderStatus.PAID)
        Mockito.`when`(orderRepository.findByRestaurantIdAndId(1L, 18L)).thenReturn(Optional.of(order))

        assertThrows(IllegalStateException::class.java) {
            orderService.deleteOrder(1L, 18L, username = "user", isWaiter = false)
        }
    }

    @Test
    fun `getOrdersForCurrentClient should return orders for client`() {
        val restaurant = createRestaurant(1L)
        val order = createOrder(14L, restaurant, "user", OrderStatus.PENDING)
        Mockito.`when`(orderRepository.findByRestaurantIdAndClientUser(1L, "user")).thenReturn(listOf(order))

        val result = orderService.getOrdersForCurrentClient(1L, "user")

        assertEquals(1, result.size)
        assertEquals(14L, result[0].id)
    }

    @Test
    fun `getOrdersForWaiter should return waiter orders in descending order`() {
        val restaurant = createRestaurant(1L)
        val order = createOrder(15L, restaurant, "user", OrderStatus.PENDING)
        Mockito.`when`(orderRepository.findByRestaurantIdOrderByCreatedAtDesc(1L)).thenReturn(listOf(order))

        val result = orderService.getOrdersForWaiter(1L)

        assertEquals(1, result.size)
    }

    @Test
    fun `getKitchenOrders should return orders with kitchen statuses`() {
        val restaurant = createRestaurant(1L)
        val order = createOrder(16L, restaurant, "user", OrderStatus.IN_PREPARATION)
        Mockito.`when`(orderRepository.findByRestaurantIdAndStatusInOrderByCreatedAtAsc(1L, listOf(OrderStatus.PENDING, OrderStatus.IN_PREPARATION))).thenReturn(listOf(order))

        val result = orderService.getKitchenOrders(1L)

        assertEquals(1, result.size)
        assertEquals(OrderStatus.IN_PREPARATION, result[0].status)
    }
}