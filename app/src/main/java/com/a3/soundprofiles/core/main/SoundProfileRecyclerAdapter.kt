package com.a3.soundprofiles.core.main

import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
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

  fun addSoundProfile(soundProfile: SoundProfile) {
    soundProfiles.add(soundProfile)
    notifyItemInserted(soundProfiles.size - 1)
  }

  // Optionally, a method to update the entire list
  fun updateSoundProfiles(newSoundProfiles: List<SoundProfile>) {
    soundProfiles.clear()
    soundProfiles.addAll(newSoundProfiles)
    notifyDataSetChanged()
  }

  override fun getItemId(position: Int): Long {
    return if (getItemViewType(position) == VIEW_TYPE_PROFILE) {
      (soundProfiles[position] as SoundProfile).id.toLong()
    } else {
      position.toLong()
    }
  }

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

class AdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
  private val adView: NativeAdView = view.findViewById(R.id.native_ad_view)

  fun bind(ad: NativeAd) {
    adView.headlineView = adView.findViewById<TextView>(R.id.title).apply { text = ad.headline }
    adView.bodyView = adView.findViewById<TextView>(R.id.description).apply { text = ad.body }
    adView.iconView =
        adView.findViewById<ShapeableImageView>(R.id.icon_image).apply {
          setImageDrawable(ad.icon?.drawable)
        }
    adView.callToActionView =
        adView.findViewById<MaterialButton>(R.id.call_to_action).apply { text = ad.callToAction }
    adView.setNativeAd(ad)
  }
}

class CardSoundProfileItemHolder(
    val activity: AppCompatActivity,
    val binding: CardSoundProfileItemBinding,
    private val soundProfileManagerLauncher: ActivityResultLauncher<Intent>,
) : RecyclerView.ViewHolder(binding.root) {

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

  fun bindSelection(isSelected: Boolean) {
    itemView.isActivated = isSelected
    binding.selectionIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE
  }

  fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
      object : ItemDetailsLookup.ItemDetails<Long>() {
        override fun getPosition(): Int = bindingAdapterPosition

        override fun getSelectionKey(): Long = itemId
      }

  private fun formatDateInfo(soundProfile: SoundProfile): String {
    return " · " +
        if (soundProfile.repeatEveryday) {
          "Every Day"
        } else {
          soundProfile.repeatDays.joinToString(" ") { it ->
            it.name.substring(0, 2).lowercase().replaceFirstChar { c -> c.uppercase() }
          }
        }
  }

  companion object {
    fun volumeToString(volume: Float): String {
      return "${(volume * 100).toInt()}%"
    }
  }
}

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

class SoundProfileItemDetailsLookup(private val recyclerView: RecyclerView) :
    ItemDetailsLookup<Long>() {
  override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
    val view = recyclerView.findChildViewUnder(e.x, e.y)
    return if (view != null) {
      val viewHolder = recyclerView.getChildViewHolder(view)
      if (viewHolder is CardSoundProfileItemHolder) {
        viewHolder.getItemDetails()
      } else {
        null
      }
    } else {
      null
    }
  }
}
