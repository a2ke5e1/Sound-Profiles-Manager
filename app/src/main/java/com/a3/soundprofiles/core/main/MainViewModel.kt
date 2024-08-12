package com.a3.soundprofiles.core.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a3.soundprofiles.core.dao.SoundProfileDao
import com.a3.soundprofiles.core.data.SoundProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class MainState {
  object Loading : MainState()

  data class Success(val soundProfiles: List<SoundProfile>) : MainState()

  object Empty : MainState()

  data class Error(val message: String) : MainState()
}

@HiltViewModel
class MainViewModel @Inject constructor(val soundProfileDao: SoundProfileDao) : ViewModel() {

  private var _state = MutableLiveData<MainState>(MainState.Loading)
  val state
    get() = _state

  fun saveCurrentSoundProfile(soundProfile: SoundProfile) {
    viewModelScope.launch(Dispatchers.IO) { soundProfileDao.insert(soundProfile) }
  }

  fun loadAllSoundProfiles() {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val soundProfiles = soundProfileDao.getAll()
        if (soundProfiles.isEmpty()) _state.postValue(MainState.Empty)
        else _state.postValue(MainState.Success(soundProfiles))
      } catch (e: Exception) {
        _state.postValue(MainState.Error(e.message ?: "An error occurred"))
      }
    }
  }

  fun deleteAllSoundProfiles() {
    viewModelScope.launch(Dispatchers.IO) {
      soundProfileDao.deleteAll()
      updateSoundProfiles()
    }
  }

  fun deleteSoundProfiles(soundProfiles: List<SoundProfile>) {
    viewModelScope.launch(Dispatchers.IO) {
      soundProfileDao.delete(soundProfiles)
      updateSoundProfiles()
    }
  }

  fun toggleIsActive(soundProfile: SoundProfile) {
    viewModelScope.launch(Dispatchers.IO) {
      soundProfileDao.update(soundProfile.copy(isActive = !soundProfile.isActive))
      updateSoundProfiles()
    }
  }

  private suspend fun updateSoundProfiles() {
    val soundProfiles = soundProfileDao.getAll()
    if (soundProfiles.isEmpty()) _state.postValue(MainState.Empty)
    else _state.postValue(MainState.Success(soundProfiles))
  }
}
