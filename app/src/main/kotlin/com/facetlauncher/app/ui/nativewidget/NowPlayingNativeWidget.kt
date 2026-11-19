package com.facetlauncher.app.ui.nativewidget

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
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
import com.facetlauncher.app.domain.NowPlayingLayout
import com.facetlauncher.app.domain.nowPlayingLayoutFor
import com.facetlauncher.app.ui.home.HomeMediaActions
import com.facetlauncher.app.ui.home.HomeMediaState
import com.facetlauncher.app.ui.home.MediaArt
import com.facetlauncher.app.ui.home.MediaProgressLine
import com.facetlauncher.app.ui.home.MediaTransport
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted

private val MIN_ART = 40.dp
private val SCRIM = Color.Black.copy(alpha = 0.5f)

/** The current track in the box it is given: [nowPlayingLayoutFor] picks one of six layouts, from a cover tile up to a full player. */
@Composable
internal fun NowPlayingNativeWidget(size: DpSize, media: HomeMediaState, actions: HomeMediaActions, modifier: Modifier = Modifier) {
    val player = media.player
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (player == null) {
            Text(stringResource(R.string.native_widget_media_empty), style = MaterialTheme.typography.bodyMedium, color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        } else {
            val layout = nowPlayingLayoutFor(size.width.value.toInt(), size.height.value.toInt())
            Box(modifier = Modifier.fillMaxSize().testTag("now_playing_layout_${layout.name}"), contentAlignment = Alignment.Center) {
                when (layout) {
                    NowPlayingLayout.TILE -> TilePlayer(player, size, actions)
                    NowPlayingLayout.COMPACT -> RowPlayer(player, size, showSkip = false, actions, maxArt = 48.dp)
                    NowPlayingLayout.STRIP -> StripPlayer(player, size, media.showSkipButtons, actions)
                    NowPlayingLayout.COVER_CARD -> CoverCardPlayer(player, size, actions)
                    NowPlayingLayout.CARD -> CardPlayer(player, size, media.showSkipButtons, actions)
                    NowPlayingLayout.FULL -> FullPlayer(player, size, media.showSkipButtons, actions)
                }
            }
        }
    }
}

@Composable
private fun TilePlayer(player: MediaPlayerState, size: DpSize, actions: HomeMediaActions) {
    val side = minOf(size.width, size.height)
    Box(modifier = Modifier.size(side), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(side)) { MediaArt(player, Ink, actions.onOpen) }
        MediaTransport(player, showSkipButtons = false, tint = Ink, onPrevious = actions.onPrevious, onPlayPause = actions.onPlayPause, onNext = actions.onNext)
    }
}

/** The compact layout: cover, text and play/pause in one row. */
@Composable
private fun RowPlayer(player: MediaPlayerState, size: DpSize, showSkip: Boolean, actions: HomeMediaActions, maxArt: Dp) {
    val art = size.height.coerceAtMost(maxArt).coerceAtLeast(MIN_ART)
    Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(art)) { MediaArt(player, Ink, actions.onOpen) }
        TrackText(player, actions.onOpen, Alignment.Start, Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MediaTransport(player, showSkip, Ink, actions.onPrevious, actions.onPlayPause, actions.onNext)
        }
    }
}

/** Home's media row: cover on the left, title and transport on one line, the progress line under the text only. */
@Composable
private fun StripPlayer(player: MediaPlayerState, size: DpSize, showSkip: Boolean, actions: HomeMediaActions) {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(size.height)) { MediaArt(player, Ink, actions.onOpen) }
        Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TrackText(player, actions.onOpen, Alignment.Start, Modifier.weight(1f))
                MediaTransport(player, showSkip, Ink, actions.onPrevious, actions.onPlayPause, actions.onNext)
            }
            MediaProgressLine(player, Ink, Modifier.padding(top = 2.dp))
        }
    }
}

/** The cover fills the box, with the title, artist and play/pause on a scrim along the bottom. */
@Composable
private fun CoverCardPlayer(player: MediaPlayerState, size: DpSize, actions: HomeMediaActions) {
    val side = minOf(size.width, size.height)
    Box(modifier = Modifier.size(side).clip(MaterialTheme.shapes.small)) {
        MediaArt(player, Ink, actions.onOpen)
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(SCRIM).padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TrackText(player, actions.onOpen, Alignment.Start, Modifier.weight(1f), titleColor = Color.White, artistColor = Color.White.copy(alpha = 0.75f))
            MediaTransport(player, showSkipButtons = false, tint = Color.White, onPrevious = actions.onPrevious, onPlayPause = actions.onPlayPause, onNext = actions.onNext)
        }
    }
}

/** A square cover on the left; title, progress and transport stacked on the right. */
@Composable
private fun CardPlayer(player: MediaPlayerState, size: DpSize, showSkip: Boolean, actions: HomeMediaActions) {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(size.height.coerceAtMost(size.width / 2))) { MediaArt(player, Ink, actions.onOpen) }
        Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
            TrackText(player, actions.onOpen, Alignment.Start, Modifier.fillMaxWidth())
            MediaProgressLine(player, Ink, Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                MediaTransport(player, showSkip, Ink, actions.onPrevious, actions.onPlayPause, actions.onNext)
            }
        }
    }
}

/** A big cover with the title beside it while the cover stays at most half the width (otherwise the text goes on the line below), then the progress line and transport tight underneath. */
@Composable
private fun FullPlayer(player: MediaPlayerState, size: DpSize, showSkip: Boolean, actions: HomeMediaActions) {
    // The beside-the-title cover would take more than half the width: stack the text under a smaller cover instead.
    val besideArt = size.height - 52.dp
    val sideBySide = besideArt * 2 <= size.width
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)) {
        if (sideBySide) {
            val art = besideArt.coerceAtLeast(MIN_ART)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(art)) { MediaArt(player, Ink, actions.onOpen) }
                TrackText(player, actions.onOpen, Alignment.Start, Modifier.weight(1f))
            }
        } else {
            val art = (size.height - 92.dp).coerceIn(MIN_ART, size.width)
            Box(modifier = Modifier.size(art)) { MediaArt(player, Ink, actions.onOpen) }
            TrackText(player, actions.onOpen, Alignment.CenterHorizontally, Modifier.fillMaxWidth())
        }
        MediaProgressLine(player, Ink, Modifier.fillMaxWidth())
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            MediaTransport(player, showSkip, Ink, actions.onPrevious, actions.onPlayPause, actions.onNext)
        }
    }
}

@Composable
private fun TrackText(
    player: MediaPlayerState,
    onOpen: () -> Unit,
    horizontalAlignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
    titleColor: Color = Ink,
    artistColor: Color = Muted,
) {
    Column(modifier = modifier.clickable(onClick = onOpen), horizontalAlignment = horizontalAlignment, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(player.title, style = MaterialTheme.typography.bodyMedium, color = titleColor, maxLines = 1, softWrap = false, modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE))
        player.artist?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = artistColor, maxLines = 1, overflow = TextOverflow.Ellipsis) }
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

@Preview(name = "Compact", showBackground = true, widthDp = 148, heightDp = 72)
@Composable
private fun NowPlayingCompactPreview() = FacetLauncherTheme { NativeWidgetPreviewBox(NativeWidgetType.NOW_PLAYING, 148, 72) }

@Preview(name = "Cover card", showBackground = true, widthDp = 148, heightDp = 148)
@Composable
private fun NowPlayingCoverCardPreview() = FacetLauncherTheme { NativeWidgetPreviewBox(NativeWidgetType.NOW_PLAYING, 148, 148) }

@Preview(name = "Card", showBackground = true, widthDp = 292, heightDp = 148)
@Composable
private fun NowPlayingCardPreview() = FacetLauncherTheme { NativeWidgetPreviewBox(NativeWidgetType.NOW_PLAYING, 292, 148) }

@Preview(name = "Full", showBackground = true, widthDp = 292, heightDp = 220)
@Preview(name = "Full dark", showBackground = true, widthDp = 292, heightDp = 220, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NowPlayingFullPreview() = FacetLauncherTheme { NativeWidgetPreviewBox(NativeWidgetType.NOW_PLAYING, 292, 220) }
