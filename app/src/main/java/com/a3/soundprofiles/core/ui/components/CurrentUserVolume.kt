package com.a3.soundprofiles.core.ui.components

import android.animation.ValueAnimator
import android.content.Context
import android.media.AudioManager
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
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

  private var currentValue: Float = 0.0f

  init {
    orientation = HORIZONTAL
  }

  fun setIcon(@DrawableRes icon: Int) {
    binding.icon.setImageResource(icon)
  }

  fun setVolume(volume: Float) {
    // animate 0 to volume
    val volumeTextAnimation = ValueAnimator.ofFloat(currentValue, volume)
    volumeTextAnimation.addUpdateListener { valueAnimator ->
      val value = valueAnimator.animatedValue as Float
      currentValue = value
      setVolumeText(value)
    }
    volumeTextAnimation.duration = ANIMATION_DURATION

    val params = binding.volumeProgressBar.layoutParams
    val target = (volume * binding.volumeContainer.layoutParams.height).toInt()
    val volumeProgressBarAnimation = ValueAnimator.ofInt(params.height, target)
    volumeProgressBarAnimation.addUpdateListener { valueAnimator ->
      binding.volumeProgressBar.layoutParams.height = valueAnimator.animatedValue as Int
      binding.volumeProgressBar.requestLayout()
      checkOverlapAndSetTextColor()
    }
    volumeProgressBarAnimation.duration = ANIMATION_DURATION

    volumeTextAnimation.start()
    volumeProgressBarAnimation.start()
  }

  fun setVolumeText(volume: Float) {
    val volumeText = volumeToString(volume)
    val spannableString = SpannableString(volumeText)
    spannableString.setSpan(
        RelativeSizeSpan(0.8f),
        volumeText.length - 1,
        volumeText.length,
        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
    binding.volumeText.text = spannableString
    checkOverlapAndSetTextColor()
  }

  private fun checkOverlapAndSetTextColor() {
    // Get the positions and dimensions of volumeProgressBar
    val progressBarLocation = IntArray(2)
    binding.volumeProgressBar.getLocationOnScreen(progressBarLocation)

    // Get the positions and dimensions of volumeText
    val textLocation = IntArray(2)
    binding.volumeText.getLocationOnScreen(textLocation)

    val textBottom = textLocation[1] + binding.volumeText.height - 5
    val isOverlapping = progressBarLocation[1] < textBottom

    // Resolve color attributes
    val colorOnPrimary = context.getColorFromAttr(com.google.android.material.R.attr.colorOnPrimary)
    val colorPrimary = context.getColorFromAttr(com.google.android.material.R.attr.colorPrimary)

    // Set the text color based on overlap
    if (isOverlapping) {
      binding.volumeText.setTextColor(colorOnPrimary)
    } else {
      binding.volumeText.setTextColor(colorPrimary)
    }
  }

  fun Context.getColorFromAttr(attr: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
  }

  fun addOnChangeListener(streamType: Int) {
    binding.volumeContainer.setOnTouchListener { view, event ->
      if (event.action == MotionEvent.ACTION_DOWN) {
        val tapHeight = getTapHeight(event, view)
        val containerHeight = view.height
        val volume = 1.0f - (tapHeight / containerHeight).toFloat()
        setVolume(volume)

        // Set the volume level for the given audio stream
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(streamType)
        val volumeLevel = (volume * maxVolume).toInt()
        audioManager.setStreamVolume(streamType, volumeLevel, AudioManager.FLAG_VIBRATE)
        performClick()
      }
      true
    }
  }

  companion object {
    private const val ANIMATION_DURATION = 1000L

    /**
     * Retrieves the current volume settings and creates a SoundProfile object.
     *
     * @param context The context to use for retrieving the AudioManager.
     * @return A SoundProfile object representing the current volume settings.
     */
    fun getCurrentVolume(context: Context): SoundProfile {
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

    private fun getTapHeight(event: MotionEvent, view: View): Float {
      // Get the y-coordinate of the tap relative to the screen
      val tapY = event.rawY
      // Get the top position of the view relative to the screen
      val viewTop = IntArray(2).apply { view.getLocationOnScreen(this) }[1]
      // Calculate the height at which the user tapped
      return tapY - viewTop
    }
  }
}
