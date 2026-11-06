package com.facetlauncher.app.ui.launcher

import com.facetlauncher.app.ui.home.widget.ClockWidgetPickerTarget
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import com.facetlauncher.app.data.model.NativeWidgetRef

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

    /** What the picker's choice does (replace a page or add one): set when the flow starts and fixed for its duration, like `ClockWidgetPickerViewModel`'s own pending target. */
    var clockWidgetPickerTarget by mutableStateOf<ClockWidgetPickerTarget?>(null)

    /** The weather sheet, opened from the clock's weather accessory. It is its own bottom sheet window, so it is not part of [anyOpen]. */
    var showWeatherSheet by mutableStateOf(false)

    /** The Weather widget the sheet was opened from, Hub or Home; `null` when it came from the clock's accessory. */
    var weatherSheetRef by mutableStateOf<NativeWidgetRef?>(null)

    /** The widget whose settings sheet is open. It is its own bottom sheet window, so it is not part of [anyOpen]. */
    var configureRef by mutableStateOf<NativeWidgetRef?>(null)

    /** Private Space, over the app drawer. */
    var showPrivateSpace by mutableStateOf(false)

    val anyOpen: Boolean get() = showWidgetPicker || showClockWidgetPicker || showPrivateSpace
}
