package com.facetlauncher.app.ui.home.clock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ClockStyleGalleryUiState(
    val templateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    val fontOption: ClockFontOption = ClockFontOption.SYSTEM,
    val colorOption: ClockColorOption = ClockColorOption.THEME,
    /** Only meaningful for templates where [com.facetlauncher.app.data.model.usesAccentColor] is true. */
    val accentColorOption: ClockColorOption = ClockColorOption.ACCENT_PRIMARY,
    val use24HourTime: Boolean = false,
    val showMeridiem: Boolean = false,
    val dateStyle: ClockDateStyle = ClockDateStyle.FULL,
    /** The calendar events strip's own font/color — part of this same Clock+Calendar design block/override, not a separate one (see chat history). */
    val calendarFontOption: ClockFontOption = ClockFontOption.LAUNCHER_DEFAULT,
    val calendarColorOption: ClockColorOption = ClockColorOption.THEME,
    val calendarFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    /** Global only — resolves [ClockFontOption.LAUNCHER_DEFAULT]'s preview here regardless of whether this instance is facet-scoped. */
    val launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
    /** Home clock's horizontal placement — part of the same Clock+Calendar design bundle as [templateId]/etc, so it resolves and persists the same way (this facet's own value when scoped, the global default otherwise). */
    val clockAlignment: ClockAlignment = ClockAlignment.LEFT,
    /** Home calendar strip's horizontal placement — independent of [clockAlignment], same resolution shape. */
    val calendarAlignment: ClockAlignment = ClockAlignment.LEFT,
)

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
        val facet = facets.find { it.id == facetId }
        if (facet != null) {
            ClockStyleGalleryUiState(
                templateId = facet.clockTemplateId,
                fontOption = facet.clockFontOption,
                colorOption = facet.clockColorOption,
                accentColorOption = facet.clockAccentColorOption,
                use24HourTime = facet.use24HourTime,
                showMeridiem = facet.clockShowMeridiem,
                dateStyle = facet.clockDateStyle,
                calendarFontOption = facet.calendarFontOption,
                calendarColorOption = facet.calendarColorOption,
                calendarFontWeight = facet.calendarFontWeight,
                launcherFontOption = settings.launcherFontOption,
                clockAlignment = facet.clockAlignment,
                calendarAlignment = facet.calendarAlignment,
            )
        } else {
            ClockStyleGalleryUiState(
                templateId = settings.clockTemplateId,
                fontOption = settings.clockFontOption,
                colorOption = settings.clockColorOption,
                accentColorOption = settings.clockAccentColorOption,
                use24HourTime = settings.use24HourTime,
                showMeridiem = settings.clockShowMeridiem,
                dateStyle = settings.clockDateStyle,
                calendarFontOption = settings.calendarFontOption,
                calendarColorOption = settings.calendarColorOption,
                calendarFontWeight = settings.calendarFontWeight,
                launcherFontOption = settings.launcherFontOption,
                clockAlignment = settings.clockAlignment,
                calendarAlignment = settings.calendarAlignment,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClockStyleGalleryUiState())

    fun setClockTemplateId(id: ClockTemplateId) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setClockTemplateId(it, id) }
            } ?: settingsRepository.setClockTemplateId(id)
        }
    }

    fun setClockFontOption(option: ClockFontOption) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setClockFontOption(it, option) }
            } ?: settingsRepository.setClockFontOption(option)
        }
    }

    fun setClockColorOption(option: ClockColorOption) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setClockColorOption(it, option) }
            } ?: settingsRepository.setClockColorOption(option)
        }
    }

    fun setClockAccentColorOption(option: ClockColorOption) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setClockAccentColorOption(it, option) }
            } ?: settingsRepository.setClockAccentColorOption(option)
        }
    }

    fun setUse24HourTime(enabled: Boolean) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setUse24HourTime(it, enabled) }
            } ?: settingsRepository.setUse24HourTime(enabled)
        }
    }

    fun setClockShowMeridiem(enabled: Boolean) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setClockShowMeridiem(it, enabled) }
            } ?: settingsRepository.setClockShowMeridiem(enabled)
        }
    }

    fun setClockDateStyle(dateStyle: ClockDateStyle) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setClockDateStyle(it, dateStyle) }
            } ?: settingsRepository.setClockDateStyle(dateStyle)
        }
    }

    fun setCalendarFontOption(option: ClockFontOption) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setCalendarFontOption(it, option) }
            } ?: settingsRepository.setCalendarFontOption(option)
        }
    }

    fun setCalendarColorOption(option: ClockColorOption) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setCalendarColorOption(it, option) }
            } ?: settingsRepository.setCalendarColorOption(option)
        }
    }

    fun setCalendarFontWeight(weight: FontWeightOption) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setCalendarFontWeight(it, weight) }
            } ?: settingsRepository.setCalendarFontWeight(weight)
        }
    }

    fun setClockAlignment(alignment: ClockAlignment) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setClockAlignment(it, alignment) }
            } ?: settingsRepository.setClockAlignment(alignment)
        }
    }

    /** Independent of [setClockAlignment]. */
    fun setCalendarAlignment(alignment: ClockAlignment) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setCalendarAlignment(it, alignment) }
            } ?: settingsRepository.setCalendarAlignment(alignment)
        }
    }

    /**
     * "Reset clock widget position" — restores the whole clock+calendar widget to its original,
     * untouched layout: the zone height back to `null` (fixed-top, bottom-anchored app list),
     * BOTH alignments back to `LEFT`, and scale back to `0.8f`.
     * Resets this facet's own values when scoped, the global defaults otherwise — same branching
     * as every setter above.
     */
    fun resetClockPosition() {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.resetClockPosition(it) }
            } ?: run {
                settingsRepository.resetClockZoneHeight()
                settingsRepository.resetClockScale()
                settingsRepository.setClockAlignment(ClockAlignment.LEFT)
                settingsRepository.setCalendarAlignment(ClockAlignment.LEFT)
            }
        }
    }
}
