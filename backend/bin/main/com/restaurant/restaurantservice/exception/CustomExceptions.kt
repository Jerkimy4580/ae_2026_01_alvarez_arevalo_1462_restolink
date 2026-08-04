package com.restaurant.restaurantservice.exception

class ResourceNotFoundException(message: String) : RuntimeException(message)

class ForbiddenAccessException(message: String) : RuntimeException(message)

class InvalidOrderStatusException(message: String) : RuntimeException(message)
