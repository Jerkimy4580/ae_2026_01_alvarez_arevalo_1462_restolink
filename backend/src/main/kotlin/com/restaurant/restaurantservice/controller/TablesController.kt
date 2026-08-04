package com.restaurant.restaurantservice.controller

import com.restaurant.restaurantservice.dto.TableRequest
import com.restaurant.restaurantservice.dto.TableResponse
import com.restaurant.restaurantservice.service.TableService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/tables")
class TablesController(
    private val tableService: TableService
) {

    @GetMapping
    fun getTables(@PathVariable restaurantId: Long): List<TableResponse> = tableService.getTablesByRestaurant(restaurantId)

    @PostMapping
    @PreAuthorize("hasRole('CHEF')")
    @ResponseStatus(HttpStatus.CREATED)
    fun createTable(@PathVariable restaurantId: Long, @RequestBody request: TableRequest): TableResponse =
        tableService.createTable(restaurantId, request)

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CHEF')")
    fun updateTable(
        @PathVariable restaurantId: Long,
        @PathVariable id: Long,
        @RequestBody request: TableRequest
    ): TableResponse = tableService.updateTable(restaurantId, id, request)
}
