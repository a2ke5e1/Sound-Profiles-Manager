package com.a3.soundprofiles.alarms

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import com.a3.soundprofiles.data.local.entities.SoundProfileEntity
import com.a3.soundprofiles.data.repository.ScheduleRepository
import com.a3.soundprofiles.data.repository.SoundProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ScheduleReceiver : BroadcastReceiver(), KoinComponent {

    private val scheduleRepository: ScheduleRepository by inject()
    private val soundProfileRepository: SoundProfileRepository by inject()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getIntExtra(ScheduleAlarmManager.EXTRA_SCHEDULE_ID, -1)
        val isStart = intent.getBooleanExtra(ScheduleAlarmManager.EXTRA_IS_START, true)

        if (scheduleId == -1) return

        val pendingResult = goAsync()

        scope.launch {
            try {
                val schedule = scheduleRepository.getScheduleById(scheduleId) ?: return@launch
                
                // If it's starting, set isActive to true and apply profile.
                // If ending, set isActive to false and apply fallback profile.
                val updatedSchedule = schedule.copy(isActive = isStart)
                scheduleRepository.updateSchedule(updatedSchedule)

                val profileId = if (isStart) updatedSchedule.profileId else updatedSchedule.fallbackProfileId
                
                val notificationHelper = NotificationHelper(context)
                
                if (profileId != null) {
                    val profile = soundProfileRepository.getProfileById(profileId)
                    if (profile != null) {
                        profile.applyToSystem(context)
                        
                        val title = if (isStart) "Started: ${schedule.name}" else "Ended: ${schedule.name}"
                        val message = "Applied profile: ${profile.name}"
                        notificationHelper.showProfileSwitchNotification(title, message)
                    } else {
                        Log.e("ScheduleReceiver", "Profile not found for ID: $profileId")
                    }
                } else {
                     val title = "${schedule.name} Ended"
                     notificationHelper.showProfileSwitchNotification(title, "Schedule deactivated")
                }

                // Reschedule for next day if needed
                if (schedule.repeatEveryday) {
                    val alarmManager = ScheduleAlarmManager(context)
                    val nextTime = if (isStart) schedule.startTime.time + 24L * 60 * 60 * 1000 else schedule.endTime.time + 24L * 60 * 60 * 1000
                    
                    val updatedTimesSchedule = updatedSchedule.copy(
                        startTime = if (isStart) java.util.Date(schedule.startTime.time + 24L * 60 * 60 * 1000) else updatedSchedule.startTime,
                        endTime = if (!isStart) java.util.Date(schedule.endTime.time + 24L * 60 * 60 * 1000) else updatedSchedule.endTime
                    )
                    scheduleRepository.updateSchedule(updatedTimesSchedule)

                    alarmManager.scheduleAlarm(scheduleId, nextTime, isStart)
                } else {
                    // Just update the active state (which was updatedSchedule)
                    scheduleRepository.updateSchedule(updatedSchedule)
                }

            } catch (e: Exception) {
                Log.e("ScheduleReceiver", "Error in receiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
