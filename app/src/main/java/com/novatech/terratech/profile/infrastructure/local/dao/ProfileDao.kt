package com.novatech.terratech.profile.infrastructure.local.dao

import androidx.room.*
import com.novatech.terratech.profile.infrastructure.local.entity.ProfileRow
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE userId=:user") fun profile(user: Int): Flow<ProfileRow?>

    @Upsert suspend fun putProfile(row: ProfileRow)

    @Query("DELETE FROM profiles") suspend fun clearProfiles()
}
