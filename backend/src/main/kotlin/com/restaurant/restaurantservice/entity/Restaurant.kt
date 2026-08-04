package com.restaurant.restaurantservice.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "restaurants")
class Restaurant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 150)
    var name: String,

    @Column(nullable = false, length = 250)
    var address: String,

    @Column(name = "chef_user_id")
    var chefUserId: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "franchise_id")
    var franchise: Franchise? = null,

    @OneToMany(mappedBy = "restaurant", cascade = [CascadeType.ALL], orphanRemoval = true)
    var tables: MutableList<TableEntity> = mutableListOf(),

    @OneToMany(mappedBy = "restaurant", cascade = [CascadeType.ALL], orphanRemoval = true)
    var dishes: MutableList<Dish> = mutableListOf(),

    @OneToMany(mappedBy = "restaurant", cascade = [CascadeType.ALL], orphanRemoval = true)
    var waiters: MutableList<RestaurantWaiter> = mutableListOf()
)
