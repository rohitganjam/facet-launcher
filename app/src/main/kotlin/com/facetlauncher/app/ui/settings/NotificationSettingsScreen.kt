package com.facetlauncher.app.ui.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Surface
import com.facetlauncher.app.ui.theme.SurfaceContainer

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
        onEnabledChange = { checked ->
            // Turning it on without access yet routes through the explanation screen first,
            // which persists the setting itself once the grant actually lands (see
            // NotificationAccessExplanationViewModel) — turning off needs no permission dance.
            if (checked && !uiState.isAccessGranted) {
                onNavigateToNotificationAccessExplanation()
            } else {
                viewModel.setEnabled(checked)
            }
        },
        onBadgeStyleChange = viewModel::setBadgeStyle,
        onShowInAppMenuChange = { checked ->
            // Same as the media player: persisted first, then the explanation screen if access is missing.
            viewModel.setShowNotificationsInAppMenu(checked)
            if (checked && !uiState.isAccessGranted) onNavigateToNotificationAccessExplanation()
        },
        onShowMediaPlayerChange = { checked ->
            // Persisted first even without access: the switch stays off (gated on the grant) until it
            // lands, and the player then appears without a second toggle.
            viewModel.setShowMediaPlayer(checked)
            if (checked && !uiState.isAccessGranted) onNavigateToNotificationAccessExplanation()
        },
        onShowSkipButtonsChange = viewModel::setShowSkipButtons,
        onGrantAccess = onNavigateToNotificationAccessExplanation,
        modifier = modifier,
    )
}

@Composable
private fun NotificationSettingsContent(
    uiState: NotificationSettingsUiState,
    onBack: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onBadgeStyleChange: (NotificationBadgeStyle) -> Unit,
    onShowInAppMenuChange: (Boolean) -> Unit,
    onShowMediaPlayerChange: (Boolean) -> Unit,
    onShowSkipButtonsChange: (Boolean) -> Unit,
    onGrantAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The stored preference alone isn't enough to actually show badges — it defaults to true and
    // is never flipped back off by an external revocation, so a switch that only read that value
    // would show "on" even when notification listener access itself has been denied or revoked,
    // exactly the silently-broken state that led to adding this gating (see chat history). The
    // switch must never show on without the real permission backing it.
    val enabled = uiState.settings.notificationDotsEnabled && uiState.isAccessGranted
    val mediaEnabled = uiState.settings.showMediaPlayerOnHome && uiState.isAccessGranted
    val inAppMenuEnabled = uiState.settings.showNotificationsInAppMenu && uiState.isAccessGranted

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceContainer)
            .testTag("notification_settings_screen")
            .windowInsetsPadding(WindowInsets.systemBars)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BackButton(onClick = onBack)
            Text(text = stringResource(R.string.settings_notifications_title), style = MaterialTheme.typography.headlineSmall, color = Ink)
        }

        BadgeSettingsCard(
            badgesEnabled = enabled,
            badgeStyle = uiState.settings.notificationBadgeStyle,
            inAppMenuEnabled = inAppMenuEnabled,
            onBadgesEnabledChange = onEnabledChange,
            onBadgeStyleChange = onBadgeStyleChange,
            onInAppMenuChange = onShowInAppMenuChange,
        )

        MediaPlayerSettingsCard(
            enabled = mediaEnabled,
            showSkipButtons = uiState.settings.showMediaSkipButtons,
            onEnabledChange = onShowMediaPlayerChange,
            onShowSkipButtonsChange = onShowSkipButtonsChange,
            modifier = Modifier.padding(top = 16.dp),
        )

        NotificationPreview(uiState = uiState, onGrantAccess = onGrantAccess)
    }
}

@Composable
private fun BadgeSettingsCard(
    badgesEnabled: Boolean,
    badgeStyle: NotificationBadgeStyle,
    inAppMenuEnabled: Boolean,
    onBadgesEnabledChange: (Boolean) -> Unit,
    onBadgeStyleChange: (NotificationBadgeStyle) -> Unit,
    onInAppMenuChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = stringResource(R.string.notification_badges_row_title), style = MaterialTheme.typography.bodyLarge, color = Ink)
            Switch(
                checked = badgesEnabled,
                onCheckedChange = onBadgesEnabledChange,
                modifier = Modifier.testTag("notification_badges_toggle"),
            )
        }
        CardDivider()
        LabeledDropdownRow(
            title = stringResource(R.string.notification_badge_style_row_title),
            options = NotificationBadgeStyle.entries,
            selected = badgeStyle,
            label = { stringResource(it.displayNameRes) },
            onSelect = onBadgeStyleChange,
            enabled = badgesEnabled,
            testTag = "badge_style_row",
            modifier = Modifier.alpha(if (badgesEnabled) 1f else 0.4f),
        )
        CardDivider()
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.notification_app_menu_row_title),
                style = MaterialTheme.typography.bodyLarge,
                color = Ink,
                modifier = Modifier.weight(1f).padding(end = 12.dp),
            )
            Switch(
                checked = inAppMenuEnabled,
                onCheckedChange = onInAppMenuChange,
                modifier = Modifier.testTag("notification_app_menu_toggle"),
            )
        }
    }
}

@Composable
private fun MediaPlayerSettingsCard(
    enabled: Boolean,
    showSkipButtons: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onShowSkipButtonsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.media_player_row_title),
                style = MaterialTheme.typography.bodyLarge,
                color = Ink,
                modifier = Modifier.weight(1f).padding(end = 12.dp),
            )
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange,
                modifier = Modifier.testTag("media_player_toggle"),
            )
        }
        CardDivider()
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp).alpha(if (enabled) 1f else 0.4f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = stringResource(R.string.media_skip_buttons_row_title), style = MaterialTheme.typography.bodyLarge, color = Ink)
            Switch(
                checked = showSkipButtons,
                onCheckedChange = onShowSkipButtonsChange,
                enabled = enabled,
                modifier = Modifier.testTag("media_skip_buttons_toggle"),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotificationSettingsScreenPreview() {
    FacetLauncherTheme {
        NotificationSettingsContent(
            uiState = NotificationSettingsUiState(),
            onBack = {},
            onEnabledChange = {},
            onBadgeStyleChange = {},
            onShowInAppMenuChange = {},
            onShowMediaPlayerChange = {},
            onShowSkipButtonsChange = {},
            onGrantAccess = {},
        )
    }
}
