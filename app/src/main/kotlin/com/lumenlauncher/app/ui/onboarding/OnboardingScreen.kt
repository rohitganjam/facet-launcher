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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
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

/** The step a "Back" action (on-screen, system, or a right swipe) lands on — every step but [OnboardingStep.INTRO] can go back. */
private fun previousStep(step: OnboardingStep): OnboardingStep = when (step) {
    OnboardingStep.INTRO -> OnboardingStep.INTRO
    OnboardingStep.HOME_SETUP -> OnboardingStep.INTRO
    OnboardingStep.PROFILES -> OnboardingStep.HOME_SETUP
    OnboardingStep.SET_DEFAULT -> OnboardingStep.PROFILES
}

/**
 * The step a "Next" action (on-screen or a left swipe) lands on. [OnboardingStep.SET_DEFAULT] has
 * no next step — finishing there has a real side effect (launching the default-launcher role
 * request), so it's deliberately reachable only via its own explicit "Set as default"/"Later"
 * buttons, never a swipe.
 */
private fun nextStep(step: OnboardingStep): OnboardingStep = when (step) {
    OnboardingStep.INTRO -> OnboardingStep.HOME_SETUP
    OnboardingStep.HOME_SETUP -> OnboardingStep.PROFILES
    OnboardingStep.PROFILES -> OnboardingStep.SET_DEFAULT
    OnboardingStep.SET_DEFAULT -> OnboardingStep.SET_DEFAULT
}

/** A swipe shorter than this is treated as a scroll/drag within the step's own content, not a page change. */
private val SWIPE_COMMIT_DISTANCE = 80.dp

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
    BackHandler(enabled = subScreen == null && step != OnboardingStep.INTRO) { step = previousStep(step) }

    val density = LocalDensity.current
    val swipeCommitPx = with(density) { SWIPE_COMMIT_DISTANCE.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Wallpaper)
            .testTag("onboarding_screen")
            // Disabled while a full-screen picker sits on top — that screen's own gestures (its
            // list scroll, its own back handling) should be the only thing responding to touches
            // there. Re-keyed on `step` so each new step starts its own clean drag-total tally
            // rather than carrying one over from the page just swiped away.
            .then(
                if (subScreen == null) {
                    Modifier.pointerInput(step) {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { totalDrag = 0f },
                            onHorizontalDrag = { change, dragAmount ->
                                totalDrag += dragAmount
                                change.consume()
                            },
                            onDragEnd = {
                                when {
                                    totalDrag <= -swipeCommitPx -> step = nextStep(step)
                                    totalDrag >= swipeCommitPx -> step = previousStep(step)
                                }
                            },
                        )
                    }
                } else {
                    Modifier
                },
            ),
    ) {
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
                OnboardingStep.INTRO -> OnboardingIntroPage(onNext = { step = nextStep(step) })
                OnboardingStep.HOME_SETUP -> OnboardingHomeSetupPage(
                    uiState = uiState,
                    onReorderDockApps = viewModel::reorderDockApps,
                    onOpenDockPicker = { subScreen = OnboardingSubScreen.DOCK_PICKER },
                    onListContentModeChanged = viewModel::setListContentMode,
                    onAppsToShowCountChanged = viewModel::setAppsToShowCount,
                    drawerPresentation = uiState.drawerPresentation,
                    onDrawerPresentationChanged = viewModel::setDrawerPresentation,
                    onOpenFavoritesPicker = { subScreen = OnboardingSubScreen.FAVORITES_PICKER },
                    onReorderFavorites = viewModel::reorderFavorites,
                    onClearFavorites = viewModel::clearFavorites,
                    onClearDockApps = viewModel::clearDockApps,
                    onBack = { step = previousStep(step) },
                    onNext = { step = nextStep(step) },
                )
                OnboardingStep.PROFILES -> OnboardingProfilesPage(
                    uiState = uiState,
                    onBack = { step = previousStep(step) },
                    onNext = { step = nextStep(step) },
                )
                OnboardingStep.SET_DEFAULT -> SetDefaultLauncherSheet(
                    uiState = uiState,
                    requestDefaultLauncherIntent = viewModel::requestDefaultLauncherIntent,
                    onBack = { step = previousStep(step) },
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
