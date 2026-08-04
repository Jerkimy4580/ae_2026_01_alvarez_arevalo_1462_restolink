package com.restaurant.clientpreferencesservice.repository

import com.restaurant.clientpreferencesservice.entity.ClientPreference
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ClientPreferenceRepository : JpaRepository<ClientPreference, Long> {
    fun findByUsername(username: String): Optional<ClientPreference>
}
