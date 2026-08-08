package com.restaurant.restaurantservice.exception

class ResourceNotFoundException(message: String) : RuntimeException(message)

class ForbiddenAccessException(message: String) : RuntimeException(message)

class InvalidOrderStatusException(message: String) : RuntimeException(message)

class AllergenConflictException(message: String) : RuntimeException(message)

class DuplicateResourceException(message: String) : RuntimeException(message)

class InvalidOrderStateException(message: String) : RuntimeException(message)
