package com.facetlauncher.app.ui.home

import android.appwidget.AppWidgetHostView
import android.content.Context
import com.facetlauncher.app.data.widget.AppWidgetRepository
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
     * The floor an interactive resize drag may not shrink below — `null` when orphaned. See
     * [clockWidgetResizeFloorDp]'s own doc for the min-vs-minResize preference.
     */
    fun resizeFloorDp(appWidgetId: Int): Pair<Int, Int>? =
        appWidgetRepository.getAppWidgetInfo(appWidgetId)?.let {
            clockWidgetResizeFloorDp(it.minWidth, it.minHeight, it.minResizeWidth, it.minResizeHeight)
        }
}

/**
 * PRD F15 — the real floor a resize drag may not shrink a hosted clock widget below. Prefers the
 * provider's own declared `minResizeWidth`/`minResizeHeight` (the size it explicitly says it can
 * still render sanely at) over its `minWidth`/`minHeight` (its *default* placement size, which is
 * commonly larger than what it can actually shrink to) — falls back to `minWidth`/`minHeight` only
 * when a provider leaves `minResizeWidth`/`minResizeHeight` unset (`0`, the platform's own "not
 * declared" sentinel for these fields). A pure function of the provider's own declared ints, kept
 * top-level (not a method) so it's trivially unit-testable without mocking
 * [android.appwidget.AppWidgetProviderInfo] or [AppWidgetRepository] at all.
 */
fun clockWidgetResizeFloorDp(minWidth: Int, minHeight: Int, minResizeWidth: Int, minResizeHeight: Int): Pair<Int, Int> {
    val width = if (minResizeWidth > 0) minResizeWidth else minWidth
    val height = if (minResizeHeight > 0) minResizeHeight else minHeight
    return width to height
}
