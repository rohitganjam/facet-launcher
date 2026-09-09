package com.lumenlauncher.app.ui.profiles

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
import com.lumenlauncher.app.data.model.CalendarEvent
import com.lumenlauncher.app.data.model.ClockAlignment
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockDateStyle
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.LauncherFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.data.model.NotificationBadgeStyle
import com.lumenlauncher.app.ui.components.BackButton
import com.lumenlauncher.app.ui.components.ConfirmDialog
import com.lumenlauncher.app.ui.components.ThemedDropdownMenu
import com.lumenlauncher.app.ui.components.ThemedDropdownMenuItem
import com.lumenlauncher.app.ui.components.rememberDragReorderState
import com.lumenlauncher.app.ui.home.AppRow
import com.lumenlauncher.app.ui.home.ClockBlock
import com.lumenlauncher.app.ui.home.DockIcon
import com.lumenlauncher.app.ui.home.HOME_CLOCK_DEFAULT_TOP_OFFSET
import com.lumenlauncher.app.ui.home.HOME_CLOCK_MIN_GAP
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.CarouselBackdrop
import com.lumenlauncher.app.ui.theme.Faint
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.LumenType
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Surface
import com.lumenlauncher.app.ui.theme.resolve

private val CAROUSEL_PAGE_SPACING = 8.dp

/**
 * The preview card is a scale model of the real screen — this fraction of the device's own
 * width *and* height — so it keeps whatever aspect ratio the current phone has, on any phone.
 * Everything else about the carousel's footprint derives from it: the horizontal inset (the
 * strip of each neighbour card that peeks in) is `(1 − scale) / 2` of the screen width, and the
 * pager's height is `scale` of the screen height plus [CAROUSEL_PAGE_CHROME_HEIGHT]. The pager
 * is content-sized (not `weight(1f)`), so the dots + Launcher-settings row pack directly beneath
 * it and the leftover space collects at the bottom (a trailing weighted spacer).
 */
private const val CAROUSEL_CARD_SCALE = 0.55f

/** The profile-name row above the card + the gear/trash row below it, inside every page. */
private val CAROUSEL_PAGE_CHROME_HEIGHT = 104.dp

private val REORDER_ROW_HEIGHT = 64.dp
private val REORDER_ROW_SPACING = 10.dp

/** Shared "picked up" drag-lift treatment, matching every other reorderable list in Settings. */
private val REORDER_DRAG_ELEVATION = 6.dp
private const val REORDER_DRAG_SCALE = 1.04f

/**
 * How [ProfileCarouselScreen] is entered:
 * - [SWITCH]: home long-press — the swipeable carousel for picking a profile ("Switch Profiles").
 *   Reordering is still reachable in-place via the "Reorder" link.
 * - [MANAGE]: Settings → Profiles — opens straight into the reorderable list ("Manage Profiles"),
 *   with no carousel; back returns to Settings.
 */
enum class ProfileCarouselMode { SWITCH, MANAGE }

/**
 * Profile carousel (`3a`, `3b`, `4n`, `4o`): switch, reorder, and reach per-profile settings.
 * [onBack] backs out one level without applying anything (e.g. reached via Settings → Profiles,
 * backs out to Settings). [onProfileApplied] fires once a card is actually tapped/applied — this
 * always returns to Home regardless of how the carousel was reached, since applying a profile is
 * meant to be felt on the home screen, not leave the user buried in Settings.
 */
@Composable
fun ProfileCarouselScreen(
    onBack: () -> Unit,
    onProfileApplied: () -> Unit,
    onEditProfile: (profileId: Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    mode: ProfileCarouselMode = ProfileCarouselMode.SWITCH,
    viewModel: ProfileCarouselViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileCarouselContent(
        uiState = uiState,
        mode = mode,
        onBack = onBack,
        onSelect = { profileId -> viewModel.selectProfile(profileId); onProfileApplied() },
        onEditProfile = onEditProfile,
        onAddProfile = viewModel::addProfile,
        onDeleteProfile = viewModel::deleteProfile,
        onReorder = viewModel::reorderProfiles,
        onNavigateToSettings = onNavigateToSettings,
        modifier = modifier,
    )
}

/**
 * The back-chevron + title row atop both the carousel and the reorder list. 24dp horizontal
 * gutter to match every other screen's header (`SettingsHeader` et al.); [trailing] is the
 * optional right-aligned slot (the carousel's "Reorder" link).
 */
@Composable
private fun CarouselHeaderTitleRow(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BackButton(onClick = onBack)
            // headlineSmall to match Settings' own header size (see chat history) — was titleMedium.
            Text(text = title, style = MaterialTheme.typography.headlineSmall, color = Ink)
        }
        trailing()
    }
}

@Composable
private fun ProfileCarouselContent(
    uiState: ProfileCarouselUiState,
    mode: ProfileCarouselMode,
    onBack: () -> Unit,
    onSelect: (Long) -> Unit,
    onEditProfile: (Long) -> Unit,
    onAddProfile: () -> Unit,
    onDeleteProfile: (ProfileEntity) -> Unit,
    onReorder: (List<ProfileEntity>) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profiles = uiState.profiles
    val manageMode = mode == ProfileCarouselMode.MANAGE
    if (profiles.isEmpty()) {
        // EnsureActiveProfileUseCase seeds the first profile on app launch — only briefly empty
        // before that resolves. Paint the backdrop + header now so the screen is opaque and
        // reads as "arrived" during its slide-in (otherwise the transparent window shows the
        // wallpaper through and it looks like a flash of Home when opened from Settings).
        // No "profile_carousel_screen" test tag here — that marks the loaded screen, and tests
        // wait on it to know the carousel content is ready.
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(CarouselBackdrop)
                .windowInsetsPadding(WindowInsets.systemBars),
        ) {
            CarouselHeaderTitleRow(title = if (manageMode) "Manage Profiles" else "Switch Profiles", onBack = onBack)
        }
        return
    }

    val pageCount = profiles.size + if (uiState.canAddProfile) 1 else 0
    val initialPage = profiles.indexOfFirst { it.id == uiState.activeProfileId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { pageCount }
    // Manage mode is the reorderable list and nothing else — it opens there and stays there.
    var isReordering by remember { mutableStateOf(manageMode) }
    var reorderWorkingList by remember(profiles) { mutableStateOf(profiles) }
    var deletingProfile by remember { mutableStateOf<ProfileEntity?>(null) }

    // In manage mode there's no carousel to fall back to, so system back leaves to Settings.
    BackHandler(enabled = isReordering && !manageMode) { isReordering = false }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CarouselBackdrop)
            .testTag("profile_carousel_screen")
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        CarouselHeaderTitleRow(
            title = if (manageMode) "Manage Profiles" else "Switch Profiles",
            onBack = { if (isReordering && !manageMode) isReordering = false else onBack() },
        ) {
            // No "Done" counterpart once reordering — every drag-release autosaves via onCommit,
            // so there's nothing left to confirm; the back button already exits reorder mode,
            // same as the system back gesture (see the BackHandler above).
            if (!isReordering) {
                Text(
                    text = "Reorder",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (profiles.size > 1) Accent else Faint,
                    modifier = Modifier
                        .testTag("profile_carousel_reorder")
                        .clickable(enabled = profiles.size > 1) {
                            reorderWorkingList = profiles
                            isReordering = true
                        },
                )
            }
        }

        if (isReordering) {
            ProfileReorderList(
                profiles = reorderWorkingList,
                canAddProfile = uiState.canAddProfile,
                canDeleteProfile = uiState.canDeleteProfile,
                onOrderChanged = { reorderWorkingList = it },
                onCommit = { onReorder(reorderWorkingList) },
                onEditProfile = onEditProfile,
                onDeleteRequest = { deletingProfile = it },
                onAddProfile = onAddProfile,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        } else {
            val config = LocalConfiguration.current
            // The card mirrors the real screen at CAROUSEL_CARD_SCALE, so its width is that
            // fraction of the screen width and the leftover half on each side is the neighbour peek.
            val screenAspectRatio = config.screenWidthDp.toFloat() / config.screenHeightDp.toFloat()
            val pageInset = (config.screenWidthDp * (1f - CAROUSEL_CARD_SCALE) / 2f).dp
            val pagerHeight = (config.screenHeightDp * CAROUSEL_CARD_SCALE).dp + CAROUSEL_PAGE_CHROME_HEIGHT
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = pageInset),
                pageSpacing = CAROUSEL_PAGE_SPACING,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(pagerHeight)
                    .testTag("profile_carousel_pager"),
            ) { page ->
                if (page < profiles.size) {
                    val profile = profiles[page]
                    // Recent-apps style: tapping any visible card — centered or peeking —
                    // applies it immediately. Browsing (without applying) is swipe-only.
                    ProfilePreviewPage(
                        profile = profile,
                        favorites = uiState.previewsByProfileId[profile.id]?.favorites.orEmpty(),
                        listContentMode = uiState.listContentMode(profile.id),
                        clockTemplateId = uiState.clockTemplateId(profile.id),
                        clockFontOption = uiState.clockFontOption(profile.id),
                        clockColorOption = uiState.clockColorOption(profile.id),
                        clockAccentColorOption = uiState.clockAccentColorOption(profile.id),
                        use24HourTime = uiState.effectiveUse24HourTime(profile.id),
                        clockShowMeridiem = uiState.clockShowMeridiem(profile.id),
                        clockDateStyle = uiState.clockDateStyle(profile.id),
                        clockAlignment = uiState.clockAlignment(profile.id),
                        clockScale = uiState.clockScale(profile.id),
                        calendarAlignment = uiState.calendarAlignment(profile.id),
                        clockZoneHeightDp = uiState.clockZoneHeightDp(profile.id),
                        calendarEvents = uiState.previewsByProfileId[profile.id]?.calendarEvents.orEmpty(),
                        calendarColors = uiState.globalSettings.calendarColors,
                        calendarFontOption = uiState.calendarFontOption(profile.id),
                        calendarColorOption = uiState.calendarColorOption(profile.id),
                        calendarFontWeight = uiState.calendarFontWeight(profile.id),
                        launcherFontOption = uiState.globalSettings.launcherFontOption,
                        appRowPosition = uiState.appRowPosition(profile.id),
                        appRowPresentation = uiState.appRowPresentation(profile.id),
                        appLabelColorOption = uiState.globalSettings.appLabelColorOption,
                        homeAppsFontWeight = uiState.globalSettings.homeAppsFontWeight,
                        dockApps = uiState.dockApps,
                        dockDisplayMode = uiState.dockDisplayMode,
                        canDelete = uiState.canDeleteProfile,
                        onEditProfileClick = { onEditProfile(profile.id) },
                        onDeleteClick = { deletingProfile = profile },
                        onCardClick = { onSelect(profile.id) },
                        screenAspectRatio = screenAspectRatio,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("profile_page_${profile.id}"),
                    )
                } else {
                    AddProfilePage(
                        onClick = onAddProfile,
                        modifier = Modifier.fillMaxSize().testTag("profile_carousel_add_page"),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                profiles.indices.forEach { index ->
                    val active = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(width = if (active) 16.dp else 5.dp, height = 5.dp)
                            .clip(RoundedCornerShape(2.5.dp))
                            .background(if (active) Accent else Faint),
                    )
                }
            }

            LauncherSettingsRow(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 20.dp),
            )

            // Leftover space collects below the whole cluster, not as a dead band inside it.
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    deletingProfile?.let { profile ->
        ConfirmDialog(
            title = "Delete ${profile.name}?",
            message = "This can't be undone. Its favorites will be removed too.",
            confirmLabel = "Delete",
            onConfirm = { onDeleteProfile(profile); deletingProfile = null },
            onDismiss = { deletingProfile = null },
        )
    }
}

/**
 * Reorder mode: a plain vertical list (never scrolls in practice — capped at
 * [com.lumenlauncher.app.data.ProfileRepository.MAX_PROFILES] rows) of drag-handle + name +
 * delete rows, with a non-reorderable "Add profile" row pinned at the bottom. Only the handle on
 * the left has a drag gesture attached — the rest of the row has no competing gesture, so there's
 * no long-press/axis-conflict to arbitrate the way there was for the carousel's own drag.
 */
@Composable
private fun ProfileReorderList(
    profiles: List<ProfileEntity>,
    canAddProfile: Boolean,
    canDeleteProfile: Boolean,
    onOrderChanged: (List<ProfileEntity>) -> Unit,
    onCommit: () -> Unit,
    onEditProfile: (Long) -> Unit,
    onDeleteRequest: (ProfileEntity) -> Unit,
    onAddProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rowSlotHeightPx = with(LocalDensity.current) { (REORDER_ROW_HEIGHT + REORDER_ROW_SPACING).toPx() }
    val reorderState = rememberDragReorderState(
        items = profiles,
        key = { it.id },
        axis = Orientation.Vertical,
        slotSizePx = rowSlotHeightPx,
        onOrderChanged = onOrderChanged,
        onDragCommit = { onCommit() },
    )

    LazyColumn(
        modifier = modifier.testTag("profile_reorder_list"),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(REORDER_ROW_SPACING),
    ) {
        item(key = "reorder_hint") {
            Text(
                text = "Drag to re-order profiles",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                modifier = Modifier.padding(bottom = 4.dp).testTag("profile_reorder_hint"),
            )
        }
        items(profiles, key = { it.id }) { profile ->
            val isDragged = reorderState.isDragging(profile)
            ProfileReorderRow(
                profile = profile,
                canDelete = canDeleteProfile,
                onEditProfileClick = { onEditProfile(profile.id) },
                onDeleteClick = { onDeleteRequest(profile) },
                dragHandleModifier = reorderState.dragModifier(profile),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isDragged) Modifier else Modifier.animateItem())
                    .graphicsLayer {
                        translationY = if (isDragged) reorderState.dragOffset else 0f
                        scaleX = if (isDragged) REORDER_DRAG_SCALE else 1f
                        scaleY = if (isDragged) REORDER_DRAG_SCALE else 1f
                        shadowElevation = if (isDragged) REORDER_DRAG_ELEVATION.toPx() else 0f
                        shape = RoundedCornerShape(12.dp)
                        clip = false
                    }
                    .zIndex(if (isDragged) 1f else 0f),
            )
        }
        item(key = "add_profile_row") {
            AddProfileRow(
                enabled = canAddProfile,
                onClick = onAddProfile,
                modifier = Modifier.fillMaxWidth().testTag("profile_reorder_add_row"),
            )
        }
    }
}

@Composable
private fun ProfileReorderRow(
    profile: ProfileEntity,
    canDelete: Boolean,
    onEditProfileClick: () -> Unit,
    onDeleteClick: () -> Unit,
    dragHandleModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .height(REORDER_ROW_HEIGHT)
            // M3's Card default shape — see CLAUDE.md's Material 3 shape section.
            .clip(MaterialTheme.shapes.medium)
            .background(Surface)
            .padding(horizontal = 12.dp)
            .testTag("profile_reorder_row_${profile.id}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.DragHandle,
            contentDescription = "Drag to reorder",
            tint = Faint,
            modifier = Modifier
                .padding(end = 12.dp)
                .testTag("profile_reorder_handle_${profile.id}")
                .then(dragHandleModifier),
        )
        Text(text = profile.name, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.testTag("profile_reorder_menu_${profile.id}"),
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "Profile options", tint = Muted)
            }
            ThemedDropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                ThemedDropdownMenuItem(
                    label = "Profile settings",
                    onClick = { menuExpanded = false; onEditProfileClick() },
                )
                ThemedDropdownMenuItem(
                    label = "Delete",
                    enabled = canDelete,
                    destructive = true,
                    onClick = { menuExpanded = false; onDeleteClick() },
                )
            }
        }
    }
}

@Composable
private fun AddProfileRow(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (!enabled) return
    Row(
        modifier = modifier
            .height(REORDER_ROW_HEIGHT)
            // M3's Card default shape — see CLAUDE.md's Material 3 shape section.
            .clip(MaterialTheme.shapes.medium)
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Accent)
        Text(text = "Add profile", style = MaterialTheme.typography.bodyLarge, color = Accent, modifier = Modifier.padding(start = 8.dp))
    }
}

/**
 * The launcher-wide settings entry point, sitting just below the carousel. Carries the same
 * shadow + hairline-border lift as [com.lumenlauncher.app.ui.components.SettingsCard] (a bare
 * [Surface] fill is invisible against this screen's dimmer backdrop, especially in dark mode) and
 * a leading [Icons.Default.Tune] glyph — `Tune`, not a gear, so it doesn't read as a second
 * "profile settings" control next to each card's own gear.
 */
@Composable
private fun LauncherSettingsRow(onClick: () -> Unit, modifier: Modifier = Modifier) {
    // M3's Card default shape — see CLAUDE.md's Material 3 shape section.
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = shape)
            .clip(shape)
            .background(Surface)
            .border(1.dp, Ink.copy(alpha = 0.14f), shape)
            .clickable(onClick = onClick)
            .testTag("profile_carousel_launcher_settings")
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = Muted,
                modifier = Modifier.padding(end = 14.dp).size(24.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = "Launcher settings", style = MaterialTheme.typography.bodyLarge, color = Ink)
                Text(text = "Default clock, favorites, drawer, badges", style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
        }
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Ink)
    }
}

/**
 * A live snapshot of what [profile]'s Home screen actually looks like — reuses the real
 * [ClockBlock]/[AppRow]/[DockIcon] composables (not a redrawn lookalike) and renders each
 * profile's own effective content ([favorites]/[calendarEvents], from
 * [com.lumenlauncher.app.domain.ObserveProfilePreviewsUseCase], one Flow per profile so browsing
 * shows each profile's own list, not just the active profile's), so the carousel card is a
 * genuine preview rather than a placeholder.
 */
@Composable
private fun ProfilePreviewPage(
    profile: ProfileEntity,
    favorites: List<AppInfo>,
    listContentMode: ListContentMode,
    clockTemplateId: ClockTemplateId,
    clockFontOption: ClockFontOption,
    clockColorOption: ClockColorOption,
    clockAccentColorOption: ClockColorOption,
    use24HourTime: Boolean,
    clockShowMeridiem: Boolean,
    clockDateStyle: ClockDateStyle,
    /** This profile's own clock alignment, falling back to the global default when not overriding (see [com.lumenlauncher.app.data.model.LauncherSettings.clockAlignment]). */
    clockAlignment: ClockAlignment,
    /** This profile's own clock scale — see [com.lumenlauncher.app.data.model.LauncherSettings.clockScale]. */
    clockScale: Float = 0.8f,
    /** Independent of [clockAlignment] (see [com.lumenlauncher.app.data.model.LauncherSettings.calendarAlignment]). */
    calendarAlignment: ClockAlignment,
    /** This profile's own clock zone height, falling back to the global default when not overriding (see [com.lumenlauncher.app.data.model.LauncherSettings.clockZoneHeightDp]) — reproduces the same clock-position formula [com.lumenlauncher.app.ui.home.HomeScreen] uses, scaled to this card's own size. */
    clockZoneHeightDp: Float?,
    calendarEvents: List<CalendarEvent>,
    calendarColors: Map<String, String>,
    calendarFontOption: ClockFontOption,
    calendarColorOption: ClockColorOption,
    calendarFontWeight: FontWeightOption,
    launcherFontOption: LauncherFontOption,
    appRowPosition: AppRowPosition,
    appRowPresentation: AppRowPresentation,
    appLabelColorOption: ClockColorOption,
    homeAppsFontWeight: FontWeightOption,
    dockApps: List<AppInfo>,
    dockDisplayMode: DockDisplayMode,
    canDelete: Boolean,
    onEditProfileClick: () -> Unit,
    onDeleteClick: () -> Unit,
    /** Tapping the card anywhere applies its profile (see chat history) — including on top of a
     *  favorite row or dock icon, whose own [AppRow]/[DockIcon] click would otherwise consume the
     *  touch instead of letting it reach the card's own clickable underneath. */
    onCardClick: () -> Unit,
    /** The device's own width:height — the card is locked to it so it stays a true scale model of the screen. */
    screenAspectRatio: Float,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Sits right above the card itself (on the carousel's own backdrop, not the card's
        // Surface background) but still belongs to this page — swiping to a neighbor swaps in
        // that page's own name along with its card, per direct request (see chat history). The
        // Profile settings/Delete icons sit below the card instead (see chat history —
        // moved back down from here, mirroring this screen's original gear/trash row below the
        // pager), so this row is name-only now.
        Text(
            text = profile.name,
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp).testTag("profile_page_name_${profile.id}"),
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                // Locked to the real screen's proportions — a genuine scale model, not a card
                // shape that happens to fall out of the surrounding layout.
                .aspectRatio(screenAspectRatio)
                // A large hero surface — M3's Dialog/ModalBottomSheet-class extraLarge shape, see
                // CLAUDE.md's Material 3 shape section.
                .clip(MaterialTheme.shapes.extraLarge)
                .background(Surface)
                // Card-only tap/ripple target — kept off the name+menu header above so a
                // long-press ripple doesn't bleed across the whole page (see chat history).
                .clickable(onClick = onCardClick),
        ) {
            // This card renders at a fraction of Home's real on-screen size (it's the screen
            // scaled by [CAROUSEL_CARD_SCALE]). Rather than hand-picking fixed dp overrides for
            // every size in here (which needed re-tuning by eye each time — see chat history), we
            // measure how tall this card actually ends up on screen, compare it to the real
            // screen height, and use that ratio to shrink every dp/sp value inside proportionally
            // via a scaled LocalDensity — so all of the card's content (icon
            // sizes, text sizes, paddings) scales down together, consistently.
            val screenHeight = LocalConfiguration.current.screenHeightDp.dp
            val contentScale = (maxHeight / screenHeight).coerceIn(0.4f, 1f)
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(baseDensity.density * contentScale, baseDensity.fontScale),
            ) {
                // Reproduces HomeScreen's own clockZoneHeightDp formula (dp values are
                // density-independent, so the same numbers are meaningful here even though this
                // card renders under the scaled density above) — a null value (never dragged)
                // resolves to the clock's original fixed top offset, same as the real Home screen;
                // a persisted value pushes the clock down by exactly as much here, proportionally
                // shrunk along with everything else in this card.
                var clockNaturalHeightDp by remember { mutableFloatStateOf(0f) }
                val previewDensity = LocalDensity.current
                val topOffsetDp = clockZoneHeightDp
                    ?.let { (it - HOME_CLOCK_MIN_GAP.value - clockNaturalHeightDp).coerceAtLeast(0f) }
                    ?: HOME_CLOCK_DEFAULT_TOP_OFFSET.value
                Column(
                    modifier = Modifier.fillMaxSize().padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ClockBlock(
                        use24HourTime = use24HourTime,
                        templateId = clockTemplateId,
                        fontOption = clockFontOption,
                        colorOption = clockColorOption,
                        accentColorOption = clockAccentColorOption,
                        showMeridiem = clockShowMeridiem,
                        dateStyle = clockDateStyle,
                        clockAlignment = clockAlignment,
                        calendarAlignment = calendarAlignment,
                        events = calendarEvents,
                        calendarColors = calendarColors,
                        calendarFontOption = calendarFontOption,
                        calendarColorOption = calendarColorOption,
                        calendarFontWeight = calendarFontWeight,
                        launcherFontOption = launcherFontOption,
                        clockScale = clockScale,
                        modifier = Modifier
                            .align(clockAlignment.resolve())
                            .padding(top = topOffsetDp.dp)
                            .onGloballyPositioned { clockNaturalHeightDp = with(previewDensity) { it.size.height.toDp().value } }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = listContentMode.previewLabel(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    )
                    if (favorites.isEmpty()) {
                        Text(text = "No favorites yet", style = MaterialTheme.typography.bodyMedium, color = Muted, modifier = Modifier.fillMaxWidth())
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            favorites.forEach { app ->
                                AppRow(
                                    app = app,
                                    onClick = onCardClick,
                                    badgeCount = null,
                                    badgeStyle = NotificationBadgeStyle.DOT,
                                    onRequestShortcuts = { emptyList() },
                                    onLaunchShortcut = {},
                                    position = appRowPosition,
                                    presentation = appRowPresentation,
                                    labelColor = appLabelColorOption.resolve(),
                                    labelFontWeight = homeAppsFontWeight.resolve(),
                                    // This card is a read-only preview of a profile's Home layout,
                                    // not a place to manage apps — long-press must not open the
                                    // real Uninstall/App Info/shortcuts menu (see chat history).
                                    enableLongPressMenu = false,
                                    // Uses AppRow's own real Home density (16dp) — the scaled
                                    // LocalDensity above shrinks it proportionally already, so no
                                    // fixed override is needed here (see chat history).
                                )
                            }
                        }
                    }

                    // Dock is shared across all profiles (not per-profile); omit-when-empty. Uses
                    // Home's own real 16dp gap — the scaled LocalDensity above shrinks it along
                    // with everything else, so no fixed override is needed (see chat history).
                    Spacer(modifier = Modifier.height(16.dp))
                    if (dockApps.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            dockApps.forEach { app ->
                                DockIcon(
                                    app = app,
                                    displayMode = dockDisplayMode,
                                    onClick = onCardClick,
                                    labelColor = appLabelColorOption.resolve(),
                                    labelFontWeight = homeAppsFontWeight.resolve(),
                                    // Same read-only-preview reasoning as the AppRow above.
                                    enableLongPressMenu = false,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Below the card, not above it with the name — mirrors this screen's original gear/trash
        // icon row sitting below the pager (see chat history), just per-page now instead of tied
        // to a shared "centered profile" concept. Two direct icon buttons, not a "..." overflow
        // menu — with only two actions and both worth surfacing at a glance, the extra tap to
        // open a menu added nothing (see chat history).
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                IconButton(
                    onClick = onEditProfileClick,
                    modifier = Modifier.testTag("profile_page_settings_${profile.id}"),
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Profile settings", tint = Muted)
                }
                IconButton(
                    onClick = onDeleteClick,
                    enabled = canDelete,
                    modifier = Modifier.testTag("profile_page_delete_${profile.id}"),
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete profile",
                        tint = if (canDelete) Muted else Faint,
                    )
                }
            }
        }
    }
}

/** Matches HomeScreen.kt's own list-header label exactly, so the preview's section header agrees with the real screen. */
private fun ListContentMode.previewLabel(): String = when (this) {
    ListContentMode.FAVORITES -> "FAVORITES"
    ListContentMode.RECENTS -> "RECENTS"
    ListContentMode.MOST_USED -> "MOST USED"
}

@Composable
private fun AddProfilePage(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        // Invisible stand-in for ProfilePreviewPage's own name row — same Text structure so it
        // measures to the exact same height, without hardcoding a dp guess. Without this, the
        // Add-profile card (which has no header of its own) would start higher up than its
        // neighboring profile cards and read as a different, taller size (see chat history).
        Text(
            text = "",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp).alpha(0f),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                // A large hero surface — M3's Dialog/ModalBottomSheet-class extraLarge shape, see
                // CLAUDE.md's Material 3 shape section.
                .clip(MaterialTheme.shapes.extraLarge)
                .background(Surface)
                .clickable(onClick = onClick)
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 96.dp, height = 70.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "+", style = LumenType.clock.copy(fontSize = 34.sp), color = Faint)
            }
            Text(text = "Add profile", style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.padding(top = 8.dp))
            Text(
                text = "Starts with your launcher's default clock, calendar, favorite apps, and settings. Customize them per profile.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        // Invisible stand-in for ProfilePreviewPage's own bottom menu row — same reasoning as the
        // header spacer above, now that the menu lives below the card instead of beside the name.
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).alpha(0f),
            horizontalArrangement = Arrangement.Center,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                IconButton(onClick = {}, enabled = false) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                }
                IconButton(onClick = {}, enabled = false) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileCarouselScreenPreview() {
    LumenLauncherTheme {
        ProfileCarouselContent(
            uiState = ProfileCarouselUiState(
                profiles = listOf(
                    ProfileEntity(id = 1, name = "Profile 1", position = 0),
                    ProfileEntity(id = 2, name = "Work", position = 1),
                ),
                activeProfileId = 1,
            ),
            mode = ProfileCarouselMode.SWITCH,
            onBack = {},
            onSelect = {},
            onEditProfile = {},
            onAddProfile = {},
            onDeleteProfile = {},
            onReorder = {},
            onNavigateToSettings = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileManageScreenPreview() {
    LumenLauncherTheme {
        ProfileCarouselContent(
            uiState = ProfileCarouselUiState(
                profiles = listOf(
                    ProfileEntity(id = 1, name = "Profile 1", position = 0),
                    ProfileEntity(id = 2, name = "Work", position = 1),
                ),
                activeProfileId = 1,
            ),
            mode = ProfileCarouselMode.MANAGE,
            onBack = {},
            onSelect = {},
            onEditProfile = {},
            onAddProfile = {},
            onDeleteProfile = {},
            onReorder = {},
            onNavigateToSettings = {},
        )
    }
}
