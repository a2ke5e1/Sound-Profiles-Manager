package com.a3.soundprofiles.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "profile_schedules",
    foreignKeys = [
        ForeignKey(
            entity = SoundProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId"])]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val scheduleId: Int = 0,
    val profileId: Int,
    val fallbackProfileId: Int?,

    val startTime: Date, // Stores full date/time
    val endTime: Date,   // Stores full date/time

    val daysOfWeek: List<DAY>,
    val isEnabled: Boolean = true,
    val isActive: Boolean = false,
    val repeatEveryday: Boolean,

    val name: String,
    val description: String = "",
    val iconName: String? = null,
)


enum class DAY {
    SUNDAY,
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY
}