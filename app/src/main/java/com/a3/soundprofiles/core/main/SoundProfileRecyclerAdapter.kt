package com.a3.soundprofiles.core.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.a3.soundprofiles.core.data.SoundProfile
import com.a3.soundprofiles.databinding.CardSoundProfileBinding

class SoundProfileRecyclerAdapter(private val dataSet: List<SoundProfile>) :
    RecyclerView.Adapter<CardSoundProfileItemHolder>() {

  // Create new views (invoked by the layout manager)
  override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CardSoundProfileItemHolder {
    // Create a new view, which defines the UI of the list item
    val inflater = LayoutInflater.from(viewGroup.context)
    val binding = CardSoundProfileBinding.inflate(inflater, viewGroup, false)
    return CardSoundProfileItemHolder(binding)
  }

  // Replace the contents of a view (invoked by the layout manager)
  override fun onBindViewHolder(viewHolder: CardSoundProfileItemHolder, position: Int) {
    // Get element from your dataset at this position and replace the
    // contents of the view with that element
    viewHolder.bind(dataSet[position])
  }

  // Return the size of your dataset (invoked by the layout manager)
  override fun getItemCount() = dataSet.size
}

class CardSoundProfileItemHolder(val binding: CardSoundProfileBinding) :
    RecyclerView.ViewHolder(binding.root) {

  fun bind(soundProfile: SoundProfile) {
    binding.soundProfileName.text = soundProfile.title
    binding.root.setOnClickListener {
      soundProfile.applyProfile(binding.root.context)
    }
  }
}
