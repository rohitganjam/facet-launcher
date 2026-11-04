package com.facetlauncher.app.ui.nativewidget

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.NativeWidgetRef
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
    /** What each widget set to its own calendars or location shows instead of the above, Hub and Home alike. */
    val overrides: ImmutableMap<NativeWidgetRef, NativeWidgetOverride> = persistentMapOf(),
)

@Immutable
data class NativeWidgetActions(
    /** A tap on a Weather widget; [NativeWidgetRef] says which one, `null` for the clock's own weather accessory. */
    val onOpenWeather: (NativeWidgetRef?) -> Unit = {},
    val onOpenEvent: (CalendarEvent) -> Unit = {},
    val media: HomeMediaActions = HomeMediaActions(),
    /** Opens a widget's own settings: from an Agenda's 3-dot button and from its weather sheet. */
    val onConfigure: (NativeWidgetRef) -> Unit = {},
    /** The "Allow" buttons of a widget whose permission is off. */
    val onRequestCalendarAccess: () -> Unit = {},
    val onRequestLocationAccess: () -> Unit = {},
)

/** A native widget on a Home clock page: which widget, and what it reads and does. */
@Immutable
data class NativeWidgetPage(val type: NativeWidgetType, val state: NativeWidgetState, val actions: NativeWidgetActions, val ref: NativeWidgetRef)

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

/** The weather a widget shows instead of the launcher's, for the sheet opened from it; unchanged when it follows the launcher. */
fun WeatherUiState.withWeatherOverride(override: NativeWidgetOverride?): WeatherUiState =
    if (override != null && (override.weather != null || override.weatherUnavailable)) copy(weather = override.weather) else this
