package com.a3.soundprofiles.core.main

import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.graphics.Rect
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.a3.soundprofiles.R
import com.a3.soundprofiles.SoundProfileManager.Companion.toDateTime
import com.a3.soundprofiles.core.SoundProfileScheduler
import com.a3.soundprofiles.core.data.SoundProfile
import com.a3.soundprofiles.core.di.components.PermissionMessageDialog
import com.a3.soundprofiles.core.ui.components.CurrentUserVolumeView
import com.a3.soundprofiles.databinding.CardSoundProfileItemBinding
import com.a3.soundprofiles.databinding.LabelItemBinding
import com.a3.soundprofiles.databinding.SoundDashboardItemBinding
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView


sealed class SoundProfileRecyclerType(
  ad: NativeAd? = null
) {
  data class Profile(val soundProfile: SoundProfile) : SoundProfileRecyclerType()
  data class Ad(val ad: NativeAd) : SoundProfileRecyclerType(ad = ad)
  data object Label : SoundProfileRecyclerType()
  data object SoundSettingsHeader : SoundProfileRecyclerType()
}


/**
 * Adapter class for managing sound profiles and ads in a RecyclerView.
 *
 * @property soundProfiles List of sound profiles and ads to be displayed.
 * @property activity The activity context.
 * @property soundProfileManagerLauncher Launcher for starting activities for result.
 * @property toggleIsActive Callback to toggle the active state of a sound profile.
 */
class SoundProfileRecyclerAdapter(
    val context: Context,
    val activity: AppCompatActivity,
    private val soundProfileManagerLauncher: ActivityResultLauncher<Intent>,
    private val toggleIsActive: (soundProfile: SoundProfile) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  var tracker: SelectionTracker<Long>? = null
  private val items = mutableListOf<SoundProfileRecyclerType>().apply {
    add(SoundProfileRecyclerType.SoundSettingsHeader)
    add(SoundProfileRecyclerType.Label)
  }

  companion object {
    private const val VIEW_TYPE_PROFILE = 0
    private const val VIEW_TYPE_AD = 1
    private const val VIEW_TYPE_LABEL = 2
    private const val VIEW_TYPE_SOUND_SETTINGS_HEADER = 3
  }

  init {
    setHasStableIds(true)
  }

  override fun getItemViewType(position: Int): Int {
    return when (items[position]) {
      is SoundProfileRecyclerType.Profile -> VIEW_TYPE_PROFILE
      is SoundProfileRecyclerType.Ad -> VIEW_TYPE_AD
      is SoundProfileRecyclerType.Label -> VIEW_TYPE_LABEL
      is SoundProfileRecyclerType.SoundSettingsHeader -> VIEW_TYPE_SOUND_SETTINGS_HEADER
      else -> throw IllegalArgumentException("Invalid item type")
    }
  }

  // Create new views (invoked by the layout manager)
  override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(viewGroup.context)
    return when (viewType) {
      VIEW_TYPE_PROFILE -> {
        val binding = CardSoundProfileItemBinding.inflate(inflater, viewGroup, false)
        CardSoundProfileItemHolder(activity, binding, soundProfileManagerLauncher)
      }

      VIEW_TYPE_SOUND_SETTINGS_HEADER -> {
        val binding = SoundDashboardItemBinding.inflate(inflater, viewGroup, false)
        SoundDashboardBViewHolder(context, binding)
      }

      VIEW_TYPE_AD -> {
        val view = inflater.inflate(R.layout.unifed_ad_item, viewGroup, false)
        AdViewHolder(view)
      }

      VIEW_TYPE_LABEL -> {
        val binding = LabelItemBinding.inflate(inflater, viewGroup, false)
        LabelViewHolder(binding)
      }

      else -> throw IllegalArgumentException("Invalid view type")
    }
  }

  // Replace the contents of a view (invoked by the layout manager)
  override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
    when (val item = items[position]) {
      is SoundProfileRecyclerType.Profile -> {
        val holder = viewHolder as CardSoundProfileItemHolder
        holder.bind(item.soundProfile) { soundProfile ->
          toggleIsActive(soundProfile)
        }
        tracker?.let {
          holder.bindSelection(it.isSelected(item.soundProfile.id.toLong()))
        }
      }

      is SoundProfileRecyclerType.Ad -> {
        val holder = viewHolder as AdViewHolder
        holder.bind(item.ad)
      }

      is SoundProfileRecyclerType.Label -> {
        // Do nothing
      }

      is SoundProfileRecyclerType.SoundSettingsHeader -> {
        val holder = viewHolder as SoundDashboardBViewHolder
        holder.bind()
      }
    }
  }

  /**
   * Inserts an ad at the specified position in the list.
   *
   * @param ad The ad to be inserted.
   */
  fun insertAd(ad: NativeAd) {
    if (items.size >= 4) {
      items.add(4, SoundProfileRecyclerType.Ad(ad))
      notifyItemInserted(4)
    }
  }

  // Return the size of your dataset (invoked by the layout manager)
  override fun getItemCount() = items.size

  /**
   * Adds a new sound profile to the list.
   *
   * @param soundProfile The sound profile to be added.
   */
  fun addSoundProfile(soundProfile: SoundProfile) {
    items.add(SoundProfileRecyclerType.Profile(soundProfile))
    notifyItemInserted(items.size - 1)
  }

  /**
   * Updates the list of sound profiles.
   *
   * @param newSoundProfiles The new list of sound profiles.
   */
  fun updateSoundProfiles(newSoundProfiles: List<SoundProfile>) {
    items.clear()
    items.add(SoundProfileRecyclerType.SoundSettingsHeader)
    items.add(SoundProfileRecyclerType.Label)
    items.addAll(newSoundProfiles.map { SoundProfileRecyclerType.Profile(it) })
    notifyDataSetChanged()
  }

  override fun getItemId(position: Int): Long {
    return if (getItemViewType(position) == VIEW_TYPE_PROFILE) {
      (items[position] as SoundProfileRecyclerType.Profile).soundProfile.id.toLong()
    } else {
      -1L // No sound profile will have an id of -1 so this is safe as long as we are only injecting
      // one ad
    }
  }

  /**
   * Retrieves the list of selected sound profiles.
   *
   * @return List of selected sound profiles.
   */
  fun getSelectedSoundProfile(): List<SoundProfile> {
    val selectedProfiles = mutableListOf<SoundProfile>()
    tracker?.let {
      for (item in items) {
        if (item is SoundProfileRecyclerType.Profile && it.isSelected(item.soundProfile.id.toLong())) {
          selectedProfiles.add(item.soundProfile)
        }
      }
    }
    return selectedProfiles
  }

  /** Selects all sound profiles in the list. */
  fun selectAll() {
    tracker?.let {
      for (item in items) {
        if (item is SoundProfileRecyclerType.Profile) {
          it.select(item.soundProfile.id.toLong())
        }
      }
    }
  }

  override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
    super.onViewDetachedFromWindow(holder)
    if (holder is SoundDashboardBViewHolder) {
      holder.onDetachedFromWindow()
    }
  }
}

class LabelViewHolder(binding: LabelItemBinding) : RecyclerView.ViewHolder(binding.root)

/**
 * ViewHolder class for displaying ads.
 *
 * @param view The view to be used for displaying the ad.
 */
class AdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
  private val nativeAdView: NativeAdView = view.findViewById(R.id.nativeAdView)

  /**
   * Binds the ad data to the view.
   *
   * @param ad The ad to be displayed.
   */
  fun bind(ad: NativeAd) {
    val callToAction = nativeAdView.findViewById<MaterialButton>(R.id.ctn)
    val headline = nativeAdView.findViewById<TextView>(R.id.title)
    val body = nativeAdView.findViewById<TextView>(R.id.body)
    val provider = nativeAdView.findViewById<TextView>(R.id.provider)
    val icon = nativeAdView.findViewById<ShapeableImageView>(R.id.icon)

    // The AdLoader has finished loading ads.

    headline.text = ad.headline
    body.text = ad.body
    if (ad.store != null) {
      provider.text = "${ad.store} · ${ad.price}"
    } else {
      provider.visibility = View.GONE
    }
    if (ad.icon != null) {
      icon.setImageDrawable(ad.icon!!.drawable)
    } else {
      icon.visibility = View.GONE
    }
    callToAction.text = ad.callToAction

    nativeAdView.callToActionView = callToAction
    nativeAdView.headlineView = headline
    nativeAdView.bodyView = body
    nativeAdView.storeView = provider
    nativeAdView.iconView = icon

    nativeAdView.setNativeAd(ad)
  }
}

/**
 * ViewHolder class for displaying sound profiles.
 *
 * @param activity The activity context.
 * @param binding The binding for the sound profile item view.
 * @param soundProfileManagerLauncher Launcher for starting activities for result.
 */
class CardSoundProfileItemHolder(
    val activity: AppCompatActivity,
    val binding: CardSoundProfileItemBinding,
    private val soundProfileManagerLauncher: ActivityResultLauncher<Intent>,
) : RecyclerView.ViewHolder(binding.root) {

  private val pref = PreferenceManager.getDefaultSharedPreferences(activity)
  private val editor = pref.edit()
  private val DEFAULT_PROFILE_ID = activity.getString(R.string.default_sound_profile_pref)
  /**
   * Binds the sound profile data to the view.
   *
   * @param soundProfile The sound profile to be displayed.
   * @param toggleIsActive Callback to toggle the active state of the sound profile.
   */
  fun bind(soundProfile: SoundProfile, toggleIsActive: (soundProfile: SoundProfile) -> Unit) {
    val context = binding.root.context

    val timeOnly = soundProfile.repeatEveryday || soundProfile.repeatDays.any()

    binding.title.text = soundProfile.title
    binding.description.text = soundProfile.description

    binding.description.visibility =
        if (soundProfile.description.isNotEmpty()) View.VISIBLE else View.GONE

    binding.dateRange.text =
        context.getString(
            R.string.date_range,
            soundProfile.startTime.toDateTime(timeOnly),
            soundProfile.endTime.toDateTime(timeOnly))
    binding.dateInfo.text = formatDateInfo(soundProfile)

    binding.mediaVolume.text = volumeToString(soundProfile.mediaVolume)
    binding.callVolume.text = volumeToString(soundProfile.callVolume)
    binding.notificationVolume.text = volumeToString(soundProfile.notificationVolume)
    binding.ringVolume.text = volumeToString(soundProfile.ringerVolume)
    binding.alarmVolume.text = volumeToString(soundProfile.alarmVolume)

    binding.applyNowBtn.setOnClickListener { soundProfile.applyProfile(context) }
    val soundProfileScheduler = SoundProfileScheduler(context)
    if (soundProfile.isActive) {
      binding.scheduleBtn.text = context.getString(R.string.cancel_schedule)
    } else {
      binding.scheduleBtn.text = context.getString(R.string.schedule)
    }

    val currentDefaultProfileId = pref.getInt(DEFAULT_PROFILE_ID, -1)
    binding.defaultIndicator.visibility = if (soundProfile.id == currentDefaultProfileId) {
      View.VISIBLE
    } else {
      View.GONE
    }

    binding.scheduleBtn.setOnClickListener {
      if (!handleExactAlarmPermission()) {
        return@setOnClickListener
      }

      if (soundProfile.isActive) {
        binding.scheduleBtn.text = context.getString(R.string.schedule)
        soundProfileScheduler.cancelScheduledSoundProfileApply(soundProfile)
      } else {
        binding.scheduleBtn.text = context.getString(R.string.cancel_schedule)
        soundProfileScheduler.scheduleSoundProfileApply(soundProfile)
      }
      toggleIsActive(soundProfile)
    }
  }

  /**
   * Handles the exact alarm permission for scheduling sound profiles.
   *
   * @return True if the permission is granted, false otherwise.
   */
  private fun handleExactAlarmPermission(): Boolean {
    val context = binding.root.context
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

      val scheduleExactAlarmDialogBox =
          PermissionMessageDialog(
              icon = R.drawable.alarm_24,
              title = context.getString(R.string.exact_alarm_permission_title),
              message = context.getString(R.string.exact_alarm_permission_message)) {
                Intent().also { intent ->
                  intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                  context.startActivity(intent)
                }
              }

      val alarmManager = SoundProfileScheduler(context)
      val hasPermission = alarmManager.hasScheduleExactAlarm()

      if (!hasPermission) {
        scheduleExactAlarmDialogBox.show(
            activity.supportFragmentManager, "scheduleExactAlarmDialogBox")
        return false
      }
    }
    return true
  }

  /**
   * Binds the selection state to the view.
   *
   * @param isSelected True if the item is selected, false otherwise.
   */
  fun bindSelection(isSelected: Boolean) {
    itemView.isActivated = isSelected
    binding.selectionIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE
  }

  /**
   * Retrieves the item details for selection.
   *
   * @return The item details.
   */
  fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
      object : ItemDetailsLookup.ItemDetails<Long>() {
        override fun getPosition(): Int = bindingAdapterPosition

        override fun getSelectionKey(): Long = itemId
      }

  /**
   * Formats the date information for the sound profile.
   *
   * @param soundProfile The sound profile.
   * @return The formatted date information.
   */
  private fun formatDateInfo(soundProfile: SoundProfile): String {
    return if (soundProfile.repeatEveryday || soundProfile.repeatDays.isNotEmpty()) {
      " · " + if (soundProfile.repeatEveryday) {
        "Every Day"
      } else {
        soundProfile.repeatDays.joinToString(" ") { it ->
          it.name.substring(0, 2).lowercase().replaceFirstChar { c -> c.uppercase() }
        }
      }
    } else {
      ""
    }
  }

  companion object {
    /**
     * Converts the volume to a string representation.
     *
     * @param volume The volume level.
     * @return The string representation of the volume.
     */
    fun volumeToString(volume: Float): String {
      return "${(volume * 100).toInt()}%"
    }


  }
}

class SoundDashboardBViewHolder(val context: Context, val binding: SoundDashboardItemBinding) :
  RecyclerView.ViewHolder(binding.root) {

  private val userSoundObserver =
    object : ContentObserver(Handler(context.mainLooper)) {
      override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)

        val currentVolume = CurrentUserVolumeView.getCurrentVolume(context)
        binding.apply {
          userCallVolume.setVolume(currentVolume.callVolume)
          userMediaVolume.setVolume(currentVolume.mediaVolume)
          userRingerVolume.setVolume(currentVolume.ringerVolume)
          userAlarmVolume.setVolume(currentVolume.alarmVolume)
          userNotificationVolume.setVolume(currentVolume.notificationVolume)
        }

      }
    }


  fun bind() {
    binding.userMediaVolume.apply {
      setStateBasedIcon(R.drawable.music_note_24, R.drawable.music_off_24)
      addOnChangeListener(AudioManager.STREAM_MUSIC)
    }
    binding.userRingerVolume.apply {
      setStateBasedIcon(R.drawable.ring_volume_24, R.drawable.vibration_24)
      addOnChangeListener(AudioManager.STREAM_RING)
    }
    binding.userAlarmVolume.apply {
      setStateBasedIcon(R.drawable.alarm_24, R.drawable.alarm_off_24)
      addOnChangeListener(AudioManager.STREAM_ALARM)
    }
    binding.userNotificationVolume.apply {
      setStateBasedIcon(R.drawable.notifications_24, R.drawable.notifications_off_24)
      addOnChangeListener(AudioManager.STREAM_NOTIFICATION)
    }
    binding.userCallVolume.apply {
      setIcon(R.drawable.call_24)
      addOnChangeListener(AudioManager.STREAM_VOICE_CALL)
    }

    // Set the initial volume values, and register a content observer to update the volume values
    val systemVolume = CurrentUserVolumeView.getCurrentVolume(context)
    binding.apply {
      userCallVolume.setVolume(systemVolume.callVolume)
      userMediaVolume.setVolume(systemVolume.mediaVolume)
      userRingerVolume.setVolume(systemVolume.ringerVolume)
      userAlarmVolume.setVolume(systemVolume.alarmVolume)
      userNotificationVolume.setVolume(systemVolume.notificationVolume)
    }

    context.contentResolver.registerContentObserver(
      Settings.System.CONTENT_URI,
      true,
      userSoundObserver
    )
  }

  fun onDetachedFromWindow() {
    context.contentResolver.unregisterContentObserver(userSoundObserver)
  }

}

/**
 * Item decoration class for adding space between items in a RecyclerView.
 *
 * @param spaceHeight The height of the space to be added.
 */
class SpaceBetweenItemDecorator(private val spaceHeight: Int) : RecyclerView.ItemDecoration() {
  override fun getItemOffsets(
      outRect: Rect,
      view: View,
      parent: RecyclerView,
      state: RecyclerView.State
  ) {
    with(outRect) {
      if (parent.getChildAdapterPosition(view) == 0) {
        bottom = spaceHeight
      } else if (parent.getChildAdapterPosition(view) == parent.adapter?.let { it.itemCount - 1 }) {
        top = spaceHeight
      } else {
        top = spaceHeight
        bottom = spaceHeight
      }

      left = 20
      right = 20
    }
  }
}

/**
 * Key provider class for retrieving item keys in a RecyclerView.
 *
 * @param recyclerView The RecyclerView instance.
 */
class SoundProfileItemKeyProvider(private val recyclerView: RecyclerView) :
    ItemKeyProvider<Long>(SCOPE_MAPPED) {

  override fun getKey(position: Int): Long? {
    return recyclerView.adapter?.getItemId(position)
  }

  override fun getPosition(key: Long): Int {
    val viewHolder = recyclerView.findViewHolderForItemId(key)
    return viewHolder?.layoutPosition ?: RecyclerView.NO_POSITION
  }
}

/**
 * Item details lookup class for retrieving item details in a RecyclerView.
 *
 * @param recyclerView The RecyclerView instance.
 */
class SoundProfileItemDetailsLookup(private val recyclerView: RecyclerView) :
    ItemDetailsLookup<Long>() {
  override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
    val view = recyclerView.findChildViewUnder(e.x, e.y)
    if (view == null) {
      return null
    }
    val viewHolder = recyclerView.getChildViewHolder(view)
    if (viewHolder !is CardSoundProfileItemHolder) {
      return null
    }
    return viewHolder.getItemDetails()
  }
}
