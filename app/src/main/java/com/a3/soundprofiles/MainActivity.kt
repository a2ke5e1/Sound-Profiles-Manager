package com.a3.soundprofiles

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import com.a3.soundprofiles.core.main.*
import com.a3.soundprofiles.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var tracker: SelectionTracker<Long>
    private lateinit var binding: ActivityMainBinding

    private val soundProfileManagerLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                mainViewModel.loadAllSoundProfiles()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupWindowInsets()
        setSupportActionBar(binding.toolbar)
        mainViewModel.loadAllSoundProfiles()
        setupRecyclerView()
        observeViewModel()
        binding.fab.setOnClickListener {
            val intent = SoundProfileManager.createIntent(this)
            soundProfileManagerLauncher.launch(intent)
        }
    }

    private fun setupWindowInsets() {
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
    }

    private fun setupRecyclerView() {
        val adapter = SoundProfileRecyclerAdapter(
            mutableListOf(),
            soundProfileManagerLauncher,
            mainViewModel::toggleIsActive
        )
        binding.recyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(SpaceBetweenItemDecorator(4))
        }
    }

    private fun observeViewModel() {
        mainViewModel.state.observe(this) { state ->
            when (state) {
                is MainState.Loading -> showLoading()
                is MainState.Empty -> showEmptyState()
                is MainState.Success -> showSuccessState(state.soundProfiles)
                is MainState.Error -> showError(state.message)
            }
        }
    }

    private fun showLoading() {
        binding.apply {
            nestedScrollView.visibility = View.GONE
            emptyProfileIndicator.visibility = View.GONE
            loadingIndicator.visibility = View.VISIBLE
        }
    }

    private fun showEmptyState() {
        (binding.recyclerView.adapter as SoundProfileRecyclerAdapter).updateSoundProfiles(emptyList())
        binding.apply {
            nestedScrollView.visibility = View.GONE
            loadingIndicator.visibility = View.GONE
            emptyProfileIndicator.visibility = View.VISIBLE
        }
    }

    private fun showSuccessState(soundProfiles: List<SoundProfile>) {
        binding.apply {
            loadingIndicator.visibility = View.GONE
            emptyProfileIndicator.visibility = View.GONE
            nestedScrollView.visibility = View.VISIBLE
        }
        val adapter = binding.recyclerView.adapter as SoundProfileRecyclerAdapter
        adapter.updateSoundProfiles(soundProfiles)
        setupSelectionTracker(adapter)
    }

    private fun setupSelectionTracker(adapter: SoundProfileRecyclerAdapter) {
        tracker = SelectionTracker.Builder(
            "soundProfileSelection",
            binding.recyclerView,
            SoundProfileItemKeyProvider(binding.recyclerView),
            SoundProfileItemDetailsLookup(binding.recyclerView),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()
        adapter.tracker = tracker
        tracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                updateToolbarMenu()
            }
        })
    }

    private fun updateToolbarMenu() {
        binding.toolbar.apply {
            menu.clear()
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            if (tracker.selection.size() > 0) {
                title = getString(R.string.selected, tracker.selection.size())
                inflateMenu(R.menu.profiles_selected_menu)
                menu.findItem(R.id.action_edit).isVisible = tracker.selection.size() == 1
                supportActionBar?.setHomeAsUpIndicator(R.drawable.close_24)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                setOnMenuItemClickListener { handleMenuItemClick(it) }
            } else {
                title = getString(R.string.app_name)
                inflateMenu(R.menu.main_menu)
                setOnMenuItemClickListener { onOptionsItemSelected(it) }
            }
        }
    }

    private fun handleMenuItemClick(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_edit -> {
                val intent = SoundProfileManager.createIntent(this, tracker.selection.first().toInt())
                soundProfileManagerLauncher.launch(intent)
                tracker.clearSelection()
                true
            }
            R.id.action_delete -> {
                mainViewModel.deleteSoundProfiles(
                    (binding.recyclerView.adapter as SoundProfileRecyclerAdapter).getSelectedSoundProfile()
                )
                tracker.clearSelection()
                true
            }
            R.id.action_delete_all -> {
                mainViewModel.deleteAllSoundProfiles()
                tracker.clearSelection()
                true
            }
            R.id.action_select_all -> {
                (binding.recyclerView.adapter as SoundProfileRecyclerAdapter).selectAll()
                true
            }
            else -> false
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
        AboutDialog().show(supportFragmentManager, "about_dialog_box")
        return true
    }
}

fun getCurrentVolume(context: Context): SoundProfile {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun getVolume(stream: Int): Float {
        val min = audioManager.getStreamMinVolume(stream)
        val max = audioManager.getStreamMaxVolume(stream)
        return (audioManager.getStreamVolume(stream) - min) / (max - min).toFloat()
    }

    return SoundProfile(
        id = 0,
        title = "Current Profile ${Date()}",
        description = "Current volume settings",
        mediaVolume = getVolume(AudioManager.STREAM_MUSIC),
        notificationVolume = getVolume(AudioManager.STREAM_NOTIFICATION),
        ringerVolume = getVolume(AudioManager.STREAM_RING),
        callVolume = getVolume(AudioManager.STREAM_VOICE_CALL),
        alarmVolume = getVolume(AudioManager.STREAM_ALARM),
        startTime = Date(),
        endTime = Date(),
        isActive = false,
        repeatEveryday = false,
        repeatDays = emptyList()
    )
}