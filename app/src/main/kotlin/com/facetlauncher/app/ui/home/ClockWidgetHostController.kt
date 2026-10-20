package com.facetlauncher.app.ui.home

import android.appwidget.AppWidgetHostView
import android.content.Context
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.domain.calculateHubCellWidth
import javax.inject.Inject

/**
 * PRD F15's clock-widget hosting hooks — pure pass-throughs to [AppWidgetRepository], mirroring
 * [com.facetlauncher.app.ui.hub.HubViewModel]'s own `createHostView`/`updateWidgetSize` shape one
 * level removed. Extracted out of [HomeViewModel] itself (which injects this rather than
 * [AppWidgetRepository] directly) purely to stay under this codebase's max-functions-per-class
 * lint rule — [HomeViewModel] already carries every other Home concern, and none of these three
 * methods read or write its own `_uiState`, unlike [HomeViewModel.switchToLauncherClockWidget]
 * (which stays there since it's real ViewModel-level orchestration, not a hosting hook).
 */
class ClockWidgetHostController @Inject constructor(
    private val appWidgetRepository: AppWidgetRepository,
) {
    /**
     * Mirrors [com.facetlauncher.app.ui.hub.HubViewModel.createHostView] exactly (same reasoning:
     * blocks the widget's own internal long-press so it doesn't fight this launcher's own
     * long-press-to-open-the-adjust-sheet gesture on the clock). `null` when [appWidgetId] has no
     * resolvable provider (orphaned).
     */
    fun createHostView(context: Context, appWidgetId: Int): AppWidgetHostView? =
        appWidgetRepository.getAppWidgetInfo(appWidgetId)?.let { info ->
            appWidgetRepository.createHostView(context, appWidgetId, info).apply {
                setOnLongClickListener { true }
            }
        }

    /**
     * A freshly-bound clock widget's starting size, straight from its own declared
     * [android.appwidget.AppWidgetProviderInfo.minWidth]/`minHeight` (already dp, per the
     * platform's own contract — no density conversion needed) — `null` when orphaned. This is a
     * placeholder default only: real interactive resize (dragging to a size the user actually
     * wants) is a follow-up; screen-fit clamping happens in the UI layer, same as the native
     * clock's own `maxScaleThatFits`.
     */
    fun defaultSizeDp(appWidgetId: Int): Pair<Int, Int>? =
        appWidgetRepository.getAppWidgetInfo(appWidgetId)?.let { it.minWidth to it.minHeight }

    /** The hosted clock widget's live on-screen size (dp) — see [AppWidgetRepository.updateWidgetSize]. */
    fun updateSize(appWidgetId: Int, widthDp: Int, heightDp: Int) =
        appWidgetRepository.updateWidgetSize(appWidgetId, widthDp, heightDp)

    /**
     * The smallest a resize drag may make the widget: one Hub grid cell (square), whatever the
     * provider declares — `null` when orphaned.
     */
    fun resizeFloorDp(context: Context, appWidgetId: Int): Pair<Int, Int>? =
        appWidgetRepository.getAppWidgetInfo(appWidgetId)?.let {
            val cellDp = calculateHubCellWidth(context)
            cellDp to cellDp
        }
}
