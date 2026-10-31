package com.facetlauncher.app.ui.launcher

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf

/**
 * Which in-place sub-screens are open over Home. They are overlays inside [HomeDrawerRoute], not
 * NavHost destinations: navigating away would tear down the route, resetting the Hub's axis and
 * losing its open and scroll state, and would replace the Hub instead of loading over it.
 */
@Stable
internal class HomeOverlayState {
    /** The Hub's add-widget picker, over the Hub. */
    var showWidgetPicker by mutableStateOf(false)

    /** The clock widget picker, over Home itself (it opens from the clock adjust sheet, not from the Hub). */
    var showClockWidgetPicker by mutableStateOf(false)

    /** The stack page being replaced: set when the flow starts and fixed for its duration, like `ClockWidgetPickerViewModel`'s own pending item. */
    var clockWidgetPickerItemId by mutableStateOf<Long?>(null)

    /** The weather sheet, opened from the clock's weather accessory. It is its own bottom sheet window, so it is not part of [anyOpen]. */
    var showWeatherSheet by mutableStateOf(false)

    /** Private Space, over the app drawer. */
    var showPrivateSpace by mutableStateOf(false)

    val anyOpen: Boolean get() = showWidgetPicker || showClockWidgetPicker || showPrivateSpace
}
