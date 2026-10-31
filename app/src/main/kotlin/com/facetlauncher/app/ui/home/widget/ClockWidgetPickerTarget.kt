package com.facetlauncher.app.ui.home.widget

/** What the clock widget picker's choice does: swap the page [itemId] in place, or append a page to [facetId]'s stack. */
sealed interface ClockWidgetPickerTarget {
    data class Replace(val itemId: Long) : ClockWidgetPickerTarget
    data class Add(val facetId: Long) : ClockWidgetPickerTarget
}
