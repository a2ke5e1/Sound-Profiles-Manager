package com.a3.soundprofiles.ui

import com.a3.soundprofiles.ui.screens.ProfileItemState
import com.a3.soundprofiles.ui.screens.ScheduleItemState


sealed class SoundProfileUiState {
    object Loading : SoundProfileUiState()
    data class Data(
        val profiles: List<ProfileItemState>,
        val schedules: List<ScheduleItemState> = emptyList()
    ) : SoundProfileUiState()
    data class Error(val message: String) : SoundProfileUiState()
}

enum class AppStartupState {
    Loading,
    Welcome,
    Main
}
