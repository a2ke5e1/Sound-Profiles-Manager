package com.a3.soundprofiles.data.repository

import com.a3.soundprofiles.data.local.dao.ScheduleDao
import com.a3.soundprofiles.data.local.entities.ScheduleEntity
import kotlinx.coroutines.flow.Flow

class ScheduleRepository(private val scheduleDao: ScheduleDao) {
    fun getAllSchedules(): Flow<List<ScheduleEntity>> = scheduleDao.getAllSchedules()

    suspend fun getScheduleById(id: Int): ScheduleEntity? = scheduleDao.getScheduleById(id)

    suspend fun insertSchedule(schedule: ScheduleEntity): Long = scheduleDao.insertSchedule(schedule)

    suspend fun updateSchedule(schedule: ScheduleEntity) = scheduleDao.updateSchedule(schedule)

    suspend fun deleteSchedule(schedule: ScheduleEntity) = scheduleDao.deleteSchedule(schedule)
}
