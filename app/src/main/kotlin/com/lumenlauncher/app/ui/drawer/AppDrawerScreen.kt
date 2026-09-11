package com.lumenlauncher.app.ui.drawer

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppShortcut
import com.lumenlauncher.app.data.model.ContactConnection
import com.lumenlauncher.app.data.model.ContactInfo
import com.lumenlauncher.app.data.model.DrawerGridSize
import com.lumenlauncher.app.data.model.DrawerListItemSize
import com.lumenlauncher.app.data.model.DrawerPresentation
import com.lumenlauncher.app.data.model.NotificationBadgeStyle
import com.lumenlauncher.app.data.model.SearchBarPosition
import com.lumenlauncher.app.domain.GroupAppsByLetterUseCase
import com.lumenlauncher.app.domain.GroupedApps
import com.lumenlauncher.app.domain.RankBySearchRelevanceUseCase
import com.lumenlauncher.app.ui.components.AppContextMenu
import com.lumenlauncher.app.ui.components.AppIcon
import com.lumenlauncher.app.ui.components.AppIconSize
import com.lumenlauncher.app.ui.components.NotificationBadge
import com.lumenlauncher.app.ui.components.ThemedDropdownMenu
import com.lumenlauncher.app.ui.components.ThemedDropdownMenuItem
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.DrawerAppTextColor
import com.lumenlauncher.app.ui.theme.DrawerHeaderTextColor
import com.lumenlauncher.app.ui.theme.DrawerOverlay
import com.lumenlauncher.app.ui.theme.IconTile
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LUMEN_TRANSITION_DURATION_MS
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.LumenTransitionEasing
import com.lumenlauncher.app.ui.theme.LumenType
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Scrim
import com.lumenlauncher.app.ui.theme.Surface
import com.lumenlauncher.app.ui.theme.SurfaceContainer
import kotlinx.coroutines.launch

private const val MAX_APP_SEARCH_RESULTS = 5

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
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    gridState: LazyGridState = rememberLazyGridState(),
    presentation: DrawerPresentation = DrawerPresentation.LIST,
    gridSize: DrawerGridSize = DrawerGridSize.FIVE_BY_SIX,
    listItemSize: DrawerListItemSize = DrawerListItemSize.REGULAR,
    showIcons: Boolean = true,
    showLabels: Boolean = true,
    labelFontWeight: FontWeight = FontWeight.Normal,
    opacity: Float = 0.6f,
    notificationBadgeStyle: NotificationBadgeStyle = NotificationBadgeStyle.DOT,
    badgeCounts: Map<String, Int> = emptyMap(),
    query: String = "",
    onQueryChanged: (String) -> Unit = {},
    searchBarPosition: SearchBarPosition = SearchBarPosition.TOP,
    onNavigateToSettings: () -> Unit = {},
    contacts: List<ContactInfo> = emptyList(),
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut> = { emptyList() },
    onLaunchShortcut: (AppShortcut) -> Unit = {},
    /** F12's long-press "Add to Favorites"/"Add to Dock" rows — see [com.lumenlauncher.app.domain.ObserveQuickAddStateUseCase]'s own doc for what `null` vs each [Boolean] means. */
    addToFavoritesOverride: Boolean? = null,
    onAddToFavorites: (AppInfo) -> Unit = {},
    addToDockOverride: Boolean? = null,
    onAddToDock: (AppInfo) -> Unit = {},
    onRequestConnections: suspend (ContactInfo) -> List<ContactConnection> = { emptyList() },
    /** True while the search wants to show contacts but `READ_CONTACTS` isn't granted — see `DrawerViewModel.showContactsPermissionPrompt`'s own doc for the exact condition. */
    showContactsPermissionPrompt: Boolean = false,
    onContactsPermissionPromptClick: () -> Unit = {},
    /** True while the drawer itself is open/opening (caller's own `isDrawerOpen`). Used only to reset the connections sheet once the drawer is fully closed — see the `LaunchedEffect` below. */
    isDrawerOpen: Boolean = true,
) {
    val rankBySearchRelevance = remember { RankBySearchRelevanceUseCase() }
    val filteredApps = remember(apps, query) { rankBySearchRelevance(apps, query) { it.label } }
    val groupUseCase = remember { GroupAppsByLetterUseCase() }
    val groupedApps = remember(filteredApps) { groupUseCase(filteredApps) }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var draggingLetter by remember { mutableStateOf<String?>(null) }
    var railHeightPx by remember { mutableFloatStateOf(0f) }
    val isSearching = query.isNotBlank()
    // Phase 9's connections sheet — hoisted here (not down in DrawerSearchResults/ContactRow) so
    // it can overlay the *whole* screen.
    var connectionsSheetContact by remember { mutableStateOf<ContactInfo?>(null) }
    var connections by remember { mutableStateOf<List<ContactConnection>>(emptyList()) }
    LaunchedEffect(connectionsSheetContact) {
        val contact = connectionsSheetContact
        connections = if (contact != null) onRequestConnections(contact) else emptyList()
    }
    // Otherwise this composable never leaves composition when the drawer closes (it's only
    // translated off-screen), so a contact left open here would still be showing the next time
    // the drawer reopens.
    LaunchedEffect(isDrawerOpen) {
        if (!isDrawerOpen) connectionsSheetContact = null
    }

    // First back-press while searching clears the query and stays in the drawer (browse mode,
    // unfiltered); only a second, empty-query back-press falls through to the caller's own
    // drawer-close BackHandler.
    BackHandler(enabled = isSearching) {
        focusManager.clearFocus()
        onQueryChanged("")
    }

    val scrollToIndex: suspend (Int) -> Unit = if (presentation == DrawerPresentation.GRID) {
        { index -> gridState.scrollToItem(index) }
    } else {
        { index -> listState.scrollToItem(index) }
    }

    fun onRailLetterChanged(letter: String?) {
        draggingLetter = letter
        // List scrolls to that letter's header row; Grid has no headers at all (a continuous
        // grid, not grouped visually) so it scrolls to that letter's first app instead.
        val indexMap = if (presentation == DrawerPresentation.GRID) groupedApps.firstAppIndexForLetter else groupedApps.headerIndexForLetter
        val index = letter?.let { indexMap[it] } ?: return
        coroutineScope.launch { scrollToIndex(index) }
    }

    // Outer Box so the connections sheet (below) can overlay the entire screen — the Column
    // itself keeps its previous fillMaxSize/background/inset treatment unchanged.
    Box(modifier = modifier.fillMaxSize()) {
    // Edge-to-edge is enforced unconditionally at this app's targetSdk (36) — without this, the
    // drawer's content (notably a TOP search bar) draws straight under the status bar/notch.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceContainer.copy(alpha = opacity))
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        if (searchBarPosition == SearchBarPosition.TOP) {
            DrawerSearchBar(query = query, onQueryChanged = onQueryChanged, onNavigateToSettings = onNavigateToSettings)
        }

        Box(modifier = Modifier.weight(1f)) {
        if (isSearching) {
            DrawerSearchResults(
                query = query,
                apps = filteredApps.take(MAX_APP_SEARCH_RESULTS),
                contacts = contacts,
                presentation = presentation,
                gridColumns = gridSize.columns,
                showIcons = showIcons,
                showLabels = showLabels,
                labelFontWeight = labelFontWeight,
                itemSize = listItemSize,
                badgeStyle = notificationBadgeStyle,
                badgeCounts = badgeCounts,
                onAppClick = onAppClick,
                onClearSearch = { onQueryChanged("") },
                onRequestShortcuts = onRequestShortcuts,
                onLaunchShortcut = onLaunchShortcut,
                addToFavoritesOverride = addToFavoritesOverride,
                onAddToFavorites = onAddToFavorites,
                addToDockOverride = addToDockOverride,
                onAddToDock = onAddToDock,
                // Closing the keyboard here (not just clearing text-field focus) matters
                // specifically because the user was very likely still typing their search query
                // when they tapped a contact result — leaving it open behind the sheet looked
                // like a stuck/forgotten keyboard (see chat history).
                onContactClick = { focusManager.clearFocus(); connectionsSheetContact = it },
                showContactsPermissionPrompt = showContactsPermissionPrompt,
                onContactsPermissionPromptClick = onContactsPermissionPromptClick,
                modifier = Modifier.fillMaxSize().testTag("drawer_search_results"),
            )
        } else {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (presentation == DrawerPresentation.GRID) {
                DrawerGridContent(
                    groupedApps = groupedApps,
                    gridState = gridState,
                    columns = gridSize.columns,
                    rows = gridSize.rows,
                    onAppClick = onAppClick,
                    showLabels = showLabels,
                    labelFontWeight = labelFontWeight,
                    badgeStyle = notificationBadgeStyle,
                    badgeCounts = badgeCounts,
                    onRequestShortcuts = onRequestShortcuts,
                    onLaunchShortcut = onLaunchShortcut,
                    addToFavoritesOverride = addToFavoritesOverride,
                    onAddToFavorites = onAddToFavorites,
                    addToDockOverride = addToDockOverride,
                    onAddToDock = onAddToDock,
                    modifier = Modifier.weight(1f).fillMaxHeight().testTag("drawer_grid"),
                )
            } else {
                DrawerListContent(
                    groupedApps = groupedApps,
                    listState = listState,
                    itemSize = listItemSize,
                    onAppClick = onAppClick,
                    showIcons = showIcons,
                    labelFontWeight = labelFontWeight,
                    badgeStyle = notificationBadgeStyle,
                    badgeCounts = badgeCounts,
                    onRequestShortcuts = onRequestShortcuts,
                    onLaunchShortcut = onLaunchShortcut,
                    addToFavoritesOverride = addToFavoritesOverride,
                    onAddToFavorites = onAddToFavorites,
                    addToDockOverride = addToDockOverride,
                    onAddToDock = onAddToDock,
                    modifier = Modifier.weight(1f).fillMaxHeight().testTag("drawer_list"),
                )
            }
            LetterJumpZone(
                letters = groupedApps.letters,
                activeLetter = draggingLetter,
                onLetterChanged = ::onRailLetterChanged,
                showRail = true,
                railHeightPx = railHeightPx,
                onRailHeightMeasured = { railHeightPx = it },
                modifier = Modifier.testTag("alphabet_rail"),
            )
        }

        LetterJumpZone(
            letters = groupedApps.letters,
            activeLetter = draggingLetter,
            onLetterChanged = ::onRailLetterChanged,
            showRail = false,
            railHeightPx = railHeightPx,
            onRailHeightMeasured = {},
            modifier = Modifier.align(Alignment.CenterStart).testTag("left_edge_letter_jump_zone"),
            requireDrag = true,
        )

        draggingLetter?.let { letter ->
            Text(
                text = letter,
                style = LumenType.clock.copy(fontSize = 40.sp, lineHeight = 40.sp),
                color = Accent,
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag("alphabet_rail_indicator")
                    .background(color = Surface.copy(alpha = 0.92f), shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 28.dp, vertical = 12.dp),
            )
        }
        }
        }

        if (searchBarPosition == SearchBarPosition.BOTTOM) {
            DrawerSearchBar(
                query = query,
                onQueryChanged = onQueryChanged,
                onNavigateToSettings = onNavigateToSettings,
                modifier = Modifier.imePadding(),
            )
        }
    }

    // Phase 9's connections sheet — scrim + slide-up-from-bottom overlay.
    AnimatedVisibility(
        visible = connectionsSheetContact != null,
        enter = fadeIn(animationSpec = tween(240)),
        exit = fadeOut(animationSpec = tween(240)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Scrim)
                .testTag("contact_connections_scrim")
                .clickable(onClick = { connectionsSheetContact = null }),
        )
    }
    AnimatedVisibility(
        visible = connectionsSheetContact != null,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(LUMEN_TRANSITION_DURATION_MS, easing = LumenTransitionEasing)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(LUMEN_TRANSITION_DURATION_MS, easing = LumenTransitionEasing)),
        modifier = Modifier.align(Alignment.BottomCenter),
    ) {
        connectionsSheetContact?.let { contact ->
            val context = LocalContext.current
            ContactConnectionsSheet(
                contactName = contact.displayName,
                connections = connections,
                onConnectionClick = { intent ->
                    runCatching { context.startActivity(intent) }
                    connectionsSheetContact = null
                },
                onViewContactClick = {
                    val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contact.id)
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                    connectionsSheetContact = null
                },
            )
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
    onQueryChanged: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
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
            onValueChange = onQueryChanged,
            placeholder = { Text("Search apps") },
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
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options", tint = Muted)
            }
            ThemedDropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                ThemedDropdownMenuItem(
                    label = "Launcher settings",
                    onClick = { menuExpanded = false; onNavigateToSettings() },
                    modifier = Modifier.testTag("drawer_search_overflow_settings"),
                )
            }
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
    apps: List<AppInfo>,
    contacts: List<ContactInfo>,
    presentation: DrawerPresentation,
    gridColumns: Int,
    showIcons: Boolean,
    showLabels: Boolean,
    labelFontWeight: FontWeight,
    itemSize: DrawerListItemSize,
    badgeStyle: NotificationBadgeStyle,
    badgeCounts: Map<String, Int>,
    onAppClick: (AppInfo) -> Unit,
    onClearSearch: () -> Unit,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    addToFavoritesOverride: Boolean?,
    onAddToFavorites: (AppInfo) -> Unit,
    addToDockOverride: Boolean?,
    onAddToDock: (AppInfo) -> Unit,
    onContactClick: (ContactInfo) -> Unit,
    showContactsPermissionPrompt: Boolean,
    onContactsPermissionPromptClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (apps.isEmpty() && contacts.isEmpty() && !showContactsPermissionPrompt) {
        DrawerSearchEmptyState(query = query, onClearSearch = onClearSearch, modifier = modifier)
        return
    }
    LazyColumn(modifier = modifier.padding(horizontal = 24.dp)) {
        if (apps.isNotEmpty()) {
            item {
                Text(
                    text = "APPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = DrawerHeaderTextColor,
                    modifier = Modifier.padding(top = 14.dp, bottom = 4.dp),
                )
            }
            if (presentation == DrawerPresentation.GRID) {
                item {
                    // A LazyVerticalGrid can't nest inside this LazyColumn — the tile count here
                    // is tiny (capped at 5), so a plain flow-row-style Column of chunked rows is
                    // simpler than wiring a second scroll container. Same 8dp horizontal / 20dp
                    // vertical spacing and DrawerGridTile itself as the real grid, just without
                    // its fixed per-row height (that's a "fill N screen rows" browse-mode
                    // computation that doesn't apply to a handful of search results).
                    apps.chunked(gridColumns).forEach { rowApps ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 20.dp)) {
                            rowApps.forEach { app ->
                                DrawerGridTile(
                                    app = app,
                                    onClick = { onAppClick(app) },
                                    showLabel = showLabels,
                                    labelFontWeight = labelFontWeight,
                                    badgeCount = badgeCounts[app.packageName],
                                    badgeStyle = badgeStyle,
                                    onRequestShortcuts = onRequestShortcuts,
                                    onLaunchShortcut = onLaunchShortcut,
                                    addToFavoritesOverride = addToFavoritesOverride,
                                    onAddToFavorites = onAddToFavorites,
                                    addToDockOverride = addToDockOverride,
                                    onAddToDock = onAddToDock,
                                )
                            }
                        }
                    }
                }
            } else {
                items(apps, key = { "app_" + it.packageName + it.activityName }) { app ->
                    DrawerAppRow(
                        app = app,
                        onClick = { onAppClick(app) },
                        showIcon = showIcons,
                        itemSize = itemSize,
                        labelFontWeight = labelFontWeight,
                        badgeCount = badgeCounts[app.packageName],
                        badgeStyle = badgeStyle,
                        onRequestShortcuts = onRequestShortcuts,
                        onLaunchShortcut = onLaunchShortcut,
                        addToFavoritesOverride = addToFavoritesOverride,
                        onAddToFavorites = onAddToFavorites,
                        addToDockOverride = addToDockOverride,
                        onAddToDock = onAddToDock,
                    )
                }
            }
        }
        if (contacts.isNotEmpty() || showContactsPermissionPrompt) {
            item {
                Text(
                    text = "CONTACTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = DrawerHeaderTextColor,
                    modifier = Modifier.padding(top = 14.dp, bottom = 4.dp),
                )
            }
            if (showContactsPermissionPrompt) {
                item {
                    ContactsAccessStrip(
                        onClick = onContactsPermissionPromptClick,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
            items(contacts, key = { "contact_" + it.id }) { contact ->
                ContactRow(contact = contact, itemSize = itemSize, onClick = { onContactClick(contact) })
            }
        }
        item { Spacer(modifier = Modifier.height(40.dp)) }
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
            text = "Contact search needs access from system settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = Modifier.weight(1f),
        )
        Text(text = "Open settings", style = MaterialTheme.typography.bodyMedium, color = Accent)
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

/** `4q` — shown when a search matches nothing at all (apps and contacts both empty). No web fallback. */
@Composable
private fun DrawerSearchEmptyState(query: String, onClearSearch: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(imageVector = Icons.Default.SearchOff, contentDescription = null, tint = Muted, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "No matches found for “$query”", style = MaterialTheme.typography.bodyMedium, color = Muted, textAlign = TextAlign.Center)
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
            Text(text = "Clear search", style = MaterialTheme.typography.bodyMedium, color = Ink)
        }
    }
}

/**
 * Touch zone for letter-jump, 90% of the drawer height (right edge always; left edge when F8
 * is on). A touch anywhere in the zone maps onto [letters] via [letterAt], clamped to the
 * visible rail's own (content-sized, much shorter) band — computed from [railHeightPx], which
 * the right-edge zone measures from its actual rendered [AlphabetRail] and reports via
 * [onRailHeightMeasured] so the left-edge zone (gesture-only, no visible rail of its own) can
 * use the same band. Only the right-edge zone renders the visible [AlphabetRail] ([showRail]).
 */
@Composable
private fun LetterJumpZone(
    letters: List<String>,
    activeLetter: String?,
    onLetterChanged: (String?) -> Unit,
    showRail: Boolean,
    railHeightPx: Float,
    onRailHeightMeasured: (Float) -> Unit,
    modifier: Modifier = Modifier,
    requireDrag: Boolean = false,
) {
    var zoneHeightPx by remember { mutableFloatStateOf(0f) }

    fun letterFor(y: Float): String? {
        val bandTop = (zoneHeightPx - railHeightPx) / 2f
        return letterAt(y = y, bandTopPx = bandTop, bandBottomPx = bandTop + railHeightPx, letters = letters)
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
            .pointerInput(letters, requireDrag) {
                if (requireDrag) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            onLetterChanged(letterFor(offset.y))
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            onLetterChanged(letterFor(change.position.y))
                        },
                        onDragEnd = { onLetterChanged(null) },
                        onDragCancel = { onLetterChanged(null) }
                    )
                } else {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        onLetterChanged(letterFor(down.position.y))
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            change.consume()
                            onLetterChanged(letterFor(change.position.y))
                        }
                        onLetterChanged(null)
                    }
                }
            },
    ) {
        if (showRail) {
            AlphabetRail(
                letters = letters,
                activeLetter = activeLetter,
                modifier = Modifier
                    .align(Alignment.Center)
                    .onSizeChanged { onRailHeightMeasured(it.height.toFloat()) },
            )
        }
    }
}

@Composable
private fun DrawerListContent(
    groupedApps: GroupedApps,
    listState: LazyListState,
    itemSize: DrawerListItemSize,
    onAppClick: (AppInfo) -> Unit,
    showIcons: Boolean,
    labelFontWeight: FontWeight,
    badgeStyle: NotificationBadgeStyle,
    badgeCounts: Map<String, Int>,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    addToFavoritesOverride: Boolean?,
    onAddToFavorites: (AppInfo) -> Unit,
    addToDockOverride: Boolean?,
    onAddToDock: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState,
        // Left inset (48dp) lines rows up with where the search bar's own content starts. No
        // right inset — this content already sits in a weight(1f) slot next to LetterJumpZone
        // (see chat history), so its own right edge already stops exactly at the rail's touch
        // zone; an extra end padding here just left an empty gap between the two.
        modifier = modifier.padding(start = 48.dp, top = 4.dp, bottom = 40.dp),
    ) {
        for ((letter, appsInGroup) in groupedApps.groups) {
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
            items(appsInGroup, key = { it.packageName + it.activityName }) { app ->
                DrawerAppRow(
                    app = app,
                    onClick = { onAppClick(app) },
                    showIcon = showIcons,
                    itemSize = itemSize,
                    labelFontWeight = labelFontWeight,
                    badgeCount = badgeCounts[app.packageName],
                    badgeStyle = badgeStyle,
                    onRequestShortcuts = onRequestShortcuts,
                    onLaunchShortcut = onLaunchShortcut,
                    addToFavoritesOverride = addToFavoritesOverride,
                    onAddToFavorites = onAddToFavorites,
                    addToDockOverride = addToDockOverride,
                    onAddToDock = onAddToDock,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerAppRow(
    app: AppInfo,
    onClick: () -> Unit,
    showIcon: Boolean,
    itemSize: DrawerListItemSize = DrawerListItemSize.COMPACT,
    labelFontWeight: FontWeight = FontWeight.Normal,
    badgeCount: Int?,
    badgeStyle: NotificationBadgeStyle,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    addToFavoritesOverride: Boolean? = null,
    onAddToFavorites: (AppInfo) -> Unit = {},
    addToDockOverride: Boolean? = null,
    onAddToDock: (AppInfo) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = modifier
                .fillMaxWidth()
                // Large/16dp (MaterialTheme.shapes.large — see CLAUDE.md's shape table, which
                // names this the "navigation drawers" token) clips the ripple/press state to a
                // rounded rect instead of a full-bleed rectangle (see chat history).
                .clip(MaterialTheme.shapes.large)
                .combinedClickable(onClick = onClick, onLongClick = { menuExpanded = true })
                .testTag("drawer_app_row_${app.packageName}")
                // Base 6dp vertical padding plus the selected size tier's extra (0/8/16dp) — see
                // DrawerListItemSize's own doc (see chat history).
                .padding(horizontal = 8.dp, vertical = 8.dp + itemSize.extraRowPaddingDp.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (showIcon) {
                // Icon grows alongside the row height for Regular/Spacious (see
                // DrawerListItemSize's own doc) — corner radius follows from size via AppIcon's
                // own M3 shape-scale default, not a hand-picked ratio (see chat history).
                AppIcon(
                    icon = app.icon,
                    size = itemSize.iconSizeDp.dp,
                    contentDescription = null,
                )
            }
            Text(text = app.label, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = labelFontWeight), color = DrawerAppTextColor)
            if (badgeCount != null && badgeCount > 0) NotificationBadge(count = badgeCount, style = badgeStyle)
        }
        AppContextMenu(
            app = app,
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            onRequestShortcuts = onRequestShortcuts,
            onLaunchShortcut = onLaunchShortcut,
            addToFavoritesOverride = addToFavoritesOverride,
            onAddToFavorites = onAddToFavorites,
            addToDockOverride = addToDockOverride,
            onAddToDock = onAddToDock,
        )
    }
}

private val GRID_CONTENT_TOP_PADDING = 4.dp
private val GRID_CONTENT_BOTTOM_PADDING = 40.dp
private val GRID_VERTICAL_SPACING = 20.dp

/**
 * Grid layout (`1i`): `columns` fixed columns, icons `44×44`/`13dp` corners, centered label
 * beneath. Unlike List, Grid renders no letter headers — a continuous grid of all apps in
 * alphabetical order, not visually grouped. The alphabet rail still works: dragging to a letter
 * scrolls to that letter's *first app* ([GroupedApps.firstAppIndexForLetter]) rather than a
 * header row, since there's no header to scroll to.
 *
 * [rows] (from the selected [DrawerGridSize][com.lumenlauncher.app.data.model.DrawerGridSize])
 * divides the *viewport* height into that many equal-height rows — each [DrawerGridTile] is
 * given exactly that height, so choosing "5 rows" always fits 5 rows of tiles in the visible
 * area regardless of how many apps there are in total, rather than "rows" being purely emergent
 * from however many happen to fit at a fixed tile size (see chat history). The grid still
 * scrolls past that computed row height when there are more apps than fit.
 */
@Composable
private fun DrawerGridContent(
    groupedApps: GroupedApps,
    gridState: LazyGridState,
    columns: Int,
    rows: Int,
    onAppClick: (AppInfo) -> Unit,
    showLabels: Boolean,
    labelFontWeight: FontWeight,
    badgeStyle: NotificationBadgeStyle,
    badgeCounts: Map<String, Int>,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    addToFavoritesOverride: Boolean?,
    onAddToFavorites: (AppInfo) -> Unit,
    addToDockOverride: Boolean?,
    onAddToDock: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val apps = remember(groupedApps) { groupedApps.groups.values.flatten() }
    BoxWithConstraints(modifier = modifier) {
        val availableHeight = maxHeight - GRID_CONTENT_TOP_PADDING - GRID_CONTENT_BOTTOM_PADDING
        val rowHeight = ((availableHeight - GRID_VERTICAL_SPACING * (rows - 1)) / rows).coerceAtLeast(0.dp)
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            // No right inset — same reasoning as DrawerListContent's identical fix (see chat
            // history): this content is already in a weight(1f) slot next to LetterJumpZone, so
            // its right edge already stops exactly at the rail's touch zone.
            contentPadding = PaddingValues(start = 48.dp, top = GRID_CONTENT_TOP_PADDING, bottom = GRID_CONTENT_BOTTOM_PADDING),
            verticalArrangement = Arrangement.spacedBy(GRID_VERTICAL_SPACING),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(apps, key = { it.packageName + it.activityName }) { app ->
                DrawerGridTile(
                    app = app,
                    onClick = { onAppClick(app) },
                    showLabel = showLabels,
                    labelFontWeight = labelFontWeight,
                    badgeCount = badgeCounts[app.packageName],
                    badgeStyle = badgeStyle,
                    onRequestShortcuts = onRequestShortcuts,
                    onLaunchShortcut = onLaunchShortcut,
                    addToFavoritesOverride = addToFavoritesOverride,
                    onAddToFavorites = onAddToFavorites,
                    addToDockOverride = addToDockOverride,
                    onAddToDock = onAddToDock,
                    modifier = Modifier.height(rowHeight),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerGridTile(
    app: AppInfo,
    onClick: () -> Unit,
    showLabel: Boolean,
    labelFontWeight: FontWeight = FontWeight.Normal,
    badgeCount: Int?,
    badgeStyle: NotificationBadgeStyle,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    addToFavoritesOverride: Boolean? = null,
    onAddToFavorites: (AppInfo) -> Unit = {},
    addToDockOverride: Boolean? = null,
    onAddToDock: (AppInfo) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box {
        Column(
            modifier = modifier
                .combinedClickable(onClick = onClick, onLongClick = { menuExpanded = true })
                .testTag("drawer_grid_tile_${app.packageName}"),
            horizontalAlignment = Alignment.CenterHorizontally,
            // The tile is now given an explicit row height (see DrawerGridContent's [rows]
            // handling) that can be taller than the icon+label's own intrinsic height — center
            // them within it instead of letting them sit pinned to the top.
            verticalArrangement = Arrangement.Center,
        ) {
            AppIcon(
                icon = app.icon,
                size = AppIconSize.TILE,
                contentDescription = null,
                notificationCount = badgeCount,
                badgeStyle = badgeStyle,
            )
            if (showLabel) {
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
        }
        AppContextMenu(
            app = app,
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            onRequestShortcuts = onRequestShortcuts,
            onLaunchShortcut = onLaunchShortcut,
            addToFavoritesOverride = addToFavoritesOverride,
            onAddToFavorites = onAddToFavorites,
            addToDockOverride = addToDockOverride,
            onAddToDock = onAddToDock,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppDrawerScreenPreview() {
    LumenLauncherTheme {
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
            },
            onAppClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppDrawerScreenGridPreview() {
    LumenLauncherTheme {
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
            },
            onAppClick = {},
            presentation = DrawerPresentation.GRID,
        )
    }
}
