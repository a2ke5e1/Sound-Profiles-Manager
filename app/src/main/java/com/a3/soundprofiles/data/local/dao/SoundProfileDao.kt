package com.a3.soundprofiles.data.local.dao

import androidx.room.*
import com.a3.soundprofiles.data.local.entities.ScheduleEntity
import com.a3.soundprofiles.data.local.entities.SoundProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SoundProfileDao {
    @Query("SELECT * FROM sound_profiles")
    fun getAllProfiles(): Flow<List<SoundProfileEntity>>

    @Query("SELECT * FROM sound_profiles WHERE id = :id")
    suspend fun getProfileById(id: Int): SoundProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: SoundProfileEntity)

    @Update
    suspend fun updateProfile(profile: SoundProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: SoundProfileEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM profile_schedules WHERE (profileId = :profileId OR fallbackProfileId = :profileId) AND isActive = 1)")
    suspend fun isProfileUsedInActiveSchedule(profileId: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM profile_schedules WHERE (profileId = :profileId OR fallbackProfileId = :profileId) AND isEnabled = 1)")
    suspend fun isProfileUsedInEnabledSchedule(profileId: Int): Boolean

    @Query("SELECT * FROM profile_schedules WHERE (profileId = :profileId OR fallbackProfileId = :profileId) AND isActive = 1")
    suspend fun getActiveSchedulesForProfile(profileId: Int): List<ScheduleEntity>

    @Query("SELECT * FROM profile_schedules WHERE (profileId = :profileId OR fallbackProfileId = :profileId) AND isEnabled = 1")
    suspend fun getEnabledSchedulesForProfile(profileId: Int): List<ScheduleEntity>
}
