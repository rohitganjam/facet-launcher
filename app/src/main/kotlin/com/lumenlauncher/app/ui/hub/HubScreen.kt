package com.lumenlauncher.app.ui.hub

import android.appwidget.AppWidgetHostView
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumenlauncher.app.ui.components.StickyHeaderLayout
import com.lumenlauncher.app.ui.theme.DrawerOverlay

/**
 * The Launcher Hub (F5) — reached by swiping right from Home. Backed by the same [DrawerOverlay]
 * scrim the Drawer itself uses (see `AppDrawerScreen`'s own root `.background(...)`), driven by
 * the same `drawerSettings.drawerOpacity` value, so opening the Hub reads the same as opening the
 * Drawer rather than as a fully transparent overlay on the wallpaper.
 */
@Composable
fun HubScreen(
    onAddClick: () -> Unit,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier,
    opacity: Float = 0.88f,
    viewModel: HubViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HubContent(
        uiState = uiState,
        createHostView = viewModel::createHostView,
        onAddClick = onAddClick,
        onManageClick = onManageClick,
        onRemoveOrphan = viewModel::onRemoveOrphan,
        onKeepOrphanSpace = { viewModel.onKeepOrphanSpace() },
        onWidgetDropped = viewModel::onWidgetDropped,
        onWidgetDroppedOnTrash = viewModel::onWidgetDroppedOnTrash,
        canResizeTo = viewModel::canResizeTo,
        onWidgetResized = viewModel::onWidgetResized,
        opacity = opacity,
        modifier = modifier,
    )
}

@Composable
private fun HubContent(
    uiState: HubUiState,
    createHostView: (Context, Int) -> AppWidgetHostView?,
    onAddClick: () -> Unit,
    onManageClick: () -> Unit,
    onRemoveOrphan: (Int) -> Unit,
    onKeepOrphanSpace: (Int) -> Unit,
    onWidgetDropped: (appWidgetId: Int, row: Int, col: Int, colSpan: Int, rowSpan: Int) -> Unit,
    onWidgetDroppedOnTrash: (Int) -> Unit,
    canResizeTo: (appWidgetId: Int, row: Int, col: Int, colSpan: Int, rowSpan: Int) -> Boolean,
    onWidgetResized: (appWidgetId: Int, row: Int, col: Int, colSpan: Int, rowSpan: Int) -> Unit,
    opacity: Float,
    modifier: Modifier = Modifier,
) {
    StickyHeaderLayout(
        // Edge-to-edge is enforced unconditionally at this app's targetSdk (36) — without this,
        // the header draws under the status bar and the grid's bottom row under the gesture/nav
        // bar, matching HomeScreen's/AppDrawerScreen's own systemBars inset handling.
        modifier = modifier.background(DrawerOverlay.copy(alpha = opacity)).windowInsetsPadding(WindowInsets.systemBars),
        header = {
            HubHeader(
                widgetCount = uiState.widgetCount,
                columns = uiState.columns,
                isAtCapacity = uiState.isAtCapacity,
                onAddClick = onAddClick,
            )
        },
        content = { headerHeight ->
            Box(modifier = Modifier.fillMaxSize().padding(top = headerHeight)) {
                if (uiState.isEmpty) {
                    HubEmptyState(onAddClick = onAddClick)
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (uiState.isAtCapacity) {
                            HubAtCapacityStrip(
                                onManageClick = onManageClick,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            )
                        }
                        HubGrid(
                            widgets = uiState.widgets,
                            columns = uiState.columns,
                            createHostView = createHostView,
                            onRemoveOrphan = onRemoveOrphan,
                            onKeepOrphanSpace = onKeepOrphanSpace,
                            onWidgetDropped = onWidgetDropped,
                            onWidgetDroppedOnTrash = onWidgetDroppedOnTrash,
                            canResizeTo = canResizeTo,
                            onWidgetResized = onWidgetResized,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
    )
}
