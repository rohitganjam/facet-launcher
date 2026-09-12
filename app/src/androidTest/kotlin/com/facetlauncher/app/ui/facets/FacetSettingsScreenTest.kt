package com.facetlauncher.app.ui.facets

import android.content.pm.LauncherApps
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * The top-level per-facet screen is now a short nav list — Rename, three Inherit/Override
 * cards, and a row into each area's own settings screen. The detailed controls (position,
 * presentation, dock display style, favorites, …) are exercised by
 * `com.facetlauncher.app.ui.settings.HomeAppsListSettingsScreenTest` /
 * `DockSettingsScreenTest`, which cover both the global and the facet-scoped instance.
 */
class FacetSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** Set by [setContent] alongside the [FacetRepository] it returns — tests that need to seed the global settings (e.g. calendar selection) read this directly rather than widening every existing `setContent()` call site's return type. */
    private lateinit var settingsRepository: SettingsRepository

    private fun setContent(
        onNavigateToAppsList: (Long) -> Unit = {},
        onNavigateToDockSettings: (Long) -> Unit = {},
        onNavigateToCalendarSettings: (Long) -> Unit = {},
        onNavigateToClockStyleGallery: (Long) -> Unit = {},
    ): FacetRepository {
        lateinit var facetRepository: FacetRepository
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                facetRepository = FacetRepository(database.facetDao())
                val appRepository = AppRepository(launcherApps)
                settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "facet-settings-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val facetId = runBlocking { facetRepository.addFacet().id }
                FacetSettingsViewModel(
                    SavedStateHandle(mapOf("facetId" to facetId)),
                    facetRepository,
                    settingsRepository,
                    FavoriteAppRepository(database.favoriteAppDao(), appRepository),
                    DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), appRepository),
                    FacetDockAppRepository(database.facetDockAppDao(), appRepository),
                    DockAppRepository(database.dockAppDao(), appRepository),
                )
            }
            FacetLauncherTheme {
                FacetSettingsScreen(
                    onBack = {},
                    onNavigateToAppsList = onNavigateToAppsList,
                    onNavigateToDockSettings = onNavigateToDockSettings,
                    onNavigateToCalendarSettings = onNavigateToCalendarSettings,
                    onNavigateToClockStyleGallery = onNavigateToClockStyleGallery,
                    viewModel = viewModel,
                )
            }
        }
        composeRule.waitForIdle()
        return facetRepository
    }

    private fun awaitFacetLoaded() {
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Facet 1").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun scrollToRow(tag: String) {
        composeRule.onNodeWithTag("facet_settings_screen").performScrollToNode(hasTestTag(tag))
    }

    @Test
    fun renameCardRendersDirectlyUnderTheHeaderBeforeApps() {
        setContent()

        val renameTop = composeRule.onNodeWithTag("facet_settings_rename_row").fetchSemanticsNode().boundsInRoot.top
        val appsSectionTop = composeRule.onNodeWithText("APPS LIST").fetchSemanticsNode().boundsInRoot.top
        assert(renameTop < appsSectionTop)
    }

    @Test
    fun headerAndRenameSubtitleShowTheFacetsName() {
        setContent()
        awaitFacetLoaded()

        composeRule.onNodeWithTag("back_button").assertExists()
        composeRule.onNodeWithTag("facet_settings_rename_row").assertTextContains("Facet 1")
    }

    @Test
    fun renamingUpdatesTheDisplayedNameAndPersists() {
        val facetRepository = setContent()
        awaitFacetLoaded()

        composeRule.onNodeWithTag("facet_settings_rename_row").performClick()
        composeRule.onNodeWithTag("rename_dialog_field").performTextReplacement("Work")
        composeRule.onNodeWithTag("rename_dialog_save").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("facet_settings_rename_row").assertTextContains("Work") }.isSuccess
        }
        runBlocking { assertEquals("Work", facetRepository.observeFacets().first().first().name) }
    }

    @Test
    fun appsListRowNavigatesWithTheFacetsId() {
        var navigatedFacetId: Long? = null
        setContent(onNavigateToAppsList = { navigatedFacetId = it })

        composeRule.onNodeWithTag("facet_apps_list_row").performClick()

        assertEquals(true, navigatedFacetId != null)
    }

    @Test
    fun dockSettingsRowNavigatesWithTheFacetsId() {
        var navigatedFacetId: Long? = null
        setContent(onNavigateToDockSettings = { navigatedFacetId = it })

        scrollToRow("facet_dock_settings_row")
        composeRule.onNodeWithTag("facet_dock_settings_row").performClick()

        assertEquals(true, navigatedFacetId != null)
    }

    @Test
    fun clockStyleAndCalendarRowsNavigateWithTheFacetsId() {
        var navigatedFromClock: Long? = null
        var navigatedFromCalendar: Long? = null
        setContent(
            onNavigateToClockStyleGallery = { navigatedFromClock = it },
            onNavigateToCalendarSettings = { navigatedFromCalendar = it },
        )

        scrollToRow("facet_clock_style_gallery_row")
        composeRule.onNodeWithTag("facet_clock_style_gallery_row").performClick()
        assertEquals(true, navigatedFromClock != null)

        scrollToRow("facet_calendar_settings_row")
        composeRule.onNodeWithTag("facet_calendar_settings_row").performClick()
        assertEquals(true, navigatedFromCalendar != null)
    }

    @Test
    fun overridingTheAppsCardPersistsTheFlag() {
        val facetRepository = setContent()
        awaitFacetLoaded()

        composeRule.onNodeWithTag("facet_apps_override_row").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { facetRepository.observeFacets().first().first().overrideApps }
        }
    }

    @Test
    fun overridingTheDockCardPersistsTheFlag() {
        val facetRepository = setContent()
        awaitFacetLoaded()

        scrollToRow("facet_dock_override_row")
        composeRule.onNodeWithTag("facet_dock_override_row").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { facetRepository.observeFacets().first().first().overrideDock }
        }
    }

    @Test
    fun overridingTheClockCardPersistsTheFlag() {
        val facetRepository = setContent()
        awaitFacetLoaded()

        scrollToRow("facet_clock_override_row")
        composeRule.onNodeWithTag("facet_clock_override_row").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { facetRepository.observeFacets().first().first().overrideClock }
        }
        // The "Clock & Calendar Style" row's subtitle then flips to "Overriding defaults · tap to
        // edit" (a plain `if (isOverridingClock)` branch — asserted at the unit level).
    }

    @Test
    fun dockRowSubtitleReflectsTheEffectiveDisplayStyle() {
        setContent()
        awaitFacetLoaded()

        // Not overriding -> inherits the global default (Icons)
        scrollToRow("facet_dock_settings_row")
        composeRule.onNodeWithTag("facet_dock_settings_row").assertTextContains("Icons", substring = true)
    }

    @Test
    fun calendarRowSubtitleShowsZeroSelectedWhenNothingHasBeenExplicitlyChosenYet() {
        // Given a fresh facet that isn't overriding, and the global selection never touched (`null` — "none decided", not "all")
        setContent()
        awaitFacetLoaded()
        scrollToRow("facet_calendar_settings_row")

        // Then the row shows a literal zero, not a device calendar total standing in for "all"
        composeRule.onNodeWithTag("facet_calendar_settings_row").assertTextContains("0 calendars selected", substring = true)
    }

    @Test
    fun calendarRowSubtitleReflectsTheGlobalSelectionWhileInheriting() {
        // Given 2 calendars explicitly selected globally, and this facet inheriting (not overriding)
        setContent()
        awaitFacetLoaded()
        runBlocking { settingsRepository.setSelectedCalendarIds(setOf("cal-1", "cal-2")) }
        scrollToRow("facet_calendar_settings_row")

        // Then the row reflects that inherited count and the all-day events visibility
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching {
                composeRule.onNodeWithTag("facet_calendar_settings_row").assertTextContains("2 calendars selected · All-day events visible", substring = true)
            }.isSuccess
        }
    }
}
