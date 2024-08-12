package com.a3.soundprofiles.core.main

import android.content.Intent
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.a3.soundprofiles.R
import com.a3.soundprofiles.SoundProfileManager
import com.a3.soundprofiles.core.SoundProfileScheduler
import com.a3.soundprofiles.core.data.SoundProfile
import com.a3.soundprofiles.databinding.CardSoundProfileItemBinding

class SoundProfileRecyclerAdapter(
    private val soundProfiles: MutableList<SoundProfile>,
    private val soundProfileManagerLauncher: ActivityResultLauncher<Intent>,
    private val toggleIsActive: (soundProfile: SoundProfile) -> Unit
) : RecyclerView.Adapter<CardSoundProfileItemHolder>() {

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
    viewHolder.bind(soundProfiles[position]) {
      toggleIsActive(it)
      notifyItemChanged(position)
    }
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
}

class CardSoundProfileItemHolder(
    val binding: CardSoundProfileItemBinding,
    private val soundProfileManagerLauncher: ActivityResultLauncher<Intent>,
) : RecyclerView.ViewHolder(binding.root) {

  fun bind(soundProfile: SoundProfile, toggleIsActive: (soundProfile: SoundProfile) -> Unit) {
    val context = binding.root.context
    binding.title.text = soundProfile.title
    binding.description.text = soundProfile.description
    binding.root.setOnClickListener {
      val intent = SoundProfileManager.createIntent(context, soundProfile.id)
      soundProfileManagerLauncher.launch(intent)
    }

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
