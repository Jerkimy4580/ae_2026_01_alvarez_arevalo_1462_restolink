package com.restaurant.restaurantservice.service

import com.restaurant.restaurantservice.dto.ClientPreferenceRequest
import com.restaurant.restaurantservice.dto.ClientPreferenceResponse
import com.restaurant.restaurantservice.exception.ResourceNotFoundException
import com.restaurant.restaurantservice.mapper.applyRequest
import com.restaurant.restaurantservice.mapper.toEntity
import com.restaurant.restaurantservice.mapper.toResponse
import com.restaurant.restaurantservice.repository.ClientPreferenceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

interface PreferenceService {
    fun createPreferences(username: String, request: ClientPreferenceRequest): ClientPreferenceResponse
    fun updatePreferences(username: String, request: ClientPreferenceRequest): ClientPreferenceResponse
    fun getMyPreferences(username: String): ClientPreferenceResponse
    fun getPreferencesForClient(username: String): ClientPreferenceResponse
}

@Service
class PreferenceServiceImpl(
    private val clientPreferenceRepository: ClientPreferenceRepository
) : PreferenceService {

    @Transactional
    override fun createPreferences(username: String, request: ClientPreferenceRequest): ClientPreferenceResponse {
        if (clientPreferenceRepository.existsById(username)) {
            throw IllegalArgumentException("Preferences already exist for user: $username")
        }

        val preference = request.toEntity(username).apply {
            updatedAt = Instant.now()
        }

        return clientPreferenceRepository.save(preference).toResponse()
    }

    @Transactional
    override fun updatePreferences(username: String, request: ClientPreferenceRequest): ClientPreferenceResponse {
        val preference = clientPreferenceRepository.findById(username)
            .orElseThrow { ResourceNotFoundException("Preferences not found for user: $username") }

        preference.applyRequest(request)
        preference.updatedAt = Instant.now()
        return clientPreferenceRepository.save(preference).toResponse()
    }

    @Transactional(readOnly = true)
    override fun getMyPreferences(username: String): ClientPreferenceResponse {
        return getPreferencesForClient(username)
    }

    @Transactional(readOnly = true)
    override fun getPreferencesForClient(username: String): ClientPreferenceResponse {
        return clientPreferenceRepository.findById(username)
            .orElseThrow { ResourceNotFoundException("Preferences not found for user: $username") }
            .toResponse()
    }
}