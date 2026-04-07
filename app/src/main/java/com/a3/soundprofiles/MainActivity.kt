package com.a3.soundprofiles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.a3.soundprofiles.ui.ProfileConfigRoute
import com.a3.soundprofiles.ui.ScheduleConfigRoute
import com.a3.soundprofiles.ui.SoundProfileUiState
import com.a3.soundprofiles.ui.SoundProfileViewModel
import com.a3.soundprofiles.ui.SoundProfilesRoute
import com.a3.soundprofiles.ui.screens.ProfileConfigScreen
import com.a3.soundprofiles.ui.screens.ScheduleConfigScreen
import com.a3.soundprofiles.ui.screens.SoundProfilesScreen
import com.a3.soundprofiles.ui.screens.WelcomeScreen
import com.a3.soundprofiles.ui.theme.SoundProfilesTheme
import com.a3.soundprofiles.util.ConsentManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.app.Activity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.a3.soundprofiles.ui.AppStartupState

class MainActivity : ComponentActivity() {

    private val viewModel: SoundProfileViewModel by viewModel()


    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            when (viewModel.startupState.value) {
                AppStartupState.Loading -> true
                AppStartupState.Main -> viewModel.uiState.value is SoundProfileUiState.Loading
                else -> false
            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoundProfilesTheme {
                val startupState by viewModel.startupState.collectAsState()

                when(startupState) {
                    AppStartupState.Loading -> {
                        // Splash screen handles this
                    }
                    AppStartupState.Welcome -> {
                        WelcomeScreen(
                            onStartClicked = {
                                viewModel.completeWelcome()
                            }
                        )
                    }
                    AppStartupState.Main -> {
                        val context = LocalContext.current
                        var isConsentShown by remember { mutableStateOf(false) }

                        LaunchedEffect(Unit) {
                            if (!isConsentShown) {
                                isConsentShown = true
                                val activity = generateSequence(context) { 
                                    if (it is android.content.ContextWrapper) it.baseContext else null 
                                }.firstOrNull { it is Activity } as? Activity
                                
                                if (activity != null) {
                                    val consentManager = ConsentManager(activity)
                                    consentManager.gatherConsent(activity) { _ -> }
                                }
                            }
                        }

                        val navController = rememberNavController()
                        val uiState by viewModel.uiState.collectAsState()

                        NavHost(
                            navController = navController,
                            startDestination = SoundProfilesRoute,
                        ) {
                            composable<SoundProfilesRoute> {
                                when (val state = uiState) {
                                    is SoundProfileUiState.Loading -> {
                                        Surface {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                LoadingIndicator()
                                            }
                                        }
                                    }

                                    is SoundProfileUiState.Data -> {
                                        SoundProfilesScreen(
                                            schedules = state.schedules,
                                            profiles = state.profiles,
                                            onAddProfileClick = {
                                                navController.navigate(ProfileConfigRoute(name = "", iconName = "work"))
                                            },
                                            onAddScheduleClick = {
                                                navController.navigate(ScheduleConfigRoute())
                                            },
                                            onScheduleClick = { schedule ->
                                                navController.navigate(ScheduleConfigRoute(scheduleId = schedule.id))
                                            },
                                            onProfileClick = { profile ->
                                                navController.navigate(
                                                    ProfileConfigRoute(
                                                        name = profile.name,
                                                        iconName = profile.icon ?: "volume_up",
                                                        profileId = profile.id
                                                    )
                                                )
                                            },
                                            onApplyProfileClick = { profile ->
                                                viewModel.applyProfile(profile.id)
                                            }
                                        )
                                    }

                                    is SoundProfileUiState.Error -> {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "Error: ${state.message}")
                                        }
                                    }
                                }
                            }

                            composable<ProfileConfigRoute> { backStackEntry ->
                                val route: ProfileConfigRoute = backStackEntry.toRoute()
                                ProfileConfigScreen(
                                    profileId = route.profileId,
                                    initialName = route.name,
                                    initialIcon = route.iconName,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            composable<ScheduleConfigRoute> { backStackEntry ->
                                val route: ScheduleConfigRoute = backStackEntry.toRoute()
                                ScheduleConfigScreen(
                                    scheduleId = route.scheduleId,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
