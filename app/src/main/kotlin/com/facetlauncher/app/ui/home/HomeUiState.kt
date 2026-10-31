package com.facetlauncher.app.ui.home

import com.facetlauncher.app.domain.ListAlignment
import com.facetlauncher.app.domain.ClockPlacement
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.LandscapeOverrides
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.MediaPlayerState
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.domain.ClockAccessoryState
import com.facetlauncher.app.domain.ResolvedClockItem
import com.facetlauncher.app.domain.ResolvedFacetStyle
import com.facetlauncher.app.domain.limitedTo
import com.facetlauncher.app.domain.resolveFacetStyle
import com.facetlauncher.app.ui.components.HOME_GESTURES_COACH_MARK_ID
import com.facetlauncher.app.ui.components.HOME_SET_DEFAULT_PROMPT_ID
import kotlinx.collections.immutable.ImmutableList

data class HomeUiState(
    val settings: LauncherSettings = LauncherSettings(),
    /** True until the first real state emission arrives — see [HomeViewModel]'s own doc for why this exists and how it resolves. */
    val isLoading: Boolean = true,
    val dockApps: List<PlacedItem> = emptyList(),
    /** Favorites, Recents, or Most Used, depending on the active facet's [ListContentMode]. Only the Favorites case can ever contain a [PlacedItem.FolderItem]. */
    val appListItems: List<PlacedItem> = emptyList(),
    /** The active facet's real Favorites contents, resolved unconditionally — see [com.facetlauncher.app.domain.HomeScreenState.favoriteItems]'s own doc for why this is separate from [appListItems]. */
    val favoriteItems: List<PlacedItem> = emptyList(),
    val facets: List<Facet> = emptyList(),
    val usageAccessGranted: Boolean = false,
    val calendarEvents: List<CalendarEvent> = emptyList(),
    val calendarDisplay: CalendarDisplayOptions = CalendarDisplayOptions(),
    val badgeCounts: Map<String, Int> = emptyMap(),
    /** The clock's next-alarm/battery accessory row state — see `ui/home/clock/ClockAccessoryRow.kt`. */
    val clockAccessories: ClockAccessoryState = ClockAccessoryState(nextAlarmMillis = null, batteryPercent = 0, isCharging = false),
    /** The media row under the clock — `null` hides it (setting off, no access, or nothing playing/paused). */
    val mediaPlayer: MediaPlayerState? = null,
    /** Set once the user taps the usage-access prompt's own button — the prompt then stays hidden
     *  for the rest of this ViewModel's lifetime regardless of whether they actually grant the
     *  permission afterward, rather than reappearing every recomposition until [usageAccessGranted]
     *  catches up (see chat history). */
    val usageAccessPromptDismissed: Boolean = false,
    /**
     * Whether Facet currently holds the `HOME` role — see [com.facetlauncher.app.data.DefaultLauncherRepository.isDefaultLauncher].
     * `null` until that async check resolves; [showSetDefaultPrompt] and [showGestureHint] both wait for it, so an
     * already-default device never flashes the prompt before the answer arrives.
     */
    val isDefaultLauncher: Boolean? = null,
    /** The clock stack page showing on the active facet, remembered while Home lives; `null` shows the first. */
    val visibleClockItemId: Long? = null,
    /** Whether Pro is held: without it only the first clock page shows, and the rest stay saved. */
    val isPro: Boolean = true,
) {
    val activeFacet: Facet?
        get() = facets.find { it.id == settings.activeFacetId }

    /** The active facet's clock pages in swipe order (PRD F15; facet-scoped only, there is no global hosted widget to inherit). Empty is the plain native clock. */
    val clockStack: ImmutableList<ResolvedClockItem> get() = style.clockStack.limitedTo(isPro)

    /** The page Home shows: the remembered one, else the first (`null` when the stack is empty). */
    val visibleClockItem: ResolvedClockItem? get() = clockStack.getOrNull(clockStack.visibleIndex(visibleClockItemId))

    /** The visible page's widget id; `null` when it is the native clock. */
    val clockWidgetAppWidgetId: Int? get() = visibleClockItem?.appWidgetId

    /** Every widget id in the stack: all of them need the host listening, not only the visible one. */
    val clockStackWidgetIds: List<Int> get() = clockStack.mapNotNull { it.appWidgetId }

    /** The active facet's overrides resolved against [settings]. */
    internal val style: ResolvedFacetStyle by lazy(LazyThreadSafetyMode.NONE) { resolveFacetStyle(activeFacet, settings) }

    val clockTemplateId: ClockTemplateId get() = style.clock.templateId
    val clockFontOption: ClockFontOption get() = style.clock.fontOption
    val clockColorOption: ClockColorOption get() = style.clock.colorOption
    val clockAccentColorOption: ClockColorOption get() = style.clock.accentColorOption
    val clockDateStyle: ClockDateStyle get() = style.clock.dateStyle
    val effectiveUse24HourTime: Boolean get() = style.clock.use24HourTime
    val clockShowMeridiem: Boolean get() = style.clock.showMeridiem
    val activeListContentMode: ListContentMode get() = style.appList.contentMode
    val activeAppRowPosition: AppRowPosition get() = style.appList.rowPosition
    val activeAppRowPresentation: AppRowPresentation get() = style.appList.rowPresentation

    /** True when the active facet's mode needs `PACKAGE_USAGE_STATS`, it isn't granted yet, and the user hasn't already dismissed this prompt by tapping its button. */
    val showUsageAccessPrompt: Boolean
        get() = activeListContentMode != ListContentMode.FAVORITES && !usageAccessGranted && !usageAccessPromptDismissed

    val clockAlignment: ClockAlignment get() = style.clockPlacement.portrait.alignment
    val clockZoneHeightDp: Float? get() = style.clockPlacement.portrait.zoneHeightDp
    val clockScale: Float get() = style.clockPlacement.portrait.scale

    /** The clock's alignment, divider position and scale for [mode], resolved by [resolveFacetStyle]. */
    fun clockPlacement(mode: HomeLayoutMode): ClockPlacement = style.clockPlacement.of(mode == HomeLayoutMode.LANDSCAPE)

    /** The apps list's row position, vertical anchor and two-column alignment for [mode], resolved by [resolveFacetStyle]. */
    fun listAlignment(mode: HomeLayoutMode): ListAlignment = style.listAlignment.of(mode == HomeLayoutMode.LANDSCAPE)

    /** The visible page's saved widget size for [mode]: landscape's own where saved, else the portrait one; `null` before any resize. */
    fun clockWidgetSizeDp(mode: HomeLayoutMode): Pair<Int, Int>? = visibleClockItem?.sizeDp?.of(mode == HomeLayoutMode.LANDSCAPE)

    /** The facet whose own clock style is in force (it overrides the clock), or `null` while the launcher-wide style applies: the sheet's scope badge. Placement is not part of it. */
    val clockStyleOwningFacet: Facet?
        get() = activeFacet?.takeIf { it.overrideClock }

    /** Names the facet overriding the Dock right now, for [com.facetlauncher.app.ui.components.QuickPlacementBadge] — `null` for the launcher-wide default, mirroring [com.facetlauncher.app.domain.QuickPlacementAction.facetName]'s own convention. */
    val dockOverrideFacetName: String?
        get() = activeFacet?.takeIf { it.overrideDock }?.name

    /** Mirrors [dockOverrideFacetName] for Favorites — uses `overridingFavorites`, this app's own (differently-named) override flag for that list. */
    val favoritesOverrideFacetName: String?
        get() = activeFacet?.takeIf { it.overridingFavorites }?.name

    val activeAppListVerticalAlignment: AppListVerticalAlignment get() = style.appList.verticalAlignment
    val activeAppListLayout: AppListLayout get() = style.appList.layout

    /** Only meaningful while [activeAppListLayout] is [AppListLayout.TWO_COLUMN]. */
    val activeAppListColumnAlignment: AppListColumnAlignment get() = style.appList.columnAlignment

    /** Only meaningful while [activeAppListLayout] is [AppListLayout.GRID]. */
    val activeAppListGridColumns: AppListGridColumns get() = style.appList.gridColumns

    /** Only meaningful while [activeAppListLayout] is [AppListLayout.GRID]. */
    val activeAppListGridDisplayMode: AppListGridDisplayMode get() = style.appList.gridDisplayMode

    /** A look field, resolved independently of `overrideDock` (that flag gates only [dockApps], already resolved per-facet in [com.facetlauncher.app.domain.ObserveHomeScreenStateUseCase]). */
    val activeDockDisplayMode: DockDisplayMode get() = style.dockDisplayMode

    /**
     * The "make Facet your home screen" step is out of the way: already dismissed, or Facet already holds the role
     * (nothing to ask). Both [showGestureHint] and [showSetDefaultPrompt] key off it so the hints never show over,
     * or before, a prompt that's still pending or still being checked.
     */
    private val setDefaultStepDone: Boolean
        get() = HOME_SET_DEFAULT_PROMPT_ID in settings.coachMarksSeen || isDefaultLauncher == true

    /** Shown once, immediately after onboarding finishes (and after the set-default step, if there is one) — scoped by `onboardingCompleted &&` rather than a launch counter, so it never reappears once dismissed (persisted in [LauncherSettings.coachMarksSeen]). */
    val showGestureHint: Boolean
        get() = settings.onboardingCompleted && HOME_GESTURES_COACH_MARK_ID !in settings.coachMarksSeen && setDefaultStepDone

    /** The first-run prompts are behind the user, so a later announcement (the What's new pill) may show without crowding them. */
    val firstRunPromptsDone: Boolean
        get() = settings.onboardingCompleted && HOME_GESTURES_COACH_MARK_ID in settings.coachMarksSeen && setDefaultStepDone

    /**
     * The "make Facet your home screen" prompt — shown once, the first time real Home renders after onboarding
     * completes, and only when Facet is confirmed *not* to hold the role: a device that already does skips it
     * entirely and goes straight to the hints ([HomeViewModel] marks the step seen on its behalf).
     */
    val showSetDefaultPrompt: Boolean
        get() = settings.onboardingCompleted && HOME_SET_DEFAULT_PROMPT_ID !in settings.coachMarksSeen && isDefaultLauncher == false
}
