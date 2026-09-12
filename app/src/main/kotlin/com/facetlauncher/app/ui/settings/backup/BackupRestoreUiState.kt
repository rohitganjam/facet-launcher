package com.facetlauncher.app.ui.settings.backup

import android.content.Intent
import android.content.IntentSender
import com.facetlauncher.app.data.model.BackupWidgetPlacement

/**
 * One widget from an imported backup awaiting the guided re-add step — see
 * [com.facetlauncher.app.data.model.BackupWidgetPlacement]'s own doc comment for why this can't
 * just be written back to `widget_placements` automatically.
 */
data class PendingWidgetUi(
    val placement: BackupWidgetPlacement,
    /** `false` when no currently-installed provider matches [placement]'s package/class — the app that owned this widget isn't installed (or isn't installed yet) on this device. */
    val providerAvailable: Boolean,
    val appLabel: String?,
    val widgetLabel: String?,
    /** Re-added or explicitly skipped — either way, removed from the actionable list. */
    val done: Boolean = false,
)

data class BackupRestoreUiState(
    val isBusy: Boolean = false,
    val message: BackupRestoreMessage? = null,
    val pendingWidgets: List<PendingWidgetUi> = emptyList(),
)

/** A one-off result banner — shown until [BackupRestoreViewModel.dismissMessage] (or the next export/import) clears it. */
sealed interface BackupRestoreMessage {
    data object ExportSucceeded : BackupRestoreMessage
    data object ExportFailed : BackupRestoreMessage
    data class ImportSucceeded(val profileCount: Int, val pendingWidgetCount: Int) : BackupRestoreMessage
    data object ImportFailedInvalidFile : BackupRestoreMessage
    data class ImportFailedUnsupportedVersion(val backupVersion: Int) : BackupRestoreMessage
}

/** One-off effects the composable must act on (system Activity-result detours during a widget re-add) — not folded into [BackupRestoreUiState], mirroring `ui/hub/picker/HubAddWidgetEvent.kt`. */
sealed interface BackupRestoreEvent {
    data class LaunchBindPermission(val intent: Intent) : BackupRestoreEvent
    data class LaunchConfigure(val intentSender: IntentSender) : BackupRestoreEvent
}
