package com.restaurant.restaurantservice.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "dishes")
class Dish(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal,

    @Column(nullable = false)
    var isAvailable: Boolean = true,

    @Column(nullable = false)
    var isDeleted: Boolean = false,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dish_allergens", joinColumns = [JoinColumn(name = "dish_id")])
    @Column(name = "allergen", nullable = false)
    @Enumerated(EnumType.STRING)
    var allergens: MutableSet<Allergen> = mutableSetOf(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    var restaurant: Restaurant
)
