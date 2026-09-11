package com.lumenlauncher.app.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dock
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppShortcut
import com.lumenlauncher.app.ui.launcher.LocalHomePressedEvent
import com.lumenlauncher.app.ui.theme.ErrorColor
import com.lumenlauncher.app.ui.theme.Faint
import com.lumenlauncher.app.ui.theme.Hairline
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Scrim
import com.lumenlauncher.app.ui.theme.Surface

/** At most this many of an app's own [AppShortcut]s are shown — a long-press sheet is a quick
 * action, not another drawer, so shortcuts beyond this many are simply dropped. */
private const val MAX_SHORTCUTS = 3

/** M3's own modal-bottom-sheet corner treatment — top corners only, [MaterialTheme.shapes.extraLarge]'s
 * 28dp (see `CLAUDE.md`'s Material 3 shape section) — written as a literal [RoundedCornerShape]
 * since [MaterialTheme.shapes] has no per-corner variant to reference directly. */
private val SHEET_SHAPE = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomEnd = 0.dp, bottomStart = 0.dp)
private val ITEM_PADDING = PaddingValues(horizontal = 20.dp, vertical = 16.dp)

/**
 * F12's long-press context menu (`4i`) — a bottom sheet rather than a floating popup (decided
 * explicitly by the user over [ThemedDropdownMenu], see chat history). The header (app icon +
 * name) sits at the top with "App info" as a row directly beneath it — grouped with the header
 * (no divider between them) rather than mixed in among the app's own quick actions
 * ([AppShortcut]s, capped at [MAX_SHORTCUTS]) below; Uninstall is always the last item. Widgets is
 * deliberately omitted (Phase 7 isn't built yet). [onRequestShortcuts] is fetched fresh each time
 * the sheet opens rather than precomputed for every app up front, since most long-presses never
 * open it.
 *
 * "Add to Favorites"/"Add to Dock" (from [com.lumenlauncher.app.domain.ObserveQuickAddStateUseCase])
 * sit between "App info" and the shortcuts — [addToFavoritesOverride]/[addToDockOverride] being
 * `null` hides the corresponding row entirely (that list is already at its cap); non-null selects
 * between "Add to Favorites"/"Add to Dock" (the launcher-wide default) and "Add to profile
 * favorites"/"Add to profile dock" (the active profile is overriding its own).
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
    addToFavoritesOverride: Boolean? = null,
    onAddToFavorites: (AppInfo) -> Unit = {},
    addToDockOverride: Boolean? = null,
    onAddToDock: (AppInfo) -> Unit = {},
) {
    val context = LocalContext.current
    var shortcuts by remember(app.packageName, app.activityName) { mutableStateOf<List<AppShortcut>>(emptyList()) }

    LaunchedEffect(expanded, app) {
        if (expanded) shortcuts = onRequestShortcuts(app)
    }

    // System back always dismisses. Physical/gesture Home doesn't reach BackHandler at all (this
    // app IS the launcher, so it gets LauncherActivity.onNewIntent instead of a back-press) — see
    // LocalHomePressedEvent's own doc for why this component listens for that event directly
    // rather than relying on whatever closed the Drawer/Hub to cascade into it.
    BackHandler(enabled = expanded, onBack = onDismissRequest)
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(icon = app.icon, size = AppIconSize.ROW_SPACIOUS, contentDescription = null)
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        AppContextMenuItem(
            label = "App info",
            modifier = Modifier.testTag("app_context_menu_app_info"),
            leadingIcon = { Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Muted) },
            onClick = {
                onDismissRequest()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", app.packageName, null))
                runCatching { context.startActivity(intent) }
            },
        )
        HorizontalDivider(color = Hairline)
        if (addToFavoritesOverride != null) {
            AppContextMenuItem(
                label = if (addToFavoritesOverride) "Add to profile favorites" else "Add to Favorites",
                modifier = Modifier.testTag("app_context_menu_add_to_favorites"),
                leadingIcon = { Icon(imageVector = Icons.Default.StarBorder, contentDescription = null, tint = Muted) },
                onClick = {
                    onDismissRequest()
                    onAddToFavorites(app)
                },
            )
            HorizontalDivider(color = Hairline)
        }
        if (addToDockOverride != null) {
            AppContextMenuItem(
                label = if (addToDockOverride) "Add to profile dock" else "Add to Dock",
                modifier = Modifier.testTag("app_context_menu_add_to_dock"),
                leadingIcon = { Icon(imageVector = Icons.Default.Dock, contentDescription = null, tint = Muted) },
                onClick = {
                    onDismissRequest()
                    onAddToDock(app)
                },
            )
            HorizontalDivider(color = Hairline)
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
            HorizontalDivider(color = Hairline)
        }
        AppContextMenuItem(
            label = "Uninstall",
            destructive = true,
            modifier = Modifier.testTag("app_context_menu_uninstall"),
            leadingIcon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = ErrorColor) },
            onClick = {
                onDismissRequest()
                val intent = Intent(Intent.ACTION_DELETE, Uri.fromParts("package", app.packageName, null))
                runCatching { context.startActivity(intent) }
            },
        )
    }
}

@Composable
private fun AppContextMenuItem(
    label: String,
    onClick: () -> Unit,
    leadingIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(ITEM_PADDING),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        leadingIcon()
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = if (destructive) ErrorColor else Ink)
    }
}

/** Mirrors [com.lumenlauncher.app.ui.onboarding.SetDefaultLauncherSheet]'s own hand-rolled drag
 * handle (34x4dp, [Faint], 2dp corners) rather than M3's [androidx.compose.material3.BottomSheetDefaults.DragHandle]
 * default, whose tint resolves to an unmapped color-scheme slot. */
@Composable
private fun AppContextMenuDragHandle() {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(width = 34.dp, height = 4.dp).background(Faint, RoundedCornerShape(2.dp)))
    }
}
