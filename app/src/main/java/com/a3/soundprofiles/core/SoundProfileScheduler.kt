package com.a3.soundprofiles.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.a3.soundprofiles.core.data.SoundProfile
import kotlin.apply
import kotlin.jvm.java

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

  companion object {
    const val SOUND_PROFILE_ID = "soundProfileId"
    const val RESET_TO_DEFAULT = "resetToDefault"
  }
}
