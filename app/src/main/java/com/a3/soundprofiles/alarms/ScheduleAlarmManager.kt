package com.a3.soundprofiles.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

class ScheduleAlarmManager(private val context: Context) {

    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
        const val EXTRA_IS_START = "extra_is_start"
    }

    private fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun scheduleAlarm(scheduleId: Int, timeInMillis: Long, isStart: Boolean) {
        if (!canScheduleExactAlarms()) return

        val intent = Intent(context, ScheduleReceiver::class.java).apply {
            putExtra(EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(EXTRA_IS_START, isStart)
        }

        // We use a unique request code for start vs end alarms
        val requestCode = if (isStart) scheduleId else -scheduleId
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            pendingIntent
        )
    }

    fun cancelAlarm(scheduleId: Int) {
        val startIntent = Intent(context, ScheduleReceiver::class.java)
        val endIntent = Intent(context, ScheduleReceiver::class.java)

        val startPending = PendingIntent.getBroadcast(
            context, scheduleId, startIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        val endPending = PendingIntent.getBroadcast(
            context, -scheduleId, endIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        startPending?.let { alarmManager.cancel(it) }
        endPending?.let { alarmManager.cancel(it) }
    }
}
