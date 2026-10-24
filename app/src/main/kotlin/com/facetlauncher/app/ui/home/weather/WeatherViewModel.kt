package com.facetlauncher.app.ui.home.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.PressureUnit
import com.facetlauncher.app.data.model.RainUnit
import com.facetlauncher.app.data.model.TemperatureUnit
import com.facetlauncher.app.data.model.WindSpeedUnit
import com.facetlauncher.app.data.weather.RefreshOutcome
import com.facetlauncher.app.domain.ObserveWeatherUseCase
import com.facetlauncher.app.domain.RefreshWeatherUseCase
import com.facetlauncher.app.domain.WeatherRefreshResult
import com.facetlauncher.app.domain.WeatherState
import com.facetlauncher.app.domain.WeatherUnits
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The units the weather is shown in, with every `AUTO` already resolved against the locale. */
data class ResolvedWeatherUnits(
    val temperature: TemperatureUnit = TemperatureUnit.CELSIUS,
    val wind: WindSpeedUnit = WindSpeedUnit.KMH,
    val rain: RainUnit = RainUnit.MM,
    val pressure: PressureUnit = PressureUnit.HPA,
)

/** What the last refresh left for the sheet to say. */
enum class WeatherNotice {
    UPDATED, UP_TO_DATE, TOO_SOON, BUSY, OFFLINE, FAILED, NEEDS_LOCATION_PERMISSION, NO_LOCATION, NO_CITY
}

data class WeatherUiState(
    val weather: WeatherState? = null,
    val units: ResolvedWeatherUnits = ResolvedWeatherUnits(),
    val notice: WeatherNotice? = null,
    val isRefreshing: Boolean = false,
    /** The launcher's 12 or 24-hour setting, for the hourly labels. */
    val use24HourTime: Boolean = false,
)

/**
 * The weather for Home: the state the accessory and the sheet show, and the refresh. Home calls [onHomeResumed] each time it
 * comes back, which refreshes only while the weather is shown, so with weather off nothing here reads the location or
 * touches the network. [refresh] is the sheet's button and its permission result; both go through the repository's gate, so
 * tapping it repeatedly cannot reach MET more than the gate allows.
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
    observeWeather: ObserveWeatherUseCase,
    settingsRepository: SettingsRepository,
    private val refreshWeather: RefreshWeatherUseCase,
) : ViewModel() {

    private val notice = MutableStateFlow<WeatherNotice?>(null)
    private val refreshing = MutableStateFlow(false)
    private var refreshJob: Job? = null

    val uiState: StateFlow<WeatherUiState> = combine(observeWeather(), settingsRepository.settings, notice, refreshing) { weather, settings, notice, refreshing ->
        val locale = Locale.getDefault()
        WeatherUiState(
            weather = weather,
            units = ResolvedWeatherUnits(
                temperature = WeatherUnits.resolve(settings.temperatureUnit, locale),
                wind = WeatherUnits.resolve(settings.windSpeedUnit, locale),
                rain = WeatherUnits.resolve(settings.rainUnit, locale),
                pressure = WeatherUnits.resolve(settings.pressureUnit, locale),
            ),
            notice = notice,
            isRefreshing = refreshing,
            use24HourTime = settings.use24HourTime,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), WeatherUiState())

    /** Called whenever Home resumes; refreshes quietly (no "up to date" message) and only while the active facet shows weather. */
    fun onHomeResumed(weatherShown: Boolean) {
        if (weatherShown) refresh(userInitiated = false)
    }

    /** Refreshes now (through the gate). [userInitiated] taps also report "already up to date" and the like. */
    fun refresh(userInitiated: Boolean = true) {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            refreshing.value = true
            notice.value = noticeFor(refreshWeather(), userInitiated)
            refreshing.value = false
        }
    }

    internal companion object {
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L

        /** A user tap hears about every result; a quiet refresh only about the ones that need the user or say something is wrong. */
        fun noticeFor(result: WeatherRefreshResult, userInitiated: Boolean): WeatherNotice? = when (result) {
            WeatherRefreshResult.NeedsLocationPermission -> WeatherNotice.NEEDS_LOCATION_PERMISSION
            WeatherRefreshResult.NoLocation -> WeatherNotice.NO_LOCATION
            WeatherRefreshResult.NoCity -> WeatherNotice.NO_CITY
            is WeatherRefreshResult.Done -> when (result.outcome) {
                is RefreshOutcome.Busy -> WeatherNotice.BUSY
                RefreshOutcome.Offline -> WeatherNotice.OFFLINE
                RefreshOutcome.Failed -> WeatherNotice.FAILED
                RefreshOutcome.Updated -> WeatherNotice.UPDATED.takeIf { userInitiated }
                RefreshOutcome.UpToDate -> WeatherNotice.UP_TO_DATE.takeIf { userInitiated }
                is RefreshOutcome.TooSoon -> WeatherNotice.TOO_SOON.takeIf { userInitiated }
            }
        }
    }
}
