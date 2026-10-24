package com.facetlauncher.app.ui.home.clock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.domain.ResolvedClockStyle
import com.facetlauncher.app.domain.resolveFacetStyle
import com.facetlauncher.app.data.model.landscape
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * `global*` fields are always the launcher-wide default — read directly for the Inherit row's
 * subtitle and to seed a facet's own row the moment it starts overriding. The effective,
 * screen-bound properties below resolve facet-or-global the same way `HomeUiState`/
 * `FacetCarouselViewModel` do at runtime: this facet's own value while [isOverriding], the global
 * default otherwise — matching [com.facetlauncher.app.ui.settings.CalendarSettingsUiState]'s shape.
 */
data class ClockStyleGalleryUiState(
    val facet: Facet? = null,
    val settings: LauncherSettings = LauncherSettings(),
) {
    val isFacetScoped: Boolean get() = facet != null
    val isOverriding: Boolean get() = facet?.overrideClock ?: false
    private val clock: ResolvedClockStyle get() = resolveFacetStyle(facet, settings).clock
    /** Global only: resolves [ClockFontOption.LAUNCHER_DEFAULT]'s preview here regardless of whether this instance is facet-scoped. */
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
    /** Home clock's horizontal placement, in the same Clock+Calendar design bundle as [templateId]; it also positions the calendar events strip. */
    val clockAlignment: ClockAlignment get() = clock.alignment
}

/** Backs the global or facet-scoped clock style gallery. */
@HiltViewModel
class ClockStyleGalleryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
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

    fun setClockTemplateId(id: ClockTemplateId) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setClockTemplateId(it.id, id) }
            } ?: settingsRepository.setClockTemplateId(id)
        }
    }

    fun setClockFontOption(option: ClockFontOption) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setClockFontOption(it.id, option) }
            } ?: settingsRepository.setClockFontOption(option)
        }
    }

    fun setClockColorOption(option: ClockColorOption) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setClockColorOption(it.id, option) }
            } ?: settingsRepository.setClockColorOption(option)
        }
    }

    fun setClockAccentColorOption(option: ClockColorOption) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setClockAccentColorOption(it.id, option) }
            } ?: settingsRepository.setClockAccentColorOption(option)
        }
    }

    fun setUse24HourTime(enabled: Boolean) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setUse24HourTime(it.id, enabled) }
            } ?: settingsRepository.setUse24HourTime(enabled)
        }
    }

    fun setClockShowMeridiem(enabled: Boolean) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setClockShowMeridiem(it.id, enabled) }
            } ?: settingsRepository.setClockShowMeridiem(enabled)
        }
    }

    fun setClockDateStyle(dateStyle: ClockDateStyle) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setClockDateStyle(it.id, dateStyle) }
            } ?: settingsRepository.setClockDateStyle(dateStyle)
        }
    }

    fun setClockAlignment(alignment: ClockAlignment) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setClockAlignment(it.id, alignment) }
            } ?: settingsRepository.setClockAlignment(alignment)
        }
    }

    /**
     * The Inherit/Override switch for this facet's whole clock design bundle — moved in from
     * `FacetSettingsScreen` so it sits on the same screen as the controls it gates (see chat
     * history). Switching to Override seeds the facet's stored values with the current *effective*
     * ones (`uiState.value`'s own properties, which still resolve to the global default at the
     * moment this runs, since [ClockStyleGalleryUiState.isOverriding] hasn't flipped yet) so the
     * screen's controls don't visibly jump when editing becomes live; switching back to Inherit
     * just clears the flag, no data loss (the facet's own values stay stored, re-shown if the user
     * overrides again later). `clockZoneHeightDp`/`clockScale` aren't touched here — they're edited
     * via the drag handles on Home itself, not this screen, so [FacetRepository.updateOverridingClock]'s
     * defaults (preserve the facet's current values) apply.
     */
    fun setOverridingClock(overriding: Boolean) {
        val pid = facetId ?: return
        val state = uiState.value
        viewModelScope.launch {
            val facet = facetRepository.getById(pid) ?: return@launch
            facetRepository.updateOverridingClock(
                facetId = facet.id,
                overriding = overriding,
                templateId = state.templateId,
                fontOption = state.fontOption,
                colorOption = state.colorOption,
                use24HourTime = state.use24HourTime,
                showMeridiem = state.showMeridiem,
                clockAlignment = state.clockAlignment,
                landscapeClockAlignment = if (overriding) state.settings.landscape.clockAlignment else facet.landscape.clockAlignment,
                clockZoneHeightDp = facet.clockZoneHeightDp,
                clockScale = facet.clockScale,
                accentColorOption = state.accentColorOption,
                dateStyle = state.dateStyle,
                showAlarm = if (overriding) state.settings.showNextAlarm else facet.clockShowAlarm,
                showBattery = if (overriding) state.settings.showBatteryPercentage else facet.clockShowBattery,
                showWeather = if (overriding) state.settings.showWeather else facet.clockShowWeather,
            )
        }
    }

    /**
     * "Reset clock widget position" — restores the whole clock+calendar widget to its original,
     * untouched layout: the zone height back to `null` (fixed-top, bottom-anchored app list),
     * alignment back to `LEFT`, and scale back to `0.8f`.
     * Resets this facet's own values when scoped, the global defaults otherwise — same branching
     * as every setter above.
     */
    fun resetClockPosition() {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.resetClockPosition(it.id) }
            } ?: run {
                settingsRepository.resetClockZoneHeight()
                settingsRepository.resetClockScale()
                settingsRepository.setClockAlignment(ClockAlignment.LEFT)
                settingsRepository.updateLandscape { it.copy(clockAlignment = null) }
            }
        }
    }
}
