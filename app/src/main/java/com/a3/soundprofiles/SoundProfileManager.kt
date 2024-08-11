package com.a3.soundprofiles

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.a3.soundprofiles.core.main.CreateEditSoundProfileViewModel
import com.a3.soundprofiles.databinding.ActivitySoundProfileManagerBinding
import com.google.android.material.slider.LabelFormatter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SoundProfileManager : AppCompatActivity() {

  private lateinit var _binding: ActivitySoundProfileManagerBinding
  private val binding
    get() = _binding

  private val soundProfileId: Int
    get() = intent.getIntExtra(SOUND_PROFILE_ID, -1)

  private val createEditSoundProfileViewModel: CreateEditSoundProfileViewModel by viewModels()

  private var isProgrammaticChange = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    _binding = ActivitySoundProfileManagerBinding.inflate(layoutInflater)
    setContentView(binding.root)
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    if (soundProfileId > 0) {
      createEditSoundProfileViewModel.loadSoundProfile(soundProfileId)
    }

    val labelFormatter = LabelFormatter { value ->
      val volume = (value * 100).toInt()
      "$volume%"
    }
    binding.ringerVolume.setLabelFormatter(labelFormatter)
    binding.mediaVolume.setLabelFormatter(labelFormatter)
    binding.notificationVolume.setLabelFormatter(labelFormatter)
    binding.callVolume.setLabelFormatter(labelFormatter)
    binding.alarmVolume.setLabelFormatter(labelFormatter)

    createEditSoundProfileViewModel.soundProfile.observe(this) { soundProfile ->
      isProgrammaticChange = true
      binding.title.setText(soundProfile.title)
      binding.description.setText(soundProfile.description)
      binding.ringerVolume.value = soundProfile.ringerVolume
      binding.mediaVolume.value = soundProfile.mediaVolume
      binding.notificationVolume.value = soundProfile.notificationVolume
      binding.callVolume.value = soundProfile.callVolume
      binding.alarmVolume.value = soundProfile.alarmVolume
      isProgrammaticChange = false
      Log.d("SoundProfileManager", "Sound profile loaded: $soundProfile")
    }

    binding.ringerVolume.addOnChangeListener { _, value, _ ->
      createEditSoundProfileViewModel.setRingerVolume(value)
    }
    binding.mediaVolume.addOnChangeListener { _, value, _ ->
      createEditSoundProfileViewModel.setMediaVolume(value)
    }
    binding.notificationVolume.addOnChangeListener { _, value, _ ->
      createEditSoundProfileViewModel.setNotificationVolume(value)
    }
    binding.callVolume.addOnChangeListener { _, value, _ ->
      createEditSoundProfileViewModel.setCallVolume(value)
    }
    binding.alarmVolume.addOnChangeListener { _, value, _ ->
      createEditSoundProfileViewModel.setAlarmVolume(value)
    }

    binding.title.doAfterTextChanged {
      if (isProgrammaticChange) return@doAfterTextChanged
      val cursorPosition = binding.title.selectionStart
      createEditSoundProfileViewModel.setTitle(it.toString())
      binding.title.setSelection(cursorPosition)
    }

    binding.description.doAfterTextChanged {
      if (isProgrammaticChange) return@doAfterTextChanged
      val cursorPosition = binding.description.selectionStart
      createEditSoundProfileViewModel.setDescription(it.toString())
      binding.description.setSelection(cursorPosition)
    }

    binding.saveSoundProfile.setOnClickListener {
      createEditSoundProfileViewModel.saveSoundProfile()
      Log.d("SoundProfileManager", "Setting result to RESULT_OK")
      setResult(RESULT_OK)
      finish()
    }
  }

  companion object {
    const val SOUND_PROFILE_ID = "soundProfileId"

    /**
     * Creates an Intent to start the SoundProfileManager activity.
     *
     * @param context The context from which the Intent is being created.
     * @param soundProfileId The ID of the sound profile to be managed. Defaults to -1 if not
     *   provided.
     * @return The Intent to start the SoundProfileManager activity.
     */
    fun createIntent(context: Context, soundProfileId: Int = -1): Intent =
        Intent(context, SoundProfileManager::class.java).apply {
          putExtra(SOUND_PROFILE_ID, soundProfileId)
        }
  }
}
