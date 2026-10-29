package com.novatech.terratech.profile.application.usecase

import com.novatech.terratech.profile.domain.entity.FarmProfile
import com.novatech.terratech.profile.domain.repository.ProfileRepository
import com.novatech.terratech.profile.domain.valueobject.AlertThresholds
import com.novatech.terratech.profile.domain.valueobject.FarmDetails

class ProfileActions(private val repository: ProfileRepository) {
    fun observe(user: Int) = repository.observe(user)

    suspend fun refresh(user: Int) = repository.refresh(user)

    suspend fun save(
        user: Int,
        name: String,
        farm: String,
        phone: String,
        location: String,
        hectares: Double,
        moistureThreshold: Double? = null,
        tempThreshold: Double? = null,
    ) {
        val details = FarmDetails.of(name, farm, phone, location, hectares)
        repository.save(
            user,
            details.name.value,
            details.farm,
            details.phone,
            details.location,
            details.area.value,
            moistureThreshold,
            tempThreshold,
        )
    }

    suspend fun saveThresholds(
        user: Int,
        currentProfile: FarmProfile,
        moisture: Double,
        temperature: Double,
    ) {
        val thresholds = AlertThresholds.of(moisture, temperature)
        val details =
            FarmDetails.of(
                currentProfile.fullName,
                currentProfile.fundoName,
                currentProfile.contactPhone,
                currentProfile.location,
                (currentProfile.sizeM2 ?: 10000.0) / 10000.0,
            )
        repository.save(
            user,
            details.name.value,
            details.farm,
            details.phone,
            details.location,
            details.area.value,
            thresholds.moisture,
            thresholds.temperature,
        )
    }
}
