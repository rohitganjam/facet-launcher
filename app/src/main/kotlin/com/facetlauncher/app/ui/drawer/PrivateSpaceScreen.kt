package com.facetlauncher.app.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.PrivateSpaceState
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.RecentlyInstalledPosition
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.FolderContentsSheet
import com.facetlauncher.app.ui.components.FolderSheetHeaderAction
import com.facetlauncher.app.ui.components.RecencyIcon
import com.facetlauncher.app.ui.theme.PrivateSpaceTheme

/**
 * Private Space's own drawer — browse + launch + search only (v1 scope, see the implementation
 * plan). Deliberately not [AppDrawerScreen]: no folders/favorites/dock/shortcuts/contacts, and its
 * search is scoped only to [apps] (already Private-Space-only by the time it reaches here), never
 * merged with the main Drawer's search. Wraps its own root in [PrivateSpaceTheme] — a distinct,
 * fixed-dark "incognito" look independent of the app's own light/dark setting.
 */
@Composable
fun PrivateSpaceScreen(
    state: PrivateSpaceState,
    apps: List<AppInfo>,
    query: String,
    onQueryChange: (String) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
    /** Its own copy of the main Drawer's "Recently installed" category — same global setting, computed independently over Private Space's own [apps] (see [PrivateSpaceViewModel.recentlyInstalledApps]'s own doc). Already empty while [query] is non-blank. */
    recentlyInstalledApps: List<AppInfo> = emptyList(),
    recentlyInstalledPosition: RecentlyInstalledPosition = RecentlyInstalledPosition.SHOW_FIRST,
) {
    PrivateSpaceTheme {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.systemBars)
                .testTag("private_space_screen"),
        ) {
            PrivateSpaceSearchBar(
                query = query,
                onQueryChange = onQueryChange,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
            when {
                state is PrivateSpaceState.Locked -> PrivateSpaceMessage(
                    text = stringResource(R.string.private_space_locked),
                    modifier = Modifier.testTag("private_space_locked_message"),
                )
                apps.isEmpty() -> PrivateSpaceMessage(
                    text = stringResource(if (query.isBlank()) R.string.private_space_no_apps else R.string.private_space_no_matches),
                    modifier = Modifier.testTag("private_space_empty_message"),
                )
                else -> LazyColumn {
                    if (recentlyInstalledApps.isNotEmpty() && recentlyInstalledPosition == RecentlyInstalledPosition.SHOW_FIRST) {
                        item(key = "header_recently_installed") { PrivateSpaceRecentlyInstalledHeader() }
                        item(key = "recently_installed") {
                            PrivateSpaceRecentlyInstalledRow(apps = recentlyInstalledApps, onAppClick = onAppClick)
                        }
                    }
                    items(apps, key = { it.packageName + it.activityName }) { app ->
                        PrivateSpaceAppRow(app = app, onClick = { onAppClick(app) })
                    }
                    if (recentlyInstalledApps.isNotEmpty() && recentlyInstalledPosition == RecentlyInstalledPosition.SHOW_LAST) {
                        item(key = "header_recently_installed") { PrivateSpaceRecentlyInstalledHeader() }
                        item(key = "recently_installed") {
                            PrivateSpaceRecentlyInstalledRow(apps = recentlyInstalledApps, onAppClick = onAppClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivateSpaceSearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 14.dp),
        )
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(stringResource(R.string.private_space_search)) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0f),
                focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0f),
            ),
            modifier = Modifier.fillMaxWidth().padding(start = 40.dp).testTag("private_space_search_field"),
        )
    }
}

@Composable
private fun PrivateSpaceAppRow(app: AppInfo, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .testTag("private_space_app_row_${app.packageName}")
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AppIcon(icon = app.icon, size = AppIconSize.ROW_REGULAR, contentDescription = null)
        Text(text = app.label, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** [PrivateSpaceRecentlyInstalledRow]'s own icon-only section header — no label, same `onSurfaceVariant` tint as the row's own icon. */
@Composable
private fun PrivateSpaceRecentlyInstalledHeader(modifier: Modifier = Modifier) {
    Icon(
        imageVector = RecencyIcon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .size(16.dp)
            .testTag("header_recently_installed"),
    )
}

/**
 * Private Space's own "Recently installed" category — same virtual-[Folder]-via-[FolderContentsSheet]
 * reuse as the main Drawer's [com.facetlauncher.app.ui.drawer.AppDrawerScreen] version, styled
 * through [PrivateSpaceAppRow]'s own `MaterialTheme.colorScheme.*` tokens (Private Space's fixed
 * dark "incognito" look, independent of the rest of the app's theme) rather than this app's usual
 * `Accent`/`Ink`. `onRequestShortcuts`/`onLaunchShortcut`/`onAppInfo` stay at [FolderContentsSheet]'s
 * own no-op defaults — [PrivateSpaceScreen] doesn't wire those up for its own rows either.
 */
@Composable
private fun PrivateSpaceRecentlyInstalledRow(apps: List<AppInfo>, onAppClick: (AppInfo) -> Unit, modifier: Modifier = Modifier) {
    var sheetOpen by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = { sheetOpen = true })
                .testTag("private_space_recently_installed_row")
                .padding(horizontal = 24.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(imageVector = RecencyIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = stringResource(R.string.drawer_recently_installed_label), color = MaterialTheme.colorScheme.onSurface)
        }
        if (sheetOpen) {
            FolderContentsSheet(
                folder = Folder(id = -1L, name = stringResource(R.string.drawer_recently_installed_label), apps = apps),
                onDismissRequest = { sheetOpen = false },
                onAppClick = onAppClick,
                presentation = DrawerPresentation.LIST,
                onRemoveFromFolder = { _, _ -> },
                headerAction = FolderSheetHeaderAction.None,
                showRecencyBadge = true,
            )
        }
    }
}

@Composable
private fun PrivateSpaceMessage(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text = text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
