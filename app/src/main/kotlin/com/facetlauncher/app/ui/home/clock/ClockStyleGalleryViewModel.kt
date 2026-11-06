package com.facetlauncher.app.ui.home.clock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.domain.UpdateClockConfigUseCase
import com.facetlauncher.app.domain.effectiveClockConfig
import com.facetlauncher.app.data.model.landscape
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.data.model.WeatherIconStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The look of one facet's clock, as it is now: its own settings, no launcher default behind them. */
data class ClockStyleGalleryUiState(
    val facet: Facet? = null,
    val settings: LauncherSettings = LauncherSettings(),
) {
    private val clock: ClockWidgetConfig get() = effectiveClockConfig(facet, settings)
    /** Resolves [ClockFontOption.LAUNCHER_DEFAULT]'s preview. */
    val launcherFontOption: LauncherFontOption get() = settings.launcherFontOption
    val templateId: ClockTemplateId get() = clock.templateId
    val fontOption: ClockFontOption get() = clock.fontOption
    val colorOption: ClockColorOption get() = clock.colorOption
    /** Only meaningful for templates where [com.facetlauncher.app.data.model.usesAccentColor] is true. */
    val accentColorOption: ClockColorOption get() = clock.accentColorOption
    val use24HourTime: Boolean get() = clock.use24HourTime
    val showMeridiem: Boolean get() = clock.showMeridiem
    val dateStyle: ClockDateStyle get() = clock.dateStyle
    val showWeather: Boolean get() = clock.showWeather
    val weatherIconStyle: WeatherIconStyle get() = clock.weatherIconStyle
    /** Home clock's horizontal placement, in the same Clock+Calendar design bundle as [templateId]; it also positions the calendar events strip. */
}

/** Backs a facet's clock style gallery. */
@HiltViewModel
class ClockStyleGalleryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val updateClockConfig: UpdateClockConfigUseCase,
) : ViewModel() {

    private val facetId: Long? = savedStateHandle.get<Long>("facetId")?.takeIf { it != NO_ACTIVE_FACET_ID }

    val uiState: StateFlow<ClockStyleGalleryUiState> = combine(
        settingsRepository.settings,
        facetRepository.observeFacets(),
    ) { settings, facets ->
        ClockStyleGalleryUiState(
            facet = facets.find { it.id == facetId },
            settings = settings,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClockStyleGalleryUiState())

    private fun change(edit: (ClockWidgetConfig) -> ClockWidgetConfig) {
        val id = facetId ?: return
        viewModelScope.launch { updateClockConfig(id, edit) }
    }

    fun setClockTemplateId(id: ClockTemplateId) = change { it.copy(templateId = id) }

    fun setClockFontOption(option: ClockFontOption) = change { it.copy(fontOption = option) }

    fun setClockColorOption(option: ClockColorOption) = change { it.copy(colorOption = option) }

    fun setClockAccentColorOption(option: ClockColorOption) = change { it.copy(accentColorOption = option) }

    fun setUse24HourTime(enabled: Boolean) = change { it.copy(use24HourTime = enabled) }

    fun setClockShowMeridiem(enabled: Boolean) = change { it.copy(showMeridiem = enabled) }

    fun setClockDateStyle(dateStyle: ClockDateStyle) = change { it.copy(dateStyle = dateStyle) }

    /**
     * "Reset clock position": this facet stops owning its placement and goes back to the launcher's fallback (zone
     * height unset, alignment left, scale 0.8). Only a facet has a placement to reset, so the launcher-wide scope does nothing.
     */
    fun resetClockPosition() {
        val pid = facetId ?: return
        viewModelScope.launch { facetRepository.resetClockPosition(pid) }
    }
}
