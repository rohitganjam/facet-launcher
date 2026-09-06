package com.lumenlauncher.app.ui.profiles

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
import com.lumenlauncher.app.data.model.ClockColorOption
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
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.CarouselBackdrop
import com.lumenlauncher.app.ui.theme.Faint
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.LumenType
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Surface
import com.lumenlauncher.app.ui.theme.resolve

/** Every page (profile or Add) renders at a fixed, position-independent scale — smaller than a
 *  full-size card so more of each neighbor peeks in on either side, recent-apps style. */
private const val CARD_SCALE = 0.855f

private val CAROUSEL_PAGE_INSET = 60.dp
private val CAROUSEL_PAGE_SPACING = 8.dp

private val REORDER_ROW_HEIGHT = 64.dp
private val REORDER_ROW_SPACING = 10.dp

/** Shared "picked up" drag-lift treatment, matching every other reorderable list in Settings. */
private val REORDER_DRAG_ELEVATION = 6.dp
private const val REORDER_DRAG_SCALE = 1.04f

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
    viewModel: ProfileCarouselViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileCarouselContent(
        uiState = uiState,
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

@Composable
private fun ProfileCarouselContent(
    uiState: ProfileCarouselUiState,
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
    if (profiles.isEmpty()) {
        // EnsureActiveProfileUseCase seeds the first profile on app launch — this is only
        // visible for the brief window before that completes.
        return
    }

    val pageCount = profiles.size + if (uiState.canAddProfile) 1 else 0
    val initialPage = profiles.indexOfFirst { it.id == uiState.activeProfileId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { pageCount }
    var isReordering by remember { mutableStateOf(false) }
    var reorderWorkingList by remember(profiles) { mutableStateOf(profiles) }
    var deletingProfile by remember { mutableStateOf<ProfileEntity?>(null) }

    BackHandler(enabled = isReordering) { isReordering = false }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CarouselBackdrop)
            .testTag("profile_carousel_screen")
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BackButton(onClick = { if (isReordering) isReordering = false else onBack() })
                // headlineSmall to match Settings' own header size (see chat history) — was titleMedium.
                Text(text = "Profiles", style = MaterialTheme.typography.headlineSmall, color = Ink)
            }
            // No "Done" counterpart once reordering — every drag-release autosaves via onCommit,
            // so there's nothing left to confirm; the back button (above) already exits reorder
            // mode, same as the system back gesture (see the BackHandler below).
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
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = CAROUSEL_PAGE_INSET),
                pageSpacing = CAROUSEL_PAGE_SPACING,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp).testTag("profile_carousel_pager"),
            ) { page ->
                // Uniform, position-independent scale for every page — see CARD_SCALE. Anchored
                // to top-center (not the default center) so the shrink only eats into the bottom
                // of the page — the name/menu row now living right at each page's own top would
                // otherwise get pushed down by half the scale's lost height too, reading as a big
                // dead gap between the screen header and the name row (see chat history).
                val visualModifier = Modifier.graphicsLayer {
                    scaleX = CARD_SCALE
                    scaleY = CARD_SCALE
                    transformOrigin = TransformOrigin(0.5f, 0f)
                }

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
                        use24HourTime = uiState.effectiveUse24HourTime(profile.id),
                        clockShowMeridiem = uiState.clockShowMeridiem(profile.id),
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
                        modifier = visualModifier
                            .fillMaxSize()
                            .testTag("profile_page_${profile.id}")
                            .clickable { onSelect(profile.id) },
                    )
                } else {
                    AddProfilePage(
                        onClick = onAddProfile,
                        modifier = visualModifier.fillMaxSize().testTag("profile_carousel_add_page"),
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
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            )
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
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(REORDER_ROW_SPACING),
    ) {
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
 * Pinned to the bottom of the carousel — reached here now instead of via the Home long-press
 * sheet's own "Launcher settings" row (see chat history: that sheet is being replaced by a direct
 * long-press-to-switch-profile action, so this screen absorbs the entry point it displaced).
 */
@Composable
private fun LauncherSettingsRow(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            // M3's Card default shape — see CLAUDE.md's Material 3 shape section.
            .clip(MaterialTheme.shapes.medium)
            .background(Surface)
            .clickable(onClick = onClick)
            .testTag("profile_carousel_launcher_settings")
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = "Launcher settings", style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(text = "Clock, favorites, drawer, badges", style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
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
    use24HourTime: Boolean,
    clockShowMeridiem: Boolean,
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
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxSize()) {
        // Sits right above the card itself (on the carousel's own backdrop, not the card's
        // Surface background) but still belongs to this page — swiping to a neighbor swaps in
        // that page's own name/menu along with its card, per direct request (see chat history).
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).testTag("profile_page_name_${profile.id}"),
            )
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("profile_page_menu_${profile.id}"),
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Profile options", tint = Muted)
                }
                ThemedDropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    ThemedDropdownMenuItem(
                        label = "Profile settings",
                        onClick = { menuExpanded = false; onEditProfileClick() },
                    )
                    ThemedDropdownMenuItem(
                        label = "Delete profile",
                        enabled = canDelete,
                        destructive = true,
                        onClick = { menuExpanded = false; onDeleteClick() },
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                // A large hero surface — M3's Dialog/ModalBottomSheet-class extraLarge shape, see
                // CLAUDE.md's Material 3 shape section.
                .clip(MaterialTheme.shapes.extraLarge)
                .background(Surface)
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ClockBlock(
                use24HourTime = use24HourTime,
                templateId = clockTemplateId,
                fontOption = clockFontOption,
                colorOption = clockColorOption,
                showMeridiem = clockShowMeridiem,
                events = calendarEvents,
                calendarColors = calendarColors,
                calendarFontOption = calendarFontOption,
                calendarColorOption = calendarColorOption,
                calendarFontWeight = calendarFontWeight,
                launcherFontOption = launcherFontOption,
                modifier = Modifier.align(Alignment.Start)
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
                            onClick = {},
                            badgeCount = null,
                            badgeStyle = NotificationBadgeStyle.DOT,
                            onRequestShortcuts = { emptyList() },
                            onLaunchShortcut = {},
                            position = appRowPosition,
                            presentation = appRowPresentation,
                            labelColor = appLabelColorOption.resolve(),
                            labelFontWeight = homeAppsFontWeight.resolve(),
                        )
                    }
                }
            }

            // Dock is shared across all profiles (not per-profile), same 16dp gap + omit-when-empty
            // + SpaceEvenly distribution as the real Home screen (ui/home/HomeScreen.kt) so this
            // preview matches it.
            Spacer(modifier = Modifier.height(16.dp))
            if (dockApps.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    dockApps.forEach { app ->
                        DockIcon(
                            app = app,
                            displayMode = dockDisplayMode,
                            onClick = {},
                            labelColor = appLabelColorOption.resolve(),
                            labelFontWeight = homeAppsFontWeight.resolve(),
                        )
                    }
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
    Column(
        modifier = modifier
            .fillMaxSize()
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
            text = "Copies this profile's settings. Favorites start empty.",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )
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
