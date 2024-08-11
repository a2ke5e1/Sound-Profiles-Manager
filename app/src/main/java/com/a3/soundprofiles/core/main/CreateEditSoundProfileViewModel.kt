package com.a3.soundprofiles.core.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a3.soundprofiles.core.dao.SoundProfileDao
import com.a3.soundprofiles.core.data.DAY
import com.a3.soundprofiles.core.data.SoundProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CreateEditSoundProfileViewModel
@Inject
constructor(private val soundProfileDao: SoundProfileDao) : ViewModel() {

  private var _soundProfile =
      MutableLiveData(
          SoundProfile(
              id = 0,
              title = "",
              description = "",
              startTime = Date(),
              endTime = Date(),
              repeatEveryday = false,
              repeatDays = emptyList(),
              mediaVolume = 1f,
              notificationVolume = 1f,
              ringerVolume = 1f,
              callVolume = 1f,
              alarmVolume = 1f,
              isActive = false))

  val soundProfile
    get() = _soundProfile

  /**
   * Loads a sound profile from the database based on the provided sound profile ID. This method
   * should be called only when in edit mode to populate the UI with the existing sound profile
   * data.
   *
   * @param soundProfileId The ID of the sound profile to be loaded.
   */
  fun loadSoundProfile(soundProfileId: Int) {
    viewModelScope.launch(Dispatchers.IO) {
      val soundProfile = soundProfileDao.getById(soundProfileId)
      _soundProfile.postValue(soundProfile)
    }
  }

  /**
   * Saves the current sound profile to the database.
   *
   * This method checks if the sound profile already exists (i.e., has a non-zero ID). If it exists,
   * it updates the existing sound profile in the database. If it does not exist, it inserts a new
   * sound profile into the database.
   */
  fun saveSoundProfile() {
    viewModelScope.launch(Dispatchers.IO) {
      val soundProfile = soundProfile.value ?: return@launch
      if (soundProfile.id != 0) {
        soundProfileDao.update(soundProfile)
        return@launch
      }
      soundProfileDao.insert(soundProfile)
    }
  }

  fun setMediaVolume(volume: Float) {
    _soundProfile.value = _soundProfile.value?.copy(mediaVolume = volume)
  }

  fun setNotificationVolume(volume: Float) {
    _soundProfile.value = _soundProfile.value?.copy(notificationVolume = volume)
  }

  fun setRingerVolume(volume: Float) {
    _soundProfile.value = _soundProfile.value?.copy(ringerVolume = volume)
  }

  fun setCallVolume(volume: Float) {
    _soundProfile.value = _soundProfile.value?.copy(callVolume = volume)
  }

  fun setAlarmVolume(volume: Float) {
    _soundProfile.value = _soundProfile.value?.copy(alarmVolume = volume)
  }

  fun setTitle(title: String) {
    _soundProfile.value = _soundProfile.value?.copy(title = title)
  }

  fun setDescription(description: String) {
    _soundProfile.value = _soundProfile.value?.copy(description = description)
  }

  fun setRepeatEveryday(bool: Boolean) {
    _soundProfile.value = _soundProfile.value?.copy(repeatEveryday = bool)
  }

  fun setRepeatDays(repeatDays: List<DAY>) {
    _soundProfile.value = _soundProfile.value?.copy(repeatDays = repeatDays)
  }

  fun setStartTime(date: Date) {
    _soundProfile.value = _soundProfile.value?.copy(startTime = date)
  }

  fun setEndTime(date: Date) {
    _soundProfile.value = _soundProfile.value?.copy(endTime = date)
  }
}
