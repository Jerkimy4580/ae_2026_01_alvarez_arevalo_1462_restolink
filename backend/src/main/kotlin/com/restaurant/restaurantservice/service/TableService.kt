package com.restaurant.restaurantservice.service

import com.restaurant.restaurantservice.dto.TableRequest
import com.restaurant.restaurantservice.dto.TableResponse
import com.restaurant.restaurantservice.entity.TableEntity
import com.restaurant.restaurantservice.exception.DuplicateResourceException
import com.restaurant.restaurantservice.exception.ResourceNotFoundException
import com.restaurant.restaurantservice.mapper.toEntity
import com.restaurant.restaurantservice.mapper.toResponse
import com.restaurant.restaurantservice.repository.RestaurantRepository
import com.restaurant.restaurantservice.repository.TableRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface TableService {
    fun getTablesByRestaurant(restaurantId: Long): List<TableResponse>
    fun createTable(restaurantId: Long, request: TableRequest): TableResponse
    fun updateTable(restaurantId: Long, id: Long, request: TableRequest): TableResponse
}

@Service
class TableServiceImpl(
    private val tableRepository: TableRepository,
    private val restaurantRepository: RestaurantRepository
) : TableService {

    @Transactional(readOnly = true)
    override fun getTablesByRestaurant(restaurantId: Long): List<TableResponse> {
        return tableRepository.findByRestaurantId(restaurantId).map(TableEntity::toResponse)
    }

    @Transactional
    override fun createTable(restaurantId: Long, request: TableRequest): TableResponse {
        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { ResourceNotFoundException("Restaurant not found: $restaurantId") }

        val table = request.toEntity(restaurant)
        return tableRepository.save(table).toResponse()
    }

    @Transactional
    override fun updateTable(restaurantId: Long, id: Long, request: TableRequest): TableResponse {
        val restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow { ResourceNotFoundException("Restaurant not found: $restaurantId") }

        val table = tableRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Table not found: $id") }

        if (table.restaurant.id != restaurant.id) {
            throw ResourceNotFoundException("Table not found in restaurant: $restaurantId")
        }

        table.reference = request.reference
        table.capacity = request.capacity
        return tableRepository.save(table).toResponse()
    }
}
