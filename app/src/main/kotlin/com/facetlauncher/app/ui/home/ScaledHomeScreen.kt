package com.facetlauncher.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.MediaPlayerState
import com.facetlauncher.app.domain.ResolvedFacetStyle
import com.facetlauncher.app.ui.components.WallpaperBackground

/**
 * A scale model of Home: the real [HomeScreen], laid out at the window's own size and shrunk to fit
 * [modifier]'s box, so every component sits where Home puts it, in portrait and in landscape alike.
 * The state comes from [toHomeScreenState] for the layout mode the window gets, the same builder Home
 * uses. A transparent layer on top takes every tap: [onClick] (the carousel's apply) or nothing, so
 * the copy is never interactive.
 *
 * [tagPrefix] goes in front of Home's test tags, which keeps several previews, and the Home beneath
 * them, apart.
 */
@Composable
internal fun ScaledHomeScreen(
    style: ResolvedFacetStyle,
    settings: LauncherSettings,
    content: HomeScreenContent,
    homeWallpaper: HomeWallpaper,
    tagPrefix: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val config = LocalConfiguration.current
    val windowWidth = config.screenWidthDp.dp
    val windowHeight = config.screenHeightDp.dp
    val mode = rememberHomeLayoutMode()
    val space = rememberHomeLandscapeSpace()
    val host = LocalClockWidgetHost.current
    val context = LocalContext.current
    val sampleMedia = previewMediaPlayer()
    val state = remember(style, settings, content, mode, host, sampleMedia) {
        val shown = content.copy(mediaPlayer = content.mediaPlayer ?: sampleMedia.takeIf { settings.showMediaPlayerOnHome })
        val sizing = HomeWidgetSizing(
            defaultSizeDp = { host?.defaultSizeDp(it) },
            resizeFloorDp = { host?.resizeFloorDp(context, it) },
        )
        style.toHomeScreenState(landscape = mode == HomeLayoutMode.LANDSCAPE, settings = settings, content = shown, widgetSizing = sizing)
    }
    val actions = remember(host) {
        HomeScreenActions(
            item = HomeItemActions(onAppClick = {}),
            widget = HomeClockWidgetActions(createHostView = { viewContext, id -> host?.createPreviewHostView(viewContext, id) }),
        )
    }

    BoxWithConstraints(modifier = modifier) {
        WallpaperBackground(homeWallpaper, Modifier.matchParentSize())
        val scale = minOf(maxWidth / windowWidth, maxHeight / windowHeight)
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.TopStart, unbounded = true)
                    .requiredSize(windowWidth, windowHeight)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0f, 0f)
                    },
            ) {
                CompositionLocalProvider(LocalHomeTagPrefix provides tagPrefix) {
                    HomeScreen(state = state, actions = actions, layoutMode = mode, landscapeSpace = space)
                }
            }
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier.pointerInput(Unit) { detectTapGestures { } }
                    },
                ),
        )
    }
}

/** A paused player at 35%, shown when "Show media player on Home" is on, so a preview marks where the row sits. */
@Composable
private fun previewMediaPlayer(): MediaPlayerState {
    val title = stringResource(R.string.preview_media_title)
    val artist = stringResource(R.string.preview_media_artist)
    return remember(title, artist) {
        MediaPlayerState(
            packageName = "",
            title = title,
            artist = artist,
            art = null,
            isPlaying = false,
            positionMs = 70_000L,
            durationMs = 200_000L,
            positionUpdatedAtMs = 0L,
            playbackSpeed = 1f,
            canSkipPrevious = true,
            canSkipNext = true,
        )
    }
}
