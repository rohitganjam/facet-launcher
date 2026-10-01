package com.facetlauncher.app

import android.content.ComponentName
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.ui.components.HomeSwipeGate
import com.facetlauncher.app.ui.components.LocalHomeSwipeGate
import com.facetlauncher.app.ui.launcher.LauncherViewModel
import com.facetlauncher.app.ui.launcher.LocalHomePressedEvent
import com.facetlauncher.app.ui.navigation.FacetNavHost
import com.facetlauncher.app.ui.onboarding.OnboardingScreen
import com.facetlauncher.app.ui.theme.AccentSwatch
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LauncherActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    @Inject lateinit var launcherApps: LauncherApps

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleFacetDeepLink(intent)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val homeSwipeGate = remember { HomeSwipeGate() }

            CompositionLocalProvider(
                LocalHomePressedEvent provides viewModel.homePressedEvent,
                LocalHomeSwipeGate provides homeSwipeGate,
            ) {
                FacetLauncherTheme(
                    themeMode = uiState.themeMode,
                    accentFromSystem = uiState.accentFromSystem,
                    customAccentSwatch = uiState.customAccentSwatch?.let { runCatching { AccentSwatch.valueOf(it) }.getOrNull() },
                    wallpaperAccentRole = uiState.wallpaperAccentRole,
                    iconRenderMode = uiState.iconRenderMode,
                    launcherFontOption = uiState.launcherFontOption,
                    homeAppsFontWeight = uiState.homeAppsFontWeight,
                    fontScaleOption = uiState.fontScaleOption,
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
        handleFacetDeepLink(intent)
    }

    /** Warm (`onNewIntent`) and cold (`onCreate`) start both funnel a facet deep link/shortcut launch here. */
    private fun handleFacetDeepLink(intent: Intent) {
        val uri = intent.data ?: return
        viewModel.activateFacetFromDeepLink(uri)
    }

    private fun launchApp(app: AppInfo) {
        val component = ComponentName(app.packageName, app.activityName)
        if (app.userHandle != Process.myUserHandle()) {
            // A plain launch Intent doesn't resolve correctly for a Work Profile/Private
            // Space/clone-profile activity — LauncherApps.startMainActivity is the API a launcher
            // must use to cross into another profile. Uses app.userHandle directly — the exact
            // handle this AppInfo was enumerated from — rather than re-deriving one from its
            // display AppProfile category, since two distinct real profiles can share that
            // category (see AppProfile's own doc) and a re-derived handle could resolve to the
            // wrong one.
            runCatching { launcherApps.startMainActivity(component, app.userHandle, null, null) }
            return
        }
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            this.component = component
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { startActivity(intent) }
    }
}
