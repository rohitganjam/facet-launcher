package com.lumenlauncher.app.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumenlauncher.app.ui.dock.DockAppPickerScreen
import com.lumenlauncher.app.ui.dock.DockAppPickerViewModel
import com.lumenlauncher.app.ui.profiles.FavoritesPickerScreen
import com.lumenlauncher.app.ui.profiles.FavoritesPickerViewModel
import com.lumenlauncher.app.ui.theme.LUMEN_TRANSITION_DURATION_MS
import com.lumenlauncher.app.ui.theme.LumenTransitionEasing
import com.lumenlauncher.app.ui.theme.Wallpaper

/** The four onboarding steps, in order — see `ONBOARDING_FLOW.md` §3. */
enum class OnboardingStep {
    INTRO,
    HOME_SETUP,
    PROFILES,
    SET_DEFAULT,
}

/** Full-screen pickers opened from the home-setup step — reused as-is from Settings/Profiles, not part of [OnboardingStep]'s own linear flow. */
private enum class OnboardingSubScreen {
    DOCK_PICKER,
    FAVORITES_PICKER,
}

/**
 * First-run flow host (`ONBOARDING_FLOW.md`) — a top-level branch in [com.lumenlauncher.app.LauncherActivity],
 * outside [com.lumenlauncher.app.ui.navigation.LumenNavHost] entirely (same isolation
 * [com.lumenlauncher.app.ui.launcher.HomeDrawerRoute] gets), so it never lands on the back stack.
 * Owns only step navigation; all data lives in the shared [OnboardingViewModel], obtained once
 * here and passed down as stateless parameters to each step. [dockPickerViewModel]/
 * [favoritesPickerViewModel] are a testing seam only — production always leaves them `null`, which
 * lets [com.lumenlauncher.app.ui.dock.DockAppPickerScreen]/[com.lumenlauncher.app.ui.profiles.FavoritesPickerScreen]
 * fall back to their own `hiltViewModel()`; a non-Hilt test host (like `OnboardingScreenTest`, which
 * already builds [OnboardingViewModel] by hand) supplies its own instance instead.
 */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
    dockPickerViewModel: DockAppPickerViewModel? = null,
    favoritesPickerViewModel: FavoritesPickerViewModel? = null,
) {
    var step by rememberSaveable { mutableStateOf(OnboardingStep.INTRO) }
    var subScreen by rememberSaveable { mutableStateOf<OnboardingSubScreen?>(null) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = subScreen != null) { subScreen = null }
    BackHandler(enabled = subScreen == null && step != OnboardingStep.INTRO) {
        step = when (step) {
            OnboardingStep.INTRO -> OnboardingStep.INTRO
            OnboardingStep.HOME_SETUP -> OnboardingStep.INTRO
            OnboardingStep.PROFILES -> OnboardingStep.HOME_SETUP
            OnboardingStep.SET_DEFAULT -> OnboardingStep.PROFILES
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Wallpaper).testTag("onboarding_screen")) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    (slideInHorizontally(tween(LUMEN_TRANSITION_DURATION_MS, easing = LumenTransitionEasing)) { it } + fadeIn(tween(LUMEN_TRANSITION_DURATION_MS, easing = LumenTransitionEasing)))
                        .togetherWith(slideOutHorizontally(tween(LUMEN_TRANSITION_DURATION_MS, easing = LumenTransitionEasing)) { -it } + fadeOut(tween(LUMEN_TRANSITION_DURATION_MS, easing = LumenTransitionEasing)))
                } else {
                    (slideInHorizontally(tween(LUMEN_TRANSITION_DURATION_MS, easing = LumenTransitionEasing)) { -it } + fadeIn(tween(LUMEN_TRANSITION_DURATION_MS, easing = LumenTransitionEasing)))
                        .togetherWith(slideOutHorizontally(tween(LUMEN_TRANSITION_DURATION_MS, easing = LumenTransitionEasing)) { it } + fadeOut(tween(LUMEN_TRANSITION_DURATION_MS, easing = LumenTransitionEasing)))
                }
            },
            label = "onboarding_step",
            modifier = Modifier.fillMaxSize(),
        ) { currentStep ->
            when (currentStep) {
                OnboardingStep.INTRO -> OnboardingIntroPage(onNext = { step = OnboardingStep.HOME_SETUP })
                OnboardingStep.HOME_SETUP -> OnboardingHomeSetupPage(
                    uiState = uiState,
                    onReorderDockApps = viewModel::reorderDockApps,
                    onOpenDockPicker = { subScreen = OnboardingSubScreen.DOCK_PICKER },
                    onListContentModeChanged = viewModel::setListContentMode,
                    onAppsToShowCountChanged = viewModel::setAppsToShowCount,
                    onOpenFavoritesPicker = { subScreen = OnboardingSubScreen.FAVORITES_PICKER },
                    onReorderFavorites = viewModel::reorderFavorites,
                    onSkip = { step = OnboardingStep.PROFILES },
                    onNext = { step = OnboardingStep.PROFILES },
                )
                OnboardingStep.PROFILES -> OnboardingProfilesPage(
                    uiState = uiState,
                    onNext = { step = OnboardingStep.SET_DEFAULT },
                )
                OnboardingStep.SET_DEFAULT -> SetDefaultLauncherSheet(
                    uiState = uiState,
                    requestDefaultLauncherIntent = viewModel::requestDefaultLauncherIntent,
                    onFinish = onFinish,
                )
            }
        }

        when (subScreen) {
            OnboardingSubScreen.DOCK_PICKER -> if (dockPickerViewModel != null) {
                DockAppPickerScreen(onDone = { subScreen = null }, viewModel = dockPickerViewModel)
            } else {
                DockAppPickerScreen(onDone = { subScreen = null })
            }
            OnboardingSubScreen.FAVORITES_PICKER -> if (favoritesPickerViewModel != null) {
                FavoritesPickerScreen(onDone = { subScreen = null }, viewModel = favoritesPickerViewModel)
            } else {
                FavoritesPickerScreen(onDone = { subScreen = null })
            }
            null -> {}
        }
    }
}
