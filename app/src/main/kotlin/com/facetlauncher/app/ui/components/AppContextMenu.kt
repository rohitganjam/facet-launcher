package com.facetlauncher.app.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Dock
import androidx.compose.material.icons.outlined.Folder as FolderIcon
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppShortcut
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.domain.QuickAddState
import com.facetlauncher.app.domain.QuickPlacementAction
import com.facetlauncher.app.ui.launcher.LocalHomePressedEvent
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.ErrorColor
import com.facetlauncher.app.ui.theme.Faint
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Scrim
import com.facetlauncher.app.ui.theme.Surface

/** At most this many of an app's own [AppShortcut]s are shown — a long-press sheet is a quick
 * action, not another drawer, so shortcuts beyond this many are simply dropped. */
private const val MAX_SHORTCUTS = 3

/** M3's own modal-bottom-sheet corner treatment — top corners only, [MaterialTheme.shapes.extraLarge]'s
 * 28dp (see `CLAUDE.md`'s Material 3 shape section) — written as a literal [RoundedCornerShape]
 * since [MaterialTheme.shapes] has no per-corner variant to reference directly. */
internal val SHEET_SHAPE = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomEnd = 0.dp, bottomStart = 0.dp)
internal val ITEM_PADDING = PaddingValues(horizontal = 24.dp, vertical = 16.dp)

/**
 * F12's long-press context menu (`4i`) — a bottom sheet rather than a floating popup (decided
 * explicitly by the user over [ThemedDropdownMenu], see chat history). The header (app icon +
 * name) sits at the top with "App info" as a row directly beneath it, then the app's own quick
 * actions ([AppShortcut]s, capped at [MAX_SHORTCUTS]); Uninstall is always the last item. Rows are
 * separated by whitespace only (no dividers) with outlined, single-tone icons throughout —
 * mirrors Niagara Launcher's own sheet, decided explicitly by the user over the busier
 * divider-per-row/filled-icon look this had before (see chat history). Widgets is deliberately
 * omitted (Phase 7 isn't built yet). [onRequestShortcuts] is fetched fresh each time the sheet
 * opens rather than precomputed for every app up front, since most long-presses never open it.
 *
 * "Add to Favorites"/"Add to Dock" (and, once already a member, "Remove from Favorites"/"Remove
 * from Dock") — from [com.facetlauncher.app.domain.ObserveQuickAddStateUseCase] via
 * [onRequestQuickAddState] — sit between "App info" and the shortcuts. A `null` action hides the
 * corresponding row entirely (not a member, and that list is already at its cap); the row's own
 * label text never changes, but a small trailing [QuickPlacementBadge] names which list it targets
 * — "Global" for the launcher-wide default, the active facet's own real name when it's overriding
 * (decided explicitly by the user over folding that distinction into the sentence itself, e.g.
 * "Add to facet favorites" — see chat history).
 *
 * "Add to folder" ([folderCandidates] non-null) sits right after those two — tapping it doesn't
 * close this sheet, it slides its content over to a second page (same `AnimatedContent`
 * slide-in-from-right + back-chevron shape as [com.facetlauncher.app.ui.drawer.ContactConnectionsSheet]'s
 * own main-list/disambiguation-page split) listing "+ Create new folder" first, then every folder
 * in the (global) library, each with its own app-count subheading. Creating a new folder opens
 * [RenameDialog] to name it immediately, adds [app] as its first member, then dismisses the whole
 * sheet via [onCreateFolder]. Tapping an *existing* folder instead previews it — opens
 * [FolderContentsSheet] on top of this sheet, in [FolderSheetHeaderAction.AddHere] mode, showing
 * that folder's real current contents with an Add action in the header; tapping Add is what
 * actually calls [onAddToFolder] and dismisses both sheets. This lets the user see what they're
 * adding alongside before confirming, rather than adding blind.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContextMenu(
    app: AppInfo,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    modifier: Modifier = Modifier,
    onRequestQuickAddState: suspend (AppInfo) -> QuickAddState = { QuickAddState() },
    onFavoritesAction: (AppInfo, QuickPlacementAction) -> Unit = { _, _ -> },
    onDockAction: (AppInfo, QuickPlacementAction) -> Unit = { _, _ -> },
    folderCandidates: List<Folder>? = null,
    onCreateFolder: (AppInfo, String) -> Unit = { _, _ -> },
    onAddToFolder: (AppInfo, Long) -> Unit = { _, _ -> },
    /** Follows the App Drawer's own List/Grid setting for the "Add to folder" preview's [FolderContentsSheet]. */
    drawerPresentation: DrawerPresentation = DrawerPresentation.LIST,
    /** Non-null only when this menu was opened on an app row *inside* [FolderContentsSheet] — shows "Remove from folder" instead of "Add to folder" (the two never both apply to the same app). */
    removeFromFolderId: Long? = null,
    onRemoveFromFolder: (AppInfo, Long) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    var shortcuts by remember(app.packageName, app.activityName) { mutableStateOf<List<AppShortcut>>(emptyList()) }
    var quickAddState by remember(app.packageName, app.activityName) { mutableStateOf(QuickAddState()) }
    var addingToFolder by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var previewingFolder by remember { mutableStateOf<Folder?>(null) }

    LaunchedEffect(expanded, app) {
        if (expanded) {
            shortcuts = onRequestShortcuts(app)
            quickAddState = onRequestQuickAddState(app)
        }
    }

    // Every fresh open starts back on the main page, regardless of where a previous open left off.
    LaunchedEffect(expanded) {
        if (!expanded) {
            addingToFolder = false
            showCreateFolderDialog = false
            previewingFolder = null
        }
    }

    // System back dismisses the whole sheet from the main page, or returns to it from the folder
    // page — mutually exclusive via `enabled` so which one fires never depends on registration order.
    BackHandler(enabled = expanded && !addingToFolder, onBack = onDismissRequest)
    BackHandler(enabled = expanded && addingToFolder) { addingToFolder = false }
    val homePressedEvent = LocalHomePressedEvent.current
    LaunchedEffect(expanded, homePressedEvent) {
        if (expanded) homePressedEvent?.collect { onDismissRequest() }
    }

    if (!expanded) return

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier.testTag("app_context_menu"),
        sheetState = rememberModalBottomSheetState(),
        shape = SHEET_SHAPE,
        containerColor = Surface,
        scrimColor = Scrim,
        dragHandle = { AppContextMenuDragHandle() },
    ) {
        AnimatedContent(
            targetState = addingToFolder,
            transitionSpec = {
                if (targetState) {
                    (slideInHorizontally(animationSpec = tween(280)) { it } + fadeIn())
                        .togetherWith(slideOutHorizontally(animationSpec = tween(280)) { -it } + fadeOut())
                } else {
                    (slideInHorizontally(animationSpec = tween(280)) { -it } + fadeIn())
                        .togetherWith(slideOutHorizontally(animationSpec = tween(280)) { it } + fadeOut())
                }
            },
            label = "app_context_menu_page",
        ) { onFolderPage ->
            if (!onFolderPage) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppIcon(icon = app.icon, size = AppIconSize.ROW_SPACIOUS, contentDescription = null)
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.titleLarge,
                            color = Ink,
                            modifier = Modifier.padding(start = 16.dp),
                        )
                    }
                    AppContextMenuItem(
                        label = "App info",
                        modifier = Modifier.testTag("app_context_menu_app_info"),
                        leadingIcon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = null, tint = Muted) },
                        onClick = {
                            onDismissRequest()
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", app.packageName, null))
                            runCatching { context.startActivity(intent) }
                        },
                    )
                    quickAddState.favoritesAction?.let { action ->
                        AppContextMenuItem(
                            label = quickPlacementLabel(action, "Favorites"),
                            modifier = Modifier.testTag("app_context_menu_add_to_favorites"),
                            leadingIcon = { Icon(imageVector = Icons.Outlined.StarBorder, contentDescription = null, tint = Muted) },
                            trailingContent = { QuickPlacementBadge(action = action) },
                            onClick = {
                                onDismissRequest()
                                onFavoritesAction(app, action)
                            },
                        )
                    }
                    quickAddState.dockAction?.let { action ->
                        AppContextMenuItem(
                            label = quickPlacementLabel(action, "Dock"),
                            modifier = Modifier.testTag("app_context_menu_add_to_dock"),
                            leadingIcon = { Icon(imageVector = Icons.Outlined.Dock, contentDescription = null, tint = Muted) },
                            trailingContent = { QuickPlacementBadge(action = action) },
                            onClick = {
                                onDismissRequest()
                                onDockAction(app, action)
                            },
                        )
                    }
                    if (folderCandidates != null) {
                        AppContextMenuItem(
                            label = "Add to folder",
                            modifier = Modifier.testTag("app_context_menu_add_to_folder"),
                            leadingIcon = { Icon(imageVector = Icons.Outlined.CreateNewFolder, contentDescription = null, tint = Muted) },
                            onClick = { addingToFolder = true },
                        )
                    }
                    if (removeFromFolderId != null) {
                        AppContextMenuItem(
                            label = "Remove from folder",
                            modifier = Modifier.testTag("app_context_menu_remove_from_folder"),
                            leadingIcon = { Icon(imageVector = Icons.Outlined.FolderIcon, contentDescription = null, tint = Muted) },
                            onClick = {
                                onDismissRequest()
                                onRemoveFromFolder(app, removeFromFolderId)
                            },
                        )
                    }
                    shortcuts.take(MAX_SHORTCUTS).forEach { shortcut ->
                        AppContextMenuItem(
                            label = shortcut.label,
                            modifier = Modifier.testTag("app_context_menu_shortcut_${shortcut.id}"),
                            leadingIcon = { AppIcon(icon = shortcut.icon, size = AppIconSize.SHORTCUT, contentDescription = null) },
                            onClick = {
                                onDismissRequest()
                                onLaunchShortcut(shortcut)
                            },
                        )
                    }
                    AppContextMenuItem(
                        label = "Uninstall",
                        destructive = true,
                        modifier = Modifier.testTag("app_context_menu_uninstall"),
                        leadingIcon = { Icon(imageVector = Icons.Outlined.DeleteOutline, contentDescription = null, tint = ErrorColor) },
                        onClick = {
                            onDismissRequest()
                            val intent = Intent(Intent.ACTION_DELETE, Uri.fromParts("package", app.packageName, null))
                            runCatching { context.startActivity(intent) }
                        },
                    )
                }
            } else {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Ink,
                            modifier = Modifier
                                .testTag("app_context_menu_folder_page_back")
                                .clickable(onClick = { addingToFolder = false })
                                .padding(8.dp),
                        )
                        Text(
                            text = "Add to folder",
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                    AppContextMenuItem(
                        label = "Create new folder",
                        modifier = Modifier.testTag("app_context_menu_create_folder"),
                        leadingIcon = { Icon(imageVector = Icons.Outlined.CreateNewFolder, contentDescription = null, tint = Accent) },
                        onClick = { showCreateFolderDialog = true },
                    )
                    CardDivider(modifier = Modifier.padding(horizontal = 24.dp))
                    Column(modifier = Modifier.heightIn(max = 280.dp).verticalScroll(rememberScrollState())) {
                        folderCandidates.orEmpty().forEach { folder ->
                            FolderCandidateRow(
                                folder = folder,
                                onClick = { previewingFolder = folder },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateFolderDialog) {
        RenameDialog(
            title = "New folder",
            explanation = "Name this folder.",
            initialValue = "",
            onSave = { name ->
                showCreateFolderDialog = false
                onDismissRequest()
                onCreateFolder(app, name)
            },
            onDismiss = { showCreateFolderDialog = false },
        )
    }

    previewingFolder?.let { folder ->
        FolderContentsSheet(
            folder = folder,
            onDismissRequest = { previewingFolder = null },
            onAppClick = {},
            presentation = drawerPresentation,
            onRemoveFromFolder = { _, _ -> },
            headerAction = FolderSheetHeaderAction.AddHere(
                onAdd = {
                    previewingFolder = null
                    onDismissRequest()
                    onAddToFolder(app, folder.id)
                },
                onBack = { previewingFolder = null },
            ),
        )
    }
}

@Composable
private fun FolderCandidateRow(folder: Folder, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_context_menu_folder_${folder.id}")
            .clickable(onClick = onClick)
            .padding(ITEM_PADDING),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(imageVector = Icons.Outlined.FolderIcon, contentDescription = null, tint = Muted)
        Column {
            Text(text = folder.name, style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(text = "${folder.apps.size} apps", style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }
}

/** Shared label wording for a Favorites/Dock quick-placement row — used by both [AppContextMenu] and [FolderTileContextMenu]. The row's own label never names a list target; that's [QuickPlacementBadge]'s job. */
internal fun quickPlacementLabel(action: QuickPlacementAction, listName: String): String {
    val verb = when (action) {
        is QuickPlacementAction.Add -> "Add to"
        is QuickPlacementAction.Remove -> "Remove from"
    }
    return "$verb $listName"
}

/** Trailing pill on a Favorites/Dock quick-placement row, naming which list it targets — see [FacetScopeBadge]. */
@Composable
internal fun QuickPlacementBadge(action: QuickPlacementAction, modifier: Modifier = Modifier) {
    FacetScopeBadge(facetName = action.facetName, modifier = modifier)
}

@Composable
internal fun AppContextMenuItem(
    label: String,
    onClick: () -> Unit,
    leadingIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(ITEM_PADDING),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        leadingIcon()
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (destructive) ErrorColor else Ink,
            modifier = Modifier.weight(1f),
        )
        trailingContent?.invoke()
    }
}

/** Mirrors [com.facetlauncher.app.ui.onboarding.SetDefaultLauncherSheet]'s own hand-rolled drag
 * handle (34x4dp, [Faint], 2dp corners) rather than M3's [androidx.compose.material3.BottomSheetDefaults.DragHandle]
 * default, whose tint resolves to an unmapped color-scheme slot. */
@Composable
internal fun AppContextMenuDragHandle() {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(width = 34.dp, height = 4.dp).background(Faint, RoundedCornerShape(2.dp)))
    }
}
