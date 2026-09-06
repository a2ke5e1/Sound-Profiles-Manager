package com.a3.soundprofiles.alarms

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import android.util.Log
import com.a3.soundprofiles.data.local.entities.SoundProfileEntity
import com.a3.soundprofiles.data.repository.ScheduleRepository
import com.a3.soundprofiles.data.repository.SoundProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ScheduleReceiver : BroadcastReceiver(), KoinComponent {

    private val scheduleRepository: ScheduleRepository by inject()
    private val soundProfileRepository: SoundProfileRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getIntExtra(ScheduleAlarmManager.EXTRA_SCHEDULE_ID, -1)
        val isStart = intent.getBooleanExtra(ScheduleAlarmManager.EXTRA_IS_START, true)

        if (scheduleId == -1) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
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
                        // Check if the profile needs DND permission for ringer mode changes
                        val needsDndPermission = profile.ringerMode != AudioManager.RINGER_MODE_NORMAL
                        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        val hasDndPermission = notificationManager.isNotificationPolicyAccessGranted

                        if (needsDndPermission && !hasDndPermission) {
                            // Still apply what we can (volumes), but warn the user about ringer mode
                            profile.applyToSystem(context)

                            val settingsIntent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            val pendingSettingsIntent = PendingIntent.getActivity(
                                context, 0, settingsIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )

                            val ringerModeName = when (profile.ringerMode) {
                                AudioManager.RINGER_MODE_SILENT -> "Silent"
                                AudioManager.RINGER_MODE_VIBRATE -> "Vibrate"
                                else -> "Custom"
                            }
                            notificationHelper.showProfileSwitchNotification(
                                title = "⚠️ Permission needed for ${profile.name}",
                                message = "Volumes applied, but $ringerModeName mode requires Do Not Disturb access. Tap to grant.",
                                actionIntent = pendingSettingsIntent
                            )
                        } else {
                            profile.applyToSystem(context)

                            val title = if (isStart) "Started: ${schedule.name}" else "Ended: ${schedule.name}"
                            val message = "Applied profile: ${profile.name}"
                            notificationHelper.showProfileSwitchNotification(title, message)
                        }
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
