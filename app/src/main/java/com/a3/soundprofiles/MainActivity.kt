package com.a3.soundprofiles

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.a3.soundprofiles.AboutDialog.Companion.shareApp
import com.a3.soundprofiles.core.data.SoundProfile
import com.a3.soundprofiles.core.main.MainState
import com.a3.soundprofiles.core.main.MainViewModel
import com.a3.soundprofiles.core.main.SoundProfileItemDetailsLookup
import com.a3.soundprofiles.core.main.SoundProfileItemKeyProvider
import com.a3.soundprofiles.core.main.SoundProfileRecyclerAdapter
import com.a3.soundprofiles.core.main.SpaceBetweenItemDecorator
import com.a3.soundprofiles.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  val mainViewModel: MainViewModel by viewModels()
  private lateinit var tracker: SelectionTracker<Long>
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

    val soundProfileRecyclerAdapter =
        SoundProfileRecyclerAdapter(
            emptyList<SoundProfile>().toMutableList(),
            soundProfileManagerLauncher,
            mainViewModel::toggleIsActive)
    val linearLayoutManager = LinearLayoutManager(this)
    binding.recyclerView.apply {
      adapter = soundProfileRecyclerAdapter
      layoutManager = linearLayoutManager
    }

    mainViewModel.state.observe(this) { state ->
      when (state) {
        is MainState.Loading -> {
          Toast.makeText(this, "Loading sound profiles", Toast.LENGTH_SHORT).show()
        }
        is MainState.Empty -> {
          soundProfileRecyclerAdapter.updateSoundProfiles(emptyList())
          Toast.makeText(this, "No sound profiles found", Toast.LENGTH_SHORT).show()
        }
        is MainState.Success -> {

          val soundProfileRecyclerAdapter =
              binding.recyclerView.adapter as SoundProfileRecyclerAdapter
          soundProfileRecyclerAdapter.updateSoundProfiles(state.soundProfiles)

          tracker =
              SelectionTracker.Builder(
                      "soundProfileSelection",
                      binding.recyclerView,
                      SoundProfileItemKeyProvider(binding.recyclerView),
                      SoundProfileItemDetailsLookup(binding.recyclerView),
                      StorageStrategy.createLongStorage())
                  .withSelectionPredicate(SelectionPredicates.createSelectAnything())
                  .build()
          soundProfileRecyclerAdapter.tracker = tracker

          tracker.addObserver(
              object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                  super.onSelectionChanged()
                  binding.toolbar.apply {
                    menu.clear()
                    inflateMenu(R.menu.main_menu)
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    if (tracker.selection.size() > 0) {
                      title = getString(R.string.selected, tracker.selection.size())
                      inflateMenu(R.menu.profiles_selected_menu)
                      menu.findItem(R.id.action_edit).isVisible = tracker.selection.size() == 1
                      supportActionBar?.setHomeAsUpIndicator(R.drawable.close_24)
                      supportActionBar?.setDisplayHomeAsUpEnabled(true)
                      setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                          R.id.action_edit -> {
                            val intent =
                                SoundProfileManager.createIntent(
                                    this@MainActivity, tracker.selection.first().toInt())
                            soundProfileManagerLauncher.launch(intent)
                            tracker.clearSelection()
                            true
                          }
                          R.id.action_delete -> {
                            val selectedProfiles =
                                soundProfileRecyclerAdapter.getSelectedSoundProfile()
                            mainViewModel.deleteSoundProfiles(selectedProfiles)
                            tracker.clearSelection()
                            true
                          }

                          R.id.action_delete_all -> {
                            mainViewModel.deleteAllSoundProfiles()
                            tracker.clearSelection()
                            true
                          }

                          R.id.action_select_all -> {
                            soundProfileRecyclerAdapter.selectAll()
                            true
                          }

                          else -> false
                        }
                      }
                    } else {
                      title = getString(R.string.app_name)
                    }
                  }
                }
              })
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

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.main_menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> {
        tracker.clearSelection()
        true
      }
      R.id.infoMenu -> showInfoMenu()
      R.id.shareMenu -> {
        shareApp(this)
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

    private fun showInfoMenu(): Boolean {
        val aboutDialogBox = AboutDialog()
        aboutDialogBox.show(supportFragmentManager, "about_dialog_box")
        return true
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
