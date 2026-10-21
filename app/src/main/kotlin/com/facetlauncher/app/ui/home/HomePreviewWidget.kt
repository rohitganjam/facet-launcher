package com.facetlauncher.app.ui.home

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/** Horizontal room a Home preview keeps on each side of its content (its 18dp padding). */
private val PREVIEW_HORIZONTAL_INSET = 18.dp

/** A hosted clock widget placed in a preview: its id and the real-dp box Home would give it. */
internal data class PreviewWidgetBox(val appWidgetId: Int, val widthDp: Dp, val heightDp: Dp)

/**
 * Where [widget] sits in a preview: its saved size, else the provider's own default from
 * [defaultSizeDp] (as on Home), kept within [maxWidth]. `null` when neither size is known, so the
 * preview draws the native clock instead.
 */
internal fun previewWidgetBox(widget: PreviewClockWidget?, defaultSizeDp: (Int) -> Pair<Int, Int>?, maxWidth: Dp): PreviewWidgetBox? {
    val size = widget?.let { it.sizeDp ?: defaultSizeDp(it.appWidgetId) }
    return if (widget != null && size != null) {
        PreviewWidgetBox(widget.appWidgetId, size.first.dp.coerceAtMost(maxWidth), size.second.dp)
    } else {
        null
    }
}

/** [previewWidgetBox] for the current screen; `null` without a [host] to read the provider from. */
@Composable
internal fun rememberPreviewWidgetBox(widget: PreviewClockWidget?, host: ClockWidgetHostController?): PreviewWidgetBox? {
    val maxWidth = LocalConfiguration.current.screenWidthDp.dp - PREVIEW_HORIZONTAL_INSET * 2
    return remember(widget, host, maxWidth) {
        host?.let { previewWidgetBox(widget, it::defaultSizeDp, maxWidth) }
    }
}

/**
 * The widget as a non-live copy, drawn at its real size and scaled down with the rest of the preview.
 * The view is laid out in real dp (the widget picks its layout for that size) and shrunk by a
 * graphics layer, because the preview's own density is already scaled and would otherwise clip it.
 */
@Composable
internal fun PreviewClockWidgetView(host: ClockWidgetHostController, box: PreviewWidgetBox, modifier: Modifier = Modifier) {
    val previewScale = LocalDensity.current.density / LocalContext.current.resources.displayMetrics.density
    Box(modifier = modifier.size(box.widthDp, box.heightDp).clipToBounds().testTag("preview_clock_widget_${box.appWidgetId}")) {
        AndroidView(
            modifier = Modifier
                .requiredSize(box.widthDp / previewScale, box.heightDp / previewScale)
                .graphicsLayer {
                    scaleX = previewScale
                    scaleY = previewScale
                    transformOrigin = TransformOrigin(0f, 0f)
                },
            factory = { context -> host.createPreviewHostView(context, box.appWidgetId) ?: View(context) },
        )
    }
}
