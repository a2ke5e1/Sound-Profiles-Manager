package com.a3.soundprofiles.ui.components.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a3.soundprofiles.data.local.entities.SoundProfileEntity
import com.a3.soundprofiles.mapNameToIcon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileSection(
    label: String,
    time: Date,
    profile: SoundProfileEntity?,
    onClick: () -> Unit
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = timeFormat.format(time),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        ListItem(
            modifier = Modifier.clickable { onClick() },
            headlineContent = {
                Text(
                    text = profile?.name ?: "Select a profile",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            supportingContent = {
                if (profile != null) {
                    Text(
                        text = profile.profileSummary(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            leadingContent = {
                Icon(
                    imageVector = if (profile != null) mapNameToIcon(profile.iconName ?: "volume_up")
                    else Icons.Default.Schedule,
                    contentDescription = null
                )
            },
            trailingContent = {
                IconButton(
                    onClick = onClick,
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Change profile"
                    )
                }
            }
        )
    }
}
