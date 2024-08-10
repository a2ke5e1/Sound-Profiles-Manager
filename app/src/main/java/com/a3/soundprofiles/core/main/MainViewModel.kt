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

  data class Error(val message: String) : MainState()
}

@HiltViewModel
class MainViewModel @Inject constructor(val soundProfileDao: SoundProfileDao) : ViewModel() {

  private var _state = MutableLiveData<MainState>(MainState.Loading)
  val state
    get() = _state

  fun loadAllSoundProfiles() {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val soundProfiles = soundProfileDao.getAll()
        _state.postValue(MainState.Success(soundProfiles))
      } catch (e: Exception) {
        _state.postValue(MainState.Error(e.message ?: "An error occurred"))
      }
    }
  }
}
