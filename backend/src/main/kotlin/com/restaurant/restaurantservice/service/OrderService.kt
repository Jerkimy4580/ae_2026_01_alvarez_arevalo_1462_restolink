package com.restaurant.restaurantservice.service

import com.restaurant.restaurantservice.dto.ClientPreferenceResponse
import com.restaurant.restaurantservice.dto.OrderRequest
import com.restaurant.restaurantservice.dto.OrderResponse
import com.restaurant.restaurantservice.dto.UpdateOrderStatusRequest
import com.restaurant.restaurantservice.entity.Order
import com.restaurant.restaurantservice.entity.OrderItem
import com.restaurant.restaurantservice.entity.OrderStatus
import com.restaurant.restaurantservice.exception.AllergenConflictException
import com.restaurant.restaurantservice.exception.ForbiddenAccessException
import com.restaurant.restaurantservice.exception.InvalidOrderStatusException
import com.restaurant.restaurantservice.exception.ResourceNotFoundException
import com.restaurant.restaurantservice.mapper.toResponse
import com.restaurant.restaurantservice.repository.DishRepository
import com.restaurant.restaurantservice.repository.OrderRepository
import com.restaurant.restaurantservice.repository.RestaurantRepository
import com.restaurant.restaurantservice.repository.TableRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.math.BigDecimal

interface OrderService {
    fun createOrder(restaurantId: Long, request: OrderRequest, username: String?): OrderResponse
    fun updateOrderStatus(restaurantId: Long, id: Long, request: UpdateOrderStatusRequest): OrderResponse
    fun deleteOrder(restaurantId: Long, id: Long, username: String?, isWaiter: Boolean): Unit
    fun getOrdersForCurrentClient(restaurantId: Long, username: String): List<OrderResponse>
    fun getOrdersForWaiter(restaurantId: Long): List<OrderResponse>
    fun getKitchenOrders(restaurantId: Long): List<OrderResponse>
}

@Service
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val dishRepository: DishRepository,
    private val restaurantRepository: RestaurantRepository,
    private val tableRepository: TableRepository,
    @Value("\${services.client-preferences.url:http://client-preferences-service:8082}") 
    private val clientPreferencesBaseUrl: String
) : OrderService {

    private val clientPreferencesWebClient: WebClient = WebClient.builder()
        .baseUrl(clientPreferencesBaseUrl)
        .build()

    @Transactional
    override fun createOrder(restaurantId: Long, request: OrderRequest, username: String?): OrderResponse {
        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { ResourceNotFoundException("Restaurant not found: $restaurantId") }

        val table = request.tableId?.let { tableId ->
            tableRepository.findById(tableId).orElseThrow { ResourceNotFoundException("Table not found: $tableId") }
                .also { if (it.restaurant.id != restaurant.id) throw IllegalArgumentException("Table does not belong to this restaurant") }
        }

        val dishesById = dishRepository.findByRestaurantId(restaurantId).associateBy { it.id }

        val restaurantIdValue = restaurant.id ?: throw IllegalStateException("Restaurant has no persisted id")

        val order = if (!username.isNullOrBlank()) {
            orderRepository.findLatestActiveOrderByClientUser(username, restaurantIdValue, listOf(OrderStatus.PAID, OrderStatus.RETURNED))
                .orElseGet {
                    Order(
                        restaurant = restaurant,
                        table = table,
                        clientUser = username,
                        status = OrderStatus.PENDING,
                        totalAmount = BigDecimal.ZERO
                    )
                }
        } else {
            Order(
                restaurant = restaurant,
                table = table,
                clientUser = username,
                status = OrderStatus.PENDING,
                totalAmount = BigDecimal.ZERO
            )
        }

        val orderItems = request.items.map { item ->
            val dish = dishesById[item.dishId] ?: throw ResourceNotFoundException("Dish not found: ${item.dishId}")
            if (!dish.isAvailable) {
                throw IllegalArgumentException("Dish is not available: ${dish.name}")
            }
            OrderItem(
                dishId = dish.id ?: -1,
                dishName = dish.name,
                unitPrice = dish.price,
                quantity = item.quantity
            )
        }

        if (!username.isNullOrBlank()) {
            val preferences = fetchClientPreferences(username)
            if (preferences != null) {
                val conflicts = orderItems.flatMap { item ->
                    val dishAllergens = dishRepository.findById(item.dishId).orElse(null)?.allergens.orEmpty()
                    dishAllergens.filter { allergen -> preferences.allergens.any { it.equals(allergen.name, ignoreCase = true) } }
                        .map { allergen -> Pair(item.dishName, allergen.name) }
                }

                if (conflicts.isNotEmpty()) {
                    val conflict = conflicts.first()
                    throw AllergenConflictException("This dish ${conflict.first} contains ${conflict.second}, to which you are allergic.")
                }
            }
        }

        orderItems.forEach { incomingItem ->
            val existingItem = order.items.find { it.dishId == incomingItem.dishId }
            if (existingItem != null) {
                existingItem.quantity += incomingItem.quantity
            } else {
                order.addItem(incomingItem)
            }
        }
        order.totalAmount = order.items.sumOf { it.unitPrice.multiply(BigDecimal(it.quantity)) }

        return orderRepository.save(order).toResponse()
    }

    @Transactional
    override fun updateOrderStatus(restaurantId: Long, id: Long, request: UpdateOrderStatusRequest): OrderResponse {
        val order = orderRepository.findByRestaurantIdAndId(restaurantId, id)
            .orElseThrow { ResourceNotFoundException("Order not found: $id") }
        val normalizedStatus = runCatching { OrderStatus.fromInput(request.status) }
            .getOrElse { throw InvalidOrderStatusException(it.message ?: "Invalid order status") }
        order.status = normalizedStatus
        return orderRepository.save(order).toResponse()
    }

    @Transactional
    override fun deleteOrder(restaurantId: Long, id: Long, username: String?, isWaiter: Boolean) {
        val order = orderRepository.findByRestaurantIdAndId(restaurantId, id)
            .orElseThrow { ResourceNotFoundException("Order not found: $id") }

        if (isWaiter) {
            orderRepository.delete(order)
            return
        }

        if (username.isNullOrBlank()) {
            throw ForbiddenAccessException("You must be authenticated to delete this order")
        }

        if (order.clientUser != username) {
            throw ForbiddenAccessException("You do not have access to this order")
        }

        if (order.status != OrderStatus.PENDING) {
            throw IllegalStateException("This order can no longer be deleted. Please contact the waiter.")
        }

        orderRepository.delete(order)
    }

    @Transactional(readOnly = true)
    override fun getOrdersForCurrentClient(restaurantId: Long, username: String): List<OrderResponse> {
        val orders = orderRepository.findByRestaurantIdAndClientUser(restaurantId, username)
        return orders.map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    override fun getOrdersForWaiter(restaurantId: Long): List<OrderResponse> {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    override fun getKitchenOrders(restaurantId: Long): List<OrderResponse> {
        val kitchenStatuses = listOf(OrderStatus.PENDING, OrderStatus.IN_PREPARATION)
        return orderRepository.findByRestaurantIdAndStatusInOrderByCreatedAtAsc(restaurantId, kitchenStatuses)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getOrderByIdForOwnershipCheck(restaurantId: Long, id: Long): Order {
        return orderRepository.findByRestaurantIdAndId(restaurantId, id)
            .orElseThrow { ResourceNotFoundException("Order not found: $id") }
    }

    fun validateOwnership(order: Order, username: String) {
        if (order.clientUser != username) {
            throw ForbiddenAccessException("You do not have access to this order")
        }
    }

    private fun fetchClientPreferences(username: String): ClientPreferenceResponse? {
        // Obtenemos el header Authorization entrante del Request actual
        val bearerToken = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)
            ?.request?.getHeader(HttpHeaders.AUTHORIZATION)

        return try {
            clientPreferencesWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder.path("/api/v1/preferences/{username}").build(username)
                }
                .headers { headers ->
                    if (!bearerToken.isNullOrBlank()) {
                        headers.set(HttpHeaders.AUTHORIZATION, bearerToken)
                    }
                }
                .retrieve()
                .bodyToMono(ClientPreferenceResponse::class.java)
                .block()
        } catch (ex: WebClientResponseException.NotFound) {
            null
        } catch (ex: WebClientResponseException.Unauthorized) {
            throw ForbiddenAccessException("You are not authorized to view this customer's preferences")
        } catch (ex: Exception) {
            // Manejador opcional por si el servicio de preferencias está caído o inaccesible
            null
        }
    }
}