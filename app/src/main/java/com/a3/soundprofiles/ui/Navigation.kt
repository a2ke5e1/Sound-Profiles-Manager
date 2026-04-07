package com.a3.soundprofiles.ui

import kotlinx.serialization.Serializable

@Serializable
object SoundProfilesRoute

@Serializable
data class EditProfileRoute(
    val profileId: Int? = null,
    val name: String? = null,
    val iconName: String? = null
)

@Serializable
data class ProfileConfigRoute(
    val name: String,
    val iconName: String,
    val profileId: Int? = null
)

@Serializable
data class ScheduleConfigRoute(
    val scheduleId: Int? = null
)
