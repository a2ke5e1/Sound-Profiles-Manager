package com.a3.soundprofiles.core.ui.components

import android.content.Context
import android.media.AudioManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.a3.soundprofiles.core.data.SoundProfile
import com.a3.soundprofiles.core.main.CardSoundProfileItemHolder.Companion.volumeToString
import com.a3.soundprofiles.databinding.ViewCurrentUserVolumeBinding
import java.util.Date

class CurrentUserVolumeView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr) {

  private val binding =
      ViewCurrentUserVolumeBinding.inflate(LayoutInflater.from(context), this, true)

  init {
    orientation = HORIZONTAL
    loadUserSoundSettings()
  }

  fun loadUserSoundSettings() {
    val soundProfile = getCurrentVolume(context)

    binding.mediaVolumeTextView.text = volumeToString(soundProfile.mediaVolume)
    binding.notificationVolumeTextView.text = volumeToString(soundProfile.notificationVolume)
    binding.ringerVolumeTextView.text = volumeToString(soundProfile.ringerVolume)
    binding.callVolumeTextView.text = volumeToString(soundProfile.callVolume)
    binding.alarmVolumeTextView.text = volumeToString(soundProfile.alarmVolume)
  }

  /**
   * Retrieves the current volume settings and creates a SoundProfile object.
   *
   * @param context The context to use for retrieving the AudioManager.
   * @return A SoundProfile object representing the current volume settings.
   */
  private fun getCurrentVolume(context: Context): SoundProfile {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    /**
     * Retrieves the volume level for a given audio stream.
     *
     * @param stream The audio stream type.
     * @return The volume level as a float between 0 and 1.
     */
    fun getVolume(stream: Int): Float {
      val min = audioManager.getStreamMinVolume(stream)
      val max = audioManager.getStreamMaxVolume(stream)
      return (audioManager.getStreamVolume(stream) - min) / (max - min).toFloat()
    }

    return SoundProfile(
        id = 0,
        title = "",
        description = "",
        mediaVolume = getVolume(AudioManager.STREAM_MUSIC),
        notificationVolume = getVolume(AudioManager.STREAM_NOTIFICATION),
        ringerVolume = getVolume(AudioManager.STREAM_RING),
        callVolume = getVolume(AudioManager.STREAM_VOICE_CALL),
        alarmVolume = getVolume(AudioManager.STREAM_ALARM),
        startTime = Date(),
        endTime = Date(),
        isActive = false,
        repeatEveryday = false,
        repeatDays = emptyList())
  }
}
