package com.a3.soundprofiles.ui.components.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun NotificationPolicyPermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.NotificationsOff,
                contentDescription = null
            )
        },
        title = {
            Text(text = "Permission Required")
        },
        text = {
            Text(
                text = "To change ringer modes (Silent/Vibrate), the app needs \"Do Not Disturb\" access. Please grant this in the next screen."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Grant Access")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}
