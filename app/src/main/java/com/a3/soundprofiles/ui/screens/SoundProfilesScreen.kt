package com.a3.soundprofiles.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a3.soundprofiles.R
import com.a3.soundprofiles.ui.components.SoundProfilesFab
import com.a3.soundprofiles.ui.components.SoundProfilesTopAppBar
import com.a3.soundprofiles.ui.components.ad.AdCard
import com.a3.soundprofiles.ui.components.core.AboutModal
import com.a3.soundprofiles.ui.components.profile.ProfileItem
import com.a3.soundprofiles.ui.components.schedule.ScheduleItem
import com.a3.soundprofiles.ui.theme.SoundProfilesTheme
import com.a3.soundprofiles.util.CommunityUtil

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SoundProfilesScreen(
    modifier: Modifier = Modifier,
    schedules: List<ScheduleItemState> = emptyList(),
    profiles: List<ProfileItemState> = emptyList(),
    onBackClick: () -> Unit = {},
    onAddProfileClick: () -> Unit = {},
    onAddScheduleClick: () -> Unit = {},
    onScheduleClick: (ScheduleItemState) -> Unit = {},
    onProfileClick: (ProfileItemState) -> Unit = {},
    onApplyProfileClick: (ProfileItemState) -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    var openAboutModal by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SoundProfilesTopAppBar(
                scrollBehavior = scrollBehavior,
                moreOptionsContent = {
                    DropdownMenuItem(text = { Text(stringResource(R.string.share)) }, onClick = {
                        CommunityUtil.onShare(context)
                    })
                    DropdownMenuItem(text = { Text(stringResource(R.string.about)) }, onClick = {
                        openAboutModal = true
                    })
                }
            )
        },
        floatingActionButton = {
            SoundProfilesFab(
                onAddScheduleClick = onAddScheduleClick,
                onAddProfileClick = onAddProfileClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.your_schedule_header),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp, top = 24.dp)
                )
                if (schedules.isEmpty()) {
                    Text(
                        text = AnnotatedString.fromHtml(stringResource(R.string.no_schedule_guide)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 0.dp, bottom = 8.dp)
                    )
                }
            }

            items(schedules) { schedule ->
                ScheduleItem(
                    schedule = schedule,
                    onClick = { onScheduleClick(schedule) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.your_profiles_header),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
                if (profiles.isEmpty()) {
                    Text(
                        text = AnnotatedString.fromHtml(stringResource(R.string.no_profile_guide)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 0.dp, bottom = 8.dp)
                    )
                }
            }

            items(profiles) { profile ->
                ProfileItem(
                    profile = profile,
                    onClick = { onProfileClick(profile) },
                    onApplyClick = { onApplyProfileClick(profile) }
                )
            }

            if (profiles.size > 1) {
                item {
                    AdCard()
                }
            }

            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }

    AboutModal(
        open = openAboutModal,
        onDismissRequest = { openAboutModal = false }
    )
}

data class ScheduleItemState(
    val id: Int = 0,
    val name: String,
    val subtitle: String,
    val isActive: Boolean = false,
    val icon: String? = null
)

data class ProfileItemState(
    val id: Int = 0,
    val name: String,
    val subtitle: String,
    val icon: String? = null
)

@Preview(showBackground = true)
@Composable
fun SoundProfilesScreenPreview() {
    SoundProfilesTheme {
        SoundProfilesScreen(
            schedules = listOf(
                ScheduleItemState(
                    name = "Office Schedule",
                    subtitle = "Active • Ends at 8:00 PM (Work Profile applied)",
                    isActive = true
                ),
                ScheduleItemState(
                    name = "Gym Session",
                    subtitle = "Starts at 6:00 AM Tomorrow (Music Profile applied)",
                    isActive = false
                ),
                ScheduleItemState(
                    name = "Deep Sleep",
                    subtitle = "Disabled • Set for 11:00 PM - 7:00 AM (Silent Profile applied)",
                    isActive = false
                )
            ),
            profiles = listOf(
                ProfileItemState(
                    name = "Slient",
                    subtitle = "All volumes at 0% • Do Not Disturb active"
                ),
                ProfileItemState(
                    name = "Work",
                    subtitle = "Ring 20% • Media 0% • Notifications Vibrate"
                ),
                ProfileItemState(
                    name = "Music",
                    subtitle = "Media 80% • Ringer 50%"
                ),
                ProfileItemState(
                    name = "Sleep",
                    subtitle = "Alarms 100% • All other sounds muted"
                )
            )
        )
    }
}
