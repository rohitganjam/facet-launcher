package com.facetlauncher.app.ui.onboarding

import android.Manifest
import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.dock.DockAppPickerScreen
import com.facetlauncher.app.ui.dock.DockAppPickerViewModel
import com.facetlauncher.app.ui.facets.FavoritesPickerScreen
import com.facetlauncher.app.ui.facets.FavoritesPickerViewModel
import com.facetlauncher.app.ui.settings.NotificationSettingsViewModel
import com.facetlauncher.app.ui.theme.FACET_TRANSITION_DURATION_MS
import com.facetlauncher.app.ui.theme.FacetTransitionEasing
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Wallpaper

/**
 * The four onboarding steps, in order — see `ONBOARDING_FLOW.md` §3. Reaching the end of
 * [NOTIFICATIONS] (via "Next" or "Skip") completes onboarding directly ([OnboardingScreen]'s [onFinish])
 * rather than advancing to a further step — the "make Facet your home screen" prompt lives on the
 * real Home screen instead, shown once there right after onboarding completes (see
 * [SetDefaultLauncherSheet]'s own doc for why).
 */
enum class OnboardingStep {
    INTRO,
    FACETS,
    HOME_SETUP,
    DRAWER_SETUP,
    NOTIFICATIONS,
}

/** Steps shown in each page's [OnboardingDots]. */
internal const val ONBOARDING_STEP_COUNT = 5

/** Full-screen pickers opened from the home-setup step — reused as-is from Settings/Facets, not part of [OnboardingStep]'s own linear flow. */
private enum class OnboardingSubScreen {
    DOCK_PICKER,
    FAVORITES_PICKER,
}

/** The step a right swipe (or the on-screen "Back" on every later step) lands on — every step but [OnboardingStep.INTRO] can go back this way. */
private fun previousStep(step: OnboardingStep): OnboardingStep = when (step) {
    OnboardingStep.INTRO -> OnboardingStep.INTRO
    OnboardingStep.FACETS -> OnboardingStep.INTRO
    OnboardingStep.HOME_SETUP -> OnboardingStep.FACETS
    OnboardingStep.DRAWER_SETUP -> OnboardingStep.HOME_SETUP
    OnboardingStep.NOTIFICATIONS -> OnboardingStep.DRAWER_SETUP
}

/**
 * The step a "Next" action (on-screen or a left swipe) lands on. [OnboardingStep.NOTIFICATIONS] maps to
 * itself — [OnboardingScreen] special-cases moving past it to call [OnboardingScreen]'s own
 * `onFinish` instead of changing `step`, the same way the old `SET_DEFAULT` step used to be the
 * fixed point every "next" resolved to.
 */
private fun nextStep(step: OnboardingStep): OnboardingStep = when (step) {
    OnboardingStep.INTRO -> OnboardingStep.FACETS
    OnboardingStep.FACETS -> OnboardingStep.HOME_SETUP
    OnboardingStep.HOME_SETUP -> OnboardingStep.DRAWER_SETUP
    OnboardingStep.DRAWER_SETUP -> OnboardingStep.NOTIFICATIONS
    OnboardingStep.NOTIFICATIONS -> OnboardingStep.NOTIFICATIONS
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
 * [favoritesPickerViewModel]/[notificationSettingsViewModel] are a testing seam only — production always leaves them `null`, which
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
    notificationSettingsViewModel: NotificationSettingsViewModel? = null,
) {
    var step by rememberSaveable { mutableStateOf(OnboardingStep.INTRO) }
    var subScreen by rememberSaveable { mutableStateOf<OnboardingSubScreen?>(null) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Copies the choices made here onto the facets before leaving, see OnboardingViewModel.finish.
    val finish = { viewModel.finish(onFinish) }

    OnboardingBackHandlers(
        subScreenOpen = subScreen != null,
        canStepBack = step != OnboardingStep.INTRO,
        onCloseSubScreen = { subScreen = null },
        onStepBack = { step = previousStep(step) },
    )

    val density = LocalDensity.current
    val swipeCommitPx = with(density) { SWIPE_COMMIT_DISTANCE.toPx() }
    val requestContactsPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.setSearchContactsEnabled(granted)
    }

    val actions = remember(viewModel) { viewModel.toActions() }
    val onSearchContactsToggle: (Boolean) -> Unit = { enabled ->
        if (enabled) requestContactsPermission.launch(Manifest.permission.READ_CONTACTS) else viewModel.setSearchContactsEnabled(false)
    }
    BlurBehindWindowEffect()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("onboarding_screen")
            .onboardingSwipe(
                enabled = subScreen == null,
                step = step,
                swipeCommitPx = swipeCommitPx,
                onNext = { if (step == OnboardingStep.NOTIFICATIONS) finish() else step = nextStep(step) },
                onPrevious = { step = previousStep(step) },
            ),
    ) {
        // Translucent Wallpaper-tone scrim over the blurred window backdrop set up above.
        Box(modifier = Modifier.fillMaxSize().background(Wallpaper.copy(alpha = ONBOARDING_BACKDROP_SCRIM_ALPHA)))

        AnimatedContent(
            targetState = step,
            transitionSpec = { onboardingStepTransition() },
            label = "onboarding_step",
            modifier = Modifier.fillMaxSize(),
        ) { currentStep ->
            OnboardingStepContent(
                step = currentStep,
                uiState = uiState,
                actions = actions,
                onSearchContactsToggle = onSearchContactsToggle,
                onBack = { step = previousStep(step) },
                onNext = { step = nextStep(step) },
                onOpenSubScreen = { subScreen = it },
                onFinish = finish,
                notificationSettingsViewModel = notificationSettingsViewModel,
            )
        }

        OnboardingSubScreenHost(
            subScreen = subScreen,
            facetId = uiState.facetId,
            onDone = { subScreen = null },
            dockPickerViewModel = dockPickerViewModel,
            favoritesPickerViewModel = favoritesPickerViewModel,
        )

        // Jumps straight past the remaining steps and completes onboarding — the "make Facet your
        // home screen" prompt itself now lives on the real Home screen, not here.
        if (subScreen == null) {
            TextActionButton(
                text = stringResource(R.string.onboarding_skip),
                onClick = finish,
                color = Muted,
                modifier = Modifier.align(Alignment.TopEnd).onboardingSkipInsets().testTag("onboarding_skip"),
            )
        }
    }
}

/** What the setup pages can change, as callbacks onto [OnboardingViewModel], so the pages never hold the ViewModel itself. */
private class OnboardingActions(viewModel: OnboardingViewModel) {
    val reorderDockApps = viewModel::reorderDockApps
    val setListContentMode = viewModel::setListContentMode
    val setAppsToShowCount = viewModel::setAppsToShowCount
    val reorderFavorites = viewModel::reorderFavorites
    val clearFavorites = viewModel::clearFavorites
    val clearDockApps = viewModel::clearDockApps
    val setDrawerPresentation = viewModel::setDrawerPresentation
    val setSearchBarPosition = viewModel::setSearchBarPosition
    val setSearchSettingsEnabled = viewModel::setSearchSettingsEnabled
    val setDrawerFolderDisplayMode = viewModel::setDrawerFolderDisplayMode
}

private fun OnboardingViewModel.toActions() = OnboardingActions(this)

/** The page for [step]. [onBack] and [onNext] move one step, and [onFinish] leaves onboarding from the last one. */
@Composable
private fun OnboardingStepContent(
    step: OnboardingStep,
    uiState: OnboardingUiState,
    actions: OnboardingActions,
    onSearchContactsToggle: (Boolean) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onOpenSubScreen: (OnboardingSubScreen) -> Unit,
    onFinish: () -> Unit,
    notificationSettingsViewModel: NotificationSettingsViewModel?,
) {
    when (step) {
        OnboardingStep.INTRO -> OnboardingIntroPage(onNext = onNext)
        OnboardingStep.HOME_SETUP -> OnboardingHomeSetupPage(
            uiState = uiState,
            onReorderDockApps = actions.reorderDockApps,
            onOpenDockPicker = { onOpenSubScreen(OnboardingSubScreen.DOCK_PICKER) },
            onListContentModeChange = actions.setListContentMode,
            onAppsToShowCountChange = actions.setAppsToShowCount,
            onOpenFavoritesPicker = { onOpenSubScreen(OnboardingSubScreen.FAVORITES_PICKER) },
            onReorderFavorites = actions.reorderFavorites,
            onClearFavorites = actions.clearFavorites,
            onClearDockApps = actions.clearDockApps,
            onBack = onBack,
            onNext = onNext,
        )
        OnboardingStep.DRAWER_SETUP -> OnboardingDrawerSetupPage(
            uiState = uiState,
            onDrawerPresentationChange = actions.setDrawerPresentation,
            onSearchBarPositionChange = actions.setSearchBarPosition,
            onSearchContactsToggle = onSearchContactsToggle,
            onSearchSettingsToggle = actions.setSearchSettingsEnabled,
            onDrawerFolderDisplayModeChange = actions.setDrawerFolderDisplayMode,
            onBack = onBack,
            onNext = onNext,
        )
        OnboardingStep.NOTIFICATIONS -> if (notificationSettingsViewModel != null) {
            OnboardingNotificationsPage(onBack = onBack, onNext = onFinish, viewModel = notificationSettingsViewModel)
        } else {
            OnboardingNotificationsPage(onBack = onBack, onNext = onFinish)
        }
        OnboardingStep.FACETS -> OnboardingFacetsPage(onBack = onBack, onNext = onNext)
    }
}

/** Back closes an open picker first, then steps back one page, and is left to the system on the first page. */
@Composable
private fun OnboardingBackHandlers(subScreenOpen: Boolean, canStepBack: Boolean, onCloseSubScreen: () -> Unit, onStepBack: () -> Unit) {
    BackHandler(enabled = subScreenOpen, onBack = onCloseSubScreen)
    BackHandler(enabled = !subScreenOpen && canStepBack, onBack = onStepBack)
}

/** Keeps the skip button clear of the status bar, with a little side padding. */
@Composable
private fun Modifier.onboardingSkipInsets(): Modifier =
    windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top)).padding(horizontal = 8.dp)

/**
 * Blurs the real wallpaper compositing behind the window (windowShowWallpaper in themes.xml) for the whole flow, rather
 * than loading it into a bitmap ourselves and blurring that copy.
 */
@Composable
private fun BlurBehindWindowEffect() {
    val density = LocalDensity.current
    val activityWindow = (LocalView.current.context as? Activity)?.window
    DisposableEffect(activityWindow) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return@DisposableEffect onDispose { }
        activityWindow?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        activityWindow?.setBackgroundBlurRadius(with(density) { ONBOARDING_BACKDROP_BLUR.roundToPx() })
        onDispose {
            activityWindow?.setBackgroundBlurRadius(0)
            activityWindow?.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        }
    }
}

/**
 * A horizontal swipe moves between steps: left for [onNext], right for [onPrevious], once the drag passes [swipeCommitPx].
 * Off while a full-screen picker sits on top, so that screen's own gestures (its list scroll, its back handling) are the only
 * thing responding there. Re-keyed on [step] so each step starts its own drag tally rather than carrying one over.
 */
private fun Modifier.onboardingSwipe(
    enabled: Boolean,
    step: OnboardingStep,
    swipeCommitPx: Float,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
): Modifier = if (!enabled) {
    this
} else {
    pointerInput(step) {
        var totalDrag = 0f
        detectHorizontalDragGestures(
            onDragStart = { totalDrag = 0f },
            onHorizontalDrag = { change, dragAmount ->
                totalDrag += dragAmount
                change.consume()
            },
            onDragEnd = {
                when {
                    totalDrag <= -swipeCommitPx -> onNext()
                    totalDrag >= swipeCommitPx -> onPrevious()
                }
            },
        )
    }
}

/** Slides forward steps in from the right and back steps in from the left, fading as they go. */
private fun AnimatedContentTransitionScope<OnboardingStep>.onboardingStepTransition(): ContentTransform {
    val offsetSpec = tween<IntOffset>(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)
    val fadeSpec = tween<Float>(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)
    val forward = targetState.ordinal > initialState.ordinal
    val enterFrom = if (forward) 1 else -1
    return (slideInHorizontally(offsetSpec) { it * enterFrom } + fadeIn(fadeSpec))
        .togetherWith(slideOutHorizontally(offsetSpec) { -it * enterFrom } + fadeOut(fadeSpec))
}

/** The full-screen dock or favorites picker over a step, or nothing. A test host supplies its own picker ViewModels. */
@Composable
private fun OnboardingSubScreenHost(
    subScreen: OnboardingSubScreen?,
    facetId: Long?,
    onDone: () -> Unit,
    dockPickerViewModel: DockAppPickerViewModel?,
    favoritesPickerViewModel: FavoritesPickerViewModel?,
) {
    // No folder can exist yet this early in first-run — showFoldersTab = false takes
    // the user straight to the apps list, matching this picker's pre-Folders behavior.
    when (subScreen) {
        OnboardingSubScreen.DOCK_PICKER -> if (dockPickerViewModel != null) {
            DockAppPickerScreen(onDone = onDone, showFoldersTab = false, facetId = facetId, viewModel = dockPickerViewModel)
        } else {
            DockAppPickerScreen(onDone = onDone, showFoldersTab = false, facetId = facetId)
        }
        OnboardingSubScreen.FAVORITES_PICKER -> if (favoritesPickerViewModel != null) {
            FavoritesPickerScreen(onDone = onDone, showFoldersTab = false, facetId = facetId, viewModel = favoritesPickerViewModel)
        } else {
            FavoritesPickerScreen(onDone = onDone, showFoldersTab = false, facetId = facetId)
        }
        null -> {}
    }
}
