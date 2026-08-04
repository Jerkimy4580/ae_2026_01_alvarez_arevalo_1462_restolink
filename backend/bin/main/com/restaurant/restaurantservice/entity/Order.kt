package com.restaurant.restaurantservice.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
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

    @Column(nullable = false)
    var tableNumber: Int,

    @Column(length = 60)
    var clientUser: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: OrderStatus = OrderStatus.PENDING,

    @Column(nullable = false, precision = 10, scale = 2)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<OrderItem> = mutableListOf()
)

enum class OrderStatus {
    PENDING,
    DELIVERED,
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
                "PENDING", "A_ENTREGAR", "TO_DELIVER" -> PENDING
                "DELIVERED", "ENTREGADO" -> DELIVERED
                "PAID", "PAGADO" -> PAID
                else -> throw IllegalArgumentException("Unsupported order status: $rawStatus")
            }
        }
    }
}
