package com.a3.soundprofiles.core.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.a3.soundprofiles.core.data.SoundProfile

@Dao
interface SoundProfileDao {
  @Query("SELECT * FROM sound_profiles") suspend fun getAll(): List<SoundProfile>

  @Query("SELECT * FROM sound_profiles WHERE id = :id") suspend fun getById(id: Int): SoundProfile

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(soundProfile: SoundProfile)

  @Query("DELETE FROM sound_profiles WHERE id = :id") suspend fun deleteById(id: Int)

  @Query("DELETE FROM sound_profiles") suspend fun deleteAll()

  @Insert suspend fun insertAll(vararg soundProfiles: SoundProfile)

  @Query("SELECT * FROM sound_profiles WHERE title LIKE :title")
  suspend fun findByTitle(title: String): SoundProfile

  @Query("SELECT * FROM sound_profiles WHERE description LIKE :description")
  suspend fun findByDescription(description: String): SoundProfile

  @Update suspend fun update(soundProfile: SoundProfile)
}
