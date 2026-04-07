package com.a3.soundprofiles.data.repository

import com.a3.soundprofiles.data.local.dao.SoundProfileDao
import com.a3.soundprofiles.data.local.entities.SoundProfileEntity
import kotlinx.coroutines.flow.Flow

class SoundProfileRepository(private val soundProfileDao: SoundProfileDao) {
    fun getAllProfiles(): Flow<List<SoundProfileEntity>> = soundProfileDao.getAllProfiles()

    suspend fun getProfileById(id: Int): SoundProfileEntity? = soundProfileDao.getProfileById(id)

    suspend fun insertProfile(profile: SoundProfileEntity) = soundProfileDao.insertProfile(profile)

    suspend fun updateProfile(profile: SoundProfileEntity) = soundProfileDao.updateProfile(profile)

    suspend fun deleteProfile(profile: SoundProfileEntity) = soundProfileDao.deleteProfile(profile)

    suspend fun isProfileUsedInActiveSchedule(profileId: Int): Boolean =
        soundProfileDao.isProfileUsedInActiveSchedule(profileId)

    suspend fun isProfileUsedInEnabledSchedule(profileId: Int): Boolean =
        soundProfileDao.isProfileUsedInEnabledSchedule(profileId)

    suspend fun getActiveSchedulesForProfile(profileId: Int) =
        soundProfileDao.getActiveSchedulesForProfile(profileId)

    suspend fun getEnabledSchedulesForProfile(profileId: Int) =
        soundProfileDao.getEnabledSchedulesForProfile(profileId)
}
