package com.a3.soundprofiles.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.a3.soundprofiles.core.dao.SoundProfileDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class SoundProfileApplyBroadcastReceiver : BroadcastReceiver() {

  @Inject lateinit var soundProfileDao: SoundProfileDao

  override fun onReceive(context: Context, intent: Intent) {
    val soundProfileId = intent.getIntExtra("soundProfileId", -1)
    if (soundProfileId != -1) {
      CoroutineScope(Dispatchers.IO).launch {
        val soundProfile = soundProfileDao.getById(soundProfileId)
        soundProfile.applyProfile(context)
      }
    }
  }

  private fun scheduleNextSoundProfile(context: Context, soundProfileId: Int) {
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
        // Schedule the next sound profile to be applied
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
}
