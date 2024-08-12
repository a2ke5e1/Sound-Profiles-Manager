package com.a3.soundprofiles.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.a3.soundprofiles.core.dao.SoundProfileDao
import com.a3.soundprofiles.core.data.SoundProfile
import java.util.Calendar
import java.util.Date
import kotlin.apply
import kotlin.jvm.java
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SoundProfileScheduler(private val context: Context) {
  private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

  fun scheduleSoundProfileApply(soundProfile: SoundProfile) {
    val startIntent =
        Intent(context, SoundProfileApplyBroadcastReceiver::class.java).apply {
          putExtra(SOUND_PROFILE_ID, soundProfile.id)
        }
    val startPendingIntent =
        PendingIntent.getBroadcast(
            context,
            soundProfile.id.hashCode(),
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    alarmManager.setAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP, soundProfile.startTime.time, startPendingIntent)

    val endIntent =
        Intent(context, SoundProfileApplyBroadcastReceiver::class.java).apply {
          putExtra(SOUND_PROFILE_ID, soundProfile.id)
          putExtra(RESET_TO_DEFAULT, true)
        }
    val endPendingIntent =
        PendingIntent.getBroadcast(
            context,
            (soundProfile.id.toString() + "_end").hashCode(),
            endIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    alarmManager.setAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP, soundProfile.endTime.time, endPendingIntent)
    Toast.makeText(context, "'${soundProfile.title}' profile scheduled", Toast.LENGTH_SHORT).show()
  }

  fun cancelScheduledSoundProfileApply(soundProfile: SoundProfile) {
    val startIntent = Intent(context, SoundProfileApplyBroadcastReceiver::class.java)
    val startPendingIntent =
        PendingIntent.getBroadcast(
            context,
            soundProfile.id.hashCode(),
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    alarmManager.cancel(startPendingIntent)

    val endIntent = Intent(context, SoundProfileApplyBroadcastReceiver::class.java)
    val endPendingIntent =
        PendingIntent.getBroadcast(
            context,
            (soundProfile.id.toString() + "_end").hashCode(),
            endIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    alarmManager.cancel(endPendingIntent)

    Toast.makeText(context, "'${soundProfile.title}' profile schedule canceled", Toast.LENGTH_SHORT)
        .show()
  }

   fun rescheduleSoundProfile(
      context: Context,
      soundProfileId: Int,
      soundProfileDao: SoundProfileDao
  ) {
    // Schedule the next sound profile to be applied
    CoroutineScope(Dispatchers.IO).launch {
      val soundProfile = soundProfileDao.getById(soundProfileId)

      if (soundProfile.repeatEveryday) {
        val newStartDate = soundProfile.startTime.addDay(1)
        val newEndDate = soundProfile.endTime.addDay(1)

        val newSoundProfile = soundProfile.copy(startTime = newStartDate, endTime = newEndDate)

        soundProfileDao.update(newSoundProfile)
        // Schedule the next sound profile to be applied
        return@launch
      }

      if (soundProfile.repeatDays.isNotEmpty()) {
        val currentDay =
            Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1 //  To make it 0-indexed
        val nextDay = soundProfile.repeatDays.first { it.ordinal > currentDay }
        val daysToAdd = nextDay.ordinal - currentDay

        val newStartDate = soundProfile.startTime.addDay(daysToAdd)
        val newEndDate = soundProfile.endTime.addDay(daysToAdd)

        val newSoundProfile = soundProfile.copy(startTime = newStartDate, endTime = newEndDate)

        soundProfileDao.update(newSoundProfile)
        scheduleSoundProfileApply(newSoundProfile)
        return@launch
      }
    }
  }

  fun Date.addDay(days: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.DATE, days)
    return calendar.time
  }

  companion object {
    const val SOUND_PROFILE_ID = "soundProfileId"
    const val RESET_TO_DEFAULT = "resetToDefault"
  }
}
