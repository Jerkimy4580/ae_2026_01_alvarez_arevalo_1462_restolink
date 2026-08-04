package com.restaurant.clientpreferencesservice.entity

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
        fun fromInput(rawValue: String): Allergen? {
            val cleanValue = rawValue.trim().uppercase()
            
            // Mapeo en español para evitar fallos si el frontend/postman envía términos en español
            val spanishMap = mapOf(
                "CACAHUETES" to PEANUTS,
                "CACAHUETE" to PEANUTS,
                "MANI" to PEANUTS,
                "LACTOSA" to LACTOSE,
                "FRUTOS SECOS" to NUTS,
                "NUECES" to NUTS,
                "MARISCOS" to SHELLFISH,
                "MARISCO" to SHELLFISH,
                "HUEVOS" to EGGS,
                "HUEVO" to EGGS,
                "PESCADO" to FISH,
                "SOJA" to SOY,
                "SESAMO" to SESAME
            )

            return spanishMap[cleanValue] 
                ?: entries.firstOrNull { it.name == cleanValue }
        }
    }
}