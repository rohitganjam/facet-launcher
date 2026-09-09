package com.lumenlauncher.app.ui.hub

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Wraps a real, live [AppWidgetHostView] — [AppWidgetHostView] has no native Compose equivalent
 * (per `design_handoff_minimal_launcher/PRD.md`), so this is the one `AndroidView` interop point
 * for the Hub. [createHostView] is a plain function (not a Repository call) so this composable
 * stays testable/stateless per `CLAUDE.md` — the real implementation is `HubViewModel::createHostView`.
 *
 * [tileWidth]/[tileHeight] are pushed into [AppWidgetHostView.updateAppWidgetSize] **and**
 * [onSizeChanged] on every recomposition where they change (a resize, most notably) — the outer
 * Compose `Box` resizing on its own only changes the *host* view's bounds; a widget's own
 * `RemoteViews` content doesn't reflow to fill the new space without this, which is why a resized
 * widget's content previously looked like it "snapped back" (see chat history). [onSizeChanged]
 * additionally sets `OPTION_APPWIDGET_SIZES` (see [com.lumenlauncher.app.data.widget.AppWidgetRepository.updateWidgetSize])
 * without which modern RemoteViews/Glance widgets render as "Can't show content".
 */
@Composable
fun HubWidgetTile(
    appWidgetId: Int,
    tileWidth: Dp,
    tileHeight: Dp,
    isInteracting: Boolean,
    createHostView: (Context, Int) -> AppWidgetHostView?,
    onSizeChanged: (appWidgetId: Int, widthDp: Int, heightDp: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var hostView: AppWidgetHostView? = remember { null }

    // When the launcher takes control of the widget (for move or resize), we must break
    // any active touch session within the widget itself. Momentarily toggling visibility
    // is a robust way to force the Android View system to send an ACTION_CANCEL to the 
    // widget's internal children, preventing accidental clicks on release.
    LaunchedEffect(isInteracting) {
        if (isInteracting) {
            hostView?.let { view ->
                if (view.visibility == View.VISIBLE) {
                    view.visibility = View.INVISIBLE
                    view.visibility = View.VISIBLE
                }
            }
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize().testTag("hub_widget_host_$appWidgetId"),
        factory = { context ->
            val view = createHostView(context, appWidgetId) ?: View(context)
            if (view is AppWidgetHostView) {
                hostView = view
            }
            view
        },
        update = { view ->
            if (view is AppWidgetHostView) {
                val widthDp = tileWidth.value.toInt()
                val heightDp = tileHeight.value.toInt()
                if (widthDp > 0 && heightDp > 0) {
                    view.updateAppWidgetSize(null, widthDp, heightDp, widthDp, heightDp)
                    // Last write wins on the merged options bundle, and this one carries a
                    // non-empty OPTION_APPWIDGET_SIZES (the view call above doesn't, on One UI).
                    onSizeChanged(appWidgetId, widthDp, heightDp)
                }
            }
        },
    )
}
