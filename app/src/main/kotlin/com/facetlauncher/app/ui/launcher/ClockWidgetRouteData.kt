package com.facetlauncher.app.ui.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.withFrameNanos
import com.facetlauncher.app.ui.home.ClockWidgetHostCache
import com.facetlauncher.app.ui.home.ClockWidgetHostController
import kotlinx.collections.immutable.ImmutableList

/** What the route knows about the widgets in the clock stack: each one's sizes, whether its app is gone, and its built host view. */
internal class ClockWidgetRouteData(
    val defaultSizesDp: Map<Int, Pair<Int, Int>?>,
    val resizeFloorsDp: Map<Int, Pair<Int, Int>?>,
    val orphans: Map<Int, Boolean>,
    val hosts: ClockWidgetHostCache,
)

/**
 * Resolved once per widget id of the stack (`getAppWidgetInfo` is a real system query), so a neighboring page is sized like the visible
 * one. [resumeCount] re-runs the orphan check, since an app can be uninstalled while Home is away.
 */
@Composable
internal fun rememberClockWidgetRouteData(stackWidgetIds: ImmutableList<Int>, host: ClockWidgetHostController, resumeCount: Int): ClockWidgetRouteData {
    val context = LocalContext.current
    val defaultSizesDp = remember(stackWidgetIds) { stackWidgetIds.associateWith { host.defaultSizeDp(it) } }
    val resizeFloorsDp = remember(stackWidgetIds) { stackWidgetIds.associateWith { host.resizeFloorDp(context, it) } }
    val orphans = remember(stackWidgetIds, resumeCount) { stackWidgetIds.associateWith { host.isOrphaned(it) } }
    // Every widget page stays built, so a swipe re-attaches a view instead of inflating one.
    val hosts = remember { ClockWidgetHostCache { viewContext, id -> host.createHostView(viewContext, id) } }
    LaunchedEffect(stackWidgetIds) {
        hosts.retainOnly(stackWidgetIds.toSet())
        // One per frame, so building them never lands inside a swipe.
        stackWidgetIds.filterNot(hosts::has).forEach {
            hosts.get(context, it)
            withFrameNanos { }
        }
    }
    DisposableEffect(hosts) { onDispose { hosts.clear() } }
    return remember(defaultSizesDp, resizeFloorsDp, orphans, hosts) { ClockWidgetRouteData(defaultSizesDp, resizeFloorsDp, orphans, hosts) }
}
