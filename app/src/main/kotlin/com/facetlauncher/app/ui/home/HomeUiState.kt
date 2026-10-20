package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.local.landscape
import com.facetlauncher.app.data.local.resolveLandscape
import com.facetlauncher.app.data.local.resolveOverride
import com.facetlauncher.app.data.local.resolveSentinel
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

    val effectiveUse24HourTime: Boolean
        get() = activeFacet?.let { if (it.overrideClock) it.use24HourTime else settings.use24HourTime } ?: settings.use24HourTime

    val clockShowMeridiem: Boolean
        get() = activeFacet?.let { if (it.overrideClock) it.clockShowMeridiem else settings.clockShowMeridiem } ?: settings.clockShowMeridiem

    val activeListContentMode: ListContentMode
        get() = activeFacet?.let { if (it.overrideApps) it.listContentMode else settings.listContentMode } ?: settings.listContentMode

    /** The active facet's app-row position — a look/placement field, edited from Settings → Appearance and resolved via its own `LAUNCHER_DEFAULT` sentinel independently of `overrideApps` (see [com.facetlauncher.app.data.local.resolveSentinel]). */
    val activeAppRowPosition: AppRowPosition
        get() = activeFacet.resolveSentinel({ it.appRowPosition }, AppRowPosition.LAUNCHER_DEFAULT, settings.appRowPosition)

    /** The active facet's app-row presentation — see [activeAppRowPosition]'s own doc for the resolution shape. */
    val activeAppRowPresentation: AppRowPresentation
        get() = activeFacet.resolveSentinel({ it.appRowPresentation }, AppRowPresentation.LAUNCHER_DEFAULT, settings.appRowPresentation)

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

    /**
     * The clock's alignment, divider position and scale for [mode]. Portrait is the facet's value while it overrides
     * the clock, the launcher's otherwise. Landscape follows [resolveLandscape]: its own value where set, else portrait.
     */
    fun clockPlacement(mode: HomeLayoutMode): ClockPlacement {
        val landscape = mode == HomeLayoutMode.LANDSCAPE
        return ClockPlacement(
            alignment = clockProp(landscape, { it.clockAlignment }, { it.clockAlignment }, settings.clockAlignment, settings.landscape.clockAlignment),
            zoneHeightDp = clockProp(landscape, { it.clockZoneHeightDp }, { it.clockZoneHeightDp }, settings.clockZoneHeightDp, settings.landscape.clockZoneHeightDp),
            scale = clockProp(landscape, { it.clockScale }, { it.clockScale }, settings.clockScale, settings.landscape.clockScale),
        )
    }

    private inline fun <T> clockProp(
        landscape: Boolean,
        facetPortrait: (FacetEntity) -> T,
        facetLandscape: (LandscapeOverrides) -> T?,
        globalPortrait: T,
        globalLandscape: T?,
    ): T {
        val facet = clockPositionOwningFacet
        val portrait = if (facet != null) facetPortrait(facet) else globalPortrait
        if (!landscape) return portrait
        return resolveLandscape(facet?.landscape?.let(facetLandscape), portrait, facet != null, globalLandscape, globalPortrait)
    }

    /** The apps list's row position, vertical anchor and two-column alignment for [mode], through each field's own `LAUNCHER_DEFAULT` sentinel. */
    fun listAlignment(mode: HomeLayoutMode): ListAlignment {
        val landscape = mode == HomeLayoutMode.LANDSCAPE
        return ListAlignment(
            rowPosition = listProp(landscape, { it.appRowPosition }, { it.appRowPosition }, AppRowPosition.LAUNCHER_DEFAULT, settings.appRowPosition, settings.landscape.appRowPosition),
            verticalAlignment = listProp(
                landscape, { it.appListVerticalAlignment }, { it.appListVerticalAlignment }, AppListVerticalAlignment.LAUNCHER_DEFAULT,
                settings.appListVerticalAlignment, settings.landscape.appListVerticalAlignment,
            ),
            columnAlignment = listProp(
                landscape, { it.appListColumnAlignment }, { it.appListColumnAlignment }, AppListColumnAlignment.LAUNCHER_DEFAULT,
                settings.appListColumnAlignment, settings.landscape.appListColumnAlignment,
            ),
        )
    }

    private inline fun <T : Any> listProp(
        landscape: Boolean,
        facetPortrait: (FacetEntity) -> T,
        facetLandscape: (LandscapeOverrides) -> T?,
        sentinel: T,
        globalPortrait: T,
        globalLandscape: T?,
    ): T {
        val facetValue = activeFacet?.let(facetPortrait)?.takeIf { it != sentinel }
        val portrait = facetValue ?: globalPortrait
        if (!landscape) return portrait
        return resolveLandscape(activeFacet?.landscape?.let(facetLandscape), portrait, facetValue != null, globalLandscape, globalPortrait)
    }

    /** The hosted clock widget's saved size for [mode]: landscape's own where saved, else the portrait one; `null` before any resize. */
    fun clockWidgetSizeDp(mode: HomeLayoutMode): Pair<Int, Int>? {
        val facet = activeFacet ?: return null
        val own = facet.landscape.takeIf { mode == HomeLayoutMode.LANDSCAPE }
        val width = own?.clockWidgetWidthDp ?: facet.clockWidgetWidthDp
        val height = own?.clockWidgetHeightDp ?: facet.clockWidgetHeightDp
        return if (width != null && height != null) width to height else null
    }

    /** The facet whose `overrideClock` bundle actually governs the clock widget's live position right now, or `null` if the global default applies — see [HomeViewModel.onClockZoneHeightCommit]. */
    val clockPositionOwningFacet: FacetEntity?
        get() = activeFacet?.takeIf { it.overrideClock }

    /** Names the facet overriding the Dock right now, for [com.facetlauncher.app.ui.components.QuickPlacementBadge] — `null` for the launcher-wide default, mirroring [com.facetlauncher.app.domain.QuickPlacementAction.facetName]'s own convention. */
    val dockOverrideFacetName: String?
        get() = activeFacet?.takeIf { it.overrideDock }?.name

    /** Mirrors [dockOverrideFacetName] for Favorites — uses `overridingFavorites`, this app's own (differently-named) override flag for that list. */
    val favoritesOverrideFacetName: String?
        get() = activeFacet?.takeIf { it.overridingFavorites }?.name

    /** The active facet's app-list vertical anchor — mirrors [activeAppRowPosition]'s own resolution shape. */
    val activeAppListVerticalAlignment: AppListVerticalAlignment
        get() = activeFacet.resolveSentinel({ it.appListVerticalAlignment }, AppListVerticalAlignment.LAUNCHER_DEFAULT, settings.appListVerticalAlignment)

    /** The active facet's app-list layout (single column/two columns/grid) — mirrors [activeAppRowPosition]'s own resolution shape. */
    val activeAppListLayout: AppListLayout
        get() = activeFacet.resolveSentinel({ it.appListLayout }, AppListLayout.LAUNCHER_DEFAULT, settings.appListLayout)

    /** Only meaningful while [activeAppListLayout] is [AppListLayout.TWO_COLUMN] — see [activeAppRowPosition]'s own resolution shape. */
    val activeAppListColumnAlignment: AppListColumnAlignment
        get() = activeFacet.resolveSentinel({ it.appListColumnAlignment }, AppListColumnAlignment.LAUNCHER_DEFAULT, settings.appListColumnAlignment)

    /** Only meaningful while [activeAppListLayout] is [AppListLayout.GRID] — see [activeAppRowPosition]'s own resolution shape. */
    val activeAppListGridColumns: AppListGridColumns
        get() = activeFacet.resolveSentinel({ it.appListGridColumns }, AppListGridColumns.LAUNCHER_DEFAULT, settings.appListGridColumns)

    /** Only meaningful while [activeAppListLayout] is [AppListLayout.GRID] — see [activeAppRowPosition]'s own resolution shape. */
    val activeAppListGridDisplayMode: AppListGridDisplayMode
        get() = activeFacet.resolveSentinel({ it.appListGridDisplayMode }, AppListGridDisplayMode.LAUNCHER_DEFAULT, settings.appListGridDisplayMode)

    /** The active facet's dock display style — a look field, resolved independently of `overrideDock` (that flag still gates only [dockApps], already resolved per-facet upstream in [com.facetlauncher.app.domain.ObserveHomeScreenStateUseCase]) — see [activeAppRowPosition]'s own doc for the resolution shape. */
    val activeDockDisplayMode: DockDisplayMode
        get() = activeFacet.resolveSentinel({ it.dockDisplayMode }, DockDisplayMode.LAUNCHER_DEFAULT, settings.dockDisplayMode)

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

    /**
     * The "make Facet your home screen" prompt — shown once, the first time real Home renders after onboarding
     * completes, and only when Facet is confirmed *not* to hold the role: a device that already does skips it
     * entirely and goes straight to the hints ([HomeViewModel] marks the step seen on its behalf).
     */
    val showSetDefaultPrompt: Boolean
        get() = settings.onboardingCompleted && HOME_SET_DEFAULT_PROMPT_ID !in settings.coachMarksSeen && isDefaultLauncher == false
}
