package com.lumenlauncher.app.ui.hub

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Wraps a real, live [AppWidgetHostView] — [AppWidgetHostView] has no native Compose equivalent
 * (per `design_handoff_minimal_launcher/PRD.md`), so this is the one `AndroidView` interop point
 * for the Hub. [createHostView] is a plain function (not a Repository call) so this composable
 * stays testable/stateless per `CLAUDE.md` — the real implementation is `HubViewModel::createHostView`.
 */
@Composable
fun HubWidgetTile(appWidgetId: Int, createHostView: (Context, Int) -> AppWidgetHostView?, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxSize().testTag("hub_widget_host_$appWidgetId"),
        factory = { context -> createHostView(context, appWidgetId) ?: View(context) },
    )
}
