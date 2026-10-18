package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.MediaPlayerState
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.SettingsSectionHeader
import com.facetlauncher.app.ui.home.MediaPlayerRow
import com.facetlauncher.app.ui.theme.FacetLauncherTheme

// Stand-in for the wallpaper Home's text sits on; light text on it matches Home's default look.
private val PreviewBackdrop = Color(0xFF171A20)
private val PreviewText = Color.White

/**
 * Sample of what Home shows for the current settings: the media player row and an app row with a
 * notification badge. Always rendered — it needs no notification access, so it stays useful (and the
 * screen isn't blank) before the permission is granted.
 */
@Composable
internal fun NotificationPreview(
    badgeStyle: NotificationBadgeStyle,
    showSkipButtons: Boolean,
    modifier: Modifier = Modifier,
) {
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
    Column(modifier = modifier.testTag("notification_preview")) {
        SettingsSectionHeader(stringResource(R.string.notification_preview_title))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .background(PreviewBackdrop)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            MediaPlayerRow(
                state = sampleMedia,
                showSkipButtons = showSkipButtons,
                onPrevious = {},
                onPlayPause = {},
                onNext = {},
                onOpen = {},
                labelColor = PreviewText,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AppIcon(
                    icon = null,
                    size = AppIconSize.ROW_REGULAR,
                    contentDescription = null,
                    notificationCount = 3,
                    badgeStyle = badgeStyle,
                )
                Text(
                    text = stringResource(R.string.notification_preview_app),
                    style = MaterialTheme.typography.titleMedium,
                    color = PreviewText,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Preview(name = "Dark", showBackground = true, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotificationPreviewPreview() {
    FacetLauncherTheme {
        NotificationPreview(badgeStyle = NotificationBadgeStyle.COUNT, showSkipButtons = true, modifier = Modifier.padding(24.dp))
    }
}
