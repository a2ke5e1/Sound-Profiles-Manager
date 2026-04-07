package com.a3.soundprofiles.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a3.soundprofiles.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SoundProfilesFab(
    onAddScheduleClick: () -> Unit,
    onAddProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fabMenuExpanded by remember { mutableStateOf(false) }

    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = fabMenuExpanded,
        button = {
            val fabSize by animateDpAsState(
                targetValue = if (fabMenuExpanded) 56.dp else 80.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "fabSize"
            )
            val rotation by animateFloatAsState(
                targetValue = if (fabMenuExpanded) 45f else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "fabRotation"
            )
            val iconSize by animateDpAsState(
                targetValue = if (fabMenuExpanded) 24.dp else 40.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "iconSize"
            )

            FloatingActionButton(
                onClick = { fabMenuExpanded = !fabMenuExpanded },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(fabSize),
                shape = if (fabMenuExpanded) MaterialTheme.shapes.extraExtraLarge else MaterialTheme.shapes.largeIncreased
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_profile_content_description),
                    modifier = Modifier
                        .rotate(rotation)
                        .size(iconSize)
                )
            }
        }
    ) {
        FloatingActionButtonMenuItem(
            onClick = {
                fabMenuExpanded = false
                onAddScheduleClick()
            },
            text = { Text("Schedule") },
            icon = { Icon(Icons.Default.Schedule, contentDescription = null) },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
        FloatingActionButtonMenuItem(
            onClick = {
                fabMenuExpanded = false
                onAddProfileClick()
            },
            text = { Text("Profile") },
            icon = { Icon(Icons.Default.MusicNote, contentDescription = null) },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}