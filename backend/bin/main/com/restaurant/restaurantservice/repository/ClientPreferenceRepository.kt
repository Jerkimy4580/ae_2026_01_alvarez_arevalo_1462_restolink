package com.restaurant.restaurantservice.repository

import com.restaurant.restaurantservice.entity.ClientPreference
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientPreferenceRepository : JpaRepository<ClientPreference, String>