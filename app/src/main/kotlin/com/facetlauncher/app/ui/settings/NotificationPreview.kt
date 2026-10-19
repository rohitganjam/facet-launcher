package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.MediaPlayerState
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.ui.components.HomeSurfacePreview
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsSectionHeader
import com.facetlauncher.app.ui.components.TextActionButton
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.resolve
import com.facetlauncher.app.ui.home.MediaPlayerRow
import com.facetlauncher.app.ui.theme.FacetLauncherTheme

// Stand-in for the wallpaper Home's text sits on; light text on it matches Home's default look.
private val PreviewBackdrop = Color(0xFF171A20)
private val PreviewText = Color.White

/**
 * Sample of what Home shows for the current settings. Without notification access, or with both badges
 * and the media player off, Home shows nothing — so the preview says that instead of showing a
 * presentation the user won't actually get.
 */
@Composable
internal fun NotificationPreview(
    uiState: NotificationSettingsUiState,
    onGrantAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showBadges = uiState.settings.notificationDotsEnabled
    val showMedia = uiState.settings.showMediaPlayerOnHome
    when {
        !uiState.isAccessGranted -> PreviewMessage(
            message = stringResource(R.string.notification_preview_no_access),
            tag = "notification_preview_no_access",
            modifier = modifier,
            actionText = stringResource(R.string.notification_preview_allow_access),
            onAction = onGrantAccess,
        )
        !showBadges && !showMedia -> PreviewMessage(
            message = stringResource(R.string.notification_preview_disabled),
            tag = "notification_preview_disabled",
            modifier = modifier,
        )
        else -> NotificationPreviewContent(uiState, showMedia = showMedia, showBadges = showBadges, modifier = modifier)
    }
}

@Composable
private fun PreviewMessage(
    message: String,
    tag: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: () -> Unit = {},
) {
    Column(modifier = modifier) {
        SettingsSectionHeader(stringResource(R.string.notification_preview_title))
        SettingsCard(modifier = Modifier.testTag(tag)) {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                Text(text = message, style = MaterialTheme.typography.bodyMedium, color = Muted)
                if (actionText != null) {
                    TextActionButton(text = actionText, onClick = onAction, modifier = Modifier.padding(top = 8.dp).testTag("notification_preview_allow_access"))
                }
            }
        }
    }
}

@Composable
private fun NotificationPreviewContent(
    uiState: NotificationSettingsUiState,
    showMedia: Boolean,
    showBadges: Boolean,
    modifier: Modifier = Modifier,
) {
    val settings = uiState.settings
    val sampleMedia = MediaPlayerState(
        packageName = "",
        title = stringResource(R.string.notification_preview_track),
        artist = stringResource(R.string.notification_preview_artist),
        art = null,
        isPlaying = true,
        positionMs = 80_000L,
        durationMs = 210_000L,
        positionUpdatedAtMs = 0L,
        playbackSpeed = 1f,
        canSkipPrevious = true,
        canSkipNext = true,
    )
    val badgeCounts = remember(uiState.homePreviewApps, uiState.dockPreviewApps) {
        // Varied counts so Count style shows more than one number; "12" also exercises the 9+ cap.
        val packages = (uiState.homePreviewApps + uiState.dockPreviewApps).mapNotNull { (it as? PlacedItem.SingleApp)?.app?.packageName }
        packages.take(SAMPLE_BADGE_COUNTS.size).zip(SAMPLE_BADGE_COUNTS).toMap() +
            (uiState.dockPreviewApps.firstOrNull() as? PlacedItem.SingleApp)?.let { mapOf(it.app.packageName to 2) }.orEmpty()
    }
    val labelColor = settings.appLabelColorOption.resolve()
    val labelWeight = settings.homeAppsFontWeight.resolve()
    Column(modifier = modifier.testTag("notification_preview")) {
        SettingsSectionHeader(stringResource(R.string.notification_preview_title))
        if (showMedia) Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .background(PreviewBackdrop)
                .padding(20.dp),
        ) {
            MediaPlayerRow(
                state = sampleMedia,
                showSkipButtons = settings.showMediaSkipButtons,
                onPrevious = {},
                onPlayPause = {},
                onNext = {},
                onOpen = {},
                labelColor = PreviewText,
            )
        }
        HomeSurfacePreview(
            appList = uiState.homePreviewApps,
            dockApps = uiState.dockPreviewApps,
            labelColor = labelColor,
            labelFontWeight = labelWeight,
            homeWallpaper = uiState.homeWallpaper,
            appRowPosition = settings.appRowPosition,
            appRowPresentation = settings.appRowPresentation,
            dockDisplayMode = settings.dockDisplayMode,
            appListLayout = settings.appListLayout,
            appListColumnAlignment = settings.appListColumnAlignment,
            appListGridColumns = settings.appListGridColumns,
            appListGridDisplayMode = settings.appListGridDisplayMode,
            notificationBadgeStyle = settings.notificationBadgeStyle,
            badgeCountFor = { pkg -> if (showBadges) badgeCounts[pkg] else null },
            modifier = Modifier.padding(top = if (showMedia) 12.dp else 0.dp).testTag("notification_home_preview"),
        )
    }
}

private val SAMPLE_BADGE_COUNTS = listOf(3, 12)

@Preview(showBackground = true, widthDp = 390)
@Preview(name = "Dark", showBackground = true, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotificationPreviewPreview() {
    FacetLauncherTheme {
        NotificationPreview(
            uiState = NotificationSettingsUiState(isAccessGranted = true),
            onGrantAccess = {},
            modifier = Modifier.padding(24.dp))
    }
}
