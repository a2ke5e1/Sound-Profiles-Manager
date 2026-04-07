package com.a3.soundprofiles.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.a3.soundprofiles.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SoundProfilesTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.sound_profiles_title),
    subtitle: String? = stringResource(R.string.sound_profiles_description),
    navigationIcon: @Composable () -> Unit = {},
    moreOptionsContent: @Composable (ColumnScope.(() -> Unit) -> Unit)? = null
) {
    LargeFlexibleTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
            )
        },
        subtitle = {
            if (subtitle != null) {
                val collapsedFraction = scrollBehavior.state.collapsedFraction
                // Hide when mostly collapsed (tweak threshold as needed)
                if (collapsedFraction < 0.3f) {
                    Text(text = subtitle)
                }
            }
        },
        navigationIcon = navigationIcon,
        actions = {
            if (moreOptionsContent != null) {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    TooltipBox(
                        positionProvider =
                            TooltipDefaults.rememberTooltipPositionProvider(
                                TooltipAnchorPosition.Companion.Below
                            ),
                        tooltip = { PlainTooltip { Text(stringResource(R.string.more_options_content_description)) } },
                        state = rememberTooltipState(),
                    ) {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.more_options_content_description),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    DropdownMenuPopup(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuGroup(
                            shapes = MenuDefaults.groupShape(0, 1)
                        ) {
                            moreOptionsContent { expanded = false }
                        }
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}
