package com.facetlauncher.app.ui.drawer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.ContactInfo
import com.facetlauncher.app.data.model.DrawerGridDensity
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.SearchBarPosition
import com.facetlauncher.app.ui.components.BlockHomeSwipesWhileShown
import com.facetlauncher.app.ui.components.AppMenuNotifications
import com.facetlauncher.app.ui.components.DismissOnHomePress
import com.facetlauncher.app.ui.theme.FACET_TRANSITION_DURATION_MS
import com.facetlauncher.app.ui.theme.FacetTransitionEasing
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer
import com.facetlauncher.app.ui.theme.launcherSystemBarInsets
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/** The sheet is always a grid: the drawer's own density when the drawer is a grid, else Regular. */
internal fun DrawerAppearance.forSearchSheet(): DrawerAppearance = copy(
    presentation = DrawerPresentation.GRID,
    gridDensity = if (presentation == DrawerPresentation.GRID) gridDensity else DrawerGridDensity.REGULAR,
)

/** Rows of tiles each suggestion section shows. */
private const val SUGGESTION_ROWS = 2

/**
 * The search sheet: slides in from the edge the search bar is on, over a scrim. Before anything is typed it suggests
 * recently installed or updated, frequently used and last used apps as a grid; once typing, it shows the search results,
 * always as a grid at the drawer's grid density (Regular when the drawer is a list). Back, the scrim and the Home button close it, returning to the drawer.
 */
@Composable
internal fun DrawerSearchSheet(
    visible: Boolean,
    onClose: () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    barPosition: SearchBarPosition,
    apps: ImmutableList<AppInfo>,
    searchResultApps: ImmutableList<AppInfo>,
    searchListState: LazyListState,
    appearance: DrawerAppearance,
    actions: DrawerItemActions,
    sources: DrawerSearchSources,
    appMenuNotifications: AppMenuNotifications,
    folderCandidates: ImmutableList<Folder>?,
    listUseCases: DrawerListUseCases,
    onContactClick: (ContactInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val close = {
        focusManager.clearFocus()
        onClose()
    }
    BackHandler(enabled = visible, onBack = close)
    DismissOnHomePress(enabled = visible, onDismiss = close)
    if (visible) BlockHomeSwipesWhileShown()

    val atBottom = barPosition == SearchBarPosition.BOTTOM
    val spec = tween<androidx.compose.ui.unit.IntOffset>(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) { SheetScrim(close) }
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(spec) { if (atBottom) it else -it },
            exit = slideOutVertically(spec) { if (atBottom) it else -it },
            modifier = Modifier.align(if (atBottom) Alignment.BottomCenter else Alignment.TopCenter),
        ) {
            val shape = if (atBottom) {
                RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            } else {
                RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize(0.92f)
                    .clip(shape)
                    .background(SurfaceContainer)
                    .pointerInput(Unit) { detectTapGestures { } }
                    .windowInsetsPadding(launcherSystemBarInsets())
                    .imePadding()
                    .testTag("drawer_search_sheet"),
            ) {
                val field = @Composable { SheetSearchField(query, onQueryChange, close) }
                if (!atBottom) field()
                SheetBody(
                    query, onQueryChange, apps, searchResultApps, searchListState, appearance, actions, sources,
                    appMenuNotifications, folderCandidates, listUseCases, onContactClick, Modifier.weight(1f),
                )
                if (atBottom) field()
            }
        }
    }
}

/** Search results once something is typed, the suggestion sections before. */
@Composable
private fun SheetBody(
    query: String,
    onQueryChange: (String) -> Unit,
    apps: ImmutableList<AppInfo>,
    searchResultApps: ImmutableList<AppInfo>,
    searchListState: LazyListState,
    appearance: DrawerAppearance,
    actions: DrawerItemActions,
    sources: DrawerSearchSources,
    appMenuNotifications: AppMenuNotifications,
    folderCandidates: ImmutableList<Folder>?,
    listUseCases: DrawerListUseCases,
    onContactClick: (ContactInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        if (query.isNotBlank()) {
            DrawerSearchResults(
                query = query,
                apps = searchResultApps,
                listState = searchListState,
                appearance = appearance.forSearchSheet(),
                actions = actions,
                sources = sources,
                onClearSearch = { onQueryChange("") },
                appMenuNotifications = appMenuNotifications,
                folderCandidates = folderCandidates,
                onContactClick = onContactClick,
                modifier = Modifier.fillMaxSize().testTag("drawer_search_results"),
            )
        } else {
            SearchSuggestions(apps, appearance, actions, sources, appMenuNotifications, folderCandidates, listUseCases, searchListState)
        }
    }
}

@Composable
private fun SheetScrim(onClose: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .pointerInput(Unit) { detectTapGestures { onClose() } }
            .testTag("drawer_search_scrim"),
    )
}

/** The three suggestion sections, as grids. Usage-based ones load when the sheet shows; without Usage Access they give way to one prompt. */
@Composable
private fun SearchSuggestions(
    apps: ImmutableList<AppInfo>,
    appearance: DrawerAppearance,
    actions: DrawerItemActions,
    sources: DrawerSearchSources,
    appMenuNotifications: AppMenuNotifications,
    folderCandidates: ImmutableList<Folder>?,
    listUseCases: DrawerListUseCases,
    listState: LazyListState,
) {
    val requestUsage by rememberUpdatedState(sources.onRequestUsageSuggestions)
    var usage by remember { mutableStateOf(UsageSuggestions(accessGranted = true)) }
    LaunchedEffect(Unit) { usage = requestUsage() }

    // Usage lookups know packages, not profiles: keep only entries the drawer itself lists.
    val listed = remember(apps) { apps.mapTo(HashSet()) { it.listKey } }
    val recentChanges = remember(apps) { listUseCases.recentlyChanged(apps).toImmutableList() }
    val frequent = remember(usage, listed) { usage.frequent.filter { it.listKey in listed }.toImmutableList() }
    val lastUsed = remember(usage, listed) { usage.lastUsed.filter { it.listKey in listed }.toImmutableList() }
    val gridAppearance = appearance.forSearchSheet()

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).testTag("drawer_search_suggestions")) {
        suggestionSection(R.string.drawer_search_section_recent_changes, recentChanges, gridAppearance, actions, appMenuNotifications, folderCandidates)
        if (usage.accessGranted) {
            suggestionSection(R.string.drawer_search_section_frequent, frequent, gridAppearance, actions, appMenuNotifications, folderCandidates)
            suggestionSection(R.string.drawer_search_section_last_used, lastUsed, gridAppearance, actions, appMenuNotifications, folderCandidates)
        } else {
            item { UsageAccessStrip(onClick = sources.onUsageAccessClick, modifier = Modifier.padding(top = 14.dp)) }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.suggestionSection(
    titleRes: Int,
    apps: ImmutableList<AppInfo>,
    appearance: DrawerAppearance,
    actions: DrawerItemActions,
    appMenuNotifications: AppMenuNotifications,
    folderCandidates: ImmutableList<Folder>?,
) {
    if (apps.isEmpty()) return
    item(key = "header_$titleRes") { SearchSectionHeader(titleRes) }
    item(key = "grid_$titleRes") {
        SearchAppsGrid(
            apps = apps,
            appearance = appearance,
            actions = actions,
            appMenuNotifications = appMenuNotifications,
            folderCandidates = folderCandidates,
            maxRows = SUGGESTION_ROWS,
        )
    }
}

@Composable
private fun UsageAccessStrip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("drawer_search_usage_access_strip")
            .drawerDashedBorder(color = com.facetlauncher.app.ui.theme.Ink.copy(alpha = 0.16f), cornerRadius = 10.dp)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.drawer_search_usage_access_needed),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = Modifier.weight(1f),
        )
        Text(text = stringResource(R.string.open_settings), style = MaterialTheme.typography.bodyMedium, color = Accent)
    }
}

/** The sheet's own field: focused as it opens, a back arrow to close, and a clear button once something is typed. */
@Composable
private fun SheetSearchField(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(CircleShape)
            .background(Surface),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose, modifier = Modifier.testTag("drawer_search_sheet_back")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_description_back), tint = Muted)
        }
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
            modifier = Modifier.weight(1f).focusRequester(focusRequester).testTag("drawer_search_field"),
        )
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.drawer_search_clear), tint = Muted)
            }
        }
    }
}
