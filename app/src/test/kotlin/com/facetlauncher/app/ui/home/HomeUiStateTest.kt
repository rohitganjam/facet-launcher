package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.clockItem
import com.facetlauncher.app.data.widgetItem
import com.facetlauncher.app.domain.ListAlignment
import com.facetlauncher.app.domain.ClockPlacement
import com.facetlauncher.app.data.model.LandscapeOverrides
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.ui.components.HOME_GESTURES_COACH_MARK_ID
import com.facetlauncher.app.ui.components.HOME_SET_DEFAULT_PROMPT_ID
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeUiStateTest {

    @Test
    fun `effective 24 hour time falls back to the global default when the active facet has no override`() {
        val state = HomeUiState(
            settings = LauncherSettings(use24HourTime = true, activeFacetId = 1L),
            facets = listOf(Facet(id = 1L, name = "Facet 1", position = 0, overrideClock = false)),
        )

        assertEquals(true, state.effectiveUse24HourTime)
    }

    @Test
    fun `effective 24 hour time uses the active facet's override when set`() {
        val state = HomeUiState(
            settings = LauncherSettings(use24HourTime = true, activeFacetId = 1L),
            facets = listOf(Facet(id = 1L, name = "Facet 1", position = 0, overrideClock = true, use24HourTime = false)),
        )

        assertEquals(false, state.effectiveUse24HourTime)
    }

    @Test
    fun `clockWidgetAppWidgetId is null when the active facet uses its native clock`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(Facet(id = 1L, name = "Facet 1", position = 0)),
        )

        assertEquals(null, state.clockWidgetAppWidgetId)
    }

    @Test
    fun `clockWidgetAppWidgetId reflects the active facet's own bound widget, facet-scoped only`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 2L),
            facets = listOf(
                Facet(id = 1L, name = "Facet 1", position = 0, clockStack = listOf(widgetItem(7, facetId = 1L))),
                Facet(id = 2L, name = "Facet 2", position = 1, clockStack = listOf(widgetItem(42, facetId = 2L))),
            ),
        )

        assertEquals(42, state.clockWidgetAppWidgetId)
    }

    @Test
    fun `usage access prompt is hidden for favorites mode regardless of grant state`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(Facet(id = 1L, name = "P1", position = 0, overrideApps = true, listContentMode = ListContentMode.FAVORITES)),
            usageAccessGranted = false,
        )

        assertEquals(false, state.showUsageAccessPrompt)
    }

    @Test
    fun `usage access prompt shows for recents mode when access isn't granted`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(Facet(id = 1L, name = "P1", position = 0, overrideApps = true, listContentMode = ListContentMode.RECENTS)),
            usageAccessGranted = false,
        )

        assertEquals(true, state.showUsageAccessPrompt)
    }

    @Test
    fun `usage access prompt is hidden for most used mode once access is granted`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(Facet(id = 1L, name = "P1", position = 0, overrideApps = true, listContentMode = ListContentMode.MOST_USED)),
            usageAccessGranted = true,
        )

        assertEquals(false, state.showUsageAccessPrompt)
    }

    @Test
    fun `usage access prompt stays hidden once dismissed even though access still isn't granted`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(Facet(id = 1L, name = "P1", position = 0, overrideApps = true, listContentMode = ListContentMode.RECENTS)),
            usageAccessGranted = false,
            usageAccessPromptDismissed = true,
        )

        assertEquals(false, state.showUsageAccessPrompt)
    }

    @Test
    fun `active app row position falls back to the global default when the active facet has no override`() {
        val state = HomeUiState(
            settings = LauncherSettings(appRowPosition = AppRowPosition.RIGHT, activeFacetId = 1L),
            facets = listOf(Facet(id = 1L, name = "Facet 1", position = 0, overrideApps = false)),
        )

        assertEquals(AppRowPosition.RIGHT, state.activeAppRowPosition)
    }

    @Test
    fun `active app row position uses the active facet's override when set`() {
        val state = HomeUiState(
            settings = LauncherSettings(appRowPosition = AppRowPosition.LEFT, activeFacetId = 1L),
            facets = listOf(
                Facet(id = 1L, name = "Facet 1", position = 0, overrideApps = true, appRowPosition = AppRowPosition.RIGHT),
            ),
        )

        assertEquals(AppRowPosition.RIGHT, state.activeAppRowPosition)
    }

    @Test
    fun `active app row presentation falls back to the global default when the active facet has no override`() {
        val state = HomeUiState(
            settings = LauncherSettings(appRowPresentation = AppRowPresentation.TEXT_ONLY, activeFacetId = 1L),
            facets = listOf(Facet(id = 1L, name = "Facet 1", position = 0, overrideApps = false)),
        )

        assertEquals(AppRowPresentation.TEXT_ONLY, state.activeAppRowPresentation)
    }

    @Test
    fun `active app row presentation uses the active facet's override when set`() {
        val state = HomeUiState(
            settings = LauncherSettings(appRowPresentation = AppRowPresentation.ICON_AND_TEXT, activeFacetId = 1L),
            facets = listOf(
                Facet(
                    id = 1L,
                    name = "Facet 1",
                    position = 0,
                    overrideApps = true,
                    appRowPresentation = AppRowPresentation.ICON_ONLY,
                ),
            ),
        )

        assertEquals(AppRowPresentation.ICON_ONLY, state.activeAppRowPresentation)
    }

    @Test
    fun `gesture hint is hidden before onboarding has completed`() {
        val state = HomeUiState(settings = LauncherSettings(onboardingCompleted = false))

        assertEquals(false, state.showGestureHint)
    }

    @Test
    fun `gesture hint shows right away when Facet already holds the home role`() {
        val state = HomeUiState(settings = LauncherSettings(onboardingCompleted = true, coachMarksSeen = emptySet()), isDefaultLauncher = true)

        assertEquals(true, state.showGestureHint)
    }

    @Test
    fun `gesture hint shows once the set-default prompt has been dismissed`() {
        val state = HomeUiState(
            settings = LauncherSettings(onboardingCompleted = true, coachMarksSeen = setOf("HOME_SET_DEFAULT_PROMPT")),
            isDefaultLauncher = false,
        )

        assertEquals(true, state.showGestureHint)
    }

    @Test
    fun `gesture hint waits while the set-default prompt is still pending`() {
        val state = HomeUiState(settings = LauncherSettings(onboardingCompleted = true, coachMarksSeen = emptySet()), isDefaultLauncher = false)

        assertEquals(false, state.showGestureHint)
    }

    @Test
    fun `gesture hint waits while it's still unknown whether Facet is the default`() {
        val state = HomeUiState(settings = LauncherSettings(onboardingCompleted = true, coachMarksSeen = emptySet()), isDefaultLauncher = null)

        assertEquals(false, state.showGestureHint)
    }

    @Test
    fun `gesture hint stays hidden once its id is in coachMarksSeen`() {
        val state = HomeUiState(
            settings = LauncherSettings(onboardingCompleted = true, coachMarksSeen = setOf("HOME_GESTURES")),
            isDefaultLauncher = true,
        )

        assertEquals(false, state.showGestureHint)
    }

    @Test
    fun `set-default prompt shows only when Facet is confirmed not to be the default`() {
        val settings = LauncherSettings(onboardingCompleted = true, coachMarksSeen = emptySet())

        assertEquals(true, HomeUiState(settings = settings, isDefaultLauncher = false).showSetDefaultPrompt)
        assertEquals(false, HomeUiState(settings = settings, isDefaultLauncher = true).showSetDefaultPrompt)
        assertEquals(false, HomeUiState(settings = settings, isDefaultLauncher = null).showSetDefaultPrompt)
    }

    @Test
    fun `set-default prompt stays hidden before onboarding completes and once dismissed`() {
        assertEquals(false, HomeUiState(settings = LauncherSettings(onboardingCompleted = false), isDefaultLauncher = false).showSetDefaultPrompt)
        assertEquals(
            false,
            HomeUiState(
                settings = LauncherSettings(onboardingCompleted = true, coachMarksSeen = setOf("HOME_SET_DEFAULT_PROMPT")),
                isDefaultLauncher = false,
            ).showSetDefaultPrompt,
        )
    }

    @Test
    fun `dock override facet name is null when the active facet isn't overriding the dock`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(Facet(id = 1L, name = "Work", position = 0, overrideDock = false)),
        )

        assertEquals(null, state.dockOverrideFacetName)
    }

    @Test
    fun `dock override facet name is the active facet's own name when it overrides the dock`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(Facet(id = 1L, name = "Work", position = 0, overrideDock = true)),
        )

        assertEquals("Work", state.dockOverrideFacetName)
    }

    @Test
    fun `favorites override facet name is null when the active facet isn't overriding favorites`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(Facet(id = 1L, name = "Work", position = 0, overridingFavorites = false)),
        )

        assertEquals(null, state.favoritesOverrideFacetName)
    }

    @Test
    fun `favorites override facet name is the active facet's own name when it overrides favorites`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(Facet(id = 1L, name = "Work", position = 0, overridingFavorites = true)),
        )

        assertEquals("Work", state.favoritesOverrideFacetName)
    }

    private val portrait = HomeLayoutMode.PORTRAIT
    private val landscape = HomeLayoutMode.LANDSCAPE

    @Test
    fun `clock placement in portrait ignores the landscape values`() {
        val state = HomeUiState(
            settings = LauncherSettings(
                clockAlignment = ClockAlignment.RIGHT,
                landscape = LandscapeOverrides(clockAlignment = ClockAlignment.CENTER, clockScale = 0.9f),
            ),
        )

        assertEquals(ClockPlacement(ClockAlignment.RIGHT, null, 0.8f), state.clockPlacement(portrait))
    }

    @Test
    fun `clock placement in landscape falls back to the portrait values when no landscape value is set`() {
        val state = HomeUiState(settings = LauncherSettings(clockAlignment = ClockAlignment.RIGHT, clockZoneHeightDp = 200f, clockScale = 1.2f))

        assertEquals(ClockPlacement(ClockAlignment.RIGHT, 200f, 1.2f), state.clockPlacement(landscape))
    }

    @Test
    fun `clock placement in landscape uses the launcher's landscape values when set`() {
        val state = HomeUiState(
            settings = LauncherSettings(
                clockAlignment = ClockAlignment.RIGHT,
                landscape = LandscapeOverrides(clockAlignment = ClockAlignment.CENTER, clockZoneHeightDp = 310f, clockScale = 0.9f),
            ),
        )

        assertEquals(ClockPlacement(ClockAlignment.CENTER, 310f, 0.9f), state.clockPlacement(landscape))
    }

    @Test
    fun `an overriding facet's landscape clock values win, then its own portrait values, never the launcher's landscape ones`() {
        val facet = Facet(
            id = 1L, name = "Facet 1", position = 0, overrideClock = true,
            clockAlignment = ClockAlignment.RIGHT, clockScale = 1.4f,
            landscapeClockAlignment = ClockAlignment.CENTER,
        )
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L, landscape = LandscapeOverrides(clockAlignment = ClockAlignment.LEFT, clockScale = 0.5f)),
            facets = listOf(facet),
        )

        val placement = state.clockPlacement(landscape)

        assertEquals(ClockAlignment.CENTER, placement.alignment)
        assertEquals(1.4f, placement.scale)
    }

    @Test
    fun `an overriding facet that never dragged its divider keeps a natural zone height in landscape`() {
        val facet = Facet(id = 1L, name = "Facet 1", position = 0, overrideClock = true)
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L, clockZoneHeightDp = 200f, landscape = LandscapeOverrides(clockZoneHeightDp = 300f)),
            facets = listOf(facet),
        )

        assertEquals(null, state.clockPlacement(landscape).zoneHeightDp)
    }

    @Test
    fun `a facet that is not overriding the clock uses the launcher's values in both orientations`() {
        val facet = Facet(id = 1L, name = "Facet 1", position = 0, overrideClock = false, landscapeClockAlignment = ClockAlignment.CENTER)
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L, landscape = LandscapeOverrides(clockAlignment = ClockAlignment.RIGHT)),
            facets = listOf(facet),
        )

        assertEquals(ClockAlignment.RIGHT, state.clockPlacement(landscape).alignment)
        assertEquals(ClockAlignment.LEFT, state.clockPlacement(portrait).alignment)
    }

    @Test
    fun `list alignment in landscape falls back to the portrait values when no landscape value is set`() {
        val state = HomeUiState(settings = LauncherSettings(appRowPosition = AppRowPosition.RIGHT, appListVerticalAlignment = AppListVerticalAlignment.TOP))

        assertEquals(ListAlignment(AppRowPosition.RIGHT, AppListVerticalAlignment.TOP, AppListColumnAlignment.BOTH_LEFT), state.listAlignment(landscape))
    }

    @Test
    fun `list alignment in landscape uses the launcher's landscape values when set, and portrait is unaffected`() {
        val state = HomeUiState(
            settings = LauncherSettings(
                appRowPosition = AppRowPosition.RIGHT,
                landscape = LandscapeOverrides(
                    appRowPosition = AppRowPosition.CENTER,
                    appListVerticalAlignment = AppListVerticalAlignment.TOP,
                    appListColumnAlignment = AppListColumnAlignment.MIRRORED,
                ),
            ),
        )

        assertEquals(ListAlignment(AppRowPosition.CENTER, AppListVerticalAlignment.TOP, AppListColumnAlignment.MIRRORED), state.listAlignment(landscape))
        assertEquals(AppRowPosition.RIGHT, state.listAlignment(portrait).rowPosition)
    }

    @Test
    fun `a facet's explicit portrait list alignment beats the launcher's landscape value`() {
        val facet = Facet(id = 1L, name = "Facet 1", position = 0, appRowPosition = AppRowPosition.RIGHT)
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L, landscape = LandscapeOverrides(appRowPosition = AppRowPosition.CENTER)),
            facets = listOf(facet),
        )

        assertEquals(AppRowPosition.RIGHT, state.listAlignment(landscape).rowPosition)
    }

    @Test
    fun `a facet's own landscape list alignment wins`() {
        val facet = Facet(id = 1L, name = "Facet 1", position = 0, appRowPosition = AppRowPosition.RIGHT, landscapeAppRowPosition = AppRowPosition.LEFT)
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L, landscape = LandscapeOverrides(appRowPosition = AppRowPosition.CENTER)),
            facets = listOf(facet),
        )

        assertEquals(AppRowPosition.LEFT, state.listAlignment(landscape).rowPosition)
    }

    @Test
    fun `a facet that inherits its portrait list alignment uses the launcher's landscape value in landscape`() {
        val facet = Facet(id = 1L, name = "Facet 1", position = 0, appRowPosition = AppRowPosition.LAUNCHER_DEFAULT)
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L, landscape = LandscapeOverrides(appRowPosition = AppRowPosition.CENTER)),
            facets = listOf(facet),
        )

        assertEquals(AppRowPosition.CENTER, state.listAlignment(landscape).rowPosition)
    }

    @Test
    fun `the hosted widget size in landscape is its own when saved, else the portrait size`() {
        val portraitOnly = Facet(id = 1L, name = "A", position = 0, clockStack = listOf(widgetItem(7, widthDp = 160, heightDp = 90)))
        val both = portraitOnly.copy(clockStack = listOf(widgetItem(7, widthDp = 160, heightDp = 90, landscapeWidthDp = 400, landscapeHeightDp = 300)))
        val settings = LauncherSettings(activeFacetId = 1L)

        assertEquals(160 to 90, HomeUiState(settings = settings, facets = listOf(portraitOnly)).clockWidgetSizeDp(landscape))
        assertEquals(400 to 300, HomeUiState(settings = settings, facets = listOf(both)).clockWidgetSizeDp(landscape))
        assertEquals(160 to 90, HomeUiState(settings = settings, facets = listOf(both)).clockWidgetSizeDp(portrait))
    }

    @Test
    fun `the visible clock page is the remembered one, else the first`() {
        // Given a stack of a clock and two widgets
        val facet = Facet(id = 1L, name = "A", position = 0, clockStack = listOf(clockItem(id = 10L, position = 0), widgetItem(41, id = 11L, position = 1), widgetItem(42, id = 12L, position = 2)))
        val settings = LauncherSettings(activeFacetId = 1L)

        // Then it starts on the first page, resumes on a remembered one, and falls back when that page is gone
        assertEquals(10L, HomeUiState(settings = settings, facets = listOf(facet)).visibleClockItem?.id)
        assertEquals(12L, HomeUiState(settings = settings, facets = listOf(facet), visibleClockItemId = 12L).visibleClockItem?.id)
        assertEquals(10L, HomeUiState(settings = settings, facets = listOf(facet), visibleClockItemId = 99L).visibleClockItem?.id)
    }

    @Test
    fun `the widget id is the visible page's, and every widget in the stack needs listening`() {
        val facet = Facet(id = 1L, name = "A", position = 0, clockStack = listOf(clockItem(id = 10L, position = 0), widgetItem(41, id = 11L, position = 1), widgetItem(42, id = 12L, position = 2)))
        val settings = LauncherSettings(activeFacetId = 1L)

        assertEquals(null, HomeUiState(settings = settings, facets = listOf(facet)).clockWidgetAppWidgetId)
        assertEquals(42, HomeUiState(settings = settings, facets = listOf(facet), visibleClockItemId = 12L).clockWidgetAppWidgetId)
        assertEquals(listOf(41, 42), HomeUiState(settings = settings, facets = listOf(facet)).clockStackWidgetIds)
    }

    @Test
    fun `without pro only the first clock page shows and the rest stay saved`() {
        // Given a stack of a clock and two widgets, the last remembered as visible
        val facet = Facet(id = 1L, name = "A", position = 0, clockStack = listOf(clockItem(id = 10L, position = 0), widgetItem(41, id = 11L, position = 1), widgetItem(42, id = 12L, position = 2)))
        val lapsed = HomeUiState(settings = LauncherSettings(activeFacetId = 1L), facets = listOf(facet), visibleClockItemId = 12L, isPro = false)

        // Then only the first page is there, it is the visible one, no widget needs listening, and the facet still holds all three
        assertEquals(listOf(10L), lapsed.clockStack.map { it.id })
        assertEquals(10L, lapsed.visibleClockItem?.id)
        assertEquals(emptyList<Int>(), lapsed.clockStackWidgetIds)
        assertEquals(3, lapsed.activeFacet?.clockStack?.size)
    }

    @Test
    fun `an empty stack has no visible page`() {
        val facet = Facet(id = 1L, name = "A", position = 0, clockStack = emptyList())

        assertEquals(null, HomeUiState(settings = LauncherSettings(activeFacetId = 1L), facets = listOf(facet)).visibleClockItem)
    }

    @Test
    fun `there is no hosted widget size before a resize`() {
        val state = HomeUiState(settings = LauncherSettings(activeFacetId = 1L), facets = listOf(Facet(id = 1L, name = "A", position = 0)))

        assertEquals(null, state.clockWidgetSizeDp(landscape))
        assertEquals(null, HomeUiState().clockWidgetSizeDp(portrait))
    }

    @Test
    fun `first run prompts are done once onboarding, the set default step and the gesture hint are behind the user`() {
        val done = HomeUiState(
            settings = LauncherSettings(onboardingCompleted = true, coachMarksSeen = setOf(HOME_GESTURES_COACH_MARK_ID, HOME_SET_DEFAULT_PROMPT_ID)),
        )

        assertEquals(true, done.firstRunPromptsDone)
        assertEquals(false, done.copy(settings = done.settings.copy(onboardingCompleted = false)).firstRunPromptsDone)
        assertEquals(false, done.copy(settings = done.settings.copy(coachMarksSeen = setOf(HOME_SET_DEFAULT_PROMPT_ID))).firstRunPromptsDone)
        assertEquals(false, done.copy(settings = done.settings.copy(coachMarksSeen = setOf(HOME_GESTURES_COACH_MARK_ID))).firstRunPromptsDone)
        // Already the default launcher: the set default step needs no answer of its own
        assertEquals(
            true,
            done.copy(settings = done.settings.copy(coachMarksSeen = setOf(HOME_GESTURES_COACH_MARK_ID)), isDefaultLauncher = true).firstRunPromptsDone,
        )
    }
}
