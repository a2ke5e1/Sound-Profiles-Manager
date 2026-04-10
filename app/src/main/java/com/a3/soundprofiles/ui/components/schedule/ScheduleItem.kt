package com.a3.soundprofiles.ui.components.schedule

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.a3.soundprofiles.util.mapNameToIcon
import com.a3.soundprofiles.ui.screens.ScheduleItemState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScheduleItem(
    schedule: ScheduleItemState,
    onClick: () -> Unit = {}
) {
    ListItem(
        selected = schedule.isActive,
        onClick = onClick,
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(vertical = 22.dp, horizontal = 16.dp),
        content = {
            Text(
                text = schedule.name,
            )
        },
        supportingContent = {
            Text(
                text = schedule.subtitle,
            )
        },
        leadingContent = {
            Icon(
                imageVector = mapNameToIcon(schedule.icon ?: ""),
                contentDescription = null,
            )
        },
//        trailingContent = {
//            IconButton(
//                onClick = {}, shapes = IconButtonDefaults.shapes()
//            ) {
//                Icon(
//                    imageVector = Icons.Default.MoreHoriz,
//                    contentDescription = stringResource(R.string.more_options_content_description),
//                )
//            }
//        },
    )
}