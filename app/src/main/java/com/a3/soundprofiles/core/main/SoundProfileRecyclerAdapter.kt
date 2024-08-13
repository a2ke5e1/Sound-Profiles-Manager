package com.a3.soundprofiles.core.main

import android.content.Intent
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.a3.soundprofiles.R
import com.a3.soundprofiles.SoundProfileManager.Companion.toDateTime
import com.a3.soundprofiles.core.SoundProfileScheduler
import com.a3.soundprofiles.core.data.SoundProfile
import com.a3.soundprofiles.databinding.CardSoundProfileItemBinding

class SoundProfileRecyclerAdapter(
    val soundProfiles: MutableList<SoundProfile>,
    private val soundProfileManagerLauncher: ActivityResultLauncher<Intent>,
    private val toggleIsActive: (soundProfile: SoundProfile) -> Unit
) : RecyclerView.Adapter<CardSoundProfileItemHolder>() {

  var tracker: SelectionTracker<Long>? = null

  init {
    setHasStableIds(true)
  }

  // Create new views (invoked by the layout manager)
  override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CardSoundProfileItemHolder {
    // Create a new view, which defines the UI of the list item
    val inflater = LayoutInflater.from(viewGroup.context)
    val binding = CardSoundProfileItemBinding.inflate(inflater, viewGroup, false)
    return CardSoundProfileItemHolder(binding, soundProfileManagerLauncher)
  }

  // Replace the contents of a view (invoked by the layout manager)
  override fun onBindViewHolder(viewHolder: CardSoundProfileItemHolder, position: Int) {
    // Get element from your dataset at this position and replace the
    // contents of the view with that element
    val soundProfile = soundProfiles[position]
    viewHolder.bind(soundProfile) {
      toggleIsActive(it)
      notifyItemChanged(position)
    }
    tracker?.let { viewHolder.bindSelection(it.isSelected(soundProfile.id.toLong())) }
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

  override fun getItemId(position: Int): Long = soundProfiles[position].id.toLong()

  fun getSelectedSoundProfile(): List<SoundProfile> {
    val selectedProfiles = mutableListOf<SoundProfile>()
    tracker?.let {
      for (soundProfile in soundProfiles) {
        if (it.isSelected(soundProfile.id.toLong())) {
          selectedProfiles.add(soundProfile)
        }
      }
    }
    return selectedProfiles
  }

  fun selectAll() {
    tracker?.let {
      for (soundProfile in soundProfiles) {
        it.select(soundProfile.id.toLong())
      }
    }
  }
}

class CardSoundProfileItemHolder(
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

  private fun volumeToString(volume: Float): String {
    return "${(volume * 100).toInt()}%"
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
      (recyclerView.getChildViewHolder(view) as CardSoundProfileItemHolder).getItemDetails()
    } else {
      null
    }
  }
}
