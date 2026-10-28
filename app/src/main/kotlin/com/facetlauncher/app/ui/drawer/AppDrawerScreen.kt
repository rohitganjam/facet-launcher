package com.facetlauncher.app.ui.drawer

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import android.os.UserHandle
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Folder as FolderIcon
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.WorkProfileInfo
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.ContactConnection
import com.facetlauncher.app.data.model.ContactInfo
import com.facetlauncher.app.data.model.DrawerFolderDisplayMode
import com.facetlauncher.app.data.model.DrawerItem
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.DrawerGridDensity
import com.facetlauncher.app.data.model.DrawerListItemSize
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.RecentlyInstalledPosition
import com.facetlauncher.app.data.model.SearchBarPosition
import com.facetlauncher.app.data.model.SettingsSearchEntry
import com.facetlauncher.app.domain.GroupAppsByLetterUseCase
import com.facetlauncher.app.domain.RankBySearchRelevanceUseCase
import com.facetlauncher.app.domain.RecentlyInstalledAppsUseCase
import com.facetlauncher.app.domain.GroupedItems
import com.facetlauncher.app.domain.QuickAddState
import com.facetlauncher.app.domain.QuickPlacementAction
import com.facetlauncher.app.ui.components.AppContextMenu
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.AppMenuNotifications
import com.facetlauncher.app.ui.components.BlockHomeSwipesWhileShown
import com.facetlauncher.app.ui.components.DismissOnHomePress
import com.facetlauncher.app.ui.components.FolderContentsSheet
import com.facetlauncher.app.ui.components.FolderSheetHeaderAction
import com.facetlauncher.app.ui.components.FolderTileContextMenu
import com.facetlauncher.app.ui.components.NotificationBadge
import com.facetlauncher.app.ui.components.RecencyIcon
import com.facetlauncher.app.ui.components.ThemedDropdownMenu
import com.facetlauncher.app.ui.components.ThemedDropdownMenuItem
import com.facetlauncher.app.ui.components.longPressReleaseClickable
import com.facetlauncher.app.ui.home.FolderTileGlyph
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.DrawerAppTextColor
import com.facetlauncher.app.ui.theme.DrawerHeaderTextColor
import com.facetlauncher.app.ui.theme.DrawerOverlay
import com.facetlauncher.app.ui.theme.IconTile
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.InkInverted
import com.facetlauncher.app.ui.theme.FACET_TRANSITION_DURATION_MS
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.FacetTransitionEasing
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Scrim
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

private const val TOUCH_ZONE_HEIGHT_FRACTION = 0.9f

/**
 * Was `34` — narrower than Android's own recommended minimum touch target of `48.dp` (this
 * project's own guideline value, see chat history), which made the edge genuinely hard to hit
 * reliably. The visible rail itself stays compact ([AlphabetRail]'s own `.width(34.dp)`) — only
 * this invisible catching area grows, so the touch target is more forgiving without the letters
 * themselves looking any wider.
 */
private const val EDGE_ZONE_WIDTH_DP = 48

/**
 * App Drawer — List (`1h`) or Grid (`1i`) presentation, per Settings. A rounded-rectangle search
 * bar (`1j`) text-filters the list and can render at the top or bottom (Settings' "Search bar
 * position"); its 3-dot overflow menu is a second entry point into Launcher Settings.
 * Alphabet-jump covers both the right rail (F7) and, always, the same behavior from a left-edge
 * gesture zone (F8) — no second rail is rendered on the left, it's the same touch behavior from
 * a different edge. Both zones span 90% of the drawer height; the visible rail itself wraps its
 * own (much shorter) content height and sits centered within that zone — a touch above/below
 * the rail's actual letters still clamps to the nearest end letter rather than requiring
 * pixel-precise placement — see [letterAt].
 *
 * The rail, its drag/band-clamping logic, and the letter-grouping computation ([GroupAppsByLetterUseCase])
 * are identical for both presentations — only the terminal scroll call (into a [LazyListState] or
 * [LazyGridState]) and the tile-rendering differ, since a flat "1 header + N per-app items"
 * sequence indexes the same way regardless of how those items are laid out visually.
 */
@Composable
fun AppDrawerScreen(
    apps: ImmutableList<AppInfo>,
    modifier: Modifier = Modifier,
    /** Every genuine Work Profile currently on this device — see [com.facetlauncher.app.ui.launcher.LauncherUiState.workProfiles]'s own doc for why this isn't just inferred from [apps]. One tab per entry, in addition to the always-present Personal tab (which also carries [AppProfile.OTHER] apps — an unclassifiable profile, like an OEM clone/dual-app profile, badged but not split into its own tab). */
    workProfiles: ImmutableList<WorkProfileInfo> = persistentListOf(),
    listState: LazyListState = rememberLazyListState(),
    gridState: LazyGridState = rememberLazyGridState(),
    /** Search results' own scroll position — distinct from [listState]/[gridState] (the browse-mode lists), so the caller's swipe-down-to-close nested scroll connection can tell whether the *visible* list is actually at its top while searching. */
    searchListState: LazyListState = rememberLazyListState(),
    actions: DrawerItemActions = DrawerItemActions(),
    appearance: DrawerAppearance = DrawerAppearance(),
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    searchBar: DrawerSearchBarConfig = DrawerSearchBarConfig(),
    searchSources: DrawerSearchSources = DrawerSearchSources(),
    appMenuNotifications: AppMenuNotifications = AppMenuNotifications.None,
    /**
     * The full folder library — both the long-press "Add to folder" row's candidate list and,
     * per [DrawerAppearance.folderDisplayMode], the browse-mode content this screen renders as its own drawer
     * entries. `null`/empty either way just means no folders exist yet.
     */
    folderCandidates: ImmutableList<Folder>? = null,
    /** True while the drawer itself is open/opening (caller's own `isDrawerOpen`). Used only to reset the connections sheet once the drawer is fully closed — see the `LaunchedEffect` below. */
    isDrawerOpen: Boolean = true,
    /** Injected into `DrawerViewModel`; the default serves previews and tests, which have none. */
    listUseCases: DrawerListUseCases = DrawerListUseCases(RankBySearchRelevanceUseCase(), GroupAppsByLetterUseCase(), RecentlyInstalledAppsUseCase()),
) {
    val isSearching = query.isNotBlank()
    val browse = rememberDrawerBrowseModel(
        apps = apps,
        workProfiles = workProfiles,
        query = query,
        folderCandidates = folderCandidates,
        presentation = appearance.presentation,
        folderDisplayMode = appearance.folderDisplayMode,
        recentlyInstalledPosition = appearance.recentlyInstalledPosition,
        listUseCases = listUseCases,
    )
    val focusManager = LocalFocusManager.current
    val connectionsSheet = rememberConnectionsSheet(searchSources.onRequestConnections, isDrawerOpen)

    // First back-press while searching clears the query and stays in the drawer (browse mode,
    // unfiltered); only a second, empty-query back-press falls through to the caller's own
    // drawer-close BackHandler.
    BackHandler(enabled = isSearching) {
        focusManager.clearFocus()
        onQueryChange("")
    }

    // Outer Box so the connections sheet can overlay the entire screen.
    Box(modifier = modifier.fillMaxSize()) {
        // Edge-to-edge is enforced unconditionally at this app's targetSdk (36) — without this, the
        // drawer's content (notably a TOP search bar) draws straight under the status bar/notch.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceContainer.copy(alpha = appearance.opacity))
                .windowInsetsPadding(WindowInsets.systemBars),
        ) {
            if (searchBar.position == SearchBarPosition.TOP) DrawerSearchBar(query, onQueryChange, searchBar)

            // Browse-mode only — hidden entirely on a device with no Work Profile, so this is a genuine
            // no-op for the common case.
            if (workProfiles.isNotEmpty() && !isSearching) {
                DrawerProfileTabRow(
                    tabs = browse.tabs,
                    selected = browse.selectedTab,
                    onSelect = browse.onSelectTab,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                if (isSearching) {
                    DrawerSearchResults(
                        query = query,
                        apps = browse.searchResultApps,
                        listState = searchListState,
                        appearance = appearance,
                        actions = actions,
                        sources = searchSources,
                        onClearSearch = { onQueryChange("") },
                        appMenuNotifications = appMenuNotifications,
                        folderCandidates = folderCandidates,
                        // Closing the keyboard here (not just clearing text-field focus): the user was very
                        // likely still typing when they tapped a contact, and it looked stuck behind the sheet.
                        onContactClick = { focusManager.clearFocus(); connectionsSheet.open(it) },
                        modifier = Modifier.fillMaxSize().testTag("drawer_search_results"),
                    )
                } else {
                    DrawerBrowseBody(browse, listState, gridState, appearance, actions, appMenuNotifications, folderCandidates)
                }
            }

            if (searchBar.position == SearchBarPosition.BOTTOM) {
                DrawerSearchBar(query, onQueryChange, searchBar, Modifier.imePadding())
            }
        }

        ContactConnectionsOverlay(connectionsSheet.contact, connectionsSheet.connections, connectionsSheet.dismiss)
    }
}

/** The connections sheet's state: the contact it is open for and that contact's ways to reach them. */
@Stable
private class ConnectionsSheet(
    val contact: ContactInfo?,
    val connections: ImmutableList<ContactConnection>,
    val open: (ContactInfo) -> Unit,
    val dismiss: () -> Unit,
)

/**
 * Hoisted to the screen (not down in the results) so the sheet can overlay the whole drawer. The screen never leaves composition
 * when the drawer closes (it is only translated off-screen), so it is reset once the drawer is closed or Home is pressed.
 */
@Composable
private fun rememberConnectionsSheet(onRequestConnections: suspend (ContactInfo) -> List<ContactConnection>, isDrawerOpen: Boolean): ConnectionsSheet {
    var contact by remember { mutableStateOf<ContactInfo?>(null) }
    var connections by remember { mutableStateOf<ImmutableList<ContactConnection>>(persistentListOf()) }
    val currentOnRequestConnections by rememberUpdatedState(onRequestConnections)
    LaunchedEffect(contact) {
        val selected = contact
        connections = if (selected != null) currentOnRequestConnections(selected).toImmutableList() else persistentListOf()
    }
    LaunchedEffect(isDrawerOpen) {
        if (!isDrawerOpen) contact = null
    }
    DismissOnHomePress(enabled = contact != null) { contact = null }
    return ConnectionsSheet(contact, connections, open = { contact = it }, dismiss = { contact = null })
}

/** Browse mode: the app list or grid with the alphabet rail beside it, the left-edge jump zone and the rail's selection popup. */
@Composable
private fun BoxScope.DrawerBrowseBody(
    browse: DrawerBrowseModel,
    listState: LazyListState,
    gridState: LazyGridState,
    appearance: DrawerAppearance,
    actions: DrawerItemActions,
    appMenuNotifications: AppMenuNotifications,
    folderCandidates: ImmutableList<Folder>?,
) {
    val coroutineScope = rememberCoroutineScope()
    var draggingSelection by remember { mutableStateOf<RailSelection?>(null) }
    var railHeightPx by remember { mutableFloatStateOf(0f) }
    val isGrid = appearance.presentation == DrawerPresentation.GRID

    // The pure index math lives in [railScrollTarget]; this only scrolls there.
    fun onRailSelectionChanged(selection: RailSelection?) {
        draggingSelection = selection
        val target = selection?.let {
            railScrollTarget(
                selection = it,
                presentation = appearance.presentation,
                groupedItems = browse.groupedItems,
                folderDisplayMode = appearance.folderDisplayMode,
                pinnedFolderCount = browse.pinnedFolders.size,
                recentlyInstalledPosition = appearance.recentlyInstalledPosition,
                recentlyInstalledOffset = browse.recentlyInstalledOffset,
            )
        } ?: return
        coroutineScope.launch { if (isGrid) gridState.scrollToItem(target) else listState.scrollToItem(target) }
    }

    Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
        if (isGrid) {
            DrawerGridContent(
                groupedItems = browse.groupedItems,
                pinnedFolders = browse.pinnedFolders,
                gridState = gridState,
                appearance = appearance,
                actions = actions,
                appMenuNotifications = appMenuNotifications,
                folderCandidates = folderCandidates,
                modifier = Modifier.weight(1f).fillMaxHeight().testTag("drawer_grid"),
            )
        } else {
            DrawerListContent(
                groupedItems = browse.groupedItems,
                pinnedFolders = browse.pinnedFolders,
                recentlyInstalledApps = browse.recentlyInstalledApps,
                listState = listState,
                appearance = appearance,
                actions = actions,
                appMenuNotifications = appMenuNotifications,
                folderCandidates = folderCandidates,
                modifier = Modifier.weight(1f).fillMaxHeight().testTag("drawer_list"),
            )
        }
        LetterJumpZone(
            letters = browse.railLetters,
            activeSelection = draggingSelection,
            folderPosition = browse.railFolderPosition,
            recentlyInstalledPosition = browse.railRecentlyInstalledPosition,
            onSelectionChange = ::onRailSelectionChanged,
            showRail = true,
            railHeightPx = railHeightPx,
            onRailHeightMeasure = { railHeightPx = it },
            modifier = Modifier.testTag("alphabet_rail"),
        )
    }

    LetterJumpZone(
        letters = browse.railLetters,
        activeSelection = draggingSelection,
        folderPosition = browse.railFolderPosition,
        recentlyInstalledPosition = browse.railRecentlyInstalledPosition,
        onSelectionChange = ::onRailSelectionChanged,
        showRail = false,
        railHeightPx = railHeightPx,
        onRailHeightMeasure = {},
        modifier = Modifier.align(Alignment.CenterStart).testTag("left_edge_letter_jump_zone"),
        requireDrag = true,
    )

    draggingSelection?.let { RailSelectionIndicator(it, Modifier.align(Alignment.Center)) }
}

/** The popup naming the letter, folder glyph or "Recently installed" glyph the rail is currently on. A fixed footprint, so a wide letter like "W" doesn't resize it versus a narrow one like "I". */
@Composable
private fun RailSelectionIndicator(selection: RailSelection, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .testTag("alphabet_rail_indicator")
            .background(color = Surface.copy(alpha = 0.92f), shape = RoundedCornerShape(20.dp))
            .size(width = 96.dp, height = 64.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (selection) {
            // displayMedium, not FacetType.clock — that's fixed ExtraLight/SansSerif regardless of the user's font setting, and reads thin next to the icons.
            is RailSelection.Letter -> Text(text = selection.letter, style = MaterialTheme.typography.displayMedium, color = Accent)
            RailSelection.Folders -> Icon(
                imageVector = Icons.Outlined.FolderIcon,
                contentDescription = stringResource(R.string.app_picker_tab_folders),
                tint = Accent,
                modifier = Modifier.size(40.dp),
            )
            RailSelection.RecentlyInstalled -> Icon(
                imageVector = RecencyIcon,
                contentDescription = stringResource(R.string.drawer_recently_installed_label),
                tint = Accent,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}

/** Phase 9's connections sheet — a scrim and a slide-up-from-bottom sheet over the whole drawer while [contact] is set. */
@Composable
private fun BoxScope.ContactConnectionsOverlay(contact: ContactInfo?, connections: ImmutableList<ContactConnection>, onDismiss: () -> Unit) {
    // Drags on the sheet belong to its own list; without this the drawer's pull-to-close takes a downward drag before the list can scroll back up.
    if (contact != null) BlockHomeSwipesWhileShown()
    AnimatedVisibility(
        visible = contact != null,
        enter = fadeIn(animationSpec = tween(240)),
        exit = fadeOut(animationSpec = tween(240)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Scrim)
                .testTag("contact_connections_scrim")
                .clickable(onClick = onDismiss),
        )
    }
    AnimatedVisibility(
        visible = contact != null,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)),
        modifier = Modifier.align(Alignment.BottomCenter),
    ) {
        contact?.let { shown ->
            val context = LocalContext.current
            ContactConnectionsSheet(
                contactName = shown.displayName,
                connections = connections,
                onConnectionClick = { intent ->
                    runCatching { context.startActivity(intent) }
                    onDismiss()
                },
                onViewContactClick = {
                    val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, shown.id)
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                    onDismiss()
                },
            )
        }
    }
}

/** Duration for [DrawerProfileTabRow]'s selection-indicator slide — matches `DockPickerTabRow`'s own `TAB_TRANSITION_DURATION_MS`. */
private const val DRAWER_TAB_TRANSITION_DURATION_MS = 220

/**
 * Personal/Work switcher shown above the app list only when a Work Profile exists — mirrors
 * `DockPickerTabRow`'s own pill/sliding-indicator construction (`DockAppPickerScreen.kt`) so it
 * reads as the same visual language rather than a one-off.
 */
@Composable
private fun DrawerProfileTabRow(tabs: ImmutableList<DrawerTab>, selected: DrawerTab, onSelect: (DrawerTab) -> Unit, modifier: Modifier = Modifier) {
    val selectedIndex = tabs.indexOf(selected).coerceAtLeast(0)
    val density = LocalDensity.current
    var rowSizePx by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(SurfaceContainer)
            .padding(4.dp)
            .onSizeChanged { rowSizePx = it },
    ) {
        if (rowSizePx.width > 0) {
            val tabWidth = with(density) { (rowSizePx.width / tabs.size).toDp() }
            val tabHeight = with(density) { rowSizePx.height.toDp() }
            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = tween(DRAWER_TAB_TRANSITION_DURATION_MS),
                label = "drawer_profile_tab_indicator",
            )
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .size(width = tabWidth, height = tabHeight)
                    .clip(CircleShape)
                    .background(Accent),
            )
        }
        Row {
            tabs.forEach { tab ->
                val isSelected = tab == selected
                val testTagSuffix = when (tab) {
                    DrawerTab.Personal -> "personal"
                    is DrawerTab.Work -> "work_${tab.handle.hashCode()}"
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = { onSelect(tab) })
                        .testTag("drawer_profile_tab_$testTagSuffix")
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (tab == DrawerTab.Personal) stringResource(R.string.drawer_tab_personal) else (tab as DrawerTab.Work).label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) Surface else Ink,
                    )
                }
            }
        }
    }
}

/**
 * Rounded-rectangle search bar (`1j`) — text-filters the drawer's app list. The 3-dot overflow
 * menu is a second entry point into Launcher Settings, alongside the long-press sheet. When
 * rendered at the bottom of the drawer (Settings' "Search bar position"), the caller applies
 * [Modifier.imePadding] so it rides above the on-screen keyboard instead of being covered by
 * it — `LauncherActivity` declares `windowSoftInputMode="adjustNothing"`, so nothing happens
 * for free here; this is Compose's own keyboard-avoidance primitive, not a third-party one.
 */
@Composable
private fun DrawerSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    config: DrawerSearchBarConfig,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            // M3's SearchBar defaults to a fully-rounded (CornerFull) container — see
            // CLAUDE.md's Material 3 shape section.
            .clip(CircleShape)
            .background(Surface)
            .testTag("drawer_search_bar"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Muted,
            modifier = Modifier.padding(start = 14.dp),
        )
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(stringResource(R.string.app_picker_search_apps)) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Surface,
                focusedContainerColor = Surface,
                unfocusedIndicatorColor = Accent.copy(alpha = 0f),
                focusedIndicatorColor = Accent.copy(alpha = 0f),
            ),
            modifier = Modifier.weight(1f).testTag("drawer_search_field"),
        )
        Box {
            IconButton(onClick = { menuExpanded = true }, modifier = Modifier.testTag("drawer_search_overflow")) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = stringResource(R.string.drawer_search_more_options), tint = Muted)
            }
            DrawerSearchOverflowMenu(
                expanded = menuExpanded,
                onDismiss = { menuExpanded = false },
                onNavigateToSettings = config.onNavigateToSettings,
                secureFolderIntent = config.secureFolderIntent,
                onOpenSecureFolder = config.onOpenSecureFolder,
                showPrivateSpaceRow = config.showPrivateSpaceRow,
                onPrivateSpaceRowClick = config.onPrivateSpaceRowClick,
            )
        }
    }
}

@Composable
private fun DrawerSearchOverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onNavigateToSettings: () -> Unit,
    secureFolderIntent: Intent?,
    onOpenSecureFolder: (Intent) -> Unit,
    showPrivateSpaceRow: Boolean,
    onPrivateSpaceRowClick: () -> Unit,
) {
    val context = LocalContext.current
    ThemedDropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        ThemedDropdownMenuItem(
            label = stringResource(R.string.drawer_search_launcher_settings),
            onClick = { onDismiss(); onNavigateToSettings() },
            modifier = Modifier.testTag("drawer_search_overflow_settings"),
        )
        ThemedDropdownMenuItem(
            label = stringResource(R.string.drawer_search_system_settings),
            onClick = {
                onDismiss()
                context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            },
            modifier = Modifier.testTag("drawer_search_overflow_system_settings"),
        )
        if (secureFolderIntent != null) {
            ThemedDropdownMenuItem(
                label = stringResource(R.string.drawer_search_open_secure_folder),
                onClick = { onDismiss(); onOpenSecureFolder(secureFolderIntent) },
                modifier = Modifier.testTag("drawer_search_overflow_secure_folder"),
            )
        }
        if (showPrivateSpaceRow) {
            ThemedDropdownMenuItem(
                label = stringResource(R.string.drawer_search_private_space),
                onClick = { onDismiss(); onPrivateSpaceRowClick() },
                modifier = Modifier.testTag("drawer_search_overflow_private_space"),
            )
        }
    }
}

/**
 * Search-mode results (`1j`): apps first (top [MAX_APP_SEARCH_RESULTS], presentation-aware —
 * rows for List, tiles for Grid, no letter headers either way), then a contacts section — never
 * shown at all when [contacts] wasn't even queried (F6 disabled or `READ_CONTACTS` denied; the
 * caller passes an empty list in that case, which reads identically to "no matches" from here,
 * exactly matching the `4p` mockup's own "denied → app results only" annotation). Both sections
 * are capped independently, no "N more" affordance. The `4q` empty state shows only when *both*
 * are empty. Apps render with the exact same [showLabels]/[itemSize] the Drawer's own browse-mode
 * grid/list uses — search used to always render at a fixed base size/label visibility regardless
 * of those settings, which read as a different, unrelated screen (see chat history). Contacts stay
 * list-form regardless of [presentation] (grid doesn't apply to them), but also honor [itemSize].
 */
@Composable
private fun DrawerSearchResults(
    query: String,
    apps: ImmutableList<AppInfo>,
    listState: LazyListState,
    appearance: DrawerAppearance,
    actions: DrawerItemActions,
    sources: DrawerSearchSources,
    onClearSearch: () -> Unit,
    appMenuNotifications: AppMenuNotifications,
    folderCandidates: ImmutableList<Folder>?,
    onContactClick: (ContactInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val hasContactsSection = sources.contacts.isNotEmpty() || sources.showContactsPermissionPrompt || sources.showContactsSettingPrompt
    if (apps.isEmpty() && sources.settingsEntries.isEmpty() && !hasContactsSection) {
        DrawerSearchEmptyState(query = query, onClearSearch = onClearSearch, modifier = modifier)
        return
    }
    LazyColumn(state = listState, modifier = modifier.padding(horizontal = 24.dp)) {
        if (apps.isNotEmpty()) {
            appsSection(apps, appearance, actions, appMenuNotifications, folderCandidates)
        }
        if (hasContactsSection) {
            contactsSection(sources, appearance.listItemSize, onContactClick)
        }
        if (sources.settingsEntries.isNotEmpty()) {
            item { SearchSectionHeader(R.string.drawer_search_section_settings) }
            items(sources.settingsEntries, key = { "settings_" + it.id }) { entry ->
                SettingsResultRow(
                    entry = entry,
                    itemSize = appearance.listItemSize,
                    onClick = { focusManager.clearFocus(); sources.onSettingsEntryClick(entry) },
                )
            }
        }
    }
}

/** The apps section: its header, then the matches as tiles (Grid) or rows (List). */
private fun LazyListScope.appsSection(
    apps: ImmutableList<AppInfo>,
    appearance: DrawerAppearance,
    actions: DrawerItemActions,
    appMenuNotifications: AppMenuNotifications,
    folderCandidates: ImmutableList<Folder>?,
) {
    item { SearchSectionHeader(R.string.drawer_search_section_apps) }
    if (appearance.presentation == DrawerPresentation.GRID) {
        item { SearchAppsGrid(apps, appearance, actions, appMenuNotifications, folderCandidates) }
    } else {
        items(apps, key = { "app_" + it.packageName + it.activityName + it.userHandle.hashCode() }) { app ->
            DrawerAppRow(
                app = app,
                onClick = { actions.onAppClick(app) },
                appearance = appearance,
                badgeCount = appearance.badgeCounts[app.packageName],
                appMenuNotifications = appMenuNotifications,
                actions = actions,
                folderCandidates = folderCandidates,
            )
        }
    }
}

/** A small label above one section of the search results. */
@Composable
private fun SearchSectionHeader(@StringRes textRes: Int) {
    Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.labelSmall,
        color = DrawerHeaderTextColor,
        modifier = Modifier.padding(top = 14.dp, bottom = 4.dp),
    )
}

/** The contacts section: its header, the permission or setting prompt when one applies, then the matching contacts. */
private fun LazyListScope.contactsSection(
    sources: DrawerSearchSources,
    itemSize: DrawerListItemSize,
    onContactClick: (ContactInfo) -> Unit,
) {
    item { SearchSectionHeader(R.string.drawer_search_section_contacts) }
    if (sources.showContactsPermissionPrompt) {
        item { ContactsAccessStrip(onClick = sources.onContactsPermissionPromptClick, modifier = Modifier.padding(bottom = 4.dp)) }
    } else if (sources.showContactsSettingPrompt) {
        item { ContactsSettingOffStrip(onClick = sources.onContactsSettingPromptClick, modifier = Modifier.padding(bottom = 4.dp)) }
    }
    items(sources.contacts, key = { "contact_" + it.id }) { contact ->
        ContactRow(contact = contact, itemSize = itemSize, onClick = { onContactClick(contact) })
    }
}

/**
 * Matching apps as rows of equal-weight tiles, in the same column count as the browse grid. A LazyVerticalGrid can't nest
 * inside the results' LazyColumn, and the tile count here is tiny, so chunked rows are simpler than a second scroll container.
 * No fixed row height: the browse grid's "fill N screen rows" doesn't apply here.
 */
@Composable
private fun SearchAppsGrid(
    apps: ImmutableList<AppInfo>,
    appearance: DrawerAppearance,
    actions: DrawerItemActions,
    appMenuNotifications: AppMenuNotifications,
    folderCandidates: ImmutableList<Folder>?,
) {
    BoxWithConstraints {
        val columns = drawerGridColumns(maxWidth, appearance.gridDensity)
        Column {
            apps.chunked(columns).forEach { rowApps ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 20.dp)) {
                    rowApps.forEach { app ->
                        Box(modifier = Modifier.weight(1f)) {
                            DrawerGridTile(
                                app = app,
                                onClick = { actions.onAppClick(app) },
                                appearance = appearance,
                                badgeCount = appearance.badgeCounts[app.packageName],
                                appMenuNotifications = appMenuNotifications,
                                actions = actions,
                                folderCandidates = folderCandidates,
                            )
                        }
                    }
                    repeat(columns - rowApps.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

/**
 * Same dashed-strip styling as Home's usage-access prompt (`HomeScreen.kt`'s `UsageAccessStrip`) —
 * duplicated locally rather than shared, since the two screens' surrounding chrome/color tokens
 * differ (`Ink`/`Muted`/`Accent` here are the Drawer's own theme values, not Home's). Shown in
 * place of contacts search results while `READ_CONTACTS` isn't granted (see
 * `DrawerViewModel.showContactsPermissionPrompt`'s doc for the exact condition).
 */
@Composable
private fun ContactsAccessStrip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("drawer_contacts_access_strip")
            .drawerDashedBorder(color = Ink.copy(alpha = 0.16f), cornerRadius = 10.dp)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.drawer_contacts_access_needed),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = Modifier.weight(1f),
        )
        Text(text = stringResource(R.string.open_settings), style = MaterialTheme.typography.bodyMedium, color = Accent)
    }
}

/**
 * Same dashed-strip styling as [ContactsAccessStrip], for the opposite gap: `READ_CONTACTS` is
 * already granted (e.g. via Settings → Permissions) but the "Search contacts" toggle itself is
 * off. Tapping turns the setting on directly (see `DrawerViewModel.enableContactSearch`) — no
 * system Settings round-trip needed since the permission is already in hand.
 */
@Composable
private fun ContactsSettingOffStrip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("drawer_contacts_setting_off_strip")
            .drawerDashedBorder(color = Ink.copy(alpha = 0.16f), cornerRadius = 10.dp)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.drawer_contacts_search_off),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = Modifier.weight(1f),
        )
        Text(text = stringResource(R.string.permission_turn_on), style = MaterialTheme.typography.bodyMedium, color = Accent)
    }
}

private fun Modifier.drawerDashedBorder(color: Color, cornerRadius: androidx.compose.ui.unit.Dp, strokeWidth: androidx.compose.ui.unit.Dp = 1.dp): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        style = Stroke(width = strokeWidth.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx()),
    )
}

/**
 * One contact match — an avatar/initials + name, tapping opens [ContactConnectionsSheet] (Phase 9)
 * rather than expanding an inline chip row in place. [itemSize] scales the avatar/row padding the
 * same way [DrawerAppRow]'s icon does, so contacts and apps read as one consistent list rather
 * than contacts staying pinned at Compact regardless of the chosen size (see chat history).
 */
@Composable
private fun ContactRow(contact: ContactInfo, onClick: () -> Unit, modifier: Modifier = Modifier, itemSize: DrawerListItemSize = DrawerListItemSize.COMPACT) {
    val avatarSize = itemSize.iconSizeDp.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("contact_row_${contact.id}")
            .padding(vertical = 8.dp + itemSize.extraRowPaddingDp.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier.size(avatarSize).clip(CircleShape).background(IconTile),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = contact.displayName.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = Surface,
            )
        }
        Text(text = contact.displayName, style = MaterialTheme.typography.bodyLarge, color = DrawerAppTextColor)
    }
}

/**
 * One system Settings deep-link match — a plain glyph (not an [AppIcon]/avatar tile, so no
 * Material shape decision applies here, see CLAUDE.md's shape section) plus the screen's label;
 * tapping fires the caller's [android.provider.Settings.ACTION_*] intent. [itemSize] scales the
 * glyph/row padding the same way [ContactRow] does, so this section reads as part of the same list.
 */
@Composable
private fun SettingsResultRow(entry: SettingsSearchEntry, onClick: () -> Unit, modifier: Modifier = Modifier, itemSize: DrawerListItemSize = DrawerListItemSize.COMPACT) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("settings_result_row_${entry.id}")
            .padding(vertical = 8.dp + itemSize.extraRowPaddingDp.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            tint = Muted,
            modifier = Modifier.size(itemSize.iconSizeDp.dp),
        )
        Text(text = entry.label, style = MaterialTheme.typography.bodyLarge, color = DrawerAppTextColor)
    }
}

/** `4q` — shown when a search matches nothing at all (apps and contacts both empty). No web fallback. */
@Composable
private fun DrawerSearchEmptyState(query: String, onClearSearch: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(imageVector = Icons.Default.SearchOff, contentDescription = null, tint = Muted, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = stringResource(R.string.drawer_search_no_matches, query), style = MaterialTheme.typography.bodyMedium, color = Muted, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .border(1.dp, Ink.copy(alpha = 0.14f), CircleShape)
                .clickable(onClick = onClearSearch)
                .testTag("drawer_search_clear")
                // defaultMinSize before padding so the 48dp Android touch-target minimum wins
                // even though bodyMedium + this row's own padding alone falls short (~38dp).
                .defaultMinSize(minHeight = 48.dp)
                .padding(horizontal = 16.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = stringResource(R.string.drawer_search_clear), style = MaterialTheme.typography.bodyMedium, color = Ink)
        }
    }
}

/**
 * Touch zone for letter-jump, 90% of the drawer height (right edge always; left edge when F8
 * is on). A touch anywhere in the zone maps onto [letters] (or, when [folderPosition] isn't
 * [RailFolderPosition.NONE], the folder glyph at that end) via [railSelectionAt], clamped to the
 * visible rail's own (content-sized, much shorter) band — computed from [railHeightPx], which
 * the right-edge zone measures from its actual rendered [AlphabetRail] (folder glyph included)
 * and reports via [onRailHeightMeasure] so the left-edge zone (gesture-only, no visible rail of
 * its own) can use the same band. Only the right-edge zone renders the visible [AlphabetRail]
 * ([showRail]).
 */
@Composable
private fun LetterJumpZone(
    letters: ImmutableList<String>,
    activeSelection: RailSelection?,
    onSelectionChange: (RailSelection?) -> Unit,
    showRail: Boolean,
    railHeightPx: Float,
    onRailHeightMeasure: (Float) -> Unit,
    modifier: Modifier = Modifier,
    folderPosition: RailFolderPosition = RailFolderPosition.NONE,
    recentlyInstalledPosition: RailFolderPosition = RailFolderPosition.NONE,
    requireDrag: Boolean = false,
) {
    var zoneHeightPx by remember { mutableFloatStateOf(0f) }

    fun selectionFor(y: Float): RailSelection? {
        val bandTop = (zoneHeightPx - railHeightPx) / 2f
        return railSelectionAt(
            y = y,
            bandTopPx = bandTop,
            bandBottomPx = bandTop + railHeightPx,
            letters = letters,
            folderPosition = folderPosition,
            recentlyInstalledPosition = recentlyInstalledPosition,
        )
    }

    Box(
        modifier = modifier
            .fillMaxHeight(TOUCH_ZONE_HEIGHT_FRACTION)
            .width(EDGE_ZONE_WIDTH_DP.dp)
            .onSizeChanged { zoneHeightPx = it.height.toFloat() }
            // The right rail (requireDrag=false) jumps straight to a letter on the initial down
            // (awaitFirstDown, no slop wait) — this makes it feel responsive. The left edge
            // (requireDrag=true) requires a drag past the system's touch-slop threshold before
            // activating, preventing accidental jumps from simple touches.
            .pointerInput(letters, folderPosition, recentlyInstalledPosition, requireDrag) {
                if (requireDrag) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            onSelectionChange(selectionFor(offset.y))
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            onSelectionChange(selectionFor(change.position.y))
                        },
                        onDragEnd = { onSelectionChange(null) },
                        onDragCancel = { onSelectionChange(null) }
                    )
                } else {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        onSelectionChange(selectionFor(down.position.y))
                        var tracking = true
                        while (tracking) {
                            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id }
                            if (change != null && change.pressed) {
                                change.consume()
                                onSelectionChange(selectionFor(change.position.y))
                            } else {
                                tracking = false
                            }
                        }
                        onSelectionChange(null)
                    }
                }
            },
    ) {
        if (showRail) {
            AlphabetRail(
                letters = letters,
                activeLetter = (activeSelection as? RailSelection.Letter)?.letter,
                folderPosition = folderPosition,
                folderActive = activeSelection is RailSelection.Folders,
                recentlyInstalledPosition = recentlyInstalledPosition,
                recentlyInstalledActive = activeSelection is RailSelection.RecentlyInstalled,
                modifier = Modifier
                    .align(Alignment.Center)
                    .onSizeChanged { onRailHeightMeasure(it.height.toFloat()) },
            )
        }
    }
}

@Composable
private fun DrawerListContent(
    groupedItems: GroupedItems<DrawerItem>,
    pinnedFolders: ImmutableList<Folder>,
    recentlyInstalledApps: ImmutableList<AppInfo>,
    listState: LazyListState,
    appearance: DrawerAppearance,
    actions: DrawerItemActions,
    appMenuNotifications: AppMenuNotifications,
    folderCandidates: ImmutableList<Folder>?,
    modifier: Modifier = Modifier,
) {
    val folderDisplayMode = appearance.folderDisplayMode
    val recentlyInstalledPosition = appearance.recentlyInstalledPosition
    val pinnedFolderContent: LazyListScope.() -> Unit = {
        drawerFolderSection(pinnedFolders, appearance, actions, appMenuNotifications)
    }
    val recentlyInstalledContent: LazyListScope.() -> Unit = {
        recentlyInstalledSection(recentlyInstalledApps, appearance, actions, appMenuNotifications)
    }
    LazyColumn(
        state = listState,
        // Left inset (48dp) lines rows up with where the search bar's own content starts. No
        // right inset — this content already sits in a weight(1f) slot next to LetterJumpZone
        // (see chat history), so its own right edge already stops exactly at the rail's touch
        // zone; an extra end padding here just left an empty gap between the two.
        modifier = modifier.padding(start = 48.dp, top = 4.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        if (recentlyInstalledPosition == RecentlyInstalledPosition.SHOW_FIRST) recentlyInstalledContent()
        if (folderDisplayMode == DrawerFolderDisplayMode.SHOW_FIRST) pinnedFolderContent()
        letterGroups(groupedItems, appearance, actions, appMenuNotifications, folderCandidates)
        if (folderDisplayMode == DrawerFolderDisplayMode.SHOW_LAST) pinnedFolderContent()
        if (recentlyInstalledPosition == RecentlyInstalledPosition.SHOW_LAST) recentlyInstalledContent()
    }
}

/** The "Recently installed" category, when there are apps for it: the icon header, then its one row. */
private fun LazyListScope.recentlyInstalledSection(
    apps: ImmutableList<AppInfo>,
    appearance: DrawerAppearance,
    actions: DrawerItemActions,
    appMenuNotifications: AppMenuNotifications,
) {
    if (apps.isEmpty()) return
    item(key = "header_recently_installed") { DrawerRecentlyInstalledHeader() }
    item(key = "recently_installed") {
        DrawerRecentlyInstalledRow(apps, appearance, actions, appMenuNotifications)
    }
}

/** The alphabetical content: a letter header, then its apps (and, in `INLINE` folder mode, folders), for every letter. */
private fun LazyListScope.letterGroups(
    groupedItems: GroupedItems<DrawerItem>,
    appearance: DrawerAppearance,
    actions: DrawerItemActions,
    appMenuNotifications: AppMenuNotifications,
    folderCandidates: ImmutableList<Folder>?,
) {
    for ((letter, itemsInGroup) in groupedItems.groups) {
        item(key = "header_$letter") {
            Text(
                text = letter,
                style = MaterialTheme.typography.labelSmall,
                color = DrawerHeaderTextColor,
                modifier = Modifier
                    .padding(top = 14.dp, bottom = 4.dp)
                    .testTag("header_$letter"),
            )
        }
        items(itemsInGroup, key = { it.itemKey() }) { item ->
            when (item) {
                is DrawerItem.AppEntry -> DrawerAppRow(
                    app = item.app,
                    onClick = { actions.onAppClick(item.app) },
                    appearance = appearance,
                    badgeCount = appearance.badgeCounts[item.app.packageName],
                    appMenuNotifications = appMenuNotifications,
                    actions = actions,
                    folderCandidates = folderCandidates,
                )
                is DrawerItem.FolderEntry -> DrawerFolderRow(item.folder, appearance, actions, appMenuNotifications)
            }
        }
    }
}

/** Stable per-item [LazyColumn]/[LazyVerticalGrid] key, shared by browse-mode's letter-grouped and pinned-folder sections. */
private fun DrawerItem.itemKey(): String = when (this) {
    is DrawerItem.AppEntry -> app.packageName + app.activityName + app.userHandle.hashCode()
    is DrawerItem.FolderEntry -> "folder_${folder.id}"
}

/** [DrawerFolderDisplayMode.SHOW_FIRST]/`SHOW_LAST`'s pinned section — its own "Folders" header (List only; Grid renders no headers at all, same as the lettered content) followed by every folder, outside alphabetical order. A no-op when [folders] is empty, so callers don't need to gate on [DrawerFolderDisplayMode] themselves beyond picking where to call it. */
private fun LazyListScope.drawerFolderSection(
    folders: List<Folder>,
    appearance: DrawerAppearance,
    actions: DrawerItemActions,
    appMenuNotifications: AppMenuNotifications,
) {
    if (folders.isEmpty()) return
    item(key = "header_folders") {
        Text(
            text = stringResource(R.string.app_picker_tab_folders),
            style = MaterialTheme.typography.labelSmall,
            color = DrawerHeaderTextColor,
            modifier = Modifier
                .padding(top = 14.dp, bottom = 4.dp)
                .testTag("header_folders"),
        )
    }
    items(folders, key = { "folder_${it.id}" }) { folder ->
        DrawerFolderRow(folder, appearance, actions, appMenuNotifications)
    }
}

/** The "Recently installed" category's own section header — icon-only (no label, unlike [drawerFolderSection]'s text "Folders" header), same [MaterialTheme.typography.labelSmall]/[DrawerHeaderTextColor]/padding treatment as a letter header. */
@Composable
private fun DrawerRecentlyInstalledHeader(modifier: Modifier = Modifier) {
    val glyphSize = with(LocalDensity.current) { MaterialTheme.typography.labelSmall.fontSize.toDp() }
    Icon(
        imageVector = RecencyIcon,
        contentDescription = null,
        tint = DrawerHeaderTextColor,
        modifier = modifier
            .padding(top = 14.dp, bottom = 4.dp)
            .size(glyphSize)
            .testTag("header_recently_installed"),
    )
}

/**
 * The "Recently installed" category — always the very first row, List-only, above even a
 * `SHOW_FIRST` folder section. A virtual item, not a real [Folder]: no long-press menu, just
 * tap-to-open. Reuses [FolderContentsSheet] via a synthetic `Folder(id = -1L, ...)` with
 * [FolderSheetHeaderAction.None] (no "Rename") and `showRecencyBadge = true`.
 */
@Composable
private fun DrawerRecentlyInstalledRow(
    apps: ImmutableList<AppInfo>,
    appearance: DrawerAppearance,
    actions: DrawerItemActions,
    appMenuNotifications: AppMenuNotifications,
    modifier: Modifier = Modifier,
) {
    val itemSize = appearance.listItemSize
    var sheetOpen by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = { sheetOpen = true })
                .testTag("drawer_recently_installed_row")
                .padding(horizontal = 8.dp, vertical = 8.dp + itemSize.extraRowPaddingDp.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            RecentlyInstalledGlyph(modifier = Modifier.size(itemSize.iconSizeDp.dp))
            Text(
                text = stringResource(R.string.drawer_recently_installed_label),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = appearance.labelFontWeight),
                color = DrawerAppTextColor,
            )
        }
        if (sheetOpen) {
            FolderContentsSheet(
                folder = Folder(id = -1L, name = stringResource(R.string.drawer_recently_installed_label), apps = apps),
                onDismissRequest = { sheetOpen = false },
                onAppClick = actions.onAppClick,
                presentation = DrawerPresentation.LIST,
                onRemoveFromFolder = { _, _ -> },
                headerAction = FolderSheetHeaderAction.None,
                showRecencyBadge = true,
                appMenuNotifications = appMenuNotifications,
                onAppInfo = actions.onAppInfo,
            )
        }
    }
}

/** [DrawerRecentlyInstalledRow]'s leading glyph — the same [FolderTileGlyph] container treatment
 * (12dp-rounded tile with an inset vignette) so this row reads as a category alongside real
 * folder rows, with [RecencyIcon] standing in for a folder's own icon. [Ink]/[InkInverted] are
 * exact opposites of each other in both themes, so this stays high-contrast either way. */
@Composable
private fun RecentlyInstalledGlyph(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Ink)
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.18f)),
                        center = center,
                        radius = size.maxDimension * 0.75f,
                    ),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = RecencyIcon,
            contentDescription = null,
            tint = InkInverted,
            modifier = Modifier.size(AppIconSize.SHORTCUT),
        )
    }
}

/** The Drawer's own folder row — [DrawerAppRow]'s layout/styling (icon size from [itemSize], [DrawerAppTextColor]/`bodyLarge` label) with [FolderTileGlyph] standing in for [AppIcon] and [FolderContentsSheet]/[FolderTileContextMenu] standing in for [AppContextMenu], mirroring [com.facetlauncher.app.ui.home.FolderRow]'s identical relationship to [com.facetlauncher.app.ui.home.AppRow] on Home. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerFolderRow(
    folder: Folder,
    appearance: DrawerAppearance,
    actions: DrawerItemActions,
    appMenuNotifications: AppMenuNotifications,
    modifier: Modifier = Modifier,
) {
    val itemSize = appearance.listItemSize
    val labelFontWeight = appearance.labelFontWeight
    var sheetOpen by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .longPressReleaseClickable(onClick = { sheetOpen = true }, onLongPress = { menuExpanded = true })
                .testTag("drawer_folder_row_${folder.id}")
                .padding(horizontal = 8.dp, vertical = 8.dp + itemSize.extraRowPaddingDp.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FolderTileGlyph(folder = folder, modifier = Modifier.size(itemSize.iconSizeDp.dp))
            Text(text = folder.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = labelFontWeight), color = DrawerAppTextColor)
        }
        if (sheetOpen) {
            FolderContentsSheet(
                folder = folder,
                onDismissRequest = { sheetOpen = false },
                onAppClick = actions.onAppClick,
                presentation = DrawerPresentation.LIST,
                onRemoveFromFolder = actions.onRemoveFromFolder,
                headerAction = FolderSheetHeaderAction.Rename(onRename = actions.onRenameFolder),
                appMenuNotifications = appMenuNotifications,
                onAppInfo = actions.onAppInfo,
            )
        }
        FolderTileContextMenu(
            folder = folder,
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            onRename = actions.onRenameFolder,
            onRequestQuickAddState = actions.onRequestFolderQuickAddState,
            onFavoritesAction = actions.onFolderFavoritesAction,
            onDockAction = actions.onFolderDockAction,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerAppRow(
    app: AppInfo,
    onClick: () -> Unit,
    appearance: DrawerAppearance,
    badgeCount: Int?,
    appMenuNotifications: AppMenuNotifications,
    actions: DrawerItemActions,
    folderCandidates: ImmutableList<Folder>?,
    modifier: Modifier = Modifier,
) {
    val itemSize = appearance.listItemSize
    val labelFontWeight = appearance.labelFontWeight
    var menuExpanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Large/16dp (MaterialTheme.shapes.large — see CLAUDE.md's shape table, which
                // names this the "navigation drawers" token) clips the ripple/press state to a
                // rounded rect instead of a full-bleed rectangle (see chat history).
                .clip(MaterialTheme.shapes.large)
                .longPressReleaseClickable(onClick = onClick, onLongPress = { menuExpanded = true })
                .testTag("drawer_app_row_${app.packageName}")
                // Base 6dp vertical padding plus the selected size tier's extra (0/8/16dp) — see
                // DrawerListItemSize's own doc (see chat history).
                .padding(horizontal = 8.dp, vertical = 8.dp + itemSize.extraRowPaddingDp.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (appearance.showIcons) {
                // Icon grows alongside the row height for Regular/Spacious (see
                // DrawerListItemSize's own doc) — corner radius follows from size via AppIcon's
                // own M3 shape-scale default, not a hand-picked ratio (see chat history).
                AppIcon(
                    icon = app.icon,
                    monochromeIcon = app.monochromeIcon,
                    size = itemSize.iconSizeDp.dp,
                    contentDescription = null,
                    isWorkApp = app.profile == AppProfile.WORK,
                    isOtherProfileApp = app.profile == AppProfile.OTHER,
                )
            }
            Text(text = app.label, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = labelFontWeight), color = DrawerAppTextColor)
            if (badgeCount != null && badgeCount > 0) NotificationBadge(count = badgeCount, style = appearance.notificationBadgeStyle)
        }
        AppContextMenu(
            app = app,
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            appMenuNotifications = appMenuNotifications,
            onOpenApp = actions.onAppClick,
            onAppInfo = actions.onAppInfo,
            onRequestQuickAddState = actions.onRequestQuickAddState,
            onFavoritesAction = actions.onFavoritesAction,
            onDockAction = actions.onDockAction,
            folderCandidates = folderCandidates,
            onCreateFolder = actions.onCreateFolder,
            onAddToFolder = actions.onAddToFolder,
            drawerPresentation = DrawerPresentation.LIST,
        )
    }
}

private val GRID_CONTENT_TOP_PADDING = 4.dp
private val GRID_CONTENT_BOTTOM_PADDING = 16.dp
private val GRID_VERTICAL_SPACING = 20.dp
private val GRID_START_PADDING = 48.dp

/**
 * Grid layout (`1i`): [density]-driven columns, icons `44×44`/`13dp` corners, centered label
 * beneath. Unlike List, Grid renders no letter headers — a continuous grid of all apps in
 * alphabetical order, not visually grouped. The alphabet rail still works: dragging to a letter
 * scrolls to that letter's *first item* ([GroupedItems.firstAppIndexForLetter]) rather than a
 * header row, since there's no header to scroll to. [pinnedFolders] follows the same no-header
 * rule when [folderDisplayMode] pins it to either end — just N folder tiles, no "Folders" label
 * (List's equivalent section gets one only because List already headers every letter group too).
 *
 * The column and row counts come from [density] and the viewport size ([drawerGridColumns],
 * [drawerGridRows]), so the same setting suits a phone, a foldable and a tablet. The rows divide
 * the *viewport* height into equal-height rows — each tile is given exactly that height, so a
 * whole number of rows always fits the visible area regardless of how many apps there are. The
 * grid still scrolls past that computed row height when there are more apps than fit.
 */
@Composable
private fun DrawerGridContent(
    groupedItems: GroupedItems<DrawerItem>,
    pinnedFolders: ImmutableList<Folder>,
    gridState: LazyGridState,
    appearance: DrawerAppearance,
    actions: DrawerItemActions,
    appMenuNotifications: AppMenuNotifications,
    folderCandidates: ImmutableList<Folder>?,
    modifier: Modifier = Modifier,
) {
    val density = appearance.gridDensity
    val folderDisplayMode = appearance.folderDisplayMode
    val items = remember(groupedItems) { groupedItems.groups.values.flatten() }
    BoxWithConstraints(modifier = modifier) {
        val availableHeight = maxHeight - GRID_CONTENT_TOP_PADDING - GRID_CONTENT_BOTTOM_PADDING
        val columns = drawerGridColumns(maxWidth - GRID_START_PADDING, density)
        val rows = drawerGridRows(availableHeight, density)
        val rowHeight = ((availableHeight - GRID_VERTICAL_SPACING * (rows - 1)) / rows).coerceAtLeast(0.dp)
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            // No right inset — same reasoning as DrawerListContent's identical fix (see chat
            // history): this content is already in a weight(1f) slot next to LetterJumpZone, so
            // its right edge already stops exactly at the rail's touch zone.
            contentPadding = PaddingValues(start = GRID_START_PADDING, top = GRID_CONTENT_TOP_PADDING, bottom = GRID_CONTENT_BOTTOM_PADDING),
            verticalArrangement = Arrangement.spacedBy(GRID_VERTICAL_SPACING),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (folderDisplayMode == DrawerFolderDisplayMode.SHOW_FIRST) {
                drawerFolderTiles(pinnedFolders, rowHeight, appearance, actions, appMenuNotifications)
            }
            items(items, key = { it.itemKey() }) { item ->
                when (item) {
                    is DrawerItem.AppEntry -> DrawerGridTile(
                        app = item.app,
                        onClick = { actions.onAppClick(item.app) },
                        appearance = appearance,
                        badgeCount = appearance.badgeCounts[item.app.packageName],
                        appMenuNotifications = appMenuNotifications,
                        actions = actions,
                        folderCandidates = folderCandidates,
                        modifier = Modifier.height(rowHeight),
                    )
                    is DrawerItem.FolderEntry -> DrawerFolderTile(
                        folder = item.folder,
                        appearance = appearance,
                        actions = actions,
                        appMenuNotifications = appMenuNotifications,
                        modifier = Modifier.height(rowHeight),
                    )
                }
            }
            if (folderDisplayMode == DrawerFolderDisplayMode.SHOW_LAST) {
                drawerFolderTiles(pinnedFolders, rowHeight, appearance, actions, appMenuNotifications)
            }
        }
    }
}

/** [DrawerGridContent]'s pinned-folder tiles — no header (see [DrawerGridContent]'s own doc), just the tiles themselves at [rowHeight]. */
private fun LazyGridScope.drawerFolderTiles(
    folders: List<Folder>,
    rowHeight: Dp,
    appearance: DrawerAppearance,
    actions: DrawerItemActions,
    appMenuNotifications: AppMenuNotifications,
) {
    items(folders, key = { "folder_${it.id}" }) { folder ->
        DrawerFolderTile(folder, appearance, actions, appMenuNotifications, Modifier.height(rowHeight))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerGridTile(
    app: AppInfo,
    onClick: () -> Unit,
    appearance: DrawerAppearance,
    badgeCount: Int?,
    appMenuNotifications: AppMenuNotifications,
    actions: DrawerItemActions,
    folderCandidates: ImmutableList<Folder>?,
    modifier: Modifier = Modifier,
) {
    val labelFontWeight = appearance.labelFontWeight
    var menuExpanded by remember { mutableStateOf(false) }
    // `modifier` carries DrawerGridContent's explicit per-row height; fillMaxWidth centres the icon in its cell.
    // The context menu is a sheet in its own window, so it takes no space inside this Column.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .longPressReleaseClickable(onClick = onClick, onLongPress = { menuExpanded = true })
            .testTag("drawer_grid_tile_${app.packageName}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        // The row height can exceed the icon+label's own height — center them within it.
        verticalArrangement = Arrangement.Center,
    ) {
        AppIcon(
            icon = app.icon,
            monochromeIcon = app.monochromeIcon,
            size = AppIconSize.TILE,
            contentDescription = null,
            notificationCount = badgeCount,
            badgeStyle = appearance.notificationBadgeStyle,
            isWorkApp = app.profile == AppProfile.WORK,
            isOtherProfileApp = app.profile == AppProfile.OTHER,
        )
        if (appearance.showLabels) {
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = app.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = labelFontWeight),
                color = DrawerAppTextColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 56.dp),
            )
        }
        AppContextMenu(
            app = app,
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            appMenuNotifications = appMenuNotifications,
            onOpenApp = actions.onAppClick,
            onAppInfo = actions.onAppInfo,
            onRequestQuickAddState = actions.onRequestQuickAddState,
            onFavoritesAction = actions.onFavoritesAction,
            onDockAction = actions.onDockAction,
            folderCandidates = folderCandidates,
            onCreateFolder = actions.onCreateFolder,
            onAddToFolder = actions.onAddToFolder,
            drawerPresentation = DrawerPresentation.GRID,
        )
    }
}

/** [DrawerGridTile]'s folder counterpart — [FolderTileGlyph] instead of [AppIcon] (no badge slot, folders don't carry notification counts), [FolderContentsSheet]/[FolderTileContextMenu] instead of [AppContextMenu]. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerFolderTile(
    folder: Folder,
    appearance: DrawerAppearance,
    actions: DrawerItemActions,
    appMenuNotifications: AppMenuNotifications,
    modifier: Modifier = Modifier,
) {
    val labelFontWeight = appearance.labelFontWeight
    var sheetOpen by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    // See DrawerGridTile: the sheets are separate windows, so they sit inside the Column.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .longPressReleaseClickable(onClick = { sheetOpen = true }, onLongPress = { menuExpanded = true })
            .testTag("drawer_grid_folder_tile_${folder.id}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        FolderTileGlyph(folder = folder, modifier = Modifier.size(AppIconSize.TILE))
        if (appearance.showLabels) {
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = folder.name,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = labelFontWeight),
                color = DrawerAppTextColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 56.dp),
            )
        }
        if (sheetOpen) {
            FolderContentsSheet(
                folder = folder,
                onDismissRequest = { sheetOpen = false },
                onAppClick = actions.onAppClick,
                presentation = DrawerPresentation.GRID,
                onRemoveFromFolder = actions.onRemoveFromFolder,
                headerAction = FolderSheetHeaderAction.Rename(onRename = actions.onRenameFolder),
                appMenuNotifications = appMenuNotifications,
                onAppInfo = actions.onAppInfo,
            )
        }
        FolderTileContextMenu(
            folder = folder,
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            onRename = actions.onRenameFolder,
            onRequestQuickAddState = actions.onRequestFolderQuickAddState,
            onFavoritesAction = actions.onFolderFavoritesAction,
            onDockAction = actions.onFolderDockAction,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppDrawerScreenPreview() {
    FacetLauncherTheme {
        AppDrawerScreen(
            apps = ('A'..'Z').flatMap { letter ->
                listOf(
                    AppInfo(
                        packageName = "com.example.$letter",
                        activityName = ".Main",
                        label = "$letter App",
                        icon = null,
                    ),
                )
            }.toImmutableList(),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppDrawerScreenGridPreview() {
    FacetLauncherTheme {
        AppDrawerScreen(
            apps = ('A'..'Z').flatMap { letter ->
                listOf(
                    AppInfo(
                        packageName = "com.example.$letter",
                        activityName = ".Main",
                        label = "$letter App",
                        icon = null,
                    ),
                )
            }.toImmutableList(),
            appearance = DrawerAppearance(presentation = DrawerPresentation.GRID),
        )
    }
}
