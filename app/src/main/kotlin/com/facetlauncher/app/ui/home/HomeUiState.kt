package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.local.ProfileEntity
import com.facetlauncher.app.data.local.resolveOverride
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.domain.ClockAccessoryState
import com.facetlauncher.app.ui.components.HOME_GESTURES_COACH_MARK_ID

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
    /** The clock's next-alarm/battery accessory row state — see `ui/home/clock/ClockAccessoryRow.kt`. */
    val clockAccessories: ClockAccessoryState = ClockAccessoryState(nextAlarmMillis = null, batteryPercent = 0, isCharging = false),
    /** Set once the user taps the usage-access prompt's own button — the prompt then stays hidden
     *  for the rest of this ViewModel's lifetime regardless of whether they actually grant the
     *  permission afterward, rather than reappearing every recomposition until [usageAccessGranted]
     *  catches up (see chat history). */
    val usageAccessPromptDismissed: Boolean = false,
) {
    val activeProfile: ProfileEntity?
        get() = profiles.find { it.id == settings.activeProfileId }

    /** The active profile's clock settings, falling back to the global defaults when not overriding. */
    val clockTemplateId: ClockTemplateId
        get() = activeProfile?.let { if (it.overrideClock) it.clockTemplateId else settings.clockTemplateId } ?: settings.clockTemplateId

    val clockFontOption: ClockFontOption
        get() = activeProfile?.let { if (it.overrideClock) it.clockFontOption else settings.clockFontOption } ?: settings.clockFontOption

    val clockColorOption: ClockColorOption
        get() = activeProfile?.let { if (it.overrideClock) it.clockColorOption else settings.clockColorOption } ?: settings.clockColorOption

    /** The active profile's clock accent color, falling back to the global default when not overriding — mirrors [clockColorOption]'s own resolution shape. */
    val clockAccentColorOption: ClockColorOption
        get() = activeProfile?.let { if (it.overrideClock) it.clockAccentColorOption else settings.clockAccentColorOption } ?: settings.clockAccentColorOption

    /** The active profile's date style, falling back to the global default when not overriding — mirrors [clockColorOption]'s own resolution shape. */
    val clockDateStyle: ClockDateStyle
        get() = activeProfile?.let { if (it.overrideClock) it.clockDateStyle else settings.clockDateStyle } ?: settings.clockDateStyle

    /** The active profile's calendar font, falling back to the global default when not overriding — mirrors [clockFontOption] but gated by [com.facetlauncher.app.data.local.ProfileEntity.overrideCalendar], not `overrideClock`. */
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

    /** The active profile's clock alignment, falling back to the global default when not overriding — mirrors [clockTemplateId]'s own resolution shape. */
    val clockAlignment: ClockAlignment
        get() = activeProfile.resolveOverride({ it.overrideClock }, { it.clockAlignment }, settings.clockAlignment)

    /** The active profile's clock zone height, falling back to the global default when not overriding — see [clockAlignment]'s own doc. */
    val clockZoneHeightDp: Float?
        get() = activeProfile.resolveOverride({ it.overrideClock }, { it.clockZoneHeightDp }, settings.clockZoneHeightDp)

    /** The active profile's clock scale factor, falling back to the global default when not overriding. */
    val clockScale: Float
        get() = activeProfile.resolveOverride({ it.overrideClock }, { it.clockScale }, settings.clockScale)

    /** The active profile's calendar alignment, falling back to the global default when not overriding — independent of [clockAlignment], same as [com.facetlauncher.app.data.model.LauncherSettings.calendarAlignment]. */
    val calendarAlignment: ClockAlignment
        get() = activeProfile.resolveOverride({ it.overrideClock }, { it.calendarAlignment }, settings.calendarAlignment)

    /** The profile whose `overrideClock` bundle actually governs the clock widget's live position right now, or `null` if the global default applies — see [HomeViewModel.onClockZoneHeightCommit]. */
    val clockPositionOwningProfile: ProfileEntity?
        get() = activeProfile?.takeIf { it.overrideClock }

    /** The active profile's app-list vertical anchor, falling back to the global default when not overriding — mirrors [activeAppRowPosition]'s own resolution shape. */
    val activeAppListVerticalAlignment: AppListVerticalAlignment
        get() = activeProfile?.let { if (it.overrideApps) it.appListVerticalAlignment else settings.appListVerticalAlignment } ?: settings.appListVerticalAlignment

    /** The active profile's dock display style, falling back to the global default when not overriding — [dockApps] itself is already resolved per-profile upstream in [com.facetlauncher.app.domain.ObserveHomeScreenStateUseCase]. */
    val activeDockDisplayMode: DockDisplayMode
        get() = activeProfile?.let { if (it.overrideDock) it.dockDisplayMode else settings.dockDisplayMode } ?: settings.dockDisplayMode

    /** Shown once, immediately after onboarding finishes — scoped by `onboardingCompleted &&` rather than a launch counter, so it never reappears once dismissed (persisted in [LauncherSettings.coachMarksSeen]). */
    val showGestureHint: Boolean
        get() = settings.onboardingCompleted && HOME_GESTURES_COACH_MARK_ID !in settings.coachMarksSeen
}
