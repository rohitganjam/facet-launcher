package com.lumenlauncher.app.ui.settings

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumenlauncher.app.ui.components.BackButton
import com.lumenlauncher.app.ui.components.CardDivider
import com.lumenlauncher.app.ui.components.SettingsCard
import com.lumenlauncher.app.ui.components.StickyHeaderLayout
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Surface
import com.lumenlauncher.app.ui.theme.SuccessColor

/**
 * Settings → Permissions — every permission Lumen uses in one place, each explaining why it's
 * needed and offering a one-tap way to grant it. A row whose permission has already been asked
 * for once and is still ungranted (the system won't show a runtime dialog for it a second time
 * once the user has said no for good) sends the user to this app's own system App Info screen
 * instead of firing a request that would silently do nothing.
 */
@Composable
fun PermissionsScreen(
    onBack: () -> Unit,
    onNavigateToUsageAccessExplanation: () -> Unit,
    onNavigateToNotificationAccessExplanation: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PermissionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun openAppInfo() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        runCatching { context.startActivity(intent) }
    }

    val requestCalendarPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.markCalendarPermissionRequested()
    }
    val requestContactsPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.markContactsPermissionRequested()
    }

    /**
     * `shouldShowRequestPermissionRationale` is `false` both *before ever asking* and *after the
     * user has permanently denied* — [hasRequestedBefore] (see [PermissionRowState]) is what
     * tells those two states apart. Only once we know we've asked before and the system still
     * won't offer a rationale-eligible re-ask do we treat this as a permanent denial.
     */
    fun onTurnOnClick(permission: PermissionRowState) {
        when (permission.kind) {
            PermissionKind.USAGE_ACCESS -> onNavigateToUsageAccessExplanation()
            PermissionKind.NOTIFICATION_ACCESS -> onNavigateToNotificationAccessExplanation()
            PermissionKind.CALENDAR -> {
                val canAskAgain = !permission.hasRequestedBefore ||
                    activity == null ||
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CALENDAR)
                if (canAskAgain) requestCalendarPermission.launch(Manifest.permission.READ_CALENDAR) else openAppInfo()
            }
            PermissionKind.CONTACTS -> {
                val canAskAgain = !permission.hasRequestedBefore ||
                    activity == null ||
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CONTACTS)
                if (canAskAgain) requestContactsPermission.launch(Manifest.permission.READ_CONTACTS) else openAppInfo()
            }
        }
    }

    PermissionsContent(
        permissions = uiState.permissions,
        onBack = onBack,
        onTurnOnClick = ::onTurnOnClick,
        modifier = modifier,
    )
}

@Composable
private fun PermissionsContent(
    permissions: List<PermissionRowState>,
    onBack: () -> Unit,
    onTurnOnClick: (PermissionRowState) -> Unit,
    modifier: Modifier = Modifier,
) {
    StickyHeaderLayout(
        modifier = modifier,
        header = { PermissionsHeader(onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Surface)
                    .testTag("permissions_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight),
            ) {
                item {
                    SettingsCard {
                        permissions.forEachIndexed { index, permission ->
                            if (index > 0) CardDivider()
                            PermissionRow(permission = permission, onTurnOnClick = { onTurnOnClick(permission) })
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        },
    )
}

@Composable
private fun PermissionsHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackButton(onClick = onBack)
        Text(text = "Permissions", style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

@Composable
private fun PermissionRow(permission: PermissionRowState, onTurnOnClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("permission_row_${permission.kind.name}")
            .padding(vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = permission.title, style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(text = permission.subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
        if (permission.isGranted) {
            Text(
                text = "On",
                style = MaterialTheme.typography.bodyMedium,
                color = SuccessColor,
                modifier = Modifier.testTag("permission_status_${permission.kind.name}"),
            )
        } else {
            Text(
                text = "Turn on",
                style = MaterialTheme.typography.bodyLarge,
                color = Accent,
                modifier = Modifier
                    .clickable(onClick = onTurnOnClick)
                    .testTag("permission_turn_on_${permission.kind.name}")
                    .padding(vertical = 4.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PermissionsScreenPreview() {
    LumenLauncherTheme {
        PermissionsContent(
            permissions = listOf(
                PermissionRowState(
                    kind = PermissionKind.CALENDAR,
                    title = "Calendar",
                    subtitle = "Shows calendar events on the clock widget.",
                    isGranted = true,
                ),
                PermissionRowState(
                    kind = PermissionKind.CONTACTS,
                    title = "Contacts",
                    subtitle = "Lets Drawer search show matching contacts with quick actions.",
                    isGranted = false,
                ),
                PermissionRowState(
                    kind = PermissionKind.USAGE_ACCESS,
                    title = "Usage access",
                    subtitle = "Needed to show Recent and Most Used apps.",
                    isGranted = false,
                ),
                PermissionRowState(
                    kind = PermissionKind.NOTIFICATION_ACCESS,
                    title = "Notification access",
                    subtitle = "Shows a dot or count badge on apps with active notifications.",
                    isGranted = false,
                ),
            ),
            onBack = {},
            onTurnOnClick = {},
        )
    }
}
