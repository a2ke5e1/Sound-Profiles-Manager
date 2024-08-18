package com.a3.soundprofiles

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.a3.soundprofiles.core.data.SoundProfile
import com.a3.soundprofiles.core.di.components.PermissionMessageDialog
import com.a3.soundprofiles.core.main.*
import com.a3.soundprofiles.core.ui.components.CurrentUserVolumeView
import com.a3.soundprofiles.core.ui.dialogbox.AboutDialog
import com.a3.soundprofiles.core.ui.dialogbox.AboutDialog.Companion.shareApp
import com.a3.soundprofiles.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity is the entry point of the application, responsible for displaying and managing sound
 * profiles.
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

  private val requestPermissionLauncher =
      registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

  /**
   * Called when the activity is first created.
   *
   * @param savedInstanceState If the activity is being re-initialized after previously being shut
   *   down then this Bundle contains the data it most recently supplied.
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    installSplashScreen()
    MobileAds.initialize(this) {}

    DynamicColors.applyToActivityIfAvailable(this)
    binding = ActivityMainBinding.inflate(layoutInflater)
    enableEdgeToEdge()
    setContentView(binding.root)
    setupWindowInsets()
    setSupportActionBar(binding.toolbar)
    mainViewModel.loadAllSoundProfiles()
    setupRecyclerView()
    observeViewModel()
    // setUpAds()
    binding.fab.setOnClickListener {
      val intent = SoundProfileManager.createIntent(this)
      soundProfileManagerLauncher.launch(intent)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      requestNotificationPermission()
    }

    val currentVolume = CurrentUserVolumeView.getCurrentVolume(this)
    binding.userMediaVolume.apply {
      setVolume(currentVolume.mediaVolume)
      setIcon(R.drawable.music_note_24)
      addOnChangeListener(AudioManager.STREAM_MUSIC)
    }
    binding.userRingerVolume.apply {
      setVolume(currentVolume.ringerVolume)
      setIcon(R.drawable.ring_volume_24)
      addOnChangeListener(AudioManager.STREAM_RING)
    }
    binding.userAlarmVolume.apply {
      setVolume(currentVolume.alarmVolume)
      setIcon(R.drawable.alarm_24)
      addOnChangeListener(AudioManager.STREAM_ALARM)
    }
    binding.userNotificationVolume.apply {
      setVolume(currentVolume.notificationVolume)
      setIcon(R.drawable.notifications_24)
      addOnChangeListener(AudioManager.STREAM_NOTIFICATION)
    }
    binding.userCallVolume.apply {
      setVolume(currentVolume.callVolume)
      setIcon(R.drawable.call_24)
      addOnChangeListener(AudioManager.STREAM_VOICE_CALL)
    }

    this.onBackPressedDispatcher.addCallback(
        this,
        object : OnBackPressedCallback(true) {
          override fun handleOnBackPressed() {
            if (tracker.hasSelection()) {
              tracker.clearSelection()
            } else {
              finish()
            }
          }
        })
  }

  override fun onResume() {
    super.onResume()
    val systemVolume = CurrentUserVolumeView.getCurrentVolume(this)
    binding.apply {
      userCallVolume.setVolume(systemVolume.callVolume)
      userMediaVolume.setVolume(systemVolume.mediaVolume)
      userRingerVolume.setVolume(systemVolume.ringerVolume)
      userAlarmVolume.setVolume(systemVolume.alarmVolume)
      userNotificationVolume.setVolume(systemVolume.notificationVolume)
    }
  }

  /** Sets up window insets to handle system bars. */
  private fun setupWindowInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(0, systemBars.top, 0, 0)
      insets
    }
  }

  /** Sets up the RecyclerView with an adapter and layout manager. */
  private fun setupRecyclerView() {
    val adapter =
        SoundProfileRecyclerAdapter(
            mutableListOf(), this, soundProfileManagerLauncher, mainViewModel::toggleIsActive)
    binding.recyclerView.apply {
      this.adapter = adapter
      layoutManager = LinearLayoutManager(this@MainActivity)
      addItemDecoration(SpaceBetweenItemDecorator(4))
    }
  }

  /** Observes changes in the ViewModel's state and updates the UI accordingly. */
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

  /** Displays a loading indicator. */
  private fun showLoading() {
    binding.apply {
      recyclerView.visibility = View.GONE
      emptyProfileIndicator.visibility = View.GONE
      loadingIndicator.visibility = View.VISIBLE
    }
  }

  /** Displays an empty state when there are no sound profiles. */
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
   *
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
    setUpAds(soundProfiles)
  }

  /**
   * Sets up the selection tracker for the RecyclerView.
   *
   * @param adapter The adapter for the RecyclerView.
   */
  private fun setupSelectionTracker(adapter: SoundProfileRecyclerAdapter) {
    tracker =
        SelectionTracker.Builder(
                "soundProfileSelection",
                binding.recyclerView,
                SoundProfileItemKeyProvider(binding.recyclerView),
                SoundProfileItemDetailsLookup(binding.recyclerView),
                StorageStrategy.createLongStorage())
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()
    adapter.tracker = tracker
    tracker.addObserver(
        object : SelectionTracker.SelectionObserver<Long>() {
          override fun onSelectionChanged() {
            super.onSelectionChanged()
            updateToolbarMenu()
          }
        })
  }

  /** Updates the toolbar menu based on the selection state. */
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
   *
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
            (binding.recyclerView.adapter as SoundProfileRecyclerAdapter).getSelectedSoundProfile())
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
   *
   * @param message The error message to display.
   */
  private fun showError(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
  }

  /**
   * Inflates the options menu.
   *
   * @param menu The options menu in which you place your items.
   * @return True for the menu to be displayed; false otherwise.
   */
  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.main_menu, menu)
    return true
  }

  /**
   * Handles options menu item clicks.
   *
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
   *
   * @return True if the info menu was displayed, false otherwise.
   */
  private fun showInfoMenu(): Boolean {
    AboutDialog().show(supportFragmentManager, "about_dialog_box")
    return true
  }

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  private fun requestNotificationPermission() {
    when {
      ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
          PackageManager.PERMISSION_GRANTED -> {
        // Permission is already granted. Continue with the notification setup.
      }
      ActivityCompat.shouldShowRequestPermissionRationale(
          this, Manifest.permission.POST_NOTIFICATIONS) -> {
        // Show an explanation to the user why the permission is needed.
        showPermissionRationaleDialog()
      }
      else -> {
        // Directly request the permission.
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  private fun showPermissionRationaleDialog() {
    val dialog =
        PermissionMessageDialog(
            icon = R.drawable.notifications_24, // Replace with your icon resource
            title = getString(R.string.permission_rationale_title),
            message = getString(R.string.permission_rationale_message),
            positiveButtonAction = { dialogFragment ->
              requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
              dialogFragment.dismiss()
            })
    dialog.show(supportFragmentManager, "PermissionMessageDialog")
  }

  lateinit var adLoader: AdLoader

  private fun setUpAds(soundProfiles: List<SoundProfile>) {
    adLoader =
        AdLoader.Builder(this, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { ad: NativeAd ->
              if (!adLoader.isLoading) {
                val adapter = binding.recyclerView.adapter as SoundProfileRecyclerAdapter
                adapter.insertAd(ad)
              }
              if (isDestroyed) {
                ad.destroy()
                return@forNativeAd
              }
            }
            .withAdListener(
                object : AdListener() {
                  override fun onAdFailedToLoad(adError: LoadAdError) {}
                })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build())
            .build()

    adLoader.loadAd(AdRequest.Builder().build())
  }

  /*
  private fun displayNativeAd(parent: LinearLayout, ad: NativeAd) {
    val view = LayoutInflater.from(this).inflate(R.layout.unifed_ad_item, parent, false)
    val adView = view.findViewById<NativeAdView>(R.id.native_ad_view)

    val headlineView = adView.findViewById<TextView>(R.id.title)
    headlineView.text = ad.headline
    adView.headlineView = headlineView

    val bodyView = adView.findViewById<TextView>(R.id.description)
    bodyView.text = ad.body
    adView.bodyView = bodyView

    val iconView = adView.findViewById<ShapeableImageView>(R.id.icon_image)
    iconView.setImageDrawable(ad.icon?.drawable)
    adView.iconView = iconView

    val callToAction = adView.findViewById<MaterialButton>(R.id.call_to_action)
    callToAction.text = ad.callToAction
    adView.callToActionView = callToAction

    adView.setNativeAd(ad)
    parent.removeAllViews()
    parent.addView(adView)
  }*/
}
