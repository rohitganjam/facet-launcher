package com.lumenlauncher.app.ui.home

import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
import com.lumenlauncher.app.data.model.CalendarEvent
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.data.model.ListContentMode

data class HomeUiState(
    val settings: LauncherSettings = LauncherSettings(),
    /** True until the first real state emission arrives — see [HomeViewModel]'s own doc for why this exists and how it resolves. */
    val isLoading: Boolean = true,
    val dockApps: List<AppInfo> = emptyList(),
    /** Favorites, Recents, or Most Used, depending on the active profile's [ListContentMode]. */
    val appListItems: List<AppInfo> = emptyList(),
    val profiles: List<ProfileEntity> = emptyList(),
    val usageAccessGranted: Boolean = false,
    val calendarEvents: List<CalendarEvent> = emptyList(),
    val badgeCounts: Map<String, Int> = emptyMap(),
    /** Set once the user taps the usage-access prompt's own button — the prompt then stays hidden
     *  for the rest of this ViewModel's lifetime regardless of whether they actually grant the
     *  permission afterward, rather than reappearing every recomposition until [usageAccessGranted]
     *  catches up (see chat history). */
    val usageAccessPromptDismissed: Boolean = false,
) {
    private val activeProfile: ProfileEntity?
        get() = profiles.find { it.id == settings.activeProfileId }

    /** The active profile's clock settings, falling back to the global defaults when not overriding. */
    val clockTemplateId: ClockTemplateId
        get() = activeProfile?.let { if (it.overrideClock) it.clockTemplateId else settings.clockTemplateId } ?: settings.clockTemplateId

    val clockFontOption: ClockFontOption
        get() = activeProfile?.let { if (it.overrideClock) it.clockFontOption else settings.clockFontOption } ?: settings.clockFontOption

    val clockColorOption: ClockColorOption
        get() = activeProfile?.let { if (it.overrideClock) it.clockColorOption else settings.clockColorOption } ?: settings.clockColorOption

    /** The active profile's calendar font, falling back to the global default when not overriding — mirrors [clockFontOption] but gated by [com.lumenlauncher.app.data.local.ProfileEntity.overrideCalendar], not `overrideClock`. */
    val activeCalendarFontOption: ClockFontOption
        get() = activeProfile?.let { if (it.overrideCalendar) it.calendarFontOption else settings.calendarFontOption } ?: settings.calendarFontOption

    val activeCalendarColorOption: ClockColorOption
        get() = activeProfile?.let { if (it.overrideCalendar) it.calendarColorOption else settings.calendarColorOption } ?: settings.calendarColorOption

    /** Mirrors [activeCalendarFontOption]'s own gating exactly — same `overrideCalendar` flag, same fallback shape. */
    val activeCalendarFontWeight: FontWeightOption
        get() = activeProfile?.let { if (it.overrideCalendar) it.calendarFontWeight else settings.calendarFontWeight } ?: settings.calendarFontWeight

    val effectiveUse24HourTime: Boolean
        get() = activeProfile?.let { if (it.overrideClock) it.use24HourTime else settings.use24HourTime } ?: settings.use24HourTime

    val clockShowMeridiem: Boolean
        get() = activeProfile?.let { if (it.overrideClock) it.clockShowMeridiem else settings.clockShowMeridiem } ?: settings.clockShowMeridiem

    val activeListContentMode: ListContentMode
        get() = activeProfile?.let { if (it.overrideApps) it.listContentMode else settings.listContentMode } ?: settings.listContentMode

    /** The active profile's app-row position, falling back to the global default when not overriding. */
    val activeAppRowPosition: AppRowPosition
        get() = activeProfile?.let { if (it.overrideApps) it.appRowPosition else settings.appRowPosition } ?: settings.appRowPosition

    /** The active profile's app-row presentation, falling back to the global default when not overriding. */
    val activeAppRowPresentation: AppRowPresentation
        get() = activeProfile?.let { if (it.overrideApps) it.appRowPresentation else settings.appRowPresentation } ?: settings.appRowPresentation

    /** True when the active profile's mode needs `PACKAGE_USAGE_STATS`, it isn't granted yet, and the user hasn't already dismissed this prompt by tapping its button. */
    val showUsageAccessPrompt: Boolean
        get() = activeListContentMode != ListContentMode.FAVORITES && !usageAccessGranted && !usageAccessPromptDismissed
}
