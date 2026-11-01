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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.icon.StyledIconCache
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.ui.components.HomeSwipeGate
import com.facetlauncher.app.ui.components.LocalHomeSwipeGate
import com.facetlauncher.app.ui.home.LocalAllowLandscape
import com.facetlauncher.app.ui.home.homeRequestedOrientation
import com.facetlauncher.app.ui.launcher.LauncherViewModel
import com.facetlauncher.app.ui.launcher.LocalHomePressedEvent
import com.facetlauncher.app.ui.components.WarmStyledIcons
import com.facetlauncher.app.ui.navigation.FacetNavHost
import com.facetlauncher.app.ui.onboarding.OnboardingScreen
import com.facetlauncher.app.ui.settings.DebugSettingsEntry
import com.facetlauncher.app.ui.settings.LocalDebugSettingsEntry
import com.facetlauncher.app.ui.theme.AccentSwatch
import com.facetlauncher.app.ui.home.ClockWidgetHostController
import com.facetlauncher.app.ui.home.LocalClockWidgetHost
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.LocalStyledIconCache
import com.facetlauncher.app.ui.theme.ProvideSystemBars
import com.facetlauncher.app.ui.theme.SystemBarsState
import dagger.hilt.android.AndroidEntryPoint
import java.util.Optional
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LauncherActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    @Inject lateinit var launcherApps: LauncherApps

    @Inject lateinit var entitlementRepository: EntitlementRepository

    @Inject lateinit var clockWidgetHost: ClockWidgetHostController

    @Inject lateinit var styledIconCache: StyledIconCache

    /** Only a debug build provides one (see `EntitlementModule`): the extra Settings rows it adds. */
    @Inject lateinit var debugSettingsEntry: Optional<DebugSettingsEntry>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleFacetDeepLink(intent)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val homeSwipeGate = remember { HomeSwipeGate() }
            val systemBarsState = remember { SystemBarsState() }
            // Locked to portrait when landscape is off, free otherwise. Applied once settings have loaded, so a
            // device already on its side doesn't flip on every start before the saved choice is known.
            LaunchedEffect(uiState.isLoading, uiState.allowLandscape) {
                if (!uiState.isLoading) {
                    requestedOrientation = homeRequestedOrientation(uiState.allowLandscape)
                }
            }

            CompositionLocalProvider(
                LocalHomePressedEvent provides viewModel.homePressedEvent,
                LocalHomeSwipeGate provides homeSwipeGate,
                LocalDebugSettingsEntry provides debugSettingsEntry.orElse(null),
                LocalAllowLandscape provides uiState.allowLandscape,
                LocalClockWidgetHost provides clockWidgetHost,
                LocalStyledIconCache provides styledIconCache,
            ) {
                FacetLauncherTheme(
                    themeMode = uiState.themeMode,
                    accentFromSystem = uiState.accentFromSystem,
                    customAccentSwatch = uiState.customAccentSwatch?.let { runCatching { AccentSwatch.valueOf(it) }.getOrNull() },
                    wallpaperAccentRole = uiState.wallpaperAccentRole,
                    iconRenderMode = uiState.iconRenderMode,
                    iconShape = uiState.iconShape,
                    launcherFontOption = uiState.launcherFontOption,
                    homeAppsFontWeight = uiState.homeAppsFontWeight,
                    fontScaleOption = uiState.fontScaleOption,
                ) {
                    WarmStyledIcons(uiState.apps, uiState.iconRenderMode)
                    ProvideSystemBars(uiState.systemBarIconStyle, systemBarsState) {
                        when {
                            uiState.isLoading || uiState.onboardingCompleted == null -> {
                                // Real settings/apps haven't loaded yet — render nothing rather than a
                                // frame styled with defaults that don't match the user's actual choices
                                // (see chat history). The window already shows the wallpaper through it
                                // (windowShowWallpaper + a transparent windowBackground in themes.xml), so
                                // an empty Box is a blank Home, not a black/white flash.
                                Box(modifier = Modifier.fillMaxSize())
                            }
                            uiState.onboardingCompleted == false -> {
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
    }

    override fun onResume() {
        super.onResume()
        // A refund, a purchase on another device or a redeemed code shows up here without a restart.
        lifecycleScope.launch { entitlementRepository.refresh() }
        viewModel.onResumed()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            viewModel.onHomePressed(whileResumed = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
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
