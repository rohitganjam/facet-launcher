package com.facetlauncher.app.ui.components

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
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
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Dock
import androidx.compose.material.icons.outlined.Folder as FolderIcon
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.NotificationInfo
import com.facetlauncher.app.domain.AppNotificationsState
import com.facetlauncher.app.domain.QuickAddState
import com.facetlauncher.app.domain.QuickPlacementAction
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.ErrorColor
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Faint
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted

/** M3's own modal-bottom-sheet corner treatment — top corners only, [MaterialTheme.shapes.extraLarge]'s
 * 28dp (see `CLAUDE.md`'s Material 3 shape section) — written as a literal [RoundedCornerShape]
 * since [MaterialTheme.shapes] has no per-corner variant to reference directly. */
internal val SHEET_SHAPE = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomEnd = 0.dp, bottomStart = 0.dp)
internal val ITEM_PADDING = PaddingValues(horizontal = 24.dp, vertical = 16.dp)

/** Side padding of every zone in the long-press sheets, matching [ITEM_PADDING]. */
internal val MENU_SIDE_PADDING = 24.dp

private val BASE_HEADER_ICON_SIZE = 24.dp

/** M3's minimum touch target. */
private val MIN_TAP_TARGET = 48.dp

/**
 * F12's long-press context menu (`4i`) — a bottom sheet rather than a floating popup (decided
 * explicitly by the user over [ThemedDropdownMenu], see chat history). Three zones, separated by
 * hairlines: a header (the app's icon and name, with App info and Uninstall as icon buttons), the
 * app's own notifications (only when it has some, or when notification access is missing), and the
 * placement actions — Favorites, Dock and Folder as tiles, or as rows once the tiles can't hold
 * their text (see [PlacementActions]). The app's own quick-action shortcuts are not shown.
 *
 * The Favorites/Dock actions come from [com.facetlauncher.app.domain.ObserveQuickAddStateUseCase]
 * via [onRequestQuickAddState]. A `null` action hides its tile entirely (not a member, and that
 * list is already at its cap). A tile whose action is "Remove" is filled, and each carries a pill
 * naming which list it targets — "Global" for the launcher-wide default, the active facet's own
 * real name when it's overriding (decided explicitly by the user over folding that distinction
 * into the sentence itself, e.g. "Add to facet favorites" — see chat history).
 *
 * "Add to folder" ([folderCandidates] non-null) is the third tile — tapping it doesn't close this
 * sheet, it slides its content over to a second page (same `AnimatedContent`
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
    modifier: Modifier = Modifier,
    appMenuNotifications: AppMenuNotifications = AppMenuNotifications.None,
    onAppInfo: (AppInfo) -> Unit = {},
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
    var quickAddState by remember(app.packageName, app.activityName) { mutableStateOf(QuickAddState()) }
    var addingToFolder by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var previewingFolder by remember { mutableStateOf<Folder?>(null) }

    LaunchedEffect(expanded, app) {
        if (expanded) quickAddState = onRequestQuickAddState(app)
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

    if (!expanded) return

    val notificationsFlow = remember(app.packageName, app.userHandle, appMenuNotifications) { appMenuNotifications.observe(app) }
    val notificationsState by notificationsFlow.collectAsStateWithLifecycle(initialValue = AppNotificationsState(enabled = false))

    val favoritesName = stringResource(R.string.quick_placement_favorites)
    val dockName = stringResource(R.string.quick_placement_dock)
    val placementActions = buildList {
        quickAddState.favoritesAction?.let { action ->
            add(
                PlacementActionUi(
                    tag = "app_context_menu_add_to_favorites",
                    icon = Icons.Outlined.StarBorder,
                    label = quickPlacementLabel(action, favoritesName),
                    scope = PlacementScope(action.facetName),
                    isRemove = action is QuickPlacementAction.Remove,
                    onClick = {
                        onDismissRequest()
                        onFavoritesAction(app, action)
                    },
                ),
            )
        }
        quickAddState.dockAction?.let { action ->
            add(
                PlacementActionUi(
                    tag = "app_context_menu_add_to_dock",
                    icon = Icons.Outlined.Dock,
                    label = quickPlacementLabel(action, dockName),
                    scope = PlacementScope(action.facetName),
                    isRemove = action is QuickPlacementAction.Remove,
                    onClick = {
                        onDismissRequest()
                        onDockAction(app, action)
                    },
                ),
            )
        }
        if (folderCandidates != null) {
            add(
                PlacementActionUi(
                    tag = "app_context_menu_add_to_folder",
                    icon = Icons.Outlined.FolderIcon,
                    label = stringResource(R.string.folder_add_to_folder),
                    scope = null,
                    isRemove = false,
                    onClick = { addingToFolder = true },
                ),
            )
        }
        if (removeFromFolderId != null) {
            add(
                PlacementActionUi(
                    tag = "app_context_menu_remove_from_folder",
                    icon = Icons.Outlined.FolderIcon,
                    label = stringResource(R.string.folder_remove_from_folder),
                    scope = null,
                    isRemove = true,
                    onClick = {
                        onDismissRequest()
                        onRemoveFromFolder(app, removeFromFolderId)
                    },
                ),
            )
        }
    }

    ThemedModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier.testTag("app_context_menu"),
        skipPartiallyExpanded = true,
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
                AppMenuMainPage(
                    app = app,
                    notifications = notificationsState,
                    placementActions = placementActions,
                    onAppInfo = {
                        onDismissRequest()
                        onAppInfo(app)
                    },
                    onUninstall = {
                        onDismissRequest()
                        // Unlike App Info (LauncherApps.startAppDetailsActivity, see
                        // AppRepository.openAppDetails), there's no public per-profile-targeted
                        // uninstall API for a third-party launcher — the OS resolves this by
                        // ambient calling context, same as every other launcher. A genuine
                        // platform limitation, not an oversight.
                        val intent = Intent(Intent.ACTION_DELETE, Uri.fromParts("package", app.packageName, null))
                        runCatching { context.startActivity(intent) }
                    },
                    onOpenNotification = { notification ->
                        onDismissRequest()
                        appMenuNotifications.onOpen(notification)
                    },
                    onDismissNotifications = appMenuNotifications.onDismiss,
                    onEnableNotificationAccess = {
                        onDismissRequest()
                        appMenuNotifications.onEnableAccess()
                    },
                )
            } else {
                AppMenuFolderPage(
                    onBack = { addingToFolder = false },
                    onCreateFolder = { showCreateFolderDialog = true },
                ) {
                    folderCandidates.orEmpty().forEach { folder ->
                        FolderCandidateRow(folder = folder, onClick = { previewingFolder = folder })
                    }
                }
            }
        }
    }

    if (showCreateFolderDialog) {
        RenameDialog(
            title = stringResource(R.string.folder_new_folder_title),
            explanation = stringResource(R.string.folder_name_this_folder),
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

/** The sheet's main page: header, then the notifications zone and the placement actions, each behind a hairline and each left out when empty. */
@Composable
internal fun AppMenuMainPage(
    app: AppInfo,
    notifications: AppNotificationsState,
    placementActions: List<PlacementActionUi>,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit,
    onOpenNotification: (NotificationInfo) -> Unit,
    onDismissNotifications: (List<String>) -> Boolean,
    onEnableNotificationAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Scrolls when the zones don't fit, as at the largest text sizes on a small screen.
    Column(modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = MENU_SIDE_PADDING, end = MENU_SIDE_PADDING - 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(
                icon = app.icon,
                size = AppIconSize.ROW_SPACIOUS,
                contentDescription = null,
                isWorkApp = app.profile == AppProfile.WORK,
                isOtherProfileApp = app.profile == AppProfile.OTHER,
            )
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp).testTag("app_context_menu_name"),
            )
            MenuIconButton(
                icon = Icons.Outlined.Info,
                contentDescription = stringResource(R.string.app_context_menu_app_info),
                tint = Muted,
                onClick = onAppInfo,
                modifier = Modifier.testTag("app_context_menu_app_info"),
            )
            MenuIconButton(
                icon = Icons.Outlined.DeleteOutline,
                contentDescription = stringResource(R.string.app_context_menu_uninstall),
                tint = ErrorColor,
                onClick = onUninstall,
                modifier = Modifier.testTag("app_context_menu_uninstall"),
            )
        }
        if (notifications.hasZone) {
            CardDivider()
            AppNotificationsSection(
                state = notifications,
                onOpen = onOpenNotification,
                onDismiss = onDismissNotifications,
                onEnableAccess = onEnableNotificationAccess,
                modifier = Modifier.padding(horizontal = MENU_SIDE_PADDING, vertical = 8.dp),
            )
        }
        if (placementActions.isNotEmpty()) {
            CardDivider()
            PlacementActions(
                actions = placementActions,
                modifier = Modifier.padding(horizontal = MENU_SIDE_PADDING, vertical = 16.dp),
            )
        }
    }
}

/** A header action: a 48dp tap target around an icon that grows with text up to [MAX_CHROME_ICON_SCALE]. */
@Composable
internal fun MenuIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(MIN_TAP_TARGET),
        colors = IconButtonDefaults.iconButtonColors(contentColor = tint),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(BASE_HEADER_ICON_SIZE * rememberChromeIconScale()),
        )
    }
}

@Composable
internal fun FolderCandidateRow(folder: Folder, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
            Text(
                text = pluralStringResource(R.plurals.folder_app_count, folder.apps.size, folder.apps.size),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
        }
    }
}

/** Shared label wording for a Favorites/Dock quick-placement action — used by both [AppContextMenu] and [FolderTileContextMenu]. The label never names a list target; the tile's pill does. */
@Composable
internal fun quickPlacementLabel(action: QuickPlacementAction, listName: String): String {
    val templateRes = when (action) {
        is QuickPlacementAction.Add -> R.string.quick_placement_add_to
        is QuickPlacementAction.Remove -> R.string.quick_placement_remove_from
    }
    return stringResource(templateRes, listName)
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

private val previewApp = AppInfo(packageName = "com.example.chat", activityName = ".Main", label = "Messages", icon = null)

@Composable
private fun previewNotifications(): List<NotificationInfo> = listOf(
    stringResource(R.string.quick_placement_favorites),
    stringResource(R.string.quick_placement_dock),
).mapIndexed { index, title ->
    NotificationInfo(
        packageName = "com.example.chat",
        silent = false,
        key = "k$index",
        title = title,
        text = stringResource(R.string.app_menu_notifications_empty),
        postTime = System.currentTimeMillis() - index * 120_000L,
    )
}

@Composable
private fun previewPlacementActions() = listOf(
    PlacementActionUi("p_fav", Icons.Outlined.StarBorder, "Add to Favorites", PlacementScope(null), isRemove = false, onClick = {}),
    PlacementActionUi("p_dock", Icons.Outlined.Dock, "Remove from Dock", PlacementScope("Work"), isRemove = true, onClick = {}),
    PlacementActionUi("p_folder", Icons.Outlined.FolderIcon, stringResource(R.string.folder_add_to_folder), scope = null, isRemove = false, onClick = {}),
)

@Preview(showBackground = true, widthDp = 390)
@Preview(name = "Dark", showBackground = true, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppMenuMainPageWithNotificationsPreview() {
    FacetLauncherTheme {
        AppMenuMainPage(
            app = previewApp,
            notifications = AppNotificationsState(previewNotifications()),
            placementActions = previewPlacementActions(),
            onAppInfo = {},
            onUninstall = {},
            onOpenNotification = {},
            onDismissNotifications = { true },
            onEnableNotificationAccess = {},
        )
    }
}

@Preview(name = "No notifications", showBackground = true, widthDp = 390)
@Composable
private fun AppMenuMainPageEmptyPreview() {
    FacetLauncherTheme {
        AppMenuMainPage(
            app = previewApp,
            notifications = AppNotificationsState(),
            placementActions = previewPlacementActions(),
            onAppInfo = {},
            onUninstall = {},
            onOpenNotification = {},
            onDismissNotifications = { true },
            onEnableNotificationAccess = {},
        )
    }
}
