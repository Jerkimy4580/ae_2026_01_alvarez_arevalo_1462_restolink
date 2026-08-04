package com.restaurant.restaurantservice.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "client_preferences")
class ClientPreference(
    @Id
    @Column(nullable = false, length = 60)
    var username: String,

    @Column(nullable = false, length = 300)
    var allergies: String = "",

    @Column(nullable = false, length = 300)
    var favoriteIngredients: String = "",

    @Column(nullable = false, length = 300)
    var dislikedIngredients: String = "",

    @Column(nullable = false, length = 500)
    var notes: String = "",

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
)