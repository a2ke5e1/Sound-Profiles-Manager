package com.a3.soundprofiles

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.a3.soundprofiles.core.data.DAY
import com.a3.soundprofiles.core.main.CreateEditSoundProfileViewModel
import com.a3.soundprofiles.databinding.ActivitySoundProfileManagerBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.timepicker.MaterialTimePicker
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class SoundProfileManager : AppCompatActivity() {

  private lateinit var _binding: ActivitySoundProfileManagerBinding
  private val binding
    get() = _binding

  private val soundProfileId: Int
    get() = intent.getIntExtra(SOUND_PROFILE_ID, -1)

  private val createEditSoundProfileViewModel: CreateEditSoundProfileViewModel by viewModels()
  private var isProgrammaticChange = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    _binding = ActivitySoundProfileManagerBinding.inflate(layoutInflater)
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

    if (soundProfileId > 0) {
      createEditSoundProfileViewModel.loadSoundProfile(soundProfileId)
    }

    val labelFormatter = LabelFormatter { value ->
      val volume = (value * 100).toInt()
      "$volume%"
    }
    binding.ringerVolume.setLabelFormatter(labelFormatter)
    binding.mediaVolume.setLabelFormatter(labelFormatter)
    binding.notificationVolume.setLabelFormatter(labelFormatter)
    binding.callVolume.setLabelFormatter(labelFormatter)
    binding.alarmVolume.setLabelFormatter(labelFormatter)

    createEditSoundProfileViewModel.soundProfile.observe(this) { soundProfile ->
      isProgrammaticChange = true
      binding.title.setText(soundProfile.title)
      binding.description.setText(soundProfile.description)
      binding.ringerVolume.value = soundProfile.ringerVolume
      binding.mediaVolume.value = soundProfile.mediaVolume
      binding.notificationVolume.value = soundProfile.notificationVolume
      binding.callVolume.value = soundProfile.callVolume
      binding.alarmVolume.value = soundProfile.alarmVolume
      binding.everyDay.isChecked = soundProfile.repeatEveryday

      if (soundProfile.repeatEveryday) {
        binding.selectDayContainer
            .animate()
            .alpha(0f)
            .withEndAction { binding.selectDayContainer.visibility = View.GONE }
            .duration = 200
      } else {
        binding.selectDayContainer
            .animate()
            .alpha(1f)
            .withStartAction { binding.selectDayContainer.visibility = View.VISIBLE }
            .duration = 200
      }

      binding.selectDayContainer.removeAllViews()
      DAY.entries.forEach { day ->
        val chip: Chip = Chip(this)
        val drawable = ChipDrawable.createFromAttributes(this, null, 0, R.style.Widget_Chip)
        chip.setChipDrawable(drawable)
        chip.text =
            day.name.toString().substring(0, 2).lowercase().replaceFirstChar { it.uppercase() }
        chip.tag = day.ordinal
        chip.isCheckable = true
        chip.isClickable = true
        chip.isCheckedIconVisible = false
        chip.isChecked = soundProfile.repeatDays.contains(day)
        binding.selectDayContainer.addView(chip)
      }

      val timeOnly = soundProfile.repeatEveryday || soundProfile.repeatDays.any()
      binding.startDatetime.text = soundProfile.startTime.toDateTime(timeOnly)
      binding.endDatetime.text = soundProfile.endTime.toDateTime(timeOnly)

      binding.startDatetime.setOnClickListener {
        showDateTimePicker(
            soundProfile.startTime,
            { newDate -> createEditSoundProfileViewModel.setStartTime(newDate) },
            timeOnly)
      }

      binding.endDatetime.setOnClickListener {
        showDateTimePicker(
            soundProfile.endTime,
            { newDate -> createEditSoundProfileViewModel.setEndTime(newDate) },
            timeOnly)
      }

      isProgrammaticChange = false
      Log.d("SoundProfileManager", "Sound profile loaded: $soundProfile")
    }

    binding.ringerVolume.addOnChangeListener { _, value, _ ->
      createEditSoundProfileViewModel.setRingerVolume(value)
    }
    binding.mediaVolume.addOnChangeListener { _, value, _ ->
      createEditSoundProfileViewModel.setMediaVolume(value)
    }
    binding.notificationVolume.addOnChangeListener { _, value, _ ->
      createEditSoundProfileViewModel.setNotificationVolume(value)
    }
    binding.callVolume.addOnChangeListener { _, value, _ ->
      createEditSoundProfileViewModel.setCallVolume(value)
    }
    binding.alarmVolume.addOnChangeListener { _, value, _ ->
      createEditSoundProfileViewModel.setAlarmVolume(value)
    }

    binding.selectDayContainer.setOnCheckedStateChangeListener { chip, isChecked ->
      val checked = binding.selectDayContainer.checkedChipIds
      val selectedDays = mutableListOf<DAY>()
      checked.forEach { id ->
        findViewById<Chip>(id).tag?.let { tag ->
          selectedDays.add(DAY.entries[tag.toString().toInt()])
        }
      }
      createEditSoundProfileViewModel.setRepeatDays(selectedDays.toList())
    }

    binding.title.doAfterTextChanged {
      if (isProgrammaticChange) return@doAfterTextChanged
      val cursorPosition = binding.title.selectionStart
      createEditSoundProfileViewModel.setTitle(it.toString())
      binding.title.setSelection(cursorPosition)
    }

    binding.description.doAfterTextChanged {
      if (isProgrammaticChange) return@doAfterTextChanged
      val cursorPosition = binding.description.selectionStart
      createEditSoundProfileViewModel.setDescription(it.toString())
      binding.description.setSelection(cursorPosition)
    }

    binding.everyDay.setOnCheckedChangeListener { _, isChecked ->
      createEditSoundProfileViewModel.setRepeatEveryday(isChecked)
    }

    binding.saveSoundProfile.setOnClickListener {
      createEditSoundProfileViewModel.saveSoundProfile()
      Log.d("SoundProfileManager", "Setting result to RESULT_OK")
      setResult(RESULT_OK)
      finish()
    }
  }

  private fun Date.toDateTime(onlyTime: Boolean = false): String {
    val format = if (onlyTime) "HH:mm" else "MMM dd, yyyy HH:mm"
    return SimpleDateFormat(format, Locale.getDefault()).format(this)
  }

  private fun Date.setDate(unix: Long): Date {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = unix

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    calendar.time = this

    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month)
    calendar.set(Calendar.DAY_OF_MONTH, day)

    return calendar.time
  }

  private fun Date.setTime(hour: Int, minute: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
  }

  private fun showDateTimePicker(
      initialDate: Date,
      onDateTimeSelected: (Date) -> Unit,
      timeOnly: Boolean
  ) {
    val datePicker = MaterialDatePicker.Builder.datePicker().build()

    val cal = Calendar.getInstance()
    cal.timeInMillis = if (timeOnly) System.currentTimeMillis() else initialDate.time
    val initialHour = cal.get(Calendar.HOUR_OF_DAY)
    val initialMinute = cal.get(Calendar.MINUTE)

    val timePicker =
        MaterialTimePicker.Builder().setHour(initialHour).setMinute(initialMinute).build().apply {
          addOnPositiveButtonClickListener {
            val selectedTime = initialDate.setTime(hour, minute)
            onDateTimeSelected(selectedTime)
          }
        }

    datePicker.addOnPositiveButtonClickListener {
      val selectedDate = initialDate.setDate(it)
      timePicker.addOnPositiveButtonClickListener {
        val selectedDateTime = selectedDate.setTime(timePicker.hour, timePicker.minute)
        onDateTimeSelected(selectedDateTime)
      }
      timePicker.show(supportFragmentManager, "timePicker")
    }

    if (timeOnly) {
      timePicker.show(supportFragmentManager, "timePicker")
    } else {
      datePicker.show(supportFragmentManager, "datePicker")
    }
  }

  companion object {
    const val SOUND_PROFILE_ID = "soundProfileId"

    /**
     * Creates an Intent to start the SoundProfileManager activity.
     *
     * @param context The context from which the Intent is being created.
     * @param soundProfileId The ID of the sound profile to be managed. Defaults to -1 if not
     *   provided.
     * @return The Intent to start the SoundProfileManager activity.
     */
    fun createIntent(context: Context, soundProfileId: Int = -1): Intent =
        Intent(context, SoundProfileManager::class.java).apply {
          putExtra(SOUND_PROFILE_ID, soundProfileId)
        }
  }
}
