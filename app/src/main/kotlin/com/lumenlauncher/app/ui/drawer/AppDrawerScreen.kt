package com.lumenlauncher.app.ui.drawer

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppShortcut
import com.lumenlauncher.app.data.model.ContactInfo
import com.lumenlauncher.app.data.model.DrawerGridSize
import com.lumenlauncher.app.data.model.DrawerListItemSize
import com.lumenlauncher.app.data.model.DrawerPresentation
import com.lumenlauncher.app.data.model.NotificationBadgeStyle
import com.lumenlauncher.app.data.model.SearchBarPosition
import com.lumenlauncher.app.domain.GroupAppsByLetterUseCase
import com.lumenlauncher.app.domain.GroupedApps
import com.lumenlauncher.app.ui.components.AppContextMenu
import com.lumenlauncher.app.ui.components.AppIcon
import com.lumenlauncher.app.ui.components.NotificationBadge
import com.lumenlauncher.app.ui.components.ThemedDropdownMenu
import com.lumenlauncher.app.ui.components.ThemedDropdownMenuItem
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.DrawerAppTextColor
import com.lumenlauncher.app.ui.theme.DrawerHeaderTextColor
import com.lumenlauncher.app.ui.theme.DrawerOverlay
import com.lumenlauncher.app.ui.theme.IconTile
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.LumenType
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Surface
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
    listItemSize: DrawerListItemSize = DrawerListItemSize.COMPACT,
    showIcons: Boolean = true,
    showLabels: Boolean = true,
    opacity: Float = 0.88f,
    notificationBadgeStyle: NotificationBadgeStyle = NotificationBadgeStyle.DOT,
    badgeCounts: Map<String, Int> = emptyMap(),
    query: String = "",
    onQueryChanged: (String) -> Unit = {},
    searchBarPosition: SearchBarPosition = SearchBarPosition.TOP,
    onNavigateToSettings: () -> Unit = {},
    contacts: List<ContactInfo> = emptyList(),
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut> = { emptyList() },
    onLaunchShortcut: (AppShortcut) -> Unit = {},
) {
    val filteredApps = remember(apps, query) {
        if (query.isBlank()) apps else apps.filter { it.label.contains(query, ignoreCase = true) }
    }
    val groupUseCase = remember { GroupAppsByLetterUseCase() }
    val groupedApps = remember(filteredApps) { groupUseCase(filteredApps) }
    val coroutineScope = rememberCoroutineScope()
    var draggingLetter by remember { mutableStateOf<String?>(null) }
    var railHeightPx by remember { mutableFloatStateOf(0f) }
    val isSearching = query.isNotBlank()

    // First back-press while searching clears the query and stays in the drawer (browse mode,
    // unfiltered); only a second, empty-query back-press falls through to the caller's own
    // drawer-close BackHandler.
    BackHandler(enabled = isSearching) { onQueryChanged("") }

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

    // Edge-to-edge is enforced unconditionally at this app's targetSdk (36) — without this, the
    // drawer's content (notably a TOP search bar) draws straight under the status bar/notch.
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DrawerOverlay.copy(alpha = opacity))
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
                badgeStyle = notificationBadgeStyle,
                badgeCounts = badgeCounts,
                onAppClick = onAppClick,
                onClearSearch = { onQueryChanged("") },
                onRequestShortcuts = onRequestShortcuts,
                onLaunchShortcut = onLaunchShortcut,
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
                    badgeStyle = notificationBadgeStyle,
                    badgeCounts = badgeCounts,
                    onRequestShortcuts = onRequestShortcuts,
                    onLaunchShortcut = onLaunchShortcut,
                    modifier = Modifier.weight(1f).fillMaxHeight().testTag("drawer_grid"),
                )
            } else {
                DrawerListContent(
                    groupedApps = groupedApps,
                    listState = listState,
                    itemSize = listItemSize,
                    onAppClick = onAppClick,
                    showIcons = showIcons,
                    badgeStyle = notificationBadgeStyle,
                    badgeCounts = badgeCounts,
                    onRequestShortcuts = onRequestShortcuts,
                    onLaunchShortcut = onLaunchShortcut,
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
        )

        draggingLetter?.let { letter ->
            Text(
                text = letter,
                style = LumenType.clock.copy(fontSize = 40.sp, lineHeight = 40.sp),
                color = DrawerAppTextColor,
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
 * are empty.
 */
@Composable
private fun DrawerSearchResults(
    query: String,
    apps: List<AppInfo>,
    contacts: List<ContactInfo>,
    presentation: DrawerPresentation,
    gridColumns: Int,
    showIcons: Boolean,
    badgeStyle: NotificationBadgeStyle,
    badgeCounts: Map<String, Int>,
    onAppClick: (AppInfo) -> Unit,
    onClearSearch: () -> Unit,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (apps.isEmpty() && contacts.isEmpty()) {
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
                    // simpler than wiring a second scroll container.
                    apps.chunked(gridColumns).forEach { rowApps ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 20.dp)) {
                            rowApps.forEach { app ->
                                DrawerGridTile(
                                    app = app,
                                    onClick = { onAppClick(app) },
                                    showLabel = true,
                                    badgeCount = badgeCounts[app.packageName],
                                    badgeStyle = badgeStyle,
                                    onRequestShortcuts = onRequestShortcuts,
                                    onLaunchShortcut = onLaunchShortcut,
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
                        badgeCount = badgeCounts[app.packageName],
                        badgeStyle = badgeStyle,
                        onRequestShortcuts = onRequestShortcuts,
                        onLaunchShortcut = onLaunchShortcut,
                    )
                }
            }
        }
        if (contacts.isNotEmpty()) {
            item {
                Text(
                    text = "CONTACTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = DrawerHeaderTextColor,
                    modifier = Modifier.padding(top = 14.dp, bottom = 4.dp),
                )
            }
            items(contacts, key = { "contact_" + it.id }) { contact ->
                ContactRow(contact = contact)
            }
        }
        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

/**
 * One contact match — collapsed to just a 32dp avatar/initials + name by default; tapping
 * toggles this row's own [expanded] state, revealing action chips beneath the name. Call/Message
 * chips always show (a phone-number match implies a number); WhatsApp only when the app is
 * actually installed. Each chip launches its target app pre-filled with the contact, per the
 * approved F6 design — never dials/sends automatically.
 */
@Composable
private fun ContactRow(contact: ContactInfo, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val hasWhatsApp = remember {
        runCatching { context.packageManager.getPackageInfo("com.whatsapp", 0) }.isSuccess
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("contact_row_${contact.id}")
            .padding(vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(IconTile),
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
        if (expanded) {
            Row(
                modifier = Modifier.padding(start = 44.dp, top = 4.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ContactActionChip(
                    label = "Call",
                    primary = true,
                    onClick = {
                        runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phoneNumber}"))) }
                    },
                )
                ContactActionChip(
                    label = "Message",
                    primary = false,
                    onClick = {
                        runCatching { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${contact.phoneNumber}"))) }
                    },
                )
                if (hasWhatsApp) {
                    ContactActionChip(
                        label = "WhatsApp",
                        primary = false,
                        onClick = {
                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=${contact.phoneNumber}")
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                        },
                    )
                }
            }
        }
    }
}

/** README's `padding: 7px 15px`, `border-radius: 8px` (M3 Chip default — `MaterialTheme.shapes.small`) — primary filled, others outlined. */
@Composable
private fun ContactActionChip(label: String, primary: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.small
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = if (primary) Surface else Ink,
        modifier = modifier
            .clip(shape)
            .then(
                if (primary) {
                    Modifier.background(Accent, shape)
                } else {
                    Modifier.background(Surface, shape).border(1.dp, Ink.copy(alpha = 0.22f), shape)
                },
            )
            .clickable(onClick = onClick)
            .testTag("contact_action_${label.lowercase()}")
            .padding(horizontal = 15.dp, vertical = 7.dp),
    )
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
        Text(
            text = "Clear search",
            style = MaterialTheme.typography.bodyMedium,
            color = Ink,
            modifier = Modifier
                .clip(CircleShape)
                .border(1.dp, Ink.copy(alpha = 0.14f), CircleShape)
                .clickable(onClick = onClearSearch)
                .testTag("drawer_search_clear")
                .padding(horizontal = 16.dp, vertical = 9.dp),
        )
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
            // A plain detectDragGestures only fires onDragStart once the touch has moved past
            // the platform's touch-slop threshold — a stationary press-and-hold does nothing
            // until the finger actually moves (see chat history). This zone has no competing
            // tap/click interaction to disambiguate a drag from, so activating immediately on
            // the initial down (awaitFirstDown, no slop wait) is safe and is what makes a single
            // touch — not just a drag — jump straight to a letter.
            .pointerInput(letters) {
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
    badgeStyle: NotificationBadgeStyle,
    badgeCounts: Map<String, Int>,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
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
                    badgeCount = badgeCounts[app.packageName],
                    badgeStyle = badgeStyle,
                    onRequestShortcuts = onRequestShortcuts,
                    onLaunchShortcut = onLaunchShortcut,
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
    // Search results render this row at its unchanged base size regardless of the List item
    // size setting — that setting is about browse-mode density, not search (see chat history).
    itemSize: DrawerListItemSize = DrawerListItemSize.COMPACT,
    badgeCount: Int?,
    badgeStyle: NotificationBadgeStyle,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
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
                .padding(horizontal = 8.dp, vertical = 6.dp + itemSize.extraRowPaddingDp.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (showIcon) {
                AppIcon(icon = app.icon, size = 32.dp, cornerRadius = 9.dp, contentDescription = null)
            }
            Text(text = app.label, style = MaterialTheme.typography.bodyLarge, color = DrawerAppTextColor)
            if (badgeCount != null && badgeCount > 0) NotificationBadge(count = badgeCount, style = badgeStyle)
        }
        AppContextMenu(
            app = app,
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            onRequestShortcuts = onRequestShortcuts,
            onLaunchShortcut = onLaunchShortcut,
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
    badgeStyle: NotificationBadgeStyle,
    badgeCounts: Map<String, Int>,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
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
                    badgeCount = badgeCounts[app.packageName],
                    badgeStyle = badgeStyle,
                    onRequestShortcuts = onRequestShortcuts,
                    onLaunchShortcut = onLaunchShortcut,
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
    badgeCount: Int?,
    badgeStyle: NotificationBadgeStyle,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
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
                size = 44.dp,
                cornerRadius = 13.dp,
                contentDescription = null,
                notificationCount = badgeCount,
                badgeStyle = badgeStyle,
            )
            if (showLabel) {
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.labelSmall,
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
