package com.restaurant.restaurantservice.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    var restaurant: Restaurant,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    var table: TableEntity? = null,

    @Column(length = 60)
    var clientUser: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: OrderStatus = OrderStatus.PENDING,

    @Column(nullable = false, precision = 10, scale = 2)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var items: MutableList<OrderItem> = mutableListOf()
) {
    fun addItem(item: OrderItem) {
        items.add(item)
        item.order = this
    }

    fun removeItem(item: OrderItem) {
        items.remove(item)
        item.order = null
    }
}

enum class OrderStatus {
    PENDING,
    IN_PREPARATION,
    READY_FOR_DELIVERY,
    DELIVERED,
    RETURNED,
    PAID;

    companion object {
        fun fromInput(rawStatus: String): OrderStatus {
            return when (
                rawStatus
                    .trim()
                    .uppercase()
                    .replace('-', '_')
                    .replace(' ', '_')
            ) {
                "PENDING", "NO_PREPARADA" -> PENDING
                "IN_PREPARATION", "PREPARANDO", "EN_PREPARACION" -> IN_PREPARATION
                "READY_FOR_DELIVERY", "READY", "MOSTRADOR" -> READY_FOR_DELIVERY
                "DELIVERED", "ENTREGADO", "SERVIDO" -> DELIVERED
                "RETURNED", "RETURN", "DEVUELTO" -> RETURNED
                "PAID", "PAGADO" -> PAID
                else -> throw IllegalArgumentException("Unsupported order status: $rawStatus")
            }
        }
    }
}
