package com.restaurant.restaurantservice.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "franchises")
class Franchise(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    @OneToMany(mappedBy = "franchise", cascade = [CascadeType.ALL], orphanRemoval = true)
    var restaurants: MutableList<Restaurant> = mutableListOf()
)
