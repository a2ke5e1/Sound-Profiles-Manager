package com.a3.soundprofiles.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.a3.soundprofiles.data.repository.ScheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Re-registers all enabled schedule alarms after device reboot or app update.
 *
 * AlarmManager alarms are wiped on reboot and app update. This receiver
 * listens for BOOT_COMPLETED and MY_PACKAGE_REPLACED to restore them.
 */
class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val scheduleRepository: ScheduleRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        Log.i("BootReceiver", "Received ${intent.action}, re-registering alarms")

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                rescheduleAllAlarms(context)
            } catch (e: Exception) {
                Log.e("BootReceiver", "Error re-registering alarms", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun rescheduleAllAlarms(context: Context) {
        val alarmManager = ScheduleAlarmManager(context)
        val enabledSchedules = scheduleRepository.getEnabledSchedules()

        for (schedule in enabledSchedules) {
            val now = System.currentTimeMillis()

            // Re-register start alarm if its time is in the future
            if (schedule.startTime.time > now) {
                alarmManager.scheduleAlarm(schedule.scheduleId, schedule.startTime.time, isStart = true)
                Log.d("BootReceiver", "Re-registered start alarm for schedule ${schedule.scheduleId}")
            }

            // Re-register end alarm if its time is in the future
            if (schedule.endTime.time > now) {
                alarmManager.scheduleAlarm(schedule.scheduleId, schedule.endTime.time, isStart = false)
                Log.d("BootReceiver", "Re-registered end alarm for schedule ${schedule.scheduleId}")
            }
        }

        Log.i("BootReceiver", "Re-registered alarms for ${enabledSchedules.size} enabled schedules")
    }
}
