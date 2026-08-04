package com.restaurant.clientpreferencesservice.service

import com.restaurant.clientpreferencesservice.dto.ClientPreferenceRequest
import com.restaurant.clientpreferencesservice.dto.ClientPreferenceResponse
import com.restaurant.clientpreferencesservice.entity.Allergen
import com.restaurant.clientpreferencesservice.entity.ClientPreference
import com.restaurant.clientpreferencesservice.repository.ClientPreferenceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface ClientPreferenceService {
    fun saveOrUpdatePreferences(username: String, request: ClientPreferenceRequest): ClientPreferenceResponse
    fun getPreferencesByUsername(username: String): ClientPreferenceResponse?
}

@Service
class ClientPreferenceServiceImpl(
    private val clientPreferenceRepository: ClientPreferenceRepository
) : ClientPreferenceService {

    @Transactional
    override fun saveOrUpdatePreferences(username: String, request: ClientPreferenceRequest): ClientPreferenceResponse {
        val preference = clientPreferenceRepository.findByUsername(username)
            .orElseGet { ClientPreference(username = username) }

        preference.allergens.clear()
        preference.allergens.addAll(request.allergens.map { rawValue ->
            Allergen.fromInput(rawValue) ?: throw IllegalArgumentException("Unsupported allergen: $rawValue")
        })

        val saved = clientPreferenceRepository.save(preference)
        return saved.toResponse()
    }

    @Transactional(readOnly = true)
    override fun getPreferencesByUsername(username: String): ClientPreferenceResponse? {
        return clientPreferenceRepository.findByUsername(username).orElse(null)?.toResponse()
    }
}

private fun ClientPreference.toResponse(): ClientPreferenceResponse = ClientPreferenceResponse(
    id = id,
    username = username,
    allergens = allergens.map { it.name }
)