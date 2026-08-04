package com.restaurant.restaurantservice.entity

enum class Allergen {
    GLUTEN,
    LACTOSE,
    NUTS,
    PEANUTS,
    SHELLFISH,
    EGGS,
    FISH,
    SOY,
    SESAME;

    companion object {
        fun fromInput(rawValue: String): Allergen? =
            entries.firstOrNull { it.name.equals(rawValue.trim(), ignoreCase = true) }
    }
}
