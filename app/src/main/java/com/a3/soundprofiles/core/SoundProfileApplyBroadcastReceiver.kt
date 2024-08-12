package com.a3.soundprofiles.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.a3.soundprofiles.core.dao.SoundProfileDao
import com.a3.soundprofiles.core.data.SoundProfile
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SoundProfileApplyBroadcastReceiver : BroadcastReceiver() {

  @Inject lateinit var soundProfileDao: SoundProfileDao

  override fun onReceive(context: Context, intent: Intent) {
    val soundProfileScheduler = SoundProfileScheduler(context)
    val soundProfileId = intent.getIntExtra(SoundProfileScheduler.SOUND_PROFILE_ID, -1)
    val resetToDefault = intent.getBooleanExtra(SoundProfileScheduler.RESET_TO_DEFAULT, false)
    if (soundProfileId != -1) {
      CoroutineScope(Dispatchers.IO).launch {
        if (resetToDefault) {
          // TODO: Modify the schema to have a default profile or some other way
          //    to choose a default profile
          SoundProfile(
                  title = "Default",
                  startTime = Date(),
                  endTime = Date(),
                  repeatDays = emptyList(),
                  description = "",
                  id = 0,
                  repeatEveryday = false,
                  isActive = false,
                  callVolume = 1f,
                  mediaVolume = 1f,
                  alarmVolume = 1f,
                  ringerVolume = 1f,
                  notificationVolume = 1f,
              )
              .applyProfile(context)
          soundProfileScheduler.rescheduleSoundProfile(context, soundProfileId, soundProfileDao)
          return@launch
        }

        val soundProfile = soundProfileDao.getById(soundProfileId)
        soundProfile.applyProfile(context)
      }
    }
  }
}
