package com.facetlauncher.app.ui.appicon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.IconOverride
import com.facetlauncher.app.data.model.IconOverrideSource
import com.facetlauncher.app.data.model.ThemeMode
import com.facetlauncher.app.domain.CustomIconRow
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.ConfirmDialog
import com.facetlauncher.app.ui.components.SettingsSectionHeader
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.settings.AppearanceSettingsHeader
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer
import kotlinx.collections.immutable.persistentListOf

private val CARD_CORNER = 28.dp

/** Settings → Appearance → Icons → Custom icons: every app with a custom icon, each with Reset, and Reset all. */
@Composable
fun CustomIconsScreen(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: CustomIconsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CustomIconsContent(uiState = uiState, onBack = onBack, onReset = viewModel::reset, onResetAll = viewModel::resetAll, modifier = modifier)
}

@Composable
internal fun CustomIconsContent(
    uiState: CustomIconsUiState,
    onBack: () -> Unit,
    onReset: (CustomIconRow) -> Unit,
    onResetAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmingResetAll by rememberSaveable { mutableStateOf(false) }
    StickyHeaderLayout(
        modifier = modifier,
        header = { AppearanceSettingsHeader(title = stringResource(R.string.custom_icons_title), onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("custom_icons_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight, bottom = 24.dp),
            ) {
                val rows = uiState.rows
                if (rows.isEmpty() && !uiState.isLoading) {
                    item {
                        Text(
                            text = stringResource(R.string.custom_icons_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted,
                            modifier = Modifier.padding(vertical = 16.dp).testTag("custom_icons_empty"),
                        )
                    }
                }
                if (rows.isNotEmpty()) {
                    item { SettingsSectionHeader(pluralStringResource(R.plurals.custom_icons_app_count, rows.size, rows.size)) }
                    itemsIndexed(rows, key = { _, row -> row.app.listKey }) { index, row ->
                        CustomIconRowItem(row = row, isFirst = index == 0, isLast = index == rows.lastIndex, onReset = { onReset(row) })
                    }
                    item {
                        TextActionButton(
                            text = stringResource(R.string.custom_icons_reset_all),
                            onClick = { confirmingResetAll = true },
                            modifier = Modifier.padding(top = 8.dp).testTag("custom_icons_reset_all"),
                        )
                    }
                }
            }
        },
    )
    if (confirmingResetAll) {
        ConfirmDialog(
            title = stringResource(R.string.custom_icons_reset_all_title),
            message = stringResource(R.string.custom_icons_reset_all_message),
            confirmLabel = stringResource(R.string.custom_icons_reset_all_confirm),
            onConfirm = {
                confirmingResetAll = false
                onResetAll()
            },
            onDismiss = { confirmingResetAll = false },
        )
    }
}

/** One app's row: its icon as it shows now, its name, where the icon comes from, and Reset. Rows join into one card. */
@Composable
private fun CustomIconRowItem(row: CustomIconRow, isFirst: Boolean, isLast: Boolean, onReset: () -> Unit) {
    val shape = RoundedCornerShape(
        topStart = if (isFirst) CARD_CORNER else 0.dp,
        topEnd = if (isFirst) CARD_CORNER else 0.dp,
        bottomEnd = if (isLast) CARD_CORNER else 0.dp,
        bottomStart = if (isLast) CARD_CORNER else 0.dp,
    )
    val resetLabel = stringResource(R.string.custom_icons_reset_app, row.app.label)
    Column(modifier = Modifier.fillMaxWidth().clip(shape).background(Surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp).testTag("custom_icons_row_${row.app.packageName}"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppIcon(app = row.app, size = AppIconSize.COMPACT, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = row.app.label, style = MaterialTheme.typography.bodyLarge, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = row.packLabel ?: stringResource(R.string.change_icon_source_own),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TextActionButton(
                text = stringResource(R.string.custom_icons_reset),
                onClick = onReset,
                modifier = Modifier.semantics { contentDescription = resetLabel }.testTag("custom_icons_reset_${row.app.packageName}"),
            )
        }
        if (!isLast) CardDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
}

private fun previewRow(pkg: String, label: String, packLabel: String?) = CustomIconRow(
    app = AppInfo(packageName = pkg, activityName = "Main", label = label, icon = null),
    override = IconOverride(pkg, "Main", 0, AppProfile.PERSONAL, if (packLabel == null) IconOverrideSource.OWN else IconOverrideSource.PACK),
    packLabel = packLabel,
)

@Preview(name = "Custom icons — light", showBackground = true)
@Composable
private fun CustomIconsPreview() {
    FacetLauncherTheme(themeMode = ThemeMode.LIGHT) {
        CustomIconsContent(
            uiState = CustomIconsUiState(persistentListOf(previewRow("a", "Spotify", "Arcticons"), previewRow("b", "Chrome", null)), isLoading = false),
            onBack = {},
            onReset = {},
            onResetAll = {},
        )
    }
}

@Preview(name = "Custom icons — dark, empty", showBackground = true)
@Composable
private fun CustomIconsEmptyPreview() {
    FacetLauncherTheme(themeMode = ThemeMode.DARK) {
        CustomIconsContent(uiState = CustomIconsUiState(isLoading = false), onBack = {}, onReset = {}, onResetAll = {})
    }
}
