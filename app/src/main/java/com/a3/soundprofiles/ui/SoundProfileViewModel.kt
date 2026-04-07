package com.a3.soundprofiles.ui

import android.app.Application
import android.content.Context
import android.icu.util.Calendar
import android.media.AudioManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.a3.soundprofiles.data.local.entities.ScheduleEntity
import com.a3.soundprofiles.data.local.entities.SoundProfileEntity
import com.a3.soundprofiles.data.local.entities.StreamVolume
import com.a3.soundprofiles.data.repository.AppSettingsRepository
import com.a3.soundprofiles.data.repository.ScheduleRepository
import com.a3.soundprofiles.data.repository.SoundProfileRepository
import com.a3.soundprofiles.ui.screens.ProfileItemState
import com.a3.soundprofiles.ui.screens.ScheduleItemState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class SoundProfileViewModel(
    application: Application,
    private val repository: SoundProfileRepository,
    private val scheduleRepository: ScheduleRepository,
    private val appSettingsRepository: AppSettingsRepository
) : AndroidViewModel(application) {

    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val _startupState = MutableStateFlow(AppStartupState.Loading)
    val startupState: StateFlow<AppStartupState> = _startupState

    init {
        viewModelScope.launch {
            appSettingsRepository.settingsFlow.collect { settings ->
                if (_startupState.value == AppStartupState.Loading) {
                    _startupState.value = if (settings.isFirstLaunch) AppStartupState.Welcome else AppStartupState.Main
                }
            }
        }
    }

    fun completeWelcome() {
        viewModelScope.launch {
            appSettingsRepository.setFirstLaunchCompleted()
            _startupState.value = AppStartupState.Main
        }
    }

    private val _allProfiles =
        repository.getAllProfiles()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _allSchedules =
        scheduleRepository.getAllSchedules()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val uiState: StateFlow<SoundProfileUiState> = combine(_allProfiles, _allSchedules) { profiles, schedules ->
        val profileItems = profiles.map { entity ->
            ProfileItemState(
                id = entity.id,
                name = entity.name,
                subtitle = entity.profileSummary(),
                icon = entity.iconName ?: "volume_up"
            )
        }
        val profileMap = profiles.associateBy { it.id }
        val scheduleItems = schedules.map { schedule ->
            ScheduleItemState(
                id = schedule.scheduleId,
                name = schedule.name,
                subtitle = buildScheduleSubtitle(schedule, profileMap),
                isActive = schedule.isActive,
                icon = schedule.iconName
            )
        }
        SoundProfileUiState.Data(
            profiles = profileItems,
            schedules = scheduleItems
        ) as SoundProfileUiState
    }
        .onStart {
            emit(SoundProfileUiState.Loading)
            delay(500)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SoundProfileUiState.Loading
        )

    private fun buildScheduleSubtitle(
        schedule: ScheduleEntity,
        profileMap: Map<Int, SoundProfileEntity>
    ): String {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val startTime = timeFormat.format(schedule.startTime)
        val endTime = timeFormat.format(schedule.endTime)

        val profileName = profileMap[schedule.profileId]?.name ?: "Unknown"
        val timeRange = "$startTime - $endTime"

        val daysStr = if (schedule.repeatEveryday) {
            "Every day"
        } else if (schedule.daysOfWeek.isNotEmpty()) {
            schedule.daysOfWeek.joinToString(", ") {
                it.name.take(3).lowercase()
                    .replaceFirstChar { c -> c.uppercase() }
            }
        } else {
            ""
        }

        return buildString {
            if (schedule.isActive) {
                append("Active")
            }
            if (!schedule.isEnabled) {
                append("Disabled")
            }
            if (schedule.isActive || !schedule.isEnabled) {
                append(" • ")
            }
            append("$timeRange")
            if (daysStr.isNotEmpty()) {
                append(" • $daysStr")
            }
            append(" ($profileName)")
        }
    }

    fun createDefaultProfile(name: String, iconName: String): SoundProfileEntity {
        return SoundProfileEntity(
            name = name,
            iconName = iconName,
            ringerVolume = getStreamVolumeInfo(AudioManager.STREAM_RING),
            notificationVolume = getStreamVolumeInfo(AudioManager.STREAM_NOTIFICATION),
            alarmVolume = getStreamVolumeInfo(AudioManager.STREAM_ALARM),
            musicVolume = getStreamVolumeInfo(AudioManager.STREAM_MUSIC),
            voiceVolume = getStreamVolumeInfo(AudioManager.STREAM_VOICE_CALL),
            ringerMode = audioManager.ringerMode
        )
    }

    private fun getStreamVolumeInfo(streamType: Int): StreamVolume {
        val max = audioManager.getStreamMaxVolume(streamType)
        val min = audioManager.getStreamMinVolume(streamType)
        val current = audioManager.getStreamVolume(streamType)
        return StreamVolume(current = current, min = min, max = max)
    }

    suspend fun getProfileById(id: Int): SoundProfileEntity? {
        return repository.getProfileById(id)
    }

    fun saveProfile(profile: SoundProfileEntity) {
        val sanitizedProfile = profile.sanitize()
        viewModelScope.launch {
            if (sanitizedProfile.id == 0) {
                repository.insertProfile(sanitizedProfile)
            } else {
                repository.updateProfile(sanitizedProfile)
            }
        }
    }

    fun deleteProfile(profile: SoundProfileEntity) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
        }
    }

    fun applyProfile(profileId: Int) {
        viewModelScope.launch {
            val profile = repository.getProfileById(profileId) ?: return@launch
            profile.applyToSystem(getApplication())
        }
    }

    // Schedule operations
    fun getAllProfileEntities(): StateFlow<List<SoundProfileEntity>> = _allProfiles

    suspend fun getScheduleById(id: Int): ScheduleEntity? {
        return scheduleRepository.getScheduleById(id)
    }

    fun saveSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            if (schedule.scheduleId == 0) {
                val id = scheduleRepository.insertSchedule(schedule)
                val newSchedule = schedule.copy(scheduleId = id.toInt())
                setupScheduleAlarm(newSchedule)
            } else {
                scheduleRepository.updateSchedule(schedule)
                setupScheduleAlarm(schedule)
            }
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            scheduleRepository.deleteSchedule(schedule)
            val alarmManager = com.a3.soundprofiles.alarms.ScheduleAlarmManager(getApplication())
            alarmManager.cancelAlarm(schedule.scheduleId)
        }
    }
    
    private fun setupScheduleAlarm(schedule: ScheduleEntity) {
        val alarmManager = com.a3.soundprofiles.alarms.ScheduleAlarmManager(getApplication())
        
        if (schedule.isEnabled) {
            val now = System.currentTimeMillis()
            var startMillis = schedule.startTime.time
            var endMillis = schedule.endTime.time

            // If repeating, ignore original date and snap to today's date
            if (schedule.repeatEveryday || schedule.daysOfWeek.isNotEmpty()) {
                val nowCal = Calendar.getInstance()
                
                val startCal = Calendar.getInstance().apply { timeInMillis = startMillis }
                startCal.set(Calendar.YEAR, nowCal.get(Calendar.YEAR))
                startCal.set(Calendar.DAY_OF_YEAR, nowCal.get(Calendar.DAY_OF_YEAR))
                startMillis = startCal.timeInMillis
                
                val endCal = Calendar.getInstance().apply { timeInMillis = endMillis }
                endCal.set(Calendar.YEAR, nowCal.get(Calendar.YEAR))
                endCal.set(Calendar.DAY_OF_YEAR, nowCal.get(Calendar.DAY_OF_YEAR))
                endMillis = endCal.timeInMillis
            }

            // If repeat everyday is true, we push the alarms to the next available future time individually
            if (schedule.repeatEveryday) {
                // If start passed today, push to tomorrow
                if (startMillis < now) {
                    startMillis += 24L * 60 * 60 * 1000
                }
                // If end passed today, push to tomorrow. 
                if (endMillis < now) {
                    endMillis += 24L * 60 * 60 * 1000
                }
            }

            // Schedule the alarms
            // If they are in the past (non-repeating), AlarmManager will fire them immediately which is expected
            alarmManager.scheduleAlarm(schedule.scheduleId, startMillis, isStart = true)
            alarmManager.scheduleAlarm(schedule.scheduleId, endMillis, isStart = false)
        } else {
            alarmManager.cancelAlarm(schedule.scheduleId)
        }
    }
}
