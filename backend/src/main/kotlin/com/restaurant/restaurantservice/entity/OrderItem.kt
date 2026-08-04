package com.restaurant.restaurantservice.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "order_items")
class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order? = null,

    @Column(nullable = false)
    var dishId: Long,

    @Column(nullable = false, length = 150)
    var dishName: String,

    @Column(nullable = false, precision = 10, scale = 2)
    var unitPrice: BigDecimal,

    @Column(nullable = false)
    var quantity: Int
)
