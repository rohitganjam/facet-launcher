package com.lumenlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumenlauncher.app.data.model.NotificationBadgeStyle
import com.lumenlauncher.app.ui.components.BackButton
import com.lumenlauncher.app.ui.components.CardDivider
import com.lumenlauncher.app.ui.components.LabeledDropdownRow
import com.lumenlauncher.app.ui.components.SettingsCard
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Surface
import com.lumenlauncher.app.ui.theme.SurfaceContainer

private fun NotificationBadgeStyle.displayLabel(): String = when (this) {
    NotificationBadgeStyle.DOT -> "Dot"
    NotificationBadgeStyle.COUNT -> "Count"
}

/** F13's own page (reached from Settings' NOTIFICATIONS card) rather than an inline toggle — see `NotificationSettingsViewModel`. */
@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit,
    onNavigateToNotificationAccessExplanation: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    // No grant-change callback for notification listener access — re-check whenever this screen
    // resumes, since returning from the explanation screen's Settings redirect is the only way
    // the grant could have changed.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshAccessGranted()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    NotificationSettingsContent(
        uiState = uiState,
        onBack = onBack,
        onEnabledChanged = { checked ->
            // Turning it on without access yet routes through the explanation screen first,
            // which persists the setting itself once the grant actually lands (see
            // NotificationAccessExplanationViewModel) — turning off needs no permission dance.
            if (checked && !uiState.isAccessGranted) {
                onNavigateToNotificationAccessExplanation()
            } else {
                viewModel.setEnabled(checked)
            }
        },
        onBadgeStyleChanged = viewModel::setBadgeStyle,
        modifier = modifier,
    )
}

@Composable
private fun NotificationSettingsContent(
    uiState: NotificationSettingsUiState,
    onBack: () -> Unit,
    onEnabledChanged: (Boolean) -> Unit,
    onBadgeStyleChanged: (NotificationBadgeStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    // The stored preference alone isn't enough to actually show badges — it defaults to true and
    // is never flipped back off by an external revocation, so a switch that only read that value
    // would show "on" even when notification listener access itself has been denied or revoked,
    // exactly the silently-broken state that led to adding this gating (see chat history). The
    // switch must never show on without the real permission backing it.
    val enabled = uiState.settings.notificationDotsEnabled && uiState.isAccessGranted

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceContainer)
            .testTag("notification_settings_screen")
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BackButton(onClick = onBack)
            Text(text = "Notifications", style = MaterialTheme.typography.headlineSmall, color = Ink)
        }

        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Notification badges", style = MaterialTheme.typography.bodyLarge, color = Ink)
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChanged,
                    modifier = Modifier.testTag("notification_badges_toggle"),
                )
            }
            CardDivider()
            LabeledDropdownRow(
                title = "Badge style",
                options = NotificationBadgeStyle.entries,
                selected = uiState.settings.notificationBadgeStyle,
                label = { it.displayLabel() },
                onSelect = onBadgeStyleChanged,
                enabled = enabled,
                testTag = "badge_style_row",
                modifier = Modifier.alpha(if (enabled) 1f else 0.4f),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotificationSettingsScreenPreview() {
    LumenLauncherTheme {
        NotificationSettingsContent(
            uiState = NotificationSettingsUiState(),
            onBack = {},
            onEnabledChanged = {},
            onBadgeStyleChanged = {},
        )
    }
}
