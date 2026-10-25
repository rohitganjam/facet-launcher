package com.facetlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.LocationRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PressureUnit
import com.facetlauncher.app.data.model.RainUnit
import com.facetlauncher.app.data.model.TemperatureUnit
import com.facetlauncher.app.data.model.WindSpeedUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The Units and location screen: how weather numbers are written and where the weather is for. Launcher-wide, never per facet. */
data class UnitsAndLocationUiState(
    val settings: LauncherSettings = LauncherSettings(),
    val locationPermissionGranted: Boolean = false,
)

@HiltViewModel
class UnitsAndLocationViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val permissionGranted = MutableStateFlow(locationRepository.isPermissionGranted())

    val uiState: StateFlow<UnitsAndLocationUiState> = combine(settingsRepository.settings, permissionGranted) { settings, granted ->
        UnitsAndLocationUiState(settings = settings, locationPermissionGranted = granted)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), UnitsAndLocationUiState())

    /** Called when the screen resumes: the grant can change on the Permissions screen. */
    fun refreshPermission() {
        permissionGranted.value = locationRepository.isPermissionGranted()
    }

    fun setTemperatureUnit(unit: TemperatureUnit) = launch { settingsRepository.setTemperatureUnit(unit) }

    fun setWindSpeedUnit(unit: WindSpeedUnit) = launch { settingsRepository.setWindSpeedUnit(unit) }

    fun setRainUnit(unit: RainUnit) = launch { settingsRepository.setRainUnit(unit) }

    fun setPressureUnit(unit: PressureUnit) = launch { settingsRepository.setPressureUnit(unit) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
