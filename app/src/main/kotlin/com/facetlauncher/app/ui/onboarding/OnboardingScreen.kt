package com.facetlauncher.app.ui.onboarding

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.ui.dock.DockAppPickerScreen
import com.facetlauncher.app.ui.dock.DockAppPickerViewModel
import com.facetlauncher.app.ui.facets.FavoritesPickerScreen
import com.facetlauncher.app.ui.facets.FavoritesPickerViewModel
import com.facetlauncher.app.ui.theme.FACET_TRANSITION_DURATION_MS
import com.facetlauncher.app.ui.theme.FacetTransitionEasing
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Wallpaper

/**
 * The three onboarding steps, in order — see `ONBOARDING_FLOW.md` §3. Reaching the end of
 * [FACETS] (via "Next" or "Skip") completes onboarding directly ([OnboardingScreen]'s [onFinish])
 * rather than advancing to a further step — the "make Facet your home screen" prompt lives on the
 * real Home screen instead, shown once there right after onboarding completes (see
 * [SetDefaultLauncherSheet]'s own doc for why).
 */
enum class OnboardingStep {
    INTRO,
    HOME_SETUP,
    FACETS,
}

/** Steps shown in each page's [OnboardingDots]. */
internal const val ONBOARDING_STEP_COUNT = 3

/** Full-screen pickers opened from the home-setup step — reused as-is from Settings/Facets, not part of [OnboardingStep]'s own linear flow. */
private enum class OnboardingSubScreen {
    DOCK_PICKER,
    FAVORITES_PICKER,
}

/** The step a right swipe (or the on-screen "Back" on [OnboardingStep.HOME_SETUP]/[OnboardingStep.FACETS]) lands on — every step but [OnboardingStep.INTRO] can go back this way. */
private fun previousStep(step: OnboardingStep): OnboardingStep = when (step) {
    OnboardingStep.INTRO -> OnboardingStep.INTRO
    OnboardingStep.HOME_SETUP -> OnboardingStep.INTRO
    OnboardingStep.FACETS -> OnboardingStep.HOME_SETUP
}

/**
 * The step a "Next" action (on-screen or a left swipe) lands on. [OnboardingStep.FACETS] maps to
 * itself — [OnboardingScreen] special-cases moving past it to call [OnboardingScreen]'s own
 * `onFinish` instead of changing `step`, the same way the old `SET_DEFAULT` step used to be the
 * fixed point every "next" resolved to.
 */
private fun nextStep(step: OnboardingStep): OnboardingStep = when (step) {
    OnboardingStep.INTRO -> OnboardingStep.HOME_SETUP
    OnboardingStep.HOME_SETUP -> OnboardingStep.FACETS
    OnboardingStep.FACETS -> OnboardingStep.FACETS
}

/** A swipe shorter than this is treated as a scroll/drag within the step's own content, not a page change. */
private val SWIPE_COMMIT_DISTANCE = 80.dp

/** Blur radius applied to the real wallpaper behind the window (see [OnboardingScreen]'s background layer). */
private val ONBOARDING_BACKDROP_BLUR = 40.dp

/** Opacity of the [Wallpaper] scrim laid over the blurred backdrop. */
private const val ONBOARDING_BACKDROP_SCRIM_ALPHA = 0.7f

/**
 * First-run flow host (`ONBOARDING_FLOW.md`) — a top-level branch in [com.facetlauncher.app.LauncherActivity],
 * outside [com.facetlauncher.app.ui.navigation.FacetNavHost] entirely (same isolation
 * [com.facetlauncher.app.ui.launcher.HomeDrawerRoute] gets), so it never lands on the back stack.
 * Owns only step navigation; all data lives in the shared [OnboardingViewModel], obtained once
 * here and passed down as stateless parameters to each step. [dockPickerViewModel]/
 * [favoritesPickerViewModel] are a testing seam only — production always leaves them `null`, which
 * lets [com.facetlauncher.app.ui.dock.DockAppPickerScreen]/[com.facetlauncher.app.ui.facets.FavoritesPickerScreen]
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
        step = previousStep(step)
    }

    val density = LocalDensity.current
    val swipeCommitPx = with(density) { SWIPE_COMMIT_DISTANCE.toPx() }

    // Blurs the real wallpaper compositing behind the window (windowShowWallpaper in themes.xml)
    // for the whole flow, rather than loading it into a bitmap ourselves and blurring that copy.
    val activityWindow = (LocalView.current.context as? Activity)?.window
    DisposableEffect(activityWindow) {
        activityWindow?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        activityWindow?.setBackgroundBlurRadius(with(density) { ONBOARDING_BACKDROP_BLUR.roundToPx() })
        onDispose {
            activityWindow?.setBackgroundBlurRadius(0)
            activityWindow?.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
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
                                    totalDrag <= -swipeCommitPx -> if (step == OnboardingStep.FACETS) onFinish() else step = nextStep(step)
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
        // Translucent Wallpaper-tone scrim over the blurred window backdrop set up above.
        Box(modifier = Modifier.fillMaxSize().background(Wallpaper.copy(alpha = ONBOARDING_BACKDROP_SCRIM_ALPHA)))

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    (slideInHorizontally(tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)) { it } + fadeIn(tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)))
                        .togetherWith(slideOutHorizontally(tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)) { -it } + fadeOut(tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)))
                } else {
                    (slideInHorizontally(tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)) { -it } + fadeIn(tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)))
                        .togetherWith(slideOutHorizontally(tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)) { it } + fadeOut(tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)))
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
                OnboardingStep.FACETS -> OnboardingFacetsPage(
                    uiState = uiState,
                    onBack = { step = previousStep(step) },
                    onNext = onFinish,
                )
            }
        }

        when (subScreen) {
            // No folder can exist yet this early in first-run — showFoldersTab = false takes
            // the user straight to the apps list, matching this picker's pre-Folders behavior.
            OnboardingSubScreen.DOCK_PICKER -> if (dockPickerViewModel != null) {
                DockAppPickerScreen(onDone = { subScreen = null }, showFoldersTab = false, viewModel = dockPickerViewModel)
            } else {
                DockAppPickerScreen(onDone = { subScreen = null }, showFoldersTab = false)
            }
            OnboardingSubScreen.FAVORITES_PICKER -> if (favoritesPickerViewModel != null) {
                FavoritesPickerScreen(onDone = { subScreen = null }, showFoldersTab = false, viewModel = favoritesPickerViewModel)
            } else {
                FavoritesPickerScreen(onDone = { subScreen = null }, showFoldersTab = false)
            }
            null -> {}
        }

        // Jumps straight past the remaining steps and completes onboarding — the "make Facet your
        // home screen" prompt itself now lives on the real Home screen, not here.
        if (subScreen == null) {
            Text(
                text = "Skip",
                style = MaterialTheme.typography.bodyLarge,
                color = Muted,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                    .padding(horizontal = 20.dp)
                    // 48dp minimum touch target (M3 guideline), centered on the text.
                    .clickable(onClick = onFinish)
                    .testTag("onboarding_skip")
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .wrapContentSize(Alignment.Center),
            )
        }
    }
}
