package com.facetlauncher.app

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.ui.launcher.LauncherViewModel
import com.facetlauncher.app.ui.launcher.LocalHomePressedEvent
import com.facetlauncher.app.ui.navigation.FacetNavHost
import com.facetlauncher.app.ui.onboarding.OnboardingScreen
import com.facetlauncher.app.ui.theme.AccentSwatch
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LauncherActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            CompositionLocalProvider(LocalHomePressedEvent provides viewModel.homePressedEvent) {
                FacetLauncherTheme(
                    themeMode = uiState.themeMode,
                    accentFromSystem = uiState.accentFromSystem,
                    customAccentSwatch = uiState.customAccentSwatch?.let { runCatching { AccentSwatch.valueOf(it) }.getOrNull() },
                    wallpaperAccentRole = uiState.wallpaperAccentRole,
                    iconRenderMode = uiState.iconRenderMode,
                    launcherFontOption = uiState.launcherFontOption,
                ) {
                    when {
                        uiState.isLoading -> {
                            // Real settings/apps haven't loaded yet — render nothing rather than a
                            // frame styled with defaults that don't match the user's actual choices
                            // (see chat history). The window already shows the wallpaper through it
                            // (windowShowWallpaper + a transparent windowBackground in themes.xml), so
                            // an empty Box is a blank Home, not a black/white flash.
                            Box(modifier = Modifier.fillMaxSize())
                        }
                        !uiState.onboardingCompleted -> {
                            // A top-level branch, outside FacetNavHost entirely — never lands on the
                            // back stack, same isolation HomeDrawerRoute gets. See ONBOARDING_FLOW.md.
                            OnboardingScreen(onFinish = viewModel::completeOnboarding)
                        }
                        else -> {
                            FacetNavHost(
                                apps = uiState.apps,
                                onAppClick = { app -> launchApp(app) },
                                launcherViewModel = viewModel,
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            viewModel.onHomePressed()
        }
    }

    private fun launchApp(app: AppInfo) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(app.packageName, app.activityName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { startActivity(intent) }
    }
}
