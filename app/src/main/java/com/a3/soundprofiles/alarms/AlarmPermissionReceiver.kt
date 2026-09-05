package com.a3.soundprofiles.alarms

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.a3.soundprofiles.data.repository.ScheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Handles SCHEDULE_EXACT_ALARM permission state changes (Android 12+).
 *
 * When the user revokes the exact alarm permission, the system silently cancels
 * all pending exact alarms. When the permission is re-granted, this receiver
 * fires and re-registers all enabled schedule alarms.
 */
class AlarmPermissionReceiver : BroadcastReceiver(), KoinComponent {

    private val scheduleRepository: ScheduleRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        if (intent.action != AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            Log.w("AlarmPermissionReceiver", "Exact alarm permission revoked — alarms will not fire")
            return
        }

        Log.i("AlarmPermissionReceiver", "Exact alarm permission re-granted, re-registering alarms")

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val scheduleAlarmManager = ScheduleAlarmManager(context)
                val enabledSchedules = scheduleRepository.getEnabledSchedules()
                val now = System.currentTimeMillis()

                for (schedule in enabledSchedules) {
                    if (schedule.startTime.time > now) {
                        scheduleAlarmManager.scheduleAlarm(schedule.scheduleId, schedule.startTime.time, isStart = true)
                    }
                    if (schedule.endTime.time > now) {
                        scheduleAlarmManager.scheduleAlarm(schedule.scheduleId, schedule.endTime.time, isStart = false)
                    }
                }

                Log.i("AlarmPermissionReceiver", "Re-registered alarms for ${enabledSchedules.size} enabled schedules")
            } catch (e: Exception) {
                Log.e("AlarmPermissionReceiver", "Error re-registering alarms", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
