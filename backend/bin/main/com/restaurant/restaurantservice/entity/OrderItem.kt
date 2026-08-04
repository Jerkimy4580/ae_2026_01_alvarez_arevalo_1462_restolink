package com.restaurant.restaurantservice.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "order_items")
class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order,

    @ManyToOne(optional = false)
    @JoinColumn(name = "dish_id", nullable = false)
    var dish: Dish,

    @Column(nullable = false)
    var quantity: Int
)
