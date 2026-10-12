package com.facetlauncher.app.ui.home

import android.content.res.Configuration
import android.os.SystemClock
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.MediaPlayerState
import com.facetlauncher.app.data.model.MediaProgressStyle
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.HomeAppTextColor
import com.facetlauncher.app.ui.theme.homeAppLabelShadow
import kotlinx.coroutines.delay

/** How often the progress line re-reads the clock while playing — a bar this thin needs no more. */
private const val PROGRESS_TICK_MS = 500L

/**
 * Home's media player row, shown under the clock/calendar block. Bare — no container — so it reads as part
 * of the list: art on the left as tall as the text + progress line, controls on the title line, progress
 * line under the text only. [IntrinsicSize.Min] sizes the art to the text column.
 */
@Composable
fun MediaPlayerRow(
    state: MediaPlayerState,
    showSkipButtons: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    progressStyle: MediaProgressStyle = MediaProgressStyle.WAVY,
    labelColor: Color = HomeAppTextColor,
    labelFontWeight: FontWeight = FontWeight.Normal,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .testTag("home_media_player"),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MediaArt(state = state, tint = labelColor, onOpen = onOpen)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MediaText(
                    state = state,
                    color = labelColor,
                    fontWeight = labelFontWeight,
                    onOpen = onOpen,
                    modifier = Modifier.weight(1f),
                )
                MediaTransport(
                    state = state,
                    showSkipButtons = showSkipButtons,
                    tint = labelColor,
                    onPrevious = onPrevious,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                )
            }
            MediaProgressLine(
                state = state,
                color = labelColor,
                style = progressStyle,
                // The wavy line is ~10dp tall on its own, so it needs less breathing room.
                modifier = Modifier.padding(top = if (progressStyle == MediaProgressStyle.WAVY) 2.dp else 8.dp),
            )
        }
    }
}

/**
 * A square as tall as the incoming height. `aspectRatio` can't do this under `IntrinsicSize.Min`: it answers the
 * intrinsic-height question with its width, so the row balloons. This reports no intrinsic size and takes the row's
 * height once that is decided by the text column.
 */
private fun Modifier.squareToHeight(): Modifier = layout { measurable, constraints ->
    if (constraints.maxHeight == Constraints.Infinity) {
        layout(0, 0) {}
    } else {
        val side = constraints.maxHeight
        val placeable = measurable.measure(Constraints.fixed(side, side))
        layout(side, side) { placeable.place(0, 0) }
    }
}

@Composable
private fun MediaArt(state: MediaPlayerState, tint: Color, onOpen: () -> Unit) {
    Box(
        modifier = Modifier
            .squareToHeight()
            .clip(MaterialTheme.shapes.small)
            .background(tint.copy(alpha = 0.12f))
            .clickable(onClick = onOpen)
            .testTag("home_media_art"),
        contentAlignment = Alignment.Center,
    ) {
        val art = state.art
        if (art != null) {
            Image(
                bitmap = art.asImageBitmap(),
                contentDescription = stringResource(R.string.media_player_open_cd),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = stringResource(R.string.media_player_open_cd),
                tint = tint,
            )
        }
    }
}

@Composable
private fun MediaText(
    state: MediaPlayerState,
    color: Color,
    fontWeight: FontWeight,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shadow = homeAppLabelShadow(color)
    Column(modifier = modifier.clickable(onClick = onOpen)) {
        Text(
            text = state.title,
            style = MaterialTheme.typography.titleSmall.copy(shadow = shadow, fontWeight = fontWeight),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("home_media_title"),
        )
        state.artist?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall.copy(shadow = shadow),
                color = color.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("home_media_artist"),
            )
        }
    }
}

@Composable
private fun MediaTransport(
    state: MediaPlayerState,
    showSkipButtons: Boolean,
    tint: Color,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
) {
    if (showSkipButtons) {
        TransportButton(
            icon = Icons.Filled.SkipPrevious,
            contentDescription = stringResource(R.string.media_player_previous_cd),
            enabled = state.canSkipPrevious,
            tint = tint,
            onClick = onPrevious,
            testTag = "home_media_previous",
        )
    }
    TransportButton(
        icon = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
        contentDescription = stringResource(if (state.isPlaying) R.string.media_player_pause_cd else R.string.media_player_play_cd),
        enabled = true,
        tint = tint,
        onClick = onPlayPause,
        testTag = "home_media_play_pause",
    )
    if (showSkipButtons) {
        TransportButton(
            icon = Icons.Filled.SkipNext,
            contentDescription = stringResource(R.string.media_player_next_cd),
            enabled = state.canSkipNext,
            tint = tint,
            onClick = onNext,
            testTag = "home_media_next",
        )
    }
}

@Composable
private fun TransportButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    tint: Color,
    onClick: () -> Unit,
    testTag: String,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(36.dp).testTag(testTag),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.alpha(if (enabled) 1f else 0.35f),
        )
    }
}

@Composable
private fun MediaProgressLine(
    state: MediaPlayerState,
    color: Color,
    style: MediaProgressStyle,
    modifier: Modifier = Modifier,
) {
    var now by remember(state) { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(state) {
        if (!state.isPlaying) return@LaunchedEffect
        while (true) {
            delay(PROGRESS_TICK_MS)
            now = SystemClock.elapsedRealtime()
        }
    }
    val progress = state.progressAt(now)
    if (style == MediaProgressStyle.WAVY && progress != null) {
        WavyProgressLine(
            progress = progress,
            isPlaying = state.isPlaying,
            color = color,
            modifier = modifier.testTag("home_media_progress"),
        )
        return
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.25f))
            .testTag("home_media_progress"),
    ) {
        if (progress != null) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(color),
            )
        }
    }
}

/** Material's wavy line up to [progress]. The wave moves while [isPlaying] and settles flat when paused. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WavyProgressLine(progress: Float, isPlaying: Boolean, color: Color, modifier: Modifier = Modifier) {
    val amplitude by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "waveAmplitude",
    )
    LinearWavyProgressIndicator(
        progress = { progress },
        modifier = modifier.fillMaxWidth(),
        color = color,
        trackColor = color.copy(alpha = 0.25f),
        amplitude = { amplitude },
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF171A20, widthDp = 390)
@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF171A20, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MediaPlayerRowPreview() {
    val previewTitle = "I Don't Care"
    val previewArtist = "Apocalyptica, Adam Gontier"
    FacetLauncherTheme {
        MediaPlayerRow(
            state = MediaPlayerState(
                packageName = "com.example.music",
                title = previewTitle,
                artist = previewArtist,
                art = null,
                isPlaying = true,
                positionMs = 80_000L,
                durationMs = 210_000L,
                positionUpdatedAtMs = 0L,
                playbackSpeed = 1f,
                canSkipPrevious = true,
                canSkipNext = true,
            ),
            showSkipButtons = true,
            onPrevious = {},
            onPlayPause = {},
            onNext = {},
            onOpen = {},
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )
    }
}
