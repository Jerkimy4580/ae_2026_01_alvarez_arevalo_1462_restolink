package com.restaurant.clientpreferencesservice.entity

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
import jakarta.persistence.Table

@Entity
@Table(name = "client_preferences")
class ClientPreference(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true, length = 100)
    var username: String,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_preference_allergens", joinColumns = [JoinColumn(name = "client_preference_id")])
    @Column(name = "allergen", nullable = false)
    @Enumerated(EnumType.STRING)
    var allergens: MutableSet<Allergen> = mutableSetOf()
)

