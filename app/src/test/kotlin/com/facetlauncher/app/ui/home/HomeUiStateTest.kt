package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeUiStateTest {

    @Test
    fun `effective 24 hour time falls back to the global default when the active facet has no override`() {
        val state = HomeUiState(
            settings = LauncherSettings(use24HourTime = true, activeFacetId = 1L),
            facets = listOf(FacetEntity(id = 1L, name = "Facet 1", position = 0, overrideClock = false)),
        )

        assertEquals(true, state.effectiveUse24HourTime)
    }

    @Test
    fun `effective 24 hour time uses the active facet's override when set`() {
        val state = HomeUiState(
            settings = LauncherSettings(use24HourTime = true, activeFacetId = 1L),
            facets = listOf(FacetEntity(id = 1L, name = "Facet 1", position = 0, overrideClock = true, use24HourTime = false)),
        )

        assertEquals(false, state.effectiveUse24HourTime)
    }

    @Test
    fun `clockWidgetAppWidgetId is null when the active facet uses its native clock`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(FacetEntity(id = 1L, name = "Facet 1", position = 0, clockWidgetAppWidgetId = null)),
        )

        assertEquals(null, state.clockWidgetAppWidgetId)
    }

    @Test
    fun `clockWidgetAppWidgetId reflects the active facet's own bound widget, facet-scoped only`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 2L),
            facets = listOf(
                FacetEntity(id = 1L, name = "Facet 1", position = 0, clockWidgetAppWidgetId = 7),
                FacetEntity(id = 2L, name = "Facet 2", position = 1, clockWidgetAppWidgetId = 42),
            ),
        )

        assertEquals(42, state.clockWidgetAppWidgetId)
    }

    @Test
    fun `usage access prompt is hidden for favorites mode regardless of grant state`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(FacetEntity(id = 1L, name = "P1", position = 0, overrideApps = true, listContentMode = ListContentMode.FAVORITES)),
            usageAccessGranted = false,
        )

        assertEquals(false, state.showUsageAccessPrompt)
    }

    @Test
    fun `usage access prompt shows for recents mode when access isn't granted`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(FacetEntity(id = 1L, name = "P1", position = 0, overrideApps = true, listContentMode = ListContentMode.RECENTS)),
            usageAccessGranted = false,
        )

        assertEquals(true, state.showUsageAccessPrompt)
    }

    @Test
    fun `usage access prompt is hidden for most used mode once access is granted`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(FacetEntity(id = 1L, name = "P1", position = 0, overrideApps = true, listContentMode = ListContentMode.MOST_USED)),
            usageAccessGranted = true,
        )

        assertEquals(false, state.showUsageAccessPrompt)
    }

    @Test
    fun `usage access prompt stays hidden once dismissed even though access still isn't granted`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(FacetEntity(id = 1L, name = "P1", position = 0, overrideApps = true, listContentMode = ListContentMode.RECENTS)),
            usageAccessGranted = false,
            usageAccessPromptDismissed = true,
        )

        assertEquals(false, state.showUsageAccessPrompt)
    }

    @Test
    fun `active app row position falls back to the global default when the active facet has no override`() {
        val state = HomeUiState(
            settings = LauncherSettings(appRowPosition = AppRowPosition.RIGHT, activeFacetId = 1L),
            facets = listOf(FacetEntity(id = 1L, name = "Facet 1", position = 0, overrideApps = false)),
        )

        assertEquals(AppRowPosition.RIGHT, state.activeAppRowPosition)
    }

    @Test
    fun `active app row position uses the active facet's override when set`() {
        val state = HomeUiState(
            settings = LauncherSettings(appRowPosition = AppRowPosition.LEFT, activeFacetId = 1L),
            facets = listOf(
                FacetEntity(id = 1L, name = "Facet 1", position = 0, overrideApps = true, appRowPosition = AppRowPosition.RIGHT),
            ),
        )

        assertEquals(AppRowPosition.RIGHT, state.activeAppRowPosition)
    }

    @Test
    fun `active app row presentation falls back to the global default when the active facet has no override`() {
        val state = HomeUiState(
            settings = LauncherSettings(appRowPresentation = AppRowPresentation.TEXT_ONLY, activeFacetId = 1L),
            facets = listOf(FacetEntity(id = 1L, name = "Facet 1", position = 0, overrideApps = false)),
        )

        assertEquals(AppRowPresentation.TEXT_ONLY, state.activeAppRowPresentation)
    }

    @Test
    fun `active app row presentation uses the active facet's override when set`() {
        val state = HomeUiState(
            settings = LauncherSettings(appRowPresentation = AppRowPresentation.ICON_AND_TEXT, activeFacetId = 1L),
            facets = listOf(
                FacetEntity(
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
            facets = listOf(FacetEntity(id = 1L, name = "Work", position = 0, overrideDock = false)),
        )

        assertEquals(null, state.dockOverrideFacetName)
    }

    @Test
    fun `dock override facet name is the active facet's own name when it overrides the dock`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(FacetEntity(id = 1L, name = "Work", position = 0, overrideDock = true)),
        )

        assertEquals("Work", state.dockOverrideFacetName)
    }

    @Test
    fun `favorites override facet name is null when the active facet isn't overriding favorites`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(FacetEntity(id = 1L, name = "Work", position = 0, overridingFavorites = false)),
        )

        assertEquals(null, state.favoritesOverrideFacetName)
    }

    @Test
    fun `favorites override facet name is the active facet's own name when it overrides favorites`() {
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(FacetEntity(id = 1L, name = "Work", position = 0, overridingFavorites = true)),
        )

        assertEquals("Work", state.favoritesOverrideFacetName)
    }

    @Test
    fun `landscape clock props fall back to the portrait ones when no landscape value is set`() {
        val state = HomeUiState(
            settings = LauncherSettings(clockAlignment = ClockAlignment.RIGHT, clockZoneHeightDp = 200f, clockScale = 1.2f),
        )

        assertEquals(ClockAlignment.RIGHT, state.landscapeClockAlignment)
        assertEquals(200f, state.landscapeClockZoneHeightDp)
        assertEquals(1.2f, state.landscapeClockScale)
    }

    @Test
    fun `landscape clock props use the global landscape values when set`() {
        val state = HomeUiState(
            settings = LauncherSettings(
                clockAlignment = ClockAlignment.RIGHT,
                landscapeClockAlignment = ClockAlignment.CENTER,
                landscapeClockZoneHeightDp = 310f,
                landscapeClockScale = 0.9f,
            ),
        )

        assertEquals(ClockAlignment.CENTER, state.landscapeClockAlignment)
        assertEquals(310f, state.landscapeClockZoneHeightDp)
        assertEquals(0.9f, state.landscapeClockScale)
        assertEquals(ClockAlignment.RIGHT, state.clockAlignment)
    }

    @Test
    fun `an overriding facet's landscape clock props win over the global ones and fall back to its own portrait values`() {
        val facet = FacetEntity(
            id = 1L, name = "Facet 1", position = 0, overrideClock = true,
            clockAlignment = ClockAlignment.RIGHT, clockScale = 1.4f,
            landscapeClockAlignment = ClockAlignment.CENTER,
        )
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L, landscapeClockAlignment = ClockAlignment.LEFT, landscapeClockScale = 0.5f),
            facets = listOf(facet),
        )

        // Then alignment is the facet's landscape value, and scale (no facet landscape value) is the facet's portrait one, not the global landscape one
        assertEquals(ClockAlignment.CENTER, state.landscapeClockAlignment)
        assertEquals(1.4f, state.landscapeClockScale)
    }

    @Test
    fun `a facet that is not overriding the clock uses the global landscape values`() {
        val facet = FacetEntity(id = 1L, name = "Facet 1", position = 0, overrideClock = false, landscapeClockAlignment = ClockAlignment.CENTER)
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L, landscapeClockAlignment = ClockAlignment.RIGHT),
            facets = listOf(facet),
        )

        assertEquals(ClockAlignment.RIGHT, state.landscapeClockAlignment)
    }

    @Test
    fun `landscape list alignment falls back to the portrait values when no landscape value is set`() {
        val state = HomeUiState(
            settings = LauncherSettings(appRowPosition = AppRowPosition.RIGHT, appListVerticalAlignment = AppListVerticalAlignment.TOP),
        )

        assertEquals(AppRowPosition.RIGHT, state.landscapeAppRowPosition)
        assertEquals(AppListVerticalAlignment.TOP, state.landscapeAppListVerticalAlignment)
        assertEquals(AppListColumnAlignment.BOTH_LEFT, state.landscapeAppListColumnAlignment)
    }

    @Test
    fun `landscape list alignment uses the global landscape values when set`() {
        val state = HomeUiState(
            settings = LauncherSettings(
                appRowPosition = AppRowPosition.RIGHT,
                landscapeAppRowPosition = AppRowPosition.CENTER,
                landscapeAppListVerticalAlignment = AppListVerticalAlignment.TOP,
                landscapeAppListColumnAlignment = AppListColumnAlignment.MIRRORED,
            ),
        )

        assertEquals(AppRowPosition.CENTER, state.landscapeAppRowPosition)
        assertEquals(AppListVerticalAlignment.TOP, state.landscapeAppListVerticalAlignment)
        assertEquals(AppListColumnAlignment.MIRRORED, state.landscapeAppListColumnAlignment)
        assertEquals(AppRowPosition.RIGHT, state.activeAppRowPosition)
    }

    @Test
    fun `a facet's explicit portrait list alignment beats the global landscape value`() {
        val facet = FacetEntity(id = 1L, name = "Facet 1", position = 0, appRowPosition = AppRowPosition.RIGHT)
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L, landscapeAppRowPosition = AppRowPosition.CENTER),
            facets = listOf(facet),
        )

        assertEquals(AppRowPosition.RIGHT, state.landscapeAppRowPosition)
    }

    @Test
    fun `a facet's own landscape list alignment wins`() {
        val facet = FacetEntity(id = 1L, name = "Facet 1", position = 0, appRowPosition = AppRowPosition.RIGHT, landscapeAppRowPosition = AppRowPosition.LEFT)
        val state = HomeUiState(
            settings = LauncherSettings(activeFacetId = 1L, landscapeAppRowPosition = AppRowPosition.CENTER),
            facets = listOf(facet),
        )

        assertEquals(AppRowPosition.LEFT, state.landscapeAppRowPosition)
    }
}
