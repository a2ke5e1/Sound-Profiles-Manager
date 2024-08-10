package com.a3.soundprofiles.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.a3.soundprofiles.core.dao.SoundProfileDao
import com.a3.soundprofiles.core.data.Converters
import com.a3.soundprofiles.core.data.SoundProfile
import kotlinx.coroutines.Dispatchers

@Database(entities = [SoundProfile::class], version = 1)
@TypeConverters(Converters::class)
abstract class SoundProfileDB : RoomDatabase() {
  abstract fun soundProfileDao(): SoundProfileDao

  companion object {
    const val DATABASE_NAME = "sound_profile_db"
    @Volatile private var INSTANCE: SoundProfileDB? = null

    fun getInstance(context: Context): SoundProfileDB {
      val tempInstance = INSTANCE
      if (tempInstance != null) {
        return tempInstance
      }
      synchronized(this) {
        val instance =
            Room.databaseBuilder(
                    context.applicationContext, SoundProfileDB::class.java, DATABASE_NAME)
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
        INSTANCE = instance
        return instance
      }
    }
  }
}
