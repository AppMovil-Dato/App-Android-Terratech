package com.novatech.terratech.profile.infrastructure.implementation

import com.novatech.terratech.core.domain.Failure
import com.novatech.terratech.core.infrastructure.local.TerraDatabase
import com.novatech.terratech.core.infrastructure.remote.apiCall
import com.novatech.terratech.profile.domain.repository.ProfileRepository
import com.novatech.terratech.profile.infrastructure.mapper.*
import com.novatech.terratech.profile.infrastructure.remote.ProfileApi
import com.novatech.terratech.profile.infrastructure.remote.dto.SaveProfileDto
import kotlinx.coroutines.flow.map

class ProfileRepositoryImpl(private val api: ProfileApi, private val db: TerraDatabase) :
    ProfileRepository {
    override fun observe(userId: Int) = db.dao().profile(userId).map { it?.domain() }

    override suspend fun refresh(userId: Int) {
        try {
            db.dao().putProfile(apiCall { api.profile() }.row(userId))
        } catch (e: Failure) {
            if (e.status != 404 || e.code != "PROFILE_NOT_FOUND") throw e
        }
    }

    override suspend fun save(
        userId: Int,
        name: String,
        farm: String,
        phone: String,
        location: String,
        sizeM2: Double,
    ) {
        db.dao()
            .putProfile(
                apiCall { api.saveProfile(SaveProfileDto(name, farm, phone, location, sizeM2)) }
                    .row(userId)
            )
    }
}
