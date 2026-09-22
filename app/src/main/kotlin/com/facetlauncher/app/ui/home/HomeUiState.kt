package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.local.resolveOverride
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
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.domain.ClockAccessoryState
import com.facetlauncher.app.ui.components.HOME_GESTURES_COACH_MARK_ID
import com.facetlauncher.app.ui.components.HOME_SET_DEFAULT_PROMPT_ID

data class HomeUiState(
    val settings: LauncherSettings = LauncherSettings(),
    /** True until the first real state emission arrives — see [HomeViewModel]'s own doc for why this exists and how it resolves. */
    val isLoading: Boolean = true,
    val dockApps: List<PlacedItem> = emptyList(),
    /** Favorites, Recents, or Most Used, depending on the active facet's [ListContentMode]. Only the Favorites case can ever contain a [PlacedItem.FolderItem]. */
    val appListItems: List<PlacedItem> = emptyList(),
    /** The active facet's real Favorites contents, resolved unconditionally — see [com.facetlauncher.app.domain.HomeScreenState.favoriteItems]'s own doc for why this is separate from [appListItems]. */
    val favoriteItems: List<PlacedItem> = emptyList(),
    val facets: List<FacetEntity> = emptyList(),
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
    /** Whether Facet currently holds the `HOME` role — see [com.facetlauncher.app.data.DefaultLauncherRepository.isDefaultLauncher]. */
    val isDefaultLauncher: Boolean = false,
) {
    val activeFacet: FacetEntity?
        get() = facets.find { it.id == settings.activeFacetId }

    /** PRD F15 — facet-scoped only, no global fallback (see F15's Scope: there's no meaningful "global hosted widget" to inherit). `null` means the active facet uses its own native clock. */
    val clockWidgetAppWidgetId: Int?
        get() = activeFacet?.clockWidgetAppWidgetId

    /** PRD F15's persisted interactive-resize size — `null` until the user drags a resize handle for the first time; see [FacetEntity.clockWidgetWidthDp]'s own doc for what governs the effective size before that. */
    val clockWidgetWidthDp: Int?
        get() = activeFacet?.clockWidgetWidthDp
    val clockWidgetHeightDp: Int?
        get() = activeFacet?.clockWidgetHeightDp

    /** The active facet's clock settings, falling back to the global defaults when not overriding. */
    val clockTemplateId: ClockTemplateId
        get() = activeFacet?.let { if (it.overrideClock) it.clockTemplateId else settings.clockTemplateId } ?: settings.clockTemplateId

    val clockFontOption: ClockFontOption
        get() = activeFacet?.let { if (it.overrideClock) it.clockFontOption else settings.clockFontOption } ?: settings.clockFontOption

    val clockColorOption: ClockColorOption
        get() = activeFacet?.let { if (it.overrideClock) it.clockColorOption else settings.clockColorOption } ?: settings.clockColorOption

    /** The active facet's clock accent color, falling back to the global default when not overriding — mirrors [clockColorOption]'s own resolution shape. */
    val clockAccentColorOption: ClockColorOption
        get() = activeFacet?.let { if (it.overrideClock) it.clockAccentColorOption else settings.clockAccentColorOption } ?: settings.clockAccentColorOption

    /** The active facet's date style, falling back to the global default when not overriding — mirrors [clockColorOption]'s own resolution shape. */
    val clockDateStyle: ClockDateStyle
        get() = activeFacet?.let { if (it.overrideClock) it.clockDateStyle else settings.clockDateStyle } ?: settings.clockDateStyle

    /** The active facet's calendar font, falling back to the global default when not overriding — mirrors [clockFontOption] but gated by [com.facetlauncher.app.data.local.FacetEntity.overrideCalendar], not `overrideClock`. */
    val activeCalendarFontOption: ClockFontOption
        get() = activeFacet?.let { if (it.overrideCalendar) it.calendarFontOption else settings.calendarFontOption } ?: settings.calendarFontOption

    val activeCalendarColorOption: ClockColorOption
        get() = activeFacet?.let { if (it.overrideCalendar) it.calendarColorOption else settings.calendarColorOption } ?: settings.calendarColorOption

    /** Mirrors [activeCalendarFontOption]'s own gating exactly — same `overrideCalendar` flag, same fallback shape. */
    val activeCalendarFontWeight: FontWeightOption
        get() = activeFacet?.let { if (it.overrideCalendar) it.calendarFontWeight else settings.calendarFontWeight } ?: settings.calendarFontWeight

    val effectiveUse24HourTime: Boolean
        get() = activeFacet?.let { if (it.overrideClock) it.use24HourTime else settings.use24HourTime } ?: settings.use24HourTime

    val clockShowMeridiem: Boolean
        get() = activeFacet?.let { if (it.overrideClock) it.clockShowMeridiem else settings.clockShowMeridiem } ?: settings.clockShowMeridiem

    val activeListContentMode: ListContentMode
        get() = activeFacet?.let { if (it.overrideApps) it.listContentMode else settings.listContentMode } ?: settings.listContentMode

    /** The active facet's app-row position, falling back to the global default when not overriding. */
    val activeAppRowPosition: AppRowPosition
        get() = activeFacet?.let { if (it.overrideApps) it.appRowPosition else settings.appRowPosition } ?: settings.appRowPosition

    /** The active facet's app-row presentation, falling back to the global default when not overriding. */
    val activeAppRowPresentation: AppRowPresentation
        get() = activeFacet?.let { if (it.overrideApps) it.appRowPresentation else settings.appRowPresentation } ?: settings.appRowPresentation

    /** True when the active facet's mode needs `PACKAGE_USAGE_STATS`, it isn't granted yet, and the user hasn't already dismissed this prompt by tapping its button. */
    val showUsageAccessPrompt: Boolean
        get() = activeListContentMode != ListContentMode.FAVORITES && !usageAccessGranted && !usageAccessPromptDismissed

    /** The active facet's clock alignment, falling back to the global default when not overriding — mirrors [clockTemplateId]'s own resolution shape. */
    val clockAlignment: ClockAlignment
        get() = activeFacet.resolveOverride({ it.overrideClock }, { it.clockAlignment }, settings.clockAlignment)

    /** The active facet's clock zone height, falling back to the global default when not overriding — see [clockAlignment]'s own doc. */
    val clockZoneHeightDp: Float?
        get() = activeFacet.resolveOverride({ it.overrideClock }, { it.clockZoneHeightDp }, settings.clockZoneHeightDp)

    /** The active facet's clock scale factor, falling back to the global default when not overriding. */
    val clockScale: Float
        get() = activeFacet.resolveOverride({ it.overrideClock }, { it.clockScale }, settings.clockScale)

    /** The active facet's calendar alignment, falling back to the global default when not overriding — independent of [clockAlignment], same as [com.facetlauncher.app.data.model.LauncherSettings.calendarAlignment]. */
    val calendarAlignment: ClockAlignment
        get() = activeFacet.resolveOverride({ it.overrideClock }, { it.calendarAlignment }, settings.calendarAlignment)

    /** The facet whose `overrideClock` bundle actually governs the clock widget's live position right now, or `null` if the global default applies — see [HomeViewModel.onClockZoneHeightCommit]. */
    val clockPositionOwningFacet: FacetEntity?
        get() = activeFacet?.takeIf { it.overrideClock }

    /** Names the facet overriding the Dock right now, for [com.facetlauncher.app.ui.components.QuickPlacementBadge] — `null` for the launcher-wide default, mirroring [com.facetlauncher.app.domain.QuickPlacementAction.facetName]'s own convention. */
    val dockOverrideFacetName: String?
        get() = activeFacet?.takeIf { it.overrideDock }?.name

    /** Mirrors [dockOverrideFacetName] for Favorites — uses `overridingFavorites`, this app's own (differently-named) override flag for that list. */
    val favoritesOverrideFacetName: String?
        get() = activeFacet?.takeIf { it.overridingFavorites }?.name

    /** The active facet's app-list vertical anchor, falling back to the global default when not overriding — mirrors [activeAppRowPosition]'s own resolution shape. */
    val activeAppListVerticalAlignment: AppListVerticalAlignment
        get() = activeFacet?.let { if (it.overrideApps) it.appListVerticalAlignment else settings.appListVerticalAlignment } ?: settings.appListVerticalAlignment

    /** The active facet's dock display style, falling back to the global default when not overriding — [dockApps] itself is already resolved per-facet upstream in [com.facetlauncher.app.domain.ObserveHomeScreenStateUseCase]. */
    val activeDockDisplayMode: DockDisplayMode
        get() = activeFacet?.let { if (it.overrideDock) it.dockDisplayMode else settings.dockDisplayMode } ?: settings.dockDisplayMode

    /** Shown once, immediately after onboarding finishes — scoped by `onboardingCompleted &&` rather than a launch counter, so it never reappears once dismissed (persisted in [LauncherSettings.coachMarksSeen]). */
    val showGestureHint: Boolean
        get() = settings.onboardingCompleted && HOME_GESTURES_COACH_MARK_ID !in settings.coachMarksSeen

    /**
     * The "make Facet your home screen" prompt (formerly onboarding's own final step) — shown
     * once, the first time real Home renders after onboarding completes, regardless of
     * [isDefaultLauncher] (a reinstall that already holds the role still sees a one-time
     * acknowledgement, see `SetDefaultLauncherSheet`'s "already default" variant). Mirrors
     * [showGestureHint]'s exact shape.
     */
    val showSetDefaultPrompt: Boolean
        get() = settings.onboardingCompleted && HOME_SET_DEFAULT_PROMPT_ID !in settings.coachMarksSeen
}
