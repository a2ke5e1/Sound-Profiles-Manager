package com.a3.soundprofiles.ui.screens

import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSizeDefaults
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.RingVolume
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.a3.soundprofiles.R
import com.a3.soundprofiles.data.local.entities.SoundProfileEntity
import com.a3.soundprofiles.mapNameToIcon
import com.a3.soundprofiles.profileIconMap
import com.a3.soundprofiles.ui.SoundProfileViewModel
import com.a3.soundprofiles.ui.components.SoundProfilesScaffold
import com.a3.soundprofiles.ui.components.core.Slider
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import kotlin.math.roundToInt

private val availableIcons = profileIconMap.keys.toList().sortedBy { it }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileConfigScreen(
    profileId: Int?,
    initialName: String = "",
    initialIcon: String = "work",
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SoundProfileViewModel = koinViewModel()
) {
    var profile by remember {
        mutableStateOf(
            viewModel.createDefaultProfile(initialName, initialIcon).copy(id = profileId ?: 0)
        )
    }

    var isEditingIdentity by rememberSaveable { 
        mutableStateOf(profileId == null || profileId == 0 || initialName.isBlank()) 
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(profileId) {
        if (profileId != null && profileId != 0) {
            viewModel.getProfileById(profileId)?.let {
                profile = it
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Profile") },
            text = { Text("Are you sure you want to delete this profile? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProfile(profile)
                        showDeleteDialog = false
                        onBackClick()
                    },
                ) {
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

    SoundProfilesScaffold(
        title = if (isEditingIdentity) if (profileId == null || profileId == 0) "Create Profile" else "Edit Profile" else profile.name,
        onBackClick = {
            if (isEditingIdentity && profileId != null && profileId != 0) {
                isEditingIdentity = false
            } else {
                onBackClick()
            }
        },
        moreOptionsContent = if (!isEditingIdentity) {
            { onDismiss ->
                DropdownMenuItem(
                    text = { Text("Rename") },
                    onClick = {
                        onDismiss()
                        isEditingIdentity = true
                    }
                )
                if (profileId != null && profileId != 0) {
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
        modifier = modifier,
        floatingActionButton = {
            if (!isEditingIdentity) {
                FloatingActionButton(onClick = {
                    viewModel.saveProfile(profile)
                    onBackClick()
                }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save"
                    )
                }
            }
        }
    ) { paddingValues ->
        if (isEditingIdentity) {
            IdentityEditor(
                name = profile.name,
                onNameChange = { profile = profile.copy(name = it) },
                selectedIcon = profile.iconName ?: "work",
                onIconSelected = { profile = profile.copy(iconName = it) },
                modifier = Modifier.padding(paddingValues),
                onContinue = { isEditingIdentity = false }
            )
        } else {
            VolumeEditor(
                profile = profile,
                onProfileChange = { profile = it },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun IdentityEditor(
    name: String,
    onNameChange: (String) -> Unit,
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 56.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .size(142.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialShapes.Cookie4Sided.toShape()
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = mapNameToIcon(selectedIcon),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Profile Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Choose an icon",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }

        items(availableIcons) { iconName ->
            val isSelected = selectedIcon == iconName
            FilledIconToggleButton(
                checked = isSelected,
                onCheckedChange = { if (it) onIconSelected(iconName) },
                modifier = Modifier.aspectRatio(1f),
                shapes = IconButtonDefaults.toggleableShapes(),
            ) {
                Icon(
                    imageVector = mapNameToIcon(iconName),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onContinue,
                    enabled = name.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(
                        text = "Continue to Volumes",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun VolumeEditor(
    profile: SoundProfileEntity,
    onProfileChange: (SoundProfileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
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
                imageVector = mapNameToIcon(profile.iconName ?: "work"),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Ring Mode Section
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.choose_ring_mode),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            ) {
                val modes = listOf(
                    Triple(stringResource(R.string.ring_mode_ring), Icons.Default.MusicNote, AudioManager.RINGER_MODE_NORMAL),
                    Triple(stringResource(R.string.ring_mode_vibrate), Icons.Default.Vibration, AudioManager.RINGER_MODE_VIBRATE),
                    Triple(stringResource(R.string.ring_mode_silent), Icons.Default.NotificationsOff, AudioManager.RINGER_MODE_SILENT)
                )
                val modifiers = listOf(Modifier.weight(1f), Modifier.weight(1f), Modifier.weight(1f))
                modes.forEachIndexed { index, (label, icon, modeValue) ->
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                            TooltipAnchorPosition.Below
                        ),
                        tooltip = { PlainTooltip { Text(text = label) } },
                        state = rememberTooltipState(),
                    ) {
                        ToggleButton(
                            modifier = modifiers[index].semantics { role = Role.RadioButton },
                            checked = profile.ringerMode == modeValue,
                            onCheckedChange = {
                                if (it) onProfileChange(profile.applyRingerModeRules(modeValue))
                            },
                            shapes =
                            when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                modes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                            contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                            Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Custom Section (Volumes)
        Column(modifier = Modifier.fillMaxWidth()) {
            val isSilentOrVibrate =
                profile.ringerMode == AudioManager.RINGER_MODE_SILENT ||
                profile.ringerMode == AudioManager.RINGER_MODE_VIBRATE

            Text(
                text = stringResource(R.string.custom_header),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            VolumeSlider(
                label = stringResource(R.string.volume_media),
                value = profile.musicVolume.current,
                min = profile.musicVolume.min,
                max = profile.musicVolume.max,
                onValueChange = { onProfileChange(profile.copy(musicVolume = profile.musicVolume.copy(current = it))) },
                icon = if (profile.musicVolume.current == 0) Icons.Default.MusicOff else Icons.Default.MusicNote                
            )
            VolumeSlider(
                label = stringResource(R.string.volume_call),
                value = profile.voiceVolume.current,
                min = profile.voiceVolume.min,
                max = profile.voiceVolume.max,
                onValueChange = { onProfileChange(profile.copy(voiceVolume = profile.voiceVolume.copy(current = it))) },
                icon = Icons.Default.Call
            )
            VolumeSlider(
                label = stringResource(R.string.volume_ring),
                value = profile.ringerVolume.current,
                min = profile.ringerVolume.min,
                max = profile.ringerVolume.max,
                onValueChange = { onProfileChange(profile.copy(ringerVolume = profile.ringerVolume.copy(current = it))) },
                icon = Icons.Default.RingVolume,
                enabled = !isSilentOrVibrate
            )
            VolumeSlider(
                label = stringResource(R.string.volume_notification),
                value = profile.notificationVolume.current,
                min = profile.notificationVolume.min,
                max = profile.notificationVolume.max,
                onValueChange = { onProfileChange(profile.copy(notificationVolume = profile.notificationVolume.copy(current = it))) },
                icon = Icons.Default.Notifications,
                enabled = !isSilentOrVibrate
            )
            VolumeSlider(
                label = stringResource(R.string.volume_alarm),
                value = profile.alarmVolume.current,
                min = profile.alarmVolume.min,
                max = profile.alarmVolume.max,
                onValueChange = { onProfileChange(profile.copy(alarmVolume = profile.alarmVolume.copy(current = it))) },
                icon = Icons.Default.Alarm
            )
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolumeSlider(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    onValueChange: (Int) -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Slider(
        modifier = modifier,
        title = label,
        startIcon = rememberVectorPainter(icon),
        value = value.toFloat(),
        valueRange = min.toFloat()..max.toFloat(),
        steps = 0,
        disabled = !enabled,
        onValueChange = { onValueChange(it.roundToInt()) },
        labelFormatter = {
            val format = NumberFormat.getPercentInstance()
            val percent = if (max > min) ((it - min) / (max - min)) else 0
            format.format(percent)
        }
    )
}
