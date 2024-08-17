package com.a3.soundprofiles

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.a3.soundprofiles.core.data.SoundProfile
import com.a3.soundprofiles.core.main.*
import com.a3.soundprofiles.core.ui.dialogbox.AboutDialog
import com.a3.soundprofiles.core.ui.dialogbox.AboutDialog.Companion.shareApp
import com.a3.soundprofiles.databinding.ActivityMainBinding
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity is the entry point of the application, responsible for displaying and managing sound profiles.
 */
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

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        DynamicColors.applyToActivityIfAvailable(this)
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

        this.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (tracker.hasSelection()) {
                    tracker.clearSelection()
                } else {
                    finish()
                }
            }
        })
    }

    /**
     * Sets up window insets to handle system bars.
     */
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    /**
     * Sets up the RecyclerView with an adapter and layout manager.
     */
    private fun setupRecyclerView() {
        val adapter = SoundProfileRecyclerAdapter(
            mutableListOf(),
            this,
            soundProfileManagerLauncher,
            mainViewModel::toggleIsActive
        )
        binding.recyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(SpaceBetweenItemDecorator(4))
        }
    }

    /**
     * Observes changes in the ViewModel's state and updates the UI accordingly.
     */
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

    /**
     * Displays a loading indicator.
     */
    private fun showLoading() {
        binding.apply {
            recyclerView.visibility = View.GONE
            emptyProfileIndicator.visibility = View.GONE
            loadingIndicator.visibility = View.VISIBLE
        }
    }

    /**
     * Displays an empty state when there are no sound profiles.
     */
    private fun showEmptyState() {
        (binding.recyclerView.adapter as SoundProfileRecyclerAdapter).updateSoundProfiles(emptyList())
        binding.apply {
            recyclerView.visibility = View.GONE
            loadingIndicator.visibility = View.GONE
            emptyProfileIndicator.visibility = View.VISIBLE
        }
    }

    /**
     * Displays the list of sound profiles.
     * @param soundProfiles List of sound profiles to display.
     */
    private fun showSuccessState(soundProfiles: List<SoundProfile>) {
        binding.apply {
            recyclerView.visibility = View.VISIBLE
            loadingIndicator.visibility = View.GONE
            emptyProfileIndicator.visibility = View.GONE
        }
        val adapter = binding.recyclerView.adapter as SoundProfileRecyclerAdapter
        adapter.updateSoundProfiles(soundProfiles)
        setupSelectionTracker(adapter)
    }

    /**
     * Sets up the selection tracker for the RecyclerView.
     * @param adapter The adapter for the RecyclerView.
     */
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

    /**
     * Updates the toolbar menu based on the selection state.
     */
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

    /**
     * Handles menu item clicks.
     * @param menuItem The menu item that was clicked.
     * @return True if the menu item click was handled, false otherwise.
     */
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

    /**
     * Displays an error message.
     * @param message The error message to display.
     */
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Inflates the options menu.
     * @param menu The options menu in which you place your items.
     * @return True for the menu to be displayed; false otherwise.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    /**
     * Handles options menu item clicks.
     * @param item The menu item that was clicked.
     * @return True if the menu item click was handled, false otherwise.
     */
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

    /**
     * Displays the info menu.
     * @return True if the info menu was displayed, false otherwise.
     */
    private fun showInfoMenu(): Boolean {
        AboutDialog().show(supportFragmentManager, "about_dialog_box")
        return true
    }
}