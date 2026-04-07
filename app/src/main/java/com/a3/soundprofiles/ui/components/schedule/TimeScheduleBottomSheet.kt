package com.a3.soundprofiles.ui.components.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.a3.soundprofiles.data.local.entities.DAY
import com.a3.soundprofiles.ui.components.core.Switch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeScheduleBottomSheet(
    startTime: Date,
    endTime: Date,
    repeatEveryday: Boolean,
    daysOfWeek: List<DAY>,
    onStartTimeChange: (Date) -> Unit,
    onEndTimeChange: (Date) -> Unit,
    onRepeatEverydayChange: (Boolean) -> Unit,
    onDaysChanged: (List<DAY>) -> Unit,
    onDismiss: () -> Unit
) {
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MMMM dd", Locale.getDefault())

    val startCal = Calendar.getInstance().apply { time = startTime }
    val endCal = Calendar.getInstance().apply { time = endTime }

    // Time Pickers
    if (showStartTimePicker) {
        TimePickerDialog(
            initialHour = startCal.get(Calendar.HOUR_OF_DAY),
            initialMinute = startCal.get(Calendar.MINUTE),
            validator = { _, _ -> true },
            onTimeSelected = { hour, minute ->
                val cal = Calendar.getInstance().apply {
                    time = startTime
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }
                onStartTimeChange(cal.time)
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            initialHour = endCal.get(Calendar.HOUR_OF_DAY),
            initialMinute = endCal.get(Calendar.MINUTE),
            validator = { _, _ -> true },
            onTimeSelected = { hour, minute ->
                val cal = Calendar.getInstance().apply {
                    time = endTime
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }
                onEndTimeChange(cal.time)
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false }
        )
    }

    // Date Pickers
    if (showStartDatePicker) {
        DatePickerDialogWrapper(
            initialDate = startTime,
            validator = { _ -> true },
            onDateSelected = { date ->
                val cal = Calendar.getInstance().apply {
                    time = date
                    val ref = Calendar.getInstance().apply { time = startTime }
                    set(Calendar.HOUR_OF_DAY, ref.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, ref.get(Calendar.MINUTE))
                }
                onStartTimeChange(cal.time)
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialogWrapper(
            initialDate = endTime,
            validator = { _ -> true },
            onDateSelected = { date ->
                val cal = Calendar.getInstance().apply {
                    time = date
                    val ref = Calendar.getInstance().apply { time = endTime }
                    set(Calendar.HOUR_OF_DAY, ref.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, ref.get(Calendar.MINUTE))
                }
                onEndTimeChange(cal.time)
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(visible = !startTime.before(endTime)) {
                Text(
                    text = "End date/time must be after start date/time",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            
            // Start / End date and time
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp)
                ,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Start",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (!repeatEveryday) {
                        Text(
                            text = dateFormat.format(startTime),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showStartDatePicker = true }
                        )
                    }
                    Text(
                        text = timeFormat.format(startTime),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { showStartTimePicker = true }
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "End",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (!repeatEveryday) {
                        Text(
                            text = dateFormat.format(endTime),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showEndDatePicker = true }
                        )
                    }
                    Text(
                        text = timeFormat.format(endTime),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { showEndTimePicker = true }
                    )
                }
            }

            // Repeat Every Day Toggle
            Switch(
                title = "Repeat Every Day",
                description = if (repeatEveryday) "Profile will be repeated every day." else "Select specific days below.",
                checked = repeatEveryday,
                onCheckedChange = onRepeatEverydayChange,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Day selector — shown when repeat is OFF
            AnimatedVisibility(visible = !repeatEveryday) {
                DayOfWeekSelector(
                    selectedDays = daysOfWeek,
                    onDaysChanged = onDaysChanged,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                )
            }
        }
    }
}
