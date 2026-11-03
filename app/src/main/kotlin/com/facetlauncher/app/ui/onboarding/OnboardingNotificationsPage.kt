package com.facetlauncher.app.ui.onboarding

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.settings.BadgeSettingsCard
import com.facetlauncher.app.ui.settings.MediaPlayerSettingsCard
import com.facetlauncher.app.ui.settings.NotificationPreview
import com.facetlauncher.app.ui.settings.NotificationSettingsUiState
import com.facetlauncher.app.ui.settings.NotificationSettingsViewModel
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted

/**
 * Onboarding's last step — badges and the Home media player, the two features that run on notification
 * listener access. Reuses Settings' [NotificationSettingsViewModel], preview and cards (minus the in-app
 * menu and skip-buttons rows), so the two screens can't drift. Turning a switch on without access jumps
 * straight to the system listener settings; there is no separate explanation screen because this page is the disclosure.
 */
@Composable
fun OnboardingNotificationsPage(
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val openAccessSettings: () -> Unit = {
        runCatching { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
    }

    // No grant-change callback — returning from the system Settings redirect is the only way it changes.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshAccessGranted()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    OnboardingNotificationsContent(
        uiState = uiState,
        onBadgesEnabledChange = { checked ->
            viewModel.setEnabled(checked)
            if (checked && !uiState.isAccessGranted) openAccessSettings()
        },
        onBadgeStyleChange = viewModel::setBadgeStyle,
        onShowMediaPlayerChange = { checked ->
            viewModel.setShowMediaPlayer(checked)
            if (checked && !uiState.isAccessGranted) openAccessSettings()
        },
        onGrantAccess = openAccessSettings,
        onBack = onBack,
        onNext = onNext,
        modifier = modifier,
    )
}

@Composable
internal fun OnboardingNotificationsContent(
    uiState: NotificationSettingsUiState,
    onBadgesEnabledChange: (Boolean) -> Unit,
    onBadgeStyleChange: (NotificationBadgeStyle) -> Unit,
    onShowMediaPlayerChange: (Boolean) -> Unit,
    onGrantAccess: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Same gating as Settings: a stored "on" must not show while the real permission is missing.
    val badgesEnabled = uiState.settings.notificationDotsEnabled && uiState.isAccessGranted
    val mediaEnabled = uiState.settings.showMediaPlayerOnHome && uiState.isAccessGranted

    Column(modifier = modifier.fillMaxSize().testTag("onboarding_notifications_page")) {
        Column(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(text = stringResource(R.string.onboarding_notifications_headline), style = MaterialTheme.typography.headlineSmall, color = Ink)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = stringResource(R.string.onboarding_notifications_subhead), style = MaterialTheme.typography.bodyLarge, color = Muted)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.onboarding_notifications_access_note),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                modifier = Modifier.testTag("onboarding_notifications_access_note"),
            )
            Spacer(modifier = Modifier.height(24.dp))
            BadgeSettingsCard(
                badgesEnabled = badgesEnabled,
                badgeStyle = uiState.settings.notificationBadgeStyle,
                inAppMenuEnabled = false,
                onBadgesEnabledChange = onBadgesEnabledChange,
                onBadgeStyleChange = onBadgeStyleChange,
                onInAppMenuChange = null,
            )
            MediaPlayerSettingsCard(
                enabled = mediaEnabled,
                showSkipButtons = uiState.settings.showMediaSkipButtons,
                onEnabledChange = onShowMediaPlayerChange,
                onShowSkipButtonsChange = null,
                modifier = Modifier.padding(top = 16.dp),
            )
            NotificationPreview(uiState = uiState, onGrantAccess = onGrantAccess)
            Spacer(modifier = Modifier.height(24.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 34.dp, top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OnboardingDots(step = 3, totalSteps = ONBOARDING_STEP_COUNT)
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                TextActionButton(
                    text = stringResource(R.string.action_back),
                    onClick = onBack,
                    color = Muted,
                    modifier = Modifier.testTag("onboarding_back"),
                )
                TextActionButton(
                    // The last step: it leads into the set-default sheet on Home, so "Continue", not "Next".
                    text = stringResource(R.string.action_continue),
                    onClick = onNext,
                    modifier = Modifier.testTag("onboarding_next"),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OnboardingNotificationsPagePreview() {
    FacetLauncherTheme {
        OnboardingNotificationsContent(
            uiState = NotificationSettingsUiState(),
            onBadgesEnabledChange = {},
            onBadgeStyleChange = {},
            onShowMediaPlayerChange = {},
            onGrantAccess = {},
            onBack = {},
            onNext = {},
        )
    }
}
