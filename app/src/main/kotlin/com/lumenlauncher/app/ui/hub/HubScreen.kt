package com.lumenlauncher.app.ui.hub

import android.appwidget.AppWidgetHostView
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumenlauncher.app.ui.components.StickyHeaderLayout

/**
 * The Launcher Hub (F5) — reached by swiping right from Home. Transparent background throughout
 * (Home/wallpaper shows through), unlike every Settings-style screen's opaque `Surface` — see
 * `HubHeader`'s own doc comment for why its text legibility comes from a shadow, not a panel.
 */
@Composable
fun HubScreen(
    onAddClick: () -> Unit,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HubViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    HubContent(
        uiState = uiState,
        createHostView = viewModel::createHostView,
        onAddClick = onAddClick,
        onManageClick = onManageClick,
        onRemoveOrphan = viewModel::onRemoveOrphan,
        onKeepOrphanSpace = { viewModel.onKeepOrphanSpace() },
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
    modifier: Modifier = Modifier,
) {
    StickyHeaderLayout(
        modifier = modifier,
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
                        )
                    }
                }
            }
        },
    )
}
