package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListShrinkToFit
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation

/** Every change the Home apps list's look controls can make; the Home apps screen builds one from its ViewModel. */
internal data class AppListLookActions(
    val onLayoutChange: (AppListLayout) -> Unit,
    val onPresentationChange: (AppRowPresentation) -> Unit,
    val onGridColumnsChange: (AppListGridColumns) -> Unit,
    val onGridDisplayModeChange: (AppListGridDisplayMode) -> Unit,
    val onShrinkToFitChange: (AppListShrinkToFit) -> Unit,
    val onRowPositionChange: (AppRowPosition) -> Unit,
    val onColumnAlignmentChange: (AppListColumnAlignment) -> Unit,
    val onVerticalAlignmentChange: (AppListVerticalAlignment) -> Unit,
    val onLandscapeRowPositionChange: (AppRowPosition?) -> Unit,
    val onLandscapeColumnAlignmentChange: (AppListColumnAlignment?) -> Unit,
    val onLandscapeVerticalAlignmentChange: (AppListVerticalAlignment?) -> Unit,
    val onAllowLandscapeChange: (Boolean) -> Unit,
) {
    companion object {
        val None = AppListLookActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}
