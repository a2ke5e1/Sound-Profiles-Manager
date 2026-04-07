package com.a3.soundprofiles.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.a3.soundprofiles.data.local.entities.ScheduleEntity
import com.a3.soundprofiles.ui.SoundProfileViewModel
import com.a3.soundprofiles.ui.components.SoundProfilesScaffold
import com.a3.soundprofiles.ui.components.schedule.ProfilePickerDialog
import com.a3.soundprofiles.ui.components.schedule.TimeScheduleBottomSheet
import com.a3.soundprofiles.ui.components.schedule.ScheduleDetailsEditor
import com.a3.soundprofiles.ui.components.schedule.ScheduleIdentityEditor
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScheduleConfigScreen(
    scheduleId: Int?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SoundProfileViewModel = koinViewModel()
) {
    val profiles by viewModel.getAllProfileEntities().collectAsState()
    val context = LocalContext.current

    val now = Calendar.getInstance()
    val defaultStart = now.time
    val defaultEndCal = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 8) }
    val defaultEnd = defaultEndCal.time

    var schedule by remember {
        mutableStateOf(
            ScheduleEntity(
                scheduleId = scheduleId ?: 0,
                profileId = 0,
                fallbackProfileId = null,
                startTime = defaultStart,
                endTime = defaultEnd,
                daysOfWeek = emptyList(),
                isEnabled = true,
                isActive = false,
                repeatEveryday = true,
                name = "",
                iconName = "schedule"
            )
        )
    }

    // Identity editing: same flow as ProfileConfigScreen
    var isEditingIdentity by rememberSaveable {
        mutableStateOf(scheduleId == null || scheduleId == 0)
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStartProfilePicker by remember { mutableStateOf(false) }
    var showEndProfilePicker by remember { mutableStateOf(false) }
    var showTimeScheduleSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showAlarmPermissionDialog by remember { mutableStateOf(false) }

    fun checkAndSaveSchedule() {
        if (!schedule.startTime.before(schedule.endTime)) {
            coroutineScope.launch { snackbarHostState.showSnackbar("End date/time must be after start date/time") }
            return
        }
        if (schedule.fallbackProfileId != null && schedule.profileId == schedule.fallbackProfileId) {
            coroutineScope.launch { snackbarHostState.showSnackbar("Start and End profiles must be different") }
            return
        }

        // Check Exact Alarm Permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                showAlarmPermissionDialog = true
                return
            }
        }

        viewModel.saveSchedule(schedule)
        onBackClick()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        // Proceed regardless of notification permission for now, but inform user if needed.
        checkAndSaveSchedule()
    }

    fun requestPermissionsAndSave() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            checkAndSaveSchedule()
        }
    }

    if (showAlarmPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showAlarmPermissionDialog = false },
            title = { Text("Exact Alarms Permission") },
            text = { Text("To automatically switch sound profiles at exact times, the app requires 'Alarms & Reminders' permission. Please enable it in the app settings.") },
            confirmButton = {
                TextButton(onClick = {
                    showAlarmPermissionDialog = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        context.startActivity(intent)
                    }
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlarmPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(scheduleId) {
        if (scheduleId != null && scheduleId != 0) {
            viewModel.getScheduleById(scheduleId)?.let {
                schedule = it
            }
        }
    }

    // Auto-assign first profile if none selected
    LaunchedEffect(profiles, schedule.profileId) {
        if (schedule.profileId == 0 && profiles.isNotEmpty()) {
            schedule = schedule.copy(profileId = profiles.first().id)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Schedule") },
            text = { Text("Are you sure you want to delete this schedule? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSchedule(schedule)
                    showDeleteDialog = false
                    onBackClick()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showStartProfilePicker) {
        ProfilePickerDialog(
            profiles = profiles,
            selectedProfileId = schedule.profileId,
            onProfileSelected = {
                val newSchedule = schedule.copy(profileId = it.id)
                schedule = if (newSchedule.fallbackProfileId == it.id) {
                    newSchedule.copy(fallbackProfileId = null)
                } else {
                    newSchedule
                }
                showStartProfilePicker = false
            },
            onDismiss = { showStartProfilePicker = false },
            title = "Select Start Profile"
        )
    }

    if (showEndProfilePicker) {
        ProfilePickerDialog(
            profiles = profiles.filter { it.id != schedule.profileId },
            selectedProfileId = schedule.fallbackProfileId,
            onProfileSelected = {
                schedule = schedule.copy(fallbackProfileId = it.id)
                showEndProfilePicker = false
            },
            onDismiss = { showEndProfilePicker = false },
            title = "Select End Profile"
        )
    }

    // Time Schedule Bottom Sheet
    if (showTimeScheduleSheet) {
        TimeScheduleBottomSheet(
            startTime = schedule.startTime,
            endTime = schedule.endTime,
            repeatEveryday = schedule.repeatEveryday,
            daysOfWeek = schedule.daysOfWeek,
            onStartTimeChange = { schedule = schedule.copy(startTime = it) },
            onEndTimeChange = { schedule = schedule.copy(endTime = it) },
            onRepeatEverydayChange = { checked ->
                schedule = if (checked) {
                    schedule.copy(repeatEveryday = true, daysOfWeek = emptyList())
                } else {
                    schedule.copy(repeatEveryday = false)
                }
            },
            onDaysChanged = { schedule = schedule.copy(daysOfWeek = it) },
            onDismiss = { showTimeScheduleSheet = false }
        )
    }

    SoundProfilesScaffold(
        title = if (isEditingIdentity) {
            if (scheduleId == null || scheduleId == 0) "Create Schedule" else "Edit Schedule"
        } else {
            schedule.name
        },
        onBackClick = {
            if (isEditingIdentity && scheduleId != null && scheduleId != 0) {
                isEditingIdentity = false
            } else {
                onBackClick()
            }
        },
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        moreOptionsContent = if (!isEditingIdentity) {
            { onDismiss ->
                DropdownMenuItem(
                    text = { Text("Rename") },
                    onClick = {
                        onDismiss()
                        isEditingIdentity = true
                    }
                )
                if (scheduleId != null && scheduleId != 0) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Delete",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            onDismiss()
                            showDeleteDialog = true
                        }
                    )
                }
            }
        } else null,
        floatingActionButton = {
            if (!isEditingIdentity) {
                FloatingActionButton(onClick = {
                    requestPermissionsAndSave()
                }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save Schedule"
                    )
                }
            }
        }
    ) { paddingValues ->
        if (isEditingIdentity) {
            // Name + Icon editor (same flow as ProfileConfigScreen)
            ScheduleIdentityEditor(
                name = schedule.name,
                onNameChange = { schedule = schedule.copy(name = it) },
                selectedIcon = schedule.iconName ?: "schedule",
                onIconSelected = { schedule = schedule.copy(iconName = it) },
                modifier = Modifier.padding(paddingValues),
                onContinue = { isEditingIdentity = false }
            )
        } else {
            // Schedule details editor
            ScheduleDetailsEditor(
                schedule = schedule,
                profiles = profiles,
                onToggleEnabled = { schedule = schedule.copy(isEnabled = it) },
                onTimeScheduleClick = { showTimeScheduleSheet = true },
                onStartProfileClick = { showStartProfilePicker = true },
                onEndProfileClick = { showEndProfilePicker = true },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
