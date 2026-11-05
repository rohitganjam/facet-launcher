package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.model.NativeWidgetType

/**
 * What a long-press on a page of the clock stack acts on: the clock (with its calendar), a widget Facet draws itself, or a hosted
 * widget. It decides the first card of the [WidgetSheetContent]; the rows under it are the same for all.
 */
sealed interface WidgetSheetTarget {
    data object Clock : WidgetSheetTarget
    data class Native(val type: NativeWidgetType) : WidgetSheetTarget
    data object Hosted : WidgetSheetTarget
}
