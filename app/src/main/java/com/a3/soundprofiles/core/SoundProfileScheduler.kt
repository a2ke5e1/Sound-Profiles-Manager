package com.a3.soundprofiles.core

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.a3.soundprofiles.R
import com.a3.soundprofiles.core.dao.SoundProfileDao
import com.a3.soundprofiles.core.data.DAY
import com.a3.soundprofiles.core.data.SoundProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import kotlin.apply
import kotlin.jvm.java

class SoundProfileScheduler(private val context: Context) {
  private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

  @SuppressLint("ScheduleExactAlarm")
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
    alarmManager.setExactAndAllowWhileIdle(
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
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP, soundProfile.endTime.time, endPendingIntent)
    Toast.makeText(context,
        context.getString(R.string.profile_scheduled, soundProfile.title), Toast.LENGTH_SHORT).show()
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

    Toast.makeText(context,
        context.getString(R.string.profile_canceled_message, soundProfile.title), Toast.LENGTH_SHORT)
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
      val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
      val newSoundProfile = createNewSoundProfile(currentDay, soundProfile)
      if (newSoundProfile != null) {
        soundProfileDao.update(newSoundProfile)
        scheduleSoundProfileApply(newSoundProfile)
      }
    }
  }

  fun hasScheduleExactAlarm(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      alarmManager.canScheduleExactAlarms()
    } else {
      true
    }
  }

  companion object {
    const val SOUND_PROFILE_ID = "soundProfileId"
    const val RESET_TO_DEFAULT = "resetToDefault"

    fun createNewSoundProfile(currentDay: Int, soundProfile: SoundProfile): SoundProfile? {
      if (soundProfile.repeatEveryday) {
        val newStartDate = soundProfile.startTime.addDay(1)
        val newEndDate = soundProfile.endTime.addDay(1)
        return soundProfile.copy(startTime = newStartDate, endTime = newEndDate)
      }
      if (soundProfile.repeatDays.isNotEmpty()) {
        val newStartDate = soundProfile.startTime.nextDate(currentDay, soundProfile.repeatDays)
        val newEndDate = soundProfile.endTime.nextDate(currentDay, soundProfile.repeatDays)
        return soundProfile.copy(startTime = newStartDate, endTime = newEndDate)
      }
      return null
    }

    fun Date.nextDate(currentDay: Int, repeatDays: List<DAY>): Date {
      val calendar = Calendar.getInstance()
      calendar.time = this
      while (true) {
        calendar.add(Calendar.DATE, 1)
        val nextDay = calendar.get(Calendar.DAY_OF_WEEK) - 1
        if (repeatDays.contains(DAY.entries[nextDay])) {
          break
        }
      }
      return calendar.time
    }

    fun Date.addDay(days: Int): Date {
      val calendar = Calendar.getInstance()
      calendar.time = this
      calendar.add(Calendar.DATE, days)
      return calendar.time
    }
  }
}
