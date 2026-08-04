package com.restaurant.restaurantservice.service

import com.restaurant.restaurantservice.dto.FranchiseRequest
import com.restaurant.restaurantservice.dto.FranchiseResponse
import com.restaurant.restaurantservice.entity.Franchise
import com.restaurant.restaurantservice.exception.DuplicateResourceException
import com.restaurant.restaurantservice.exception.ResourceNotFoundException
import com.restaurant.restaurantservice.mapper.toEntity
import com.restaurant.restaurantservice.mapper.toResponse
import com.restaurant.restaurantservice.repository.FranchiseRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface FranchiseService {
    fun getAllFranchises(): List<FranchiseResponse>
    fun createFranchise(request: FranchiseRequest): FranchiseResponse
    fun getFranchiseById(id: Long): FranchiseResponse
}

@Service
class FranchiseServiceImpl(
    private val franchiseRepository: FranchiseRepository
) : FranchiseService {

    @Transactional(readOnly = true)
    override fun getAllFranchises(): List<FranchiseResponse> =
        franchiseRepository.findAll().map(Franchise::toResponse)

    @Transactional
    override fun createFranchise(request: FranchiseRequest): FranchiseResponse {
        if (franchiseRepository.existsByNameIgnoreCase(request.name.trim())) {
            throw DuplicateResourceException("A franchise with the name '${request.name.trim()}' already exists")
        }

        val franchise = request.toEntity()
        return franchiseRepository.save(franchise).toResponse()
    }

    @Transactional(readOnly = true)
    override fun getFranchiseById(id: Long): FranchiseResponse {
        val franchise = franchiseRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Franchise not found: $id") }
        return franchise.toResponse()
    }
}
