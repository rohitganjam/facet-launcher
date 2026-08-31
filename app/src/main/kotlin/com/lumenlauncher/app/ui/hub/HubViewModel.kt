package com.lumenlauncher.app.ui.hub

import android.appwidget.AppWidgetHostView
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.widget.AppWidgetRepository
import com.lumenlauncher.app.domain.DeleteWidgetUseCase
import com.lumenlauncher.app.domain.ObserveHubStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The Launcher Hub (F5) — a fixed-column grid of user-placed widgets, to Home's left. */
@HiltViewModel
class HubViewModel @Inject constructor(
    observeHubState: ObserveHubStateUseCase,
    private val appWidgetRepository: AppWidgetRepository,
    private val deleteWidget: DeleteWidgetUseCase,
) : ViewModel() {

    val uiState = observeHubState().map { domainState ->
        HubUiState(
            widgets = domainState.widgets.map { widget ->
                HubWidgetUi(
                    appWidgetId = widget.appWidgetId,
                    row = widget.row,
                    col = widget.col,
                    colSpan = widget.colSpan,
                    rowSpan = widget.rowSpan,
                    providerLabel = widget.providerLabel,
                    isOrphaned = widget.isOrphaned,
                )
            },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HubUiState())

    /** Scoped to Hub's own visible lifetime — avoids unnecessary background IPC/battery cost. */
    fun onHubVisible() {
        appWidgetRepository.startListening()
    }

    fun onHubHidden() {
        appWidgetRepository.stopListening()
    }

    /** `null` when the widget id has no resolvable provider (e.g. it's orphaned). */
    fun createHostView(context: Context, appWidgetId: Int): AppWidgetHostView? =
        appWidgetRepository.getAppWidgetInfo(appWidgetId)?.let { info ->
            appWidgetRepository.createHostView(context, appWidgetId, info)
        }

    fun onRemoveOrphan(appWidgetId: Int) {
        viewModelScope.launch { deleteWidget(appWidgetId) }
    }

    /** The footprint/dashed placeholder simply stays until the provider's app is reinstalled — no state to change. */
    fun onKeepOrphanSpace() = Unit
}
