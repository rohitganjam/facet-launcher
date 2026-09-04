package com.lumenlauncher.app.ui.profiles

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.ui.components.AppIcon
import com.lumenlauncher.app.ui.components.BackButton
import com.lumenlauncher.app.ui.components.StickyHeaderLayout
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Surface

/**
 * Favorites picker (`4j`): full-screen, own search, checkbox rows. Favorited apps show first,
 * ordered however this profile's favorites stood when this screen was opened; that split and
 * order are frozen for the rest of this visit — checking/unchecking apps never reshuffles either
 * section, only a fresh open of this screen does. Removal only ever happens by unchecking a row
 * here; drag-reordering favorites themselves lives on this profile's own Settings page, not here.
 */
@Composable
fun FavoritesPickerScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesPickerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FavoritesPickerContent(
        uiState = uiState,
        onQueryChanged = viewModel::onQueryChanged,
        onToggleApp = viewModel::toggleFavorite,
        onDone = onDone,
        modifier = modifier,
    )
}

@Composable
private fun FavoritesPickerContent(
    uiState: FavoritesPickerUiState,
    onQueryChanged: (String) -> Unit,
    onToggleApp: (AppInfo) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StickyHeaderLayout(
        modifier = modifier
            .fillMaxSize()
            .background(Surface)
            .testTag("favorites_picker_screen"),
        header = {
            FavoritesPickerHeader(
                title = if (uiState.isProfileScoped) "Favorites" else "Default favorites",
                onDone = onDone,
            )
        },
        content = { headerHeight ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = headerHeight),
        ) {
            item {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = onQueryChanged,
                    placeholder = { Text("Search apps") },
                    modifier = Modifier.fillMaxWidth().testTag("favorites_picker_search").padding(bottom = 12.dp),
                    singleLine = true,
                )
            }

            if (uiState.selectedResults.isNotEmpty()) {
                item { PickerSectionHeader("FAVORITES") }
                items(uiState.selectedResults, key = { it.packageName + it.activityName }) { app ->
                    val isFavorite = (app.packageName to app.activityName) in uiState.favoriteComponents
                    FavoritesPickerRow(
                        app = app,
                        checked = isFavorite,
                        enabled = isFavorite || uiState.canAddMore,
                        onToggle = { onToggleApp(app) },
                    )
                }
            }
            if (uiState.otherResults.isNotEmpty()) {
                item { PickerSectionHeader("ALL APPS") }
                items(uiState.otherResults, key = { it.packageName + it.activityName }) { app ->
                    val isFavorite = (app.packageName to app.activityName) in uiState.favoriteComponents
                    FavoritesPickerRow(
                        app = app,
                        checked = isFavorite,
                        enabled = isFavorite || uiState.canAddMore,
                        onToggle = { onToggleApp(app) },
                    )
                }
            }
        }
        },
    )
}

@Composable
private fun FavoritesPickerHeader(title: String, onDone: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BackButton(onClick = onDone)
            Text(text = title, style = MaterialTheme.typography.headlineSmall, color = Ink)
        }
        Text(
            text = "Done",
            style = MaterialTheme.typography.bodyLarge,
            color = Ink,
            modifier = Modifier.testTag("favorites_picker_done").clickable(onClick = onDone),
        )
    }
}

@Composable
private fun PickerSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = Muted,
        modifier = modifier.padding(top = 12.dp, bottom = 6.dp),
    )
}

@Composable
private fun FavoritesPickerRow(app: AppInfo, checked: Boolean, enabled: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.4f)
            .clickable(enabled = enabled, onClick = onToggle)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AppIcon(icon = app.icon, size = 32.dp, cornerRadius = 9.dp, contentDescription = null)
        Text(text = app.label, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            enabled = enabled,
            modifier = Modifier.testTag("favorites_picker_row_${app.packageName}"),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FavoritesPickerScreenPreview() {
    LumenLauncherTheme {
        FavoritesPickerContent(
            uiState = FavoritesPickerUiState(
                selectedResults = (1..3).map { AppInfo("com.example.$it", ".Main", "App $it", null) },
                otherResults = (4..8).map { AppInfo("com.example.$it", ".Main", "App $it", null) },
            ),
            onQueryChanged = {},
            onToggleApp = {},
            onDone = {},
        )
    }
}
