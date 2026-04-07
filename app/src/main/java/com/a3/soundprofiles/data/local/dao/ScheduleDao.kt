package com.a3.soundprofiles.data.local.dao

import androidx.room.*
import com.a3.soundprofiles.data.local.entities.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM profile_schedules")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM profile_schedules WHERE scheduleId = :id")
    suspend fun getScheduleById(id: Int): ScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)
}
