package com.facetlauncher.app.ui.settings.backup

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.ConfirmDialog
import com.facetlauncher.app.ui.components.LocalSettingsRowInset
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsIconBadge
import com.facetlauncher.app.ui.components.SettingsIconBadgeInset
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.ErrorColor
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SettingsSectionHue
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * F14 Backup & Restore (`Settings → Backup & restore`). Export writes a JSON snapshot to a
 * user-picked destination (system document picker, `CreateDocument` — never a guessed path);
 * import is a destructive full replace, confirmed via [ConfirmDialog] *before* the file picker
 * ever launches. A successful import with widgets the backup couldn't rebind automatically shows
 * a guided re-add list right on this same screen — see [BackupRestoreViewModel]'s own doc comment
 * for why that isn't a separate destination.
 */
@Composable
fun BackupRestoreScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BackupRestoreViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showImportConfirm by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        viewModel.onExportDestinationChosen(uri)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        viewModel.onImportFileChosen(uri)
    }
    val configureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        viewModel.onConfigureResult(result.resultCode == Activity.RESULT_OK)
    }
    val bindPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        viewModel.onBindResult(result.resultCode == Activity.RESULT_OK)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is BackupRestoreEvent.LaunchBindPermission -> bindPermissionLauncher.launch(event.intent)
                is BackupRestoreEvent.LaunchConfigure -> configureLauncher.launch(IntentSenderRequest.Builder(event.intentSender).build())
            }
        }
    }

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            delay(4000)
            viewModel.dismissMessage()
        }
    }

    if (showImportConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.backup_restore_confirm_title),
            message = stringResource(R.string.backup_restore_confirm_message),
            confirmLabel = stringResource(R.string.backup_restore_confirm_replace),
            onConfirm = {
                showImportConfirm = false
                importLauncher.launch(arrayOf("application/json"))
            },
            onDismiss = { showImportConfirm = false },
        )
    }

    BackupRestoreContent(
        uiState = uiState,
        onBack = onBack,
        onExportClick = {
            val timestamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            exportLauncher.launch("facet-backup-$timestamp.json")
        },
        onImportClick = { showImportConfirm = true },
        onReimportWidget = viewModel::onReimportWidget,
        onSkipWidget = viewModel::onSkipWidget,
        modifier = modifier,
    )
}

@Composable
private fun BackupRestoreContent(
    uiState: BackupRestoreUiState,
    onBack: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onReimportWidget: (Int) -> Unit,
    onSkipWidget: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionablePending = uiState.pendingWidgets.withIndex().filter { !it.value.done }

    StickyHeaderLayout(
        modifier = modifier,
        header = { BackupRestoreHeader(onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("backup_restore_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight, bottom = 24.dp),
            ) {
                uiState.message?.let { message ->
                    item { MessageBanner(message) }
                }

                item {
                    SettingsCard(fullBleedRows = true) {
                        RowScaffold(
                            icon = Icons.Outlined.FileUpload,
                            title = stringResource(R.string.backup_restore_export_title),
                            subtitle = stringResource(R.string.backup_restore_export_subtitle),
                            onClick = onExportClick,
                            enabled = !uiState.isBusy,
                            testTag = "export_backup_row",
                        )
                        CardDivider(startInset = SettingsIconBadgeInset)
                        RowScaffold(
                            icon = Icons.Outlined.FileDownload,
                            title = stringResource(R.string.backup_restore_import_title),
                            subtitle = stringResource(R.string.backup_restore_import_subtitle),
                            onClick = onImportClick,
                            enabled = !uiState.isBusy,
                            testTag = "import_backup_row",
                        )
                    }
                }

                if (uiState.isBusy) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator(color = Accent, modifier = Modifier.testTag("backup_restore_progress"))
                        }
                    }
                }

                if (actionablePending.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.backup_restore_widgets_to_readd_header),
                            style = MaterialTheme.typography.labelMedium,
                            color = Muted,
                            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                        )
                    }
                    item {
                        Text(
                            text = stringResource(R.string.backup_restore_widgets_to_readd_explanation),
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    item {
                        SettingsCard {
                            actionablePending.forEachIndexed { position, (index, widget) ->
                                PendingWidgetRow(
                                    widget = widget,
                                    onReadd = { onReimportWidget(index) },
                                    onSkip = { onSkipWidget(index) },
                                )
                                if (position != actionablePending.lastIndex) CardDivider()
                            }
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun MessageBanner(message: BackupRestoreMessage, modifier: Modifier = Modifier) {
    val (text, isError) = when (message) {
        BackupRestoreMessage.ExportSucceeded -> stringResource(R.string.backup_restore_export_succeeded) to false
        BackupRestoreMessage.ExportFailed -> stringResource(R.string.backup_restore_export_failed) to true
        is BackupRestoreMessage.ImportSucceeded -> {
            val facetsPart = pluralStringResource(R.plurals.backup_restored_facets_count, message.facetCount, message.facetCount)
            val text = if (message.pendingWidgetCount > 0) {
                stringResource(
                    R.string.dot_join_2,
                    facetsPart,
                    pluralStringResource(R.plurals.backup_widgets_need_readding_count, message.pendingWidgetCount, message.pendingWidgetCount),
                )
            } else {
                facetsPart
            }
            text to false
        }
        BackupRestoreMessage.ImportFailedInvalidFile -> stringResource(R.string.backup_restore_import_failed_invalid_file) to true
        is BackupRestoreMessage.ImportFailedUnsupportedVersion -> stringResource(R.string.backup_restore_import_failed_unsupported_version) to true
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (isError) ErrorColor else Ink,
        modifier = modifier
            .testTag("backup_restore_message")
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun PendingWidgetRow(
    widget: PendingWidgetUi,
    onReadd: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = widget.widgetLabel ?: widget.placement.providerPackageName,
                style = MaterialTheme.typography.bodyLarge,
                color = if (widget.providerAvailable) Ink else Muted,
            )
            Text(
                text = if (widget.providerAvailable) (widget.appLabel ?: "") else stringResource(R.string.backup_restore_app_not_installed),
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        }
        if (widget.providerAvailable) {
            TextButton(onClick = onReadd, modifier = Modifier.testTag("widget_readd_button")) {
                Text(text = stringResource(R.string.backup_restore_readd), color = Accent, style = MaterialTheme.typography.bodyMedium)
            }
        }
        TextButton(onClick = onSkip, modifier = Modifier.testTag("widget_skip_button")) {
            Text(text = stringResource(R.string.backup_restore_skip), color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun RowScaffold(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = LocalSettingsRowInset.current, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsIconBadge(icon, SettingsSectionHue.SYSTEM, modifier = Modifier.alpha(if (enabled) 1f else 0.4f))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = if (enabled) Ink else Muted)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
    }
}

@Composable
private fun BackupRestoreHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceContainer)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackButton(onClick = onBack)
        Text(text = stringResource(R.string.settings_backup_restore_title), style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BackupRestoreScreenPreview() {
    FacetLauncherTheme {
        BackupRestoreContent(
            uiState = BackupRestoreUiState(),
            onBack = {},
            onExportClick = {},
            onImportClick = {},
            onReimportWidget = {},
            onSkipWidget = {},
        )
    }
}
