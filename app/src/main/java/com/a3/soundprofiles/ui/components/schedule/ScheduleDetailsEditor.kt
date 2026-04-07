package com.a3.soundprofiles.ui.components.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a3.soundprofiles.data.local.entities.ScheduleEntity
import com.a3.soundprofiles.data.local.entities.SoundProfileEntity
import com.a3.soundprofiles.mapNameToIcon
import com.a3.soundprofiles.ui.components.core.SwitchWithOptions
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScheduleDetailsEditor(
    schedule: ScheduleEntity,
    profiles: List<SoundProfileEntity>,
    onToggleEnabled: (Boolean) -> Unit,
    onTimeScheduleClick: () -> Unit,
    onStartProfileClick: () -> Unit,
    onEndProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Large Icon
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialShapes.Cookie4Sided.toShape()
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = mapNameToIcon(schedule.iconName ?: "schedule"),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // "Choose ring mode" label
        Text(
            text = "Choose ring mode",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, start = 16.dp, end = 16.dp)
        )

        // Time range row with enable/disable toggle
        // Tap on left side opens the time schedule bottom sheet
        val timeRange = "${timeFormat.format(schedule.startTime)} - ${timeFormat.format(schedule.endTime)}"
        val daysText = if (schedule.repeatEveryday) {
            "Every day"
        } else if (schedule.daysOfWeek.isNotEmpty()) {
            schedule.daysOfWeek.joinToString(" and ") {
                it.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() }
            }
        } else {
            "No days selected"
        }

        SwitchWithOptions(
            title = timeRange,
            summary = daysText,
            checked = schedule.isEnabled,
            onCheckedChange = onToggleEnabled,
            onOptionClicked = onTimeScheduleClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Start Profile Section
        ProfileSection(
            label = "Start",
            time = schedule.startTime,
            profile = profiles.find { it.id == schedule.profileId },
            onClick = onStartProfileClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // End / Fallback Profile Section
        ProfileSection(
            label = "End",
            time = schedule.endTime,
            profile = profiles.find { it.id == schedule.fallbackProfileId },
            onClick = onEndProfileClick
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}
