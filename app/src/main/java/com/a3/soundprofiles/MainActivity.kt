package com.a3.soundprofiles

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.a3.soundprofiles.core.data.SoundProfile
import com.a3.soundprofiles.core.main.MainState
import com.a3.soundprofiles.core.main.MainViewModel
import com.a3.soundprofiles.core.main.SoundProfileRecyclerAdapter
import com.a3.soundprofiles.core.main.SpaceBetweenItemDecorator
import com.a3.soundprofiles.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  val mainViewModel: MainViewModel by viewModels()
  private lateinit var _binding: ActivityMainBinding
  private val binding
    get() = _binding

  private var soundProfileManagerLauncher: ActivityResultLauncher<Intent> =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d("MainActivity", "Result code: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
          mainViewModel.loadAllSoundProfiles()
        }
      }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    _binding = ActivityMainBinding.inflate(layoutInflater)
    enableEdgeToEdge()
    setContentView(binding.root)
    ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(0, systemBars.top, 0, 0)
      insets
    }
    ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(0, 0, 0, systemBars.bottom)
      insets
    }
    setSupportActionBar(binding.toolbar)

    mainViewModel.loadAllSoundProfiles()

    mainViewModel.state.observe(this) { state ->
      when (state) {
        is MainState.Loading -> {}

        is MainState.Success -> {
          val soundProfileRecyclerAdapter =
              SoundProfileRecyclerAdapter(
                  state.soundProfiles.toMutableList(),
                  soundProfileManagerLauncher,
                  mainViewModel::toggleIsActive)
          val linearLayoutManager = LinearLayoutManager(this)
          binding.recyclerView.apply {
            adapter = soundProfileRecyclerAdapter
            layoutManager = linearLayoutManager
          }
        }

        is MainState.Error -> {
          // Handle error
          Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
        }
      }
    }

    // Add ItemDecoration to the RecyclerView
    // only once to prevent extra space on every update.
    binding.recyclerView.addItemDecoration(SpaceBetweenItemDecorator(4))
    binding.fab.setOnClickListener {
      val intent = SoundProfileManager.createIntent(this)
      soundProfileManagerLauncher.launch(intent)
    }
  }
}

fun getCurrentVolume(context: Context): SoundProfile {
  val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

  val minMediaVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
  val maxMediaVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
  val minRingerVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_RING)
  val maxRingerVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
  val minCallVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_VOICE_CALL)
  val maxCallVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
  val minAlarmVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_ALARM)
  val maxAlarmVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
  val minNotificationVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_NOTIFICATION)
  val maxNotificationVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)

  val currentMediaVolume =
      (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) - minMediaVolume) /
          (maxMediaVolume - minMediaVolume).toFloat()
  val currentRingerVolume =
      (audioManager.getStreamVolume(AudioManager.STREAM_RING) - minRingerVolume) /
          (maxRingerVolume - minRingerVolume).toFloat()
  val currentCallVolume =
      (audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL) - minCallVolume) /
          (maxCallVolume - minCallVolume).toFloat()
  val currentAlarmVolume =
      (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) - minAlarmVolume) /
          (maxAlarmVolume - minAlarmVolume).toFloat()
  val currentNotificationVolume =
      (audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) - minNotificationVolume) /
          (maxNotificationVolume - minNotificationVolume).toFloat()

  return SoundProfile(
      id = 0,
      title = "Current Profile ${Date()}",
      description = "Current volume settings",
      mediaVolume = currentMediaVolume,
      notificationVolume = currentNotificationVolume,
      ringerVolume = currentRingerVolume,
      callVolume = currentCallVolume,
      alarmVolume = currentAlarmVolume,
      startTime = Date(),
      endTime = Date(),
      isActive = false,
      repeatEveryday = false,
      repeatDays = emptyList())
}
