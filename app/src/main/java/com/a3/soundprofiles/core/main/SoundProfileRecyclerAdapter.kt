package com.a3.soundprofiles.core.main

import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
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
import com.a3.soundprofiles.databinding.CardSoundProfileItemBinding
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

/**
 * Adapter class for managing sound profiles and ads in a RecyclerView.
 *
 * @property soundProfiles List of sound profiles and ads to be displayed.
 * @property activity The activity context.
 * @property soundProfileManagerLauncher Launcher for starting activities for result.
 * @property toggleIsActive Callback to toggle the active state of a sound profile.
 */
class SoundProfileRecyclerAdapter(
    val soundProfiles: MutableList<Any>,
    val activity: AppCompatActivity,
    private val soundProfileManagerLauncher: ActivityResultLauncher<Intent>,
    private val toggleIsActive: (soundProfile: SoundProfile) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  var tracker: SelectionTracker<Long>? = null

  companion object {
    private const val VIEW_TYPE_PROFILE = 0
    private const val VIEW_TYPE_AD = 1
  }

  init {
    setHasStableIds(true)
  }

  override fun getItemViewType(position: Int): Int {
    return if (soundProfiles[position] is SoundProfile) VIEW_TYPE_PROFILE else VIEW_TYPE_AD
  }

  // Create new views (invoked by the layout manager)
  override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return if (viewType == VIEW_TYPE_PROFILE) {
      val inflater = LayoutInflater.from(viewGroup.context)
      val binding = CardSoundProfileItemBinding.inflate(inflater, viewGroup, false)
      CardSoundProfileItemHolder(activity, binding, soundProfileManagerLauncher)
    } else {
      val inflater = LayoutInflater.from(viewGroup.context)
      val view = inflater.inflate(R.layout.unifed_ad_item, viewGroup, false)
      AdViewHolder(view)
    }
  }

  // Replace the contents of a view (invoked by the layout manager)
  override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
    if (getItemViewType(position) == VIEW_TYPE_PROFILE) {
      val soundProfile = soundProfiles[position] as SoundProfile
      (viewHolder as CardSoundProfileItemHolder).bind(soundProfile) {
        toggleIsActive(it)
        notifyItemChanged(position)
      }
      tracker?.let { viewHolder.bindSelection(it.isSelected(soundProfile.id.toLong())) }
    } else {
      val ad = soundProfiles[position] as NativeAd
      (viewHolder as AdViewHolder).bind(ad)
    }
  }

  /**
   * Inserts an ad at the specified position in the list.
   *
   * @param ad The ad to be inserted.
   */
  fun insertAd(ad: NativeAd) {
    if (soundProfiles.size > 2) {
      soundProfiles.add(2, ad)
    } else {
      soundProfiles.add(ad)
    }
    notifyItemInserted(2)
  }

  // Return the size of your dataset (invoked by the layout manager)
  override fun getItemCount() = soundProfiles.size

  /**
   * Adds a new sound profile to the list.
   *
   * @param soundProfile The sound profile to be added.
   */
  fun addSoundProfile(soundProfile: SoundProfile) {
    soundProfiles.add(soundProfile)
    notifyItemInserted(soundProfiles.size - 1)
  }

  /**
   * Updates the list of sound profiles.
   *
   * @param newSoundProfiles The new list of sound profiles.
   */
  fun updateSoundProfiles(newSoundProfiles: List<SoundProfile>) {
    soundProfiles.clear()
    soundProfiles.addAll(newSoundProfiles)
    notifyDataSetChanged()
  }

  override fun getItemId(position: Int): Long {
    return if (getItemViewType(position) == VIEW_TYPE_PROFILE) {
      (soundProfiles[position] as SoundProfile).id.toLong()
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
      for (soundProfile in soundProfiles) {
        if (soundProfile is SoundProfile && it.isSelected(soundProfile.id.toLong())) {
          selectedProfiles.add(soundProfile)
        }
      }
    }
    return selectedProfiles
  }

  /** Selects all sound profiles in the list. */
  fun selectAll() {
    tracker?.let {
      for (soundProfile in soundProfiles) {
        if (soundProfile is SoundProfile) {
          it.select(soundProfile.id.toLong())
        }
      }
    }
  }
}

/**
 * ViewHolder class for displaying ads.
 *
 * @param view The view to be used for displaying the ad.
 */
class AdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
  private val adView: NativeAdView = view.findViewById(R.id.native_ad_view)

  /**
   * Binds the ad data to the view.
   *
   * @param ad The ad to be displayed.
   */
  fun bind(ad: NativeAd) {
    adView.headlineView = adView.findViewById<TextView>(R.id.title).apply { text = ad.headline }
    adView.bodyView = adView.findViewById<TextView>(R.id.description).apply { text = ad.body }
    adView.iconView =
        adView.findViewById<ShapeableImageView>(R.id.icon_image).apply {
          setImageDrawable(ad.icon?.drawable)
          visibility = if (ad.icon == null) View.GONE else View.VISIBLE
        }
    adView.callToActionView =
        adView.findViewById<MaterialButton>(R.id.call_to_action).apply { text = ad.callToAction }

    adView.setNativeAd(ad)
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
