package com.lumenlauncher.app.ui.launcher

import android.content.ContentUris
import android.content.Intent
import android.provider.CalendarContract
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.DrawerPresentation
import com.lumenlauncher.app.ui.drawer.AppDrawerScreen
import com.lumenlauncher.app.ui.drawer.DrawerViewModel
import com.lumenlauncher.app.ui.home.HomeScreen
import com.lumenlauncher.app.ui.home.HomeViewModel
import com.lumenlauncher.app.ui.home.LongPressSheet
import com.lumenlauncher.app.ui.theme.Scrim
import kotlinx.coroutines.launch

/** README's drawer-transform timing/easing (`.34s cubic-bezier(.32,.72,0,1)`), reused for the settle animation. */
private val DRAWER_SETTLE_SPEC = tween<Float>(durationMillis = 340, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f))

/** A drag needs to travel at least this fraction of the container height, in either direction, to commit open/closed. */
private const val COMMIT_TRAVEL_FRACTION = 0.20f
private const val VELOCITY_THRESHOLD_PX = 1000f
private const val HOME_FADE_SCALE_RANGE = 0.03f

/** README's swipe-up-opens-drawer distance (`>55px`), reused as the swipe-down-opens-shade distance — a downward swipe starting from a fully closed drawer that clears either this or [VELOCITY_THRESHOLD_PX] expands the notification shade instead of just springing back. */
private val SWIPE_DOWN_SHADE_DISTANCE = 55.dp

/**
 * Home + Drawer, merged into one composable/route rather than two separate `NavHost`
 * destinations. A follow-finger drag needs continuous, cancellable animation (an
 * [Animatable] driving both layers' position every frame) — `NavHost`'s declarative
 * enter/exit transitions only animate already-committed navigation events, and F10 already
 * frames the Drawer as "an overlay on top of the home background," not a distinct screen.
 * `progress` is 0f (Home only, Drawer off-screen) to 1f (Drawer fully covers Home); dragging
 * up on Home or down on the drawer's own list (only once it's scrolled to the top) tracks the
 * finger 1:1. Releasing commits open/closed once the drag has travelled at least
 * [COMMIT_TRAVEL_FRACTION] of the container height *from wherever this particular drag
 * started* (not a fixed 50% of the full range — that felt too hard to trigger); anything
 * short of that springs back to that starting state instead.
 */
@Composable
fun HomeDrawerRoute(
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfileCarousel: () -> Unit,
    onNavigateToEditProfile: (profileId: Long) -> Unit,
    onNavigateToUsageAccessExplanation: () -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel(),
    drawerViewModel: DrawerViewModel = hiltViewModel(),
) {
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val drawerSettings by drawerViewModel.settings.collectAsStateWithLifecycle()
    val contactResults by drawerViewModel.contactResults.collectAsStateWithLifecycle()
    val drawerBadgeCounts by drawerViewModel.badgeCounts.collectAsStateWithLifecycle()

    // PACKAGE_USAGE_STATS has no grant-change callback — re-check whenever the user returns to
    // Home (e.g. from the usage-access Settings redirect) rather than only on profile changes.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) homeViewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    var containerHeightPx by remember { mutableFloatStateOf(2000f) }
    var showLongPressSheet by remember { mutableStateOf(false) }
    var drawerQuery by remember { mutableStateOf("") }
    var dragStartProgress by remember { mutableFloatStateOf(0f) }
    var dragTargetProgress by remember { mutableFloatStateOf(0f) }
    var dragActive by remember { mutableStateOf(false) }
    var homeDragStartedClosed by remember { mutableStateOf(false) }
    var homeRawDragDistance by remember { mutableFloatStateOf(0f) }
    val velocityTracker = remember { VelocityTracker() }
    val context = LocalContext.current
    val density = LocalDensity.current
    val swipeDownShadeDistancePx = with(density) { SWIPE_DOWN_SHADE_DISTANCE.toPx() }

    fun settle(velocity: Float = 0f) {
        val currentProgress = if (dragActive) dragTargetProgress else progress.value
        val traveled = currentProgress - dragStartProgress
        val target = when {
            velocity > VELOCITY_THRESHOLD_PX -> 1f
            velocity < -VELOCITY_THRESHOLD_PX -> 0f
            traveled >= COMMIT_TRAVEL_FRACTION -> 1f
            traveled <= -COMMIT_TRAVEL_FRACTION -> 0f
            dragStartProgress > 0.5f -> 1f
            else -> 0f
        }
        dragActive = false
        coroutineScope.launch { progress.animateTo(target, DRAWER_SETTLE_SPEC) }
    }

    fun dragBy(deltaPx: Float) {
        if (!dragActive) {
            dragActive = true
            dragStartProgress = progress.value
            dragTargetProgress = progress.value
        }
        dragTargetProgress = (dragTargetProgress + deltaPx / containerHeightPx).coerceIn(0f, 1f)
        coroutineScope.launch { progress.snapTo(dragTargetProgress) }
    }

    val nestedScrollConnection = remember(listState, gridState, drawerSettings.drawerPresentation) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Fling-sourced deltas are the list's own post-release deceleration, not the
                // user's finger — once onPreFling below has resolved open/closed via settle(),
                // letting those leftover frames keep calling dragBy() would re-arm dragActive
                // and snapTo() over the in-flight settle animation, stranding the drawer
                // wherever the residual fling happened to decay to (the "low-velocity fling
                // stalls partway" bug). Only real finger movement should drive the drag.
                if (source != NestedScrollSource.Drag) return Offset.Zero
                val atTop = if (drawerSettings.drawerPresentation == DrawerPresentation.GRID) {
                    gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset == 0
                } else {
                    listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                }
                if (available.y > 0f && atTop && progress.value > 0f) {
                    dragBy(-available.y)
                    return available
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (dragActive) {
                    settle(velocity = -available.y)
                }
                return super.onPreFling(available)
            }
        }
    }

    val isDrawerOpen by remember { derivedStateOf { progress.value > 0f } }
    BackHandler(enabled = isDrawerOpen || showLongPressSheet) {
        if (showLongPressSheet) {
            showLongPressSheet = false
        } else {
            coroutineScope.launch { progress.animateTo(0f, DRAWER_SETTLE_SPEC) }
        }
    }

    Box(modifier = modifier.fillMaxSize().onSizeChanged { containerHeightPx = it.height.toFloat() }) {
        HomeScreen(
            appListItems = homeUiState.appListItems,
            listContentMode = homeUiState.activeListContentMode,
            showUsageAccessPrompt = homeUiState.showUsageAccessPrompt,
            onUsageAccessPromptClick = onNavigateToUsageAccessExplanation,
            dockApps = homeUiState.dockApps,
            dockDisplayMode = homeUiState.settings.dockDisplayMode,
            notificationBadgeStyle = homeUiState.settings.notificationBadgeStyle,
            badgeCounts = homeUiState.badgeCounts,
            use24HourTime = homeUiState.effectiveUse24HourTime,
            clockTemplateId = homeUiState.settings.clockTemplateId,
            clockFontOption = homeUiState.settings.clockFontOption,
            clockColorOption = homeUiState.settings.clockColorOption,
            clockShowMeridiem = homeUiState.settings.clockShowMeridiem,
            calendarEvents = homeUiState.calendarEvents,
            calendarColors = homeUiState.settings.calendarColors,
            calendarFontOption = homeUiState.settings.calendarFontOption,
            calendarColorOption = homeUiState.settings.calendarColorOption,
            onEventClick = { event ->
                val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.id)
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
            },
            onAppClick = onAppClick,
            onRequestShortcuts = drawerViewModel::getShortcuts,
            onLaunchShortcut = drawerViewModel::launchShortcut,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 1f - progress.value
                    val scale = 1f - progress.value * HOME_FADE_SCALE_RANGE
                    scaleX = scale
                    scaleY = scale
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            velocityTracker.resetTracking()
                            homeDragStartedClosed = progress.value == 0f
                            homeRawDragDistance = 0f
                        },
                        onDragEnd = {
                            val velocity = velocityTracker.calculateVelocity().y
                            settle(velocity = -velocity)
                            if (homeDragStartedClosed &&
                                (homeRawDragDistance >= swipeDownShadeDistancePx || velocity >= VELOCITY_THRESHOLD_PX)
                            ) {
                                homeViewModel.expandNotificationShade()
                            }
                        },
                        onDragCancel = { settle() }
                    ) { change, dragAmount ->
                        dragBy(-dragAmount)
                        homeRawDragDistance += dragAmount
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        change.consume()
                    }
                }
                // README specifies a 420ms/8px long-press. detectTapGestures's onLongPress is
                // Compose's own primitive for exactly this — it uses the platform's default
                // long-press timeout (~500ms) and touch slop instead of the literal 420ms/8px,
                // a close enough match that it isn't perceptible.
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { showLongPressSheet = true })
                },
        )

        AppDrawerScreen(
            apps = apps,
            onAppClick = onAppClick,
            listState = listState,
            gridState = gridState,
            presentation = drawerSettings.drawerPresentation,
            gridSize = drawerSettings.drawerGridSize,
            listItemSize = drawerSettings.drawerListItemSize,
            showIcons = drawerSettings.showDrawerIcons,
            showLabels = drawerSettings.showDrawerLabels,
            opacity = drawerSettings.drawerOpacity,
            notificationBadgeStyle = drawerSettings.notificationBadgeStyle,
            badgeCounts = drawerBadgeCounts,
            query = drawerQuery,
            onQueryChanged = { drawerQuery = it; drawerViewModel.onQueryChanged(it) },
            searchBarPosition = drawerSettings.searchBarPosition,
            onNavigateToSettings = onNavigateToSettings,
            contacts = contactResults,
            onRequestShortcuts = drawerViewModel::getShortcuts,
            onLaunchShortcut = drawerViewModel::launchShortcut,
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, ((1f - progress.value) * containerHeightPx).toInt()) }
                .nestedScroll(nestedScrollConnection),
        )

        // Scrim
        AnimatedVisibility(
            visible = showLongPressSheet,
            enter = fadeIn(animationSpec = tween(240)),
            exit = fadeOut(animationSpec = tween(240))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Scrim)
                    .testTag("long_press_sheet_scrim")
                    .clickable(onClick = { showLongPressSheet = false })
            )
        }

        // Sheet
        AnimatedVisibility(
            visible = showLongPressSheet,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(340, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f))
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(340, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f))
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val activeProfile = homeUiState.profiles.find { it.id == homeUiState.settings.activeProfileId }
            val activeProfilePosition = homeUiState.profiles.indexOf(activeProfile) + 1
            LongPressSheet(
                activeProfileName = activeProfile?.name ?: "",
                activeProfilePosition = activeProfilePosition,
                profileCount = homeUiState.profiles.size,
                onSwitchProfileClick = {
                    showLongPressSheet = false
                    onNavigateToProfileCarousel()
                },
                onEditProfileClick = {
                    showLongPressSheet = false
                    onNavigateToEditProfile(homeUiState.settings.activeProfileId)
                },
                onLauncherSettingsClick = {
                    showLongPressSheet = false
                    onNavigateToSettings()
                },
                onChangeWallpaperClick = {
                    showLongPressSheet = false
                    runCatching { context.startActivity(Intent(Intent.ACTION_SET_WALLPAPER)) }
                },
                modifier = Modifier.testTag("long_press_sheet")
            )
        }
    }
}
