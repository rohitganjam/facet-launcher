package com.facetlauncher.app.ui.nativewidget

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.domain.NativeWidgetOverride
import com.facetlauncher.app.ui.home.HomeCalendarState
import com.facetlauncher.app.ui.home.HomeMediaActions
import com.facetlauncher.app.ui.home.HomeMediaState
import com.facetlauncher.app.ui.home.weather.WeatherUiState

/** What the widgets Facet draws itself show: the same weather, calendar and media state Home already holds. */
@Immutable
data class NativeWidgetState(
    val weather: WeatherUiState = WeatherUiState(),
    val calendar: HomeCalendarState = HomeCalendarState(),
    val media: HomeMediaState = HomeMediaState(),
    val access: NativeWidgetAccess = NativeWidgetAccess(),
)

@Immutable
data class NativeWidgetActions(
    val onOpenWeather: () -> Unit = {},
    val onOpenEvent: (CalendarEvent) -> Unit = {},
    val media: HomeMediaActions = HomeMediaActions(),
    /** Opens the widget's own settings; the Hub's menu offers it for Weather and Agenda. */
    val onConfigure: (appWidgetId: Int) -> Unit = {},
    /** The "Allow" buttons of a widget whose permission is off. */
    val onRequestCalendarAccess: () -> Unit = {},
    val onRequestLocationAccess: () -> Unit = {},
)

/** A native widget on a Home clock page: which widget, and what it reads and does. */
@Immutable
data class NativeWidgetPage(val type: NativeWidgetType, val state: NativeWidgetState, val actions: NativeWidgetActions)

/** What the Hub gives the widgets Facet draws itself on its grid: the data they read and what a tap does. */
@Immutable
data class NativeWidgetEnvironment(val state: NativeWidgetState = NativeWidgetState(), val actions: NativeWidgetActions = NativeWidgetActions())

/** Every widget Facet can draw, as the stable list the pickers take. */
val ALL_NATIVE_WIDGETS: ImmutableList<NativeWidgetType> = NativeWidgetType.entries.toImmutableList()

/** [this] with what one widget is set to in place of the launcher-wide data; unchanged when [override] is `null`. */
fun NativeWidgetState.withOverride(override: NativeWidgetOverride?): NativeWidgetState = when {
    override == null -> this
    else -> copy(
        weather = if (override.weather != null || override.weatherUnavailable) weather.copy(weather = override.weather) else weather,
        calendar = override.events?.let { calendar.copy(events = it.toImmutableList()) } ?: calendar,
        access = when {
            override.weatherUnavailable -> access.copy(locationMissing = override.locationMissing)
            override.weather != null -> access.copy(locationMissing = false)
            else -> access
        },
    )
}
