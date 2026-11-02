package com.facetlauncher.app.ui.launcher

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.data.PrivateSpaceState
import com.facetlauncher.app.data.WorkProfileInfo
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.ContactConnection
import com.facetlauncher.app.data.model.ContactInfo
import com.facetlauncher.app.data.model.DrawerListItemSize
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.RecentlyInstalledPosition
import com.facetlauncher.app.data.model.SettingsSearchEntry
import com.facetlauncher.app.ui.components.AppMenuNotifications
import com.facetlauncher.app.ui.components.LocalHomeSwipeGate
import com.facetlauncher.app.ui.drawer.AppDrawerScreen
import com.facetlauncher.app.ui.drawer.DrawerAppearance
import com.facetlauncher.app.ui.drawer.DrawerItemActions
import com.facetlauncher.app.ui.drawer.DrawerListUseCases
import com.facetlauncher.app.ui.drawer.DrawerSearchBarConfig
import com.facetlauncher.app.ui.drawer.DrawerSearchSources
import com.facetlauncher.app.ui.drawer.DrawerViewModel
import com.facetlauncher.app.ui.drawer.PrivateSpaceScreen
import com.facetlauncher.app.ui.drawer.PrivateSpaceViewModel
import com.facetlauncher.app.ui.home.HomeItemActions
import com.facetlauncher.app.ui.theme.FACET_TRANSITION_DURATION_MS
import com.facetlauncher.app.ui.theme.FacetTransitionEasing
import com.facetlauncher.app.ui.theme.resolve
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

/** What the app drawer shows, collected from its view model so [HomeAppDrawerOverlay] stays stateless. */
@Immutable
internal data class HomeDrawerState(
    val apps: ImmutableList<AppInfo>,
    val workProfiles: ImmutableList<WorkProfileInfo>,
    val folders: ImmutableList<Folder>,
    val settings: LauncherSettings,
    val badgeCounts: ImmutableMap<String, Int>,
    val contacts: ImmutableList<ContactInfo>,
    val settingsEntries: ImmutableList<SettingsSearchEntry>,
    val showContactsPermissionPrompt: Boolean,
    val showContactsSettingPrompt: Boolean,
    val privateSpaceState: PrivateSpaceState,
    val secureFolderIntent: Intent?,
    val appMenuNotifications: AppMenuNotifications,
    val listUseCases: DrawerListUseCases,
)

/** What the drawer asks of its view model, beyond the item actions it shares with Home. */
@Immutable
internal data class HomeDrawerActions(
    val onQueryChange: (String) -> Unit,
    val onRequestUnlockPrivateSpace: () -> Unit,
    val onRequestConnections: suspend (ContactInfo) -> List<ContactConnection>,
    val onDismissContactsPermissionPrompt: () -> Unit,
    val onEnableContactSearch: () -> Unit,
)

/** The drawer's own state from its view model. The lists Home already holds (apps, work profiles, folders) are added by the caller with `copy`. */
@Composable
internal fun rememberHomeDrawerState(viewModel: DrawerViewModel, appMenuNotifications: AppMenuNotifications): HomeDrawerState = HomeDrawerState(
    apps = persistentListOf(),
    workProfiles = persistentListOf(),
    folders = persistentListOf(),
    settings = viewModel.settings.collectAsStateWithLifecycle().value,
    badgeCounts = viewModel.badgeCounts.collectAsStateWithLifecycle().value,
    contacts = viewModel.contactResults.collectAsStateWithLifecycle().value,
    settingsEntries = viewModel.settingsResults.collectAsStateWithLifecycle().value,
    showContactsPermissionPrompt = viewModel.showContactsPermissionPrompt.collectAsStateWithLifecycle().value,
    showContactsSettingPrompt = viewModel.showContactsSettingPrompt.collectAsStateWithLifecycle().value,
    privateSpaceState = viewModel.privateSpaceState.collectAsStateWithLifecycle().value,
    secureFolderIntent = viewModel.secureFolderIntent,
    appMenuNotifications = appMenuNotifications,
    listUseCases = viewModel.listUseCases,
)

internal fun homeDrawerActions(viewModel: DrawerViewModel) = HomeDrawerActions(
    onQueryChange = viewModel::onQueryChange,
    onRequestUnlockPrivateSpace = viewModel::requestUnlockPrivateSpace,
    onRequestConnections = viewModel::getConnections,
    onDismissContactsPermissionPrompt = viewModel::dismissContactsPermissionPrompt,
    onEnableContactSearch = viewModel::enableContactSearch,
)

/**
 * The app drawer, which slides up over Home as [drawer] opens. When it closes it resets: the
 * query, the scroll position and Private Space. [itemActions] are Home's own, except the app click.
 */
@Composable
internal fun HomeAppDrawerOverlay(
    state: HomeDrawerState,
    actions: HomeDrawerActions,
    itemActions: HomeItemActions,
    drawer: SwipeAxisState,
    containerHeightPx: Float,
    isDrawerOpen: Boolean,
    overlays: HomeOverlayState,
    onNavigateToSettings: () -> Unit,
    searchFocusRequest: Int = 0,
) {
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val searchListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var query by remember { mutableStateOf("") }

    LaunchedEffect(isDrawerOpen) {
        if (!isDrawerOpen) {
            query = ""
            actions.onQueryChange("")
            listState.scrollToItem(0)
            gridState.scrollToItem(0)
            searchListState.scrollToItem(0)
            overlays.showPrivateSpace = false
        }
    }
    val isSearching = query.isNotBlank()
    val swipeGate = LocalHomeSwipeGate.current
    val scrollConnection = remember(listState, gridState, searchListState, state.settings.drawerPresentation, isSearching, swipeGate) {
        drawerNestedScroll(drawer, state.settings.drawerPresentation, isSearching, listState, gridState, searchListState) { swipeGate?.isBlocked == true }
    }

    HomeDrawerScreen(
        state = state,
        actions = actions,
        itemActions = itemActions.copy(
            onAppClick = { app ->
                focusManager.clearFocus()
                itemActions.onAppClick(app)
                coroutineScope.launch { drawer.close() }
            },
        ),
        scroll = DrawerScrollStates(listState, gridState, searchListState),
        query = query,
        onQueryChange = { query = it; actions.onQueryChange(it) },
        onOpenPrivateSpace = { overlays.showPrivateSpace = true },
        onNavigateToSettings = onNavigateToSettings,
        isDrawerOpen = isDrawerOpen,
        searchFocusRequest = searchFocusRequest,
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(0, ((1f - drawer.progress.value) * containerHeightPx).toInt()) }
            .nestedScroll(scrollConnection),
    )
}

private fun Context.openAppDetailsSettings() {
    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))
}

/** Tapping the Private Space row opens it when unlocked and asks to unlock it when locked. */
private fun PrivateSpaceState.onRowClick(open: () -> Unit, requestUnlock: () -> Unit) {
    when (this) {
        is PrivateSpaceState.Unlocked -> open()
        is PrivateSpaceState.Locked -> requestUnlock()
        is PrivateSpaceState.NotConfigured -> Unit
    }
}

private class DrawerScrollStates(val list: LazyListState, val grid: LazyGridState, val search: LazyListState)

@Composable
private fun HomeDrawerScreen(
    state: HomeDrawerState,
    actions: HomeDrawerActions,
    itemActions: HomeItemActions,
    scroll: DrawerScrollStates,
    query: String,
    onQueryChange: (String) -> Unit,
    onOpenPrivateSpace: () -> Unit,
    onNavigateToSettings: () -> Unit,
    isDrawerOpen: Boolean,
    searchFocusRequest: Int,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val settings = state.settings
    AppDrawerScreen(
        apps = state.apps,
        workProfiles = state.workProfiles,
        listState = scroll.list,
        gridState = scroll.grid,
        searchListState = scroll.search,
        actions = itemActions.toDrawerItemActions(),
        appearance = settings.toDrawerAppearance(state.badgeCounts),
        query = query,
        onQueryChange = onQueryChange,
        searchBar = DrawerSearchBarConfig(
            position = settings.searchBarPosition,
            onNavigateToSettings = onNavigateToSettings,
            secureFolderIntent = state.secureFolderIntent,
            onOpenSecureFolder = { intent -> runCatching { context.startActivity(intent) } },
            showPrivateSpaceRow = state.privateSpaceState !is PrivateSpaceState.NotConfigured,
            onPrivateSpaceRowClick = { state.privateSpaceState.onRowClick(onOpenPrivateSpace, actions.onRequestUnlockPrivateSpace) },
            focusRequest = searchFocusRequest,
        ),
        searchSources = DrawerSearchSources(
            contacts = state.contacts,
            onRequestConnections = actions.onRequestConnections,
            showContactsPermissionPrompt = state.showContactsPermissionPrompt,
            onContactsPermissionPromptClick = {
                actions.onDismissContactsPermissionPrompt()
                context.openAppDetailsSettings()
            },
            showContactsSettingPrompt = state.showContactsSettingPrompt,
            onContactsSettingPromptClick = actions.onEnableContactSearch,
            settingsEntries = state.settingsEntries,
            onSettingsEntryClick = { entry -> runCatching { context.startActivity(Intent(entry.action)) } },
        ),
        appMenuNotifications = state.appMenuNotifications,
        folderCandidates = state.folders,
        isDrawerOpen = isDrawerOpen,
        listUseCases = state.listUseCases,
        modifier = modifier,
    )
}

private fun HomeItemActions.toDrawerItemActions() = DrawerItemActions(
    onAppClick = onAppClick,
    onAppInfo = onAppInfo,
    onRequestQuickAddState = onRequestQuickAddState,
    onFavoritesAction = onFavoritesAction,
    onDockAction = onDockAction,
    onCreateFolder = onCreateFolder,
    onAddToFolder = onAddToFolder,
    onRemoveFromFolder = onRemoveFromFolder,
    onRenameFolder = onRenameFolder,
    onRequestFolderQuickAddState = onRequestFolderQuickAddState,
    onFolderFavoritesAction = onFolderFavoritesAction,
    onFolderDockAction = onFolderDockAction,
)

@Composable
private fun LauncherSettings.toDrawerAppearance(badgeCounts: ImmutableMap<String, Int>) = DrawerAppearance(
    presentation = drawerPresentation,
    gridDensity = drawerGridDensity,
    listItemSize = drawerListItemSize,
    showIcons = showDrawerIcons,
    showLabels = showDrawerLabels,
    labelFontWeight = homeAppsFontWeight.resolve(),
    opacity = drawerOpacity,
    notificationBadgeStyle = notificationBadgeStyle,
    badgeCounts = badgeCounts,
    folderDisplayMode = drawerFolderDisplayMode,
    recentlyInstalledPosition = recentlyInstalledPosition,
)

/** What Private Space shows. */
@Immutable
internal data class HomePrivateSpaceState(
    val state: PrivateSpaceState,
    val apps: ImmutableList<AppInfo>,
    val recentlyInstalledApps: ImmutableList<AppInfo>,
    val recentlyInstalledPosition: RecentlyInstalledPosition,
    /** The main drawer's List item size, which Private Space's rows follow. */
    val listItemSize: DrawerListItemSize = DrawerListItemSize.REGULAR,
)

@Composable
internal fun rememberHomePrivateSpaceState(
    viewModel: PrivateSpaceViewModel,
    state: PrivateSpaceState,
    position: RecentlyInstalledPosition,
    listItemSize: DrawerListItemSize,
) =
    HomePrivateSpaceState(
        state = state,
        apps = viewModel.filteredApps.collectAsStateWithLifecycle().value,
        recentlyInstalledApps = viewModel.recentlyInstalledApps.collectAsStateWithLifecycle().value,
        recentlyInstalledPosition = position,
        listItemSize = listItemSize,
    )

/** Private Space, an in-place overlay over the app drawer: one back press returns to the drawer, a second closes it. */
@Composable
internal fun HomePrivateSpaceOverlay(
    overlays: HomeOverlayState,
    state: HomePrivateSpaceState,
    onQueryChange: (String) -> Unit,
    onAppClick: (AppInfo) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val currentOnQueryChange by rememberUpdatedState(onQueryChange)
    // Its own search, never shared with the drawer's.
    LaunchedEffect(overlays.showPrivateSpace) {
        if (!overlays.showPrivateSpace) {
            query = ""
            currentOnQueryChange("")
        }
    }
    AnimatedVisibility(
        visible = overlays.showPrivateSpace,
        enter = fadeIn(animationSpec = tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)),
        exit = fadeOut(animationSpec = tween(FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)),
        modifier = Modifier.testTag("private_space_screen_overlay"),
    ) {
        PrivateSpaceScreen(
            state = state.state,
            apps = state.apps,
            recentlyInstalledApps = state.recentlyInstalledApps,
            recentlyInstalledPosition = state.recentlyInstalledPosition,
            listItemSize = state.listItemSize,
            query = query,
            onQueryChange = { query = it; onQueryChange(it) },
            onAppClick = onAppClick,
        )
    }
}

/**
 * Pulls the drawer shut when a downward drag meets the top of its list. Only real finger movement
 * drives it: fling deltas are the list's own deceleration, and letting them call `dragBy` would
 * re-arm the drag and snap over the settle animation, stranding the drawer partway. It stands down while
 * [swipeBlocked] (a sheet is open over the drawer), so a drag on that sheet's own list is not taken as a pull-to-close.
 */
internal fun drawerNestedScroll(
    drawer: SwipeAxisState,
    presentation: DrawerPresentation,
    isSearching: Boolean,
    listState: LazyListState,
    gridState: LazyGridState,
    searchListState: LazyListState,
    swipeBlocked: () -> Boolean = { false },
): NestedScrollConnection = object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        // Search results are always their own LazyColumn, whatever the browse presentation: check
        // that list's state, not the untouched browse list's, which is always "at top" while searching.
        val atTop = when {
            isSearching -> searchListState.firstVisibleItemIndex == 0 && searchListState.firstVisibleItemScrollOffset == 0
            presentation == DrawerPresentation.GRID -> gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset == 0
            else -> listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
        val pullsClosed = source == NestedScrollSource.Drag && available.y > 0f && atTop && drawer.progress.value > 0f && !swipeBlocked()
        if (pullsClosed) drawer.dragBy(-available.y)
        return if (pullsClosed) available else Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (drawer.dragActive) drawer.settle(velocity = -available.y)
        return super.onPreFling(available)
    }
}
