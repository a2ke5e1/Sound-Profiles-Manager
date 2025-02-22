package com.a3.soundprofiles.core.main

import android.content.Context
import android.database.ContentObserver
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.a3.soundprofiles.R
import com.a3.soundprofiles.core.dao.SoundProfileDao
import com.a3.soundprofiles.core.data.SoundProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.logging.Handler
import javax.inject.Inject

sealed class MainState {
  data object Loading : MainState()

  data class Success(val soundProfiles: List<SoundProfile>) : MainState()

  data object Empty : MainState()

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

  fun setDefaultSoundProfile(context: Context, id: Int) {
    val pref = PreferenceManager.getDefaultSharedPreferences(context)
    val DEFAULT_PROFILE_ID = context.getString(R.string.default_sound_profile_pref)
    val editor = pref.edit()

    viewModelScope.launch(Dispatchers.IO) {
      val soundProfile = soundProfileDao.getById(id)
      editor.putInt(DEFAULT_PROFILE_ID, id)
      editor.commit()
      launch(Dispatchers.Main) {
        Toast.makeText(context, "Default Set: ${soundProfile.title}", Toast.LENGTH_SHORT).show()
      }
    }
  }
}
