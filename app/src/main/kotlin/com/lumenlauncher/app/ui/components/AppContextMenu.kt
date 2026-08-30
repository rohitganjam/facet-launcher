package com.lumenlauncher.app.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppShortcut
import com.lumenlauncher.app.ui.theme.ErrorColor
import com.lumenlauncher.app.ui.theme.Hairline
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.Muted

/** This menu's own floating-card shape — rounder than [ThemedDropdownMenu]'s compact-picker
 * default, matching [SettingsCard]'s own shape ([MaterialTheme.shapes.medium], 12dp — M3's real
 * Card default, see `CLAUDE.md`'s Material 3 shape section) rather than a hand-picked radius: a
 * long-press context card reads as its own floating card surface, and this is the closest already-
 * accepted card treatment in the app to match against, since a long-press menu has no exact M3
 * component counterpart of its own. */
private val CONTEXT_MENU_SHAPE @Composable get() = MaterialTheme.shapes.medium
private val CONTEXT_MENU_ITEM_PADDING = PaddingValues(horizontal = 16.dp, vertical = 14.dp)

/**
 * F12's long-press context menu (`4i`) — App info / Uninstall, plus the app's own quick actions
 * ([AppShortcut]s) when it publishes any. Widgets is deliberately omitted (Phase 7 isn't built
 * yet). [onRequestShortcuts] is fetched fresh each time the menu opens rather than precomputed
 * for every app up front, since most long-presses never open it.
 */
@Composable
fun AppContextMenu(
    app: AppInfo,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var shortcuts by remember(app.packageName, app.activityName) { mutableStateOf<List<AppShortcut>>(emptyList()) }

    LaunchedEffect(expanded, app) {
        if (expanded) shortcuts = onRequestShortcuts(app)
    }

    ThemedDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.testTag("app_context_menu"),
        shape = CONTEXT_MENU_SHAPE,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(icon = app.icon, size = 40.dp, cornerRadius = 12.dp, contentDescription = null)
            Text(text = app.label, style = MaterialTheme.typography.titleMedium, color = Ink, modifier = Modifier.padding(start = 12.dp))
        }
        HorizontalDivider(color = Hairline)
        ThemedDropdownMenuItem(
            label = "App info",
            modifier = Modifier.testTag("app_context_menu_app_info"),
            leadingIcon = { Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Muted) },
            contentPadding = CONTEXT_MENU_ITEM_PADDING,
            onClick = {
                onDismissRequest()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", app.packageName, null))
                runCatching { context.startActivity(intent) }
            },
        )
        HorizontalDivider(color = Hairline)
        ThemedDropdownMenuItem(
            label = "Uninstall",
            destructive = true,
            modifier = Modifier.testTag("app_context_menu_uninstall"),
            leadingIcon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = ErrorColor) },
            contentPadding = CONTEXT_MENU_ITEM_PADDING,
            onClick = {
                onDismissRequest()
                val intent = Intent(Intent.ACTION_DELETE, Uri.fromParts("package", app.packageName, null))
                runCatching { context.startActivity(intent) }
            },
        )
        shortcuts.forEach { shortcut ->
            HorizontalDivider(color = Hairline)
            ThemedDropdownMenuItem(
                label = shortcut.label,
                modifier = Modifier.testTag("app_context_menu_shortcut_${shortcut.id}"),
                leadingIcon = { AppIcon(icon = shortcut.icon, size = 20.dp, cornerRadius = 6.dp, contentDescription = null) },
                contentPadding = CONTEXT_MENU_ITEM_PADDING,
                onClick = {
                    onDismissRequest()
                    onLaunchShortcut(shortcut)
                },
            )
        }
    }
}
