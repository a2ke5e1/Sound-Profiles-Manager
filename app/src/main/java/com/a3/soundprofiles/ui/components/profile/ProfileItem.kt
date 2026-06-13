package com.a3.soundprofiles.ui.components.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.a3.soundprofiles.R
import com.a3.soundprofiles.util.mapNameToIcon
import com.a3.soundprofiles.ui.screens.ProfileItemState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileItem(
    profile: ProfileItemState,
    onClick: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    ListItem(
        onClick = { showDialog = true },
        verticalAlignment = Alignment.CenterVertically,
        content = {
            Text(
                text = profile.name,
            )
        },
        supportingContent = {
            Text(
                text = profile.subtitle,
            )
        },
        leadingContent = {
            Icon(
                imageVector = mapNameToIcon(profile.icon ?: ""),
                contentDescription = null,
            )
        },
        trailingContent = {
            Box {
                IconButton(
                    onClick = { expanded = true }, shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = stringResource(R.string.more_options_content_description)
                    )
                }
                DropdownMenuPopup(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuGroup(
                        shapes = MenuDefaults.groupShape(0, 1)
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit)) },
                            onClick = {
                                expanded = false
                                onEditClick()
                            }
                        )
                    }
                }
            }
        },
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = {
                Icon(
                    imageVector = mapNameToIcon(profile.icon ?: ""),
                    contentDescription = null,
                )
            },
            title = {
                Text(stringResource(R.string.apply_profile_title, profile.name))
            },
            text = {
                Text(stringResource(R.string.apply_profile_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onClick()
                    }
                ) {
                    Text(stringResource(R.string.apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}