package com.a3.soundprofiles.ui.components.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a3.soundprofiles.data.local.entities.SoundProfileEntity
import com.a3.soundprofiles.mapNameToIcon

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfilePickerDialog(
    profiles: List<SoundProfileEntity>,
    selectedProfileId: Int?,
    onProfileSelected: (SoundProfileEntity) -> Unit,
    onDismiss: () -> Unit,
    title: String
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
            )

            if (profiles.isEmpty()) {
                Text(
                    text = "No profiles available. Please create a profile first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            } else {
                profiles.forEachIndexed { index, profile ->
                    ListItem(
                        checked = profile.id == selectedProfileId,
                        onCheckedChange = { onProfileSelected(profile) },
                        modifier = Modifier.padding(horizontal = 8.dp),
                        content = { Text(profile.name) },
                        supportingContent = { Text(profile.profileSummary()) },
                        leadingContent = {
                            Icon(
                                imageVector = mapNameToIcon(profile.iconName ?: "volume_up"),
                                contentDescription = null
                            )
                        },
                        shapes = ListItemDefaults.segmentedShapes(index, profiles.size)
                    )
                }
            }
        }
    }
}
