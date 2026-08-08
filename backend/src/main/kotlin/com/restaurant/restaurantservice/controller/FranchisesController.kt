package com.restaurant.restaurantservice.controller

import com.restaurant.restaurantservice.dto.FranchiseRequest
import com.restaurant.restaurantservice.dto.FranchiseResponse
import com.restaurant.restaurantservice.service.FranchiseService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/franchises")
class FranchisesController(
    private val franchiseService: FranchiseService
) {

    @GetMapping
    fun getFranchises(): List<FranchiseResponse> = franchiseService.getAllFranchises()

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun createFranchise(@RequestBody request: FranchiseRequest): FranchiseResponse = franchiseService.createFranchise(request)

    @GetMapping("/{id}")
    fun getFranchise(@PathVariable id: Long): FranchiseResponse = franchiseService.getFranchiseById(id)
}
