package com.a3.soundprofiles.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.a3.soundprofiles.data.local.dao.ScheduleDao
import com.a3.soundprofiles.data.local.dao.SoundProfileDao
import com.a3.soundprofiles.data.local.entities.ScheduleEntity
import com.a3.soundprofiles.data.local.entities.SoundProfileEntity

@Database(
    entities = [SoundProfileEntity::class, ScheduleEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun soundProfileDao(): SoundProfileDao
    abstract fun scheduleDao(): ScheduleDao
}
