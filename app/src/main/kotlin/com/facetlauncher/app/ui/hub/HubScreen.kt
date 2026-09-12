package com.facetlauncher.app.ui.hub

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
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.theme.SurfaceContainer

/**
 * The Launcher Hub (F5) — reached by swiping right from Home. A translucent overlay on Home:
 * [SurfaceContainer] (every settings screen's own page background, and the Switch Profiles
 * carousel's) at its own fixed [opacity] — deliberately *not* tied to `drawerSettings.drawerOpacity`.
 */
@Composable
fun HubScreen(
    onAddClick: () -> Unit,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier,
    opacity: Float = 0.6f,
    viewModel: HubViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HubContent(
        uiState = uiState,
        createHostView = viewModel::createHostView,
        onWidgetSizeChanged = viewModel::updateWidgetSize,
        onAddClick = onAddClick,
        onManageClick = onManageClick,
        onRemoveOrphan = viewModel::onRemoveOrphan,
        onKeepOrphanSpace = { viewModel.onKeepOrphanSpace() },
        onWidgetDropped = viewModel::onWidgetDropped,
        onWidgetDroppedOnTrash = viewModel::onWidgetDroppedOnTrash,
        onWidgetResized = viewModel::onWidgetResized,
        opacity = opacity,
        modifier = modifier,
    )
}

@Composable
private fun HubContent(
    uiState: HubUiState,
    createHostView: (Context, Int) -> AppWidgetHostView?,
    onWidgetSizeChanged: (appWidgetId: Int, widthDp: Int, heightDp: Int) -> Unit,
    onAddClick: () -> Unit,
    onManageClick: () -> Unit,
    onRemoveOrphan: (Int) -> Unit,
    onKeepOrphanSpace: (Int) -> Unit,
    onWidgetDropped: (appWidgetId: Int, row: Int, col: Int, colSpan: Int, rowSpan: Int) -> Unit,
    onWidgetDroppedOnTrash: (Int) -> Unit,
    onWidgetResized: (appWidgetId: Int, row: Int, col: Int, colSpan: Int, rowSpan: Int) -> Unit,
    opacity: Float,
    modifier: Modifier = Modifier,
) {
    StickyHeaderLayout(
        // Edge-to-edge is enforced unconditionally at this app's targetSdk (36) — without this,
        // the header draws under the status bar and the grid's bottom row under the gesture/nav
        // bar, matching HomeScreen's/AppDrawerScreen's own systemBars inset handling.
        modifier = modifier.background(SurfaceContainer.copy(alpha = opacity)).windowInsetsPadding(WindowInsets.systemBars),
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
                            onWidgetSizeChanged = onWidgetSizeChanged,
                            onRemoveOrphan = onRemoveOrphan,
                            onKeepOrphanSpace = onKeepOrphanSpace,
                            onWidgetDropped = onWidgetDropped,
                            onWidgetDroppedOnTrash = onWidgetDroppedOnTrash,
                            onWidgetResized = onWidgetResized,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
    )
}
