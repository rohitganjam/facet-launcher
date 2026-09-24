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

/**
 * `global*` fields are always the launcher-wide default — read directly for the Inherit row's
 * subtitle and to seed a facet's own row the moment it starts overriding. The effective,
 * screen-bound properties below resolve facet-or-global the same way `HomeUiState`/
 * `FacetCarouselViewModel` do at runtime: this facet's own value while [isOverriding], the global
 * default otherwise — matching [com.facetlauncher.app.ui.settings.CalendarSettingsUiState]'s shape.
 */
data class ClockStyleGalleryUiState(
    val facet: FacetEntity? = null,
    val globalTemplateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    val globalFontOption: ClockFontOption = ClockFontOption.SYSTEM,
    val globalColorOption: ClockColorOption = ClockColorOption.THEME,
    val globalAccentColorOption: ClockColorOption = ClockColorOption.ACCENT_PRIMARY,
    val globalUse24HourTime: Boolean = false,
    val globalShowMeridiem: Boolean = false,
    val globalDateStyle: ClockDateStyle = ClockDateStyle.FULL,
    /** Global only — resolves [ClockFontOption.LAUNCHER_DEFAULT]'s preview here regardless of whether this instance is facet-scoped. */
    val launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
    val globalClockAlignment: ClockAlignment = ClockAlignment.LEFT,
) {
    val isFacetScoped: Boolean get() = facet != null
    val isOverriding: Boolean get() = facet?.overrideClock ?: false
    val templateId: ClockTemplateId get() = if (isOverriding) facet?.clockTemplateId ?: globalTemplateId else globalTemplateId
    val fontOption: ClockFontOption get() = if (isOverriding) facet?.clockFontOption ?: globalFontOption else globalFontOption
    val colorOption: ClockColorOption get() = if (isOverriding) facet?.clockColorOption ?: globalColorOption else globalColorOption
    /** Only meaningful for templates where [com.facetlauncher.app.data.model.usesAccentColor] is true. */
    val accentColorOption: ClockColorOption get() = if (isOverriding) facet?.clockAccentColorOption ?: globalAccentColorOption else globalAccentColorOption
    val use24HourTime: Boolean get() = if (isOverriding) facet?.use24HourTime ?: globalUse24HourTime else globalUse24HourTime
    val showMeridiem: Boolean get() = if (isOverriding) facet?.clockShowMeridiem ?: globalShowMeridiem else globalShowMeridiem
    val dateStyle: ClockDateStyle get() = if (isOverriding) facet?.clockDateStyle ?: globalDateStyle else globalDateStyle
    /** Home clock's horizontal placement — part of the same Clock+Calendar design bundle as [templateId]/etc, so it resolves and persists the same way. Also positions the calendar events strip now, which has no alignment of its own. */
    val clockAlignment: ClockAlignment get() = if (isOverriding) facet?.clockAlignment ?: globalClockAlignment else globalClockAlignment
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
            globalTemplateId = settings.clockTemplateId,
            globalFontOption = settings.clockFontOption,
            globalColorOption = settings.clockColorOption,
            globalAccentColorOption = settings.clockAccentColorOption,
            globalUse24HourTime = settings.use24HourTime,
            globalShowMeridiem = settings.clockShowMeridiem,
            globalDateStyle = settings.clockDateStyle,
            launcherFontOption = settings.launcherFontOption,
            globalClockAlignment = settings.clockAlignment,
        )
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

    fun setClockAlignment(alignment: ClockAlignment) {
        viewModelScope.launch {
            facetId?.let { pid ->
                facetRepository.getById(pid)?.let { facetRepository.setClockAlignment(it, alignment) }
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
                facet = facet,
                overriding = overriding,
                templateId = state.templateId,
                fontOption = state.fontOption,
                colorOption = state.colorOption,
                use24HourTime = state.use24HourTime,
                showMeridiem = state.showMeridiem,
                clockAlignment = state.clockAlignment,
                accentColorOption = state.accentColorOption,
                dateStyle = state.dateStyle,
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
                facetRepository.getById(pid)?.let { facetRepository.resetClockPosition(it) }
            } ?: run {
                settingsRepository.resetClockZoneHeight()
                settingsRepository.resetClockScale()
                settingsRepository.setClockAlignment(ClockAlignment.LEFT)
            }
        }
    }
}
