package com.facetlauncher.app.ui.nativewidget

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.MediaPlayerState
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.domain.WidgetTier
import com.facetlauncher.app.ui.home.HomeMediaActions
import com.facetlauncher.app.ui.home.HomeMediaState
import com.facetlauncher.app.ui.home.MediaArt
import com.facetlauncher.app.ui.home.MediaProgressLine
import com.facetlauncher.app.ui.home.MediaTransport
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted

private val MIN_ART = 40.dp

/** The current track in the box it is given: the cover with play/pause, then the title and skip buttons, then a large cover and the progress line. */
@Composable
internal fun NowPlayingNativeWidget(tier: WidgetTier, size: DpSize, media: HomeMediaState, actions: HomeMediaActions, modifier: Modifier = Modifier) {
    val player = media.player
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (player == null) {
            Text(stringResource(R.string.native_widget_media_empty), style = MaterialTheme.typography.bodyMedium, color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        } else {
            when {
                tier == WidgetTier.SMALL -> SmallPlayer(player, size, actions)
                size.width >= 220.dp && tier == WidgetTier.MEDIUM -> WidePlayer(player, size, media.showSkipButtons, actions)
                else -> TallPlayer(player, size, media.showSkipButtons, actions, withProgress = tier == WidgetTier.LARGE)
            }
        }
    }
}

@Composable
private fun SmallPlayer(player: MediaPlayerState, size: DpSize, actions: HomeMediaActions) {
    val side = minOf(size.width, size.height)
    Box(modifier = Modifier.size(side), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(side)) { MediaArt(player, Ink, actions.onOpen) }
        MediaTransport(player, showSkipButtons = false, tint = Ink, onPrevious = actions.onPrevious, onPlayPause = actions.onPlayPause, onNext = actions.onNext)
    }
}

@Composable
private fun WidePlayer(player: MediaPlayerState, size: DpSize, showSkip: Boolean, actions: HomeMediaActions) {
    val art = (size.height).coerceAtLeast(MIN_ART)
    Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(art)) { MediaArt(player, Ink, actions.onOpen) }
        TrackText(player, actions.onOpen, Alignment.Start, Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MediaTransport(player, showSkip, Ink, actions.onPrevious, actions.onPlayPause, actions.onNext)
        }
    }
}

@Composable
private fun TallPlayer(player: MediaPlayerState, size: DpSize, showSkip: Boolean, actions: HomeMediaActions, withProgress: Boolean) {
    val art = (size.height - 96.dp).coerceIn(MIN_ART, size.width)
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)) {
        Box(modifier = Modifier.size(art)) { MediaArt(player, Ink, actions.onOpen) }
        TrackText(player, actions.onOpen, Alignment.CenterHorizontally, Modifier.fillMaxWidth())
        if (withProgress) MediaProgressLine(player, Ink, Modifier.fillMaxWidth())
        Row(verticalAlignment = Alignment.CenterVertically) {
            MediaTransport(player, showSkip, Ink, actions.onPrevious, actions.onPlayPause, actions.onNext)
        }
    }
}

@Composable
private fun TrackText(player: MediaPlayerState, onOpen: () -> Unit, horizontalAlignment: Alignment.Horizontal, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clickable(onClick = onOpen), horizontalAlignment = horizontalAlignment, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(player.title, style = MaterialTheme.typography.titleSmall, color = Ink, maxLines = 1, softWrap = false, modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE))
        player.artist?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

internal fun previewMediaState(title: String, artist: String) = HomeMediaState(
    player = MediaPlayerState(
        packageName = "com.example.music", title = title, artist = artist, art = null, isPlaying = true,
        positionMs = 80_000L, durationMs = 210_000L, positionUpdatedAtMs = 0L, playbackSpeed = 1f, canSkipPrevious = true, canSkipNext = true,
    ),
)

@Preview(name = "Small", showBackground = true, widthDp = 72, heightDp = 72)
@Preview(name = "Small dark", showBackground = true, widthDp = 72, heightDp = 72, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NowPlayingSmallPreview() = FacetLauncherTheme { NativeWidgetPreviewBox(NativeWidgetType.NOW_PLAYING, 72, 72) }

@Preview(name = "Medium", showBackground = true, widthDp = 300, heightDp = 72)
@Preview(name = "Medium dark", showBackground = true, widthDp = 300, heightDp = 72, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NowPlayingMediumPreview() = FacetLauncherTheme { NativeWidgetPreviewBox(NativeWidgetType.NOW_PLAYING, 300, 72) }

@Preview(name = "Large", showBackground = true, widthDp = 376, heightDp = 224)
@Preview(name = "Large dark", showBackground = true, widthDp = 376, heightDp = 224, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NowPlayingLargePreview() = FacetLauncherTheme { NativeWidgetPreviewBox(NativeWidgetType.NOW_PLAYING, 376, 224) }
