package com.facetlauncher.app.ui.facets

import android.content.pm.LauncherApps
import android.os.UserManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
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
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.domain.ObservePlacedItemsUseCase
import com.facetlauncher.app.domain.ActivateFacetByIdUseCase
import com.facetlauncher.app.ui.testAutomation
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import com.facetlauncher.app.data.PlacementRepository

/**
 * The top-level per-facet screen is now a short nav list — Rename, an "Appearance" card, then
 * one "Apps and folders" card with a plain row into each area's own settings screen (Apps list /
 * Dock / Calendars), mirroring `SettingsScreen`'s own layout. No Inherit/Override switches here any
 * more — each destination screen now owns its own (see chat history), exercised by
 * `com.facetlauncher.app.ui.settings.HomeAppsListSettingsScreenTest` / `DockSettingsScreenTest` /
 * `com.facetlauncher.app.ui.home.clock.ClockStyleGalleryScreenTest`, which cover both the global
 * and the facet-scoped instance of each.
 */
class FacetSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** Set by [setContent] alongside the [FacetRepository] it returns — tests that need to seed the global settings (e.g. calendar selection) read this directly rather than widening every existing `setContent()` call site's return type. */
    private lateinit var settingsRepository: SettingsRepository

    private fun setContent(
        onNavigateToAppsList: (Long) -> Unit = {},
        onNavigateToDockSettings: (Long) -> Unit = {},
        onNavigateToAppearance: (Long) -> Unit = {},
        onFacetApply: () -> Unit = {},
    ): FacetRepository {
        lateinit var facetRepository: FacetRepository
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                facetRepository = FacetRepository(database.facetDao())
                val appRepository = AppRepository(launcherApps, context.getSystemService(UserManager::class.java), context)
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
                    ObservePlacedItemsUseCase(PlacementRepository(database.appPlacementDao(), database.folderPlacementDao()), FolderRepository(database.folderDao(), appRepository), appRepository),
                    testAutomation(context, database, facetRepository, settingsRepository).activate,
                )
            }
            FacetLauncherTheme {
                FacetSettingsScreen(
                    onBack = {},
                    onNavigateToAppsList = onNavigateToAppsList,
                    onNavigateToDockSettings = onNavigateToDockSettings,
                    onNavigateToAppearance = onNavigateToAppearance,
                    onFacetApply = onFacetApply,
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
        val appsSectionTop = composeRule.onNodeWithText("Apps and folders").fetchSemanticsNode().boundsInRoot.top
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
    fun calendarRowIsGoneBecauseAppearanceOwnsIt() {
        // Given a facet's settings
        setContent()
        awaitFacetLoaded()
        scrollToRow("facet_dock_settings_row")

        // Then there's no calendar row here — it moved into the facet's Appearance screen
        composeRule.onNodeWithTag("facet_calendar_settings_row").assertDoesNotExist()
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
    fun appearanceRowNavigatesWithTheFacetsIdAndReflectsTheEffectiveLookFields() {
        var navigatedFromAppearance: Long? = null
        setContent(onNavigateToAppearance = { navigatedFromAppearance = it })
        awaitFacetLoaded()

        // This row previews the facet's clock style, then the dock display style and app row
        // presentation — inheriting the global defaults (Icons, Icon & Text) since this facet
        // overrides none of them — each named for its surface: "<template> clock · Dock icons · Home icon & text".
        scrollToRow("facet_appearance_row")
        composeRule.onNodeWithTag("facet_appearance_row").assertTextContains(" clock · Dock icons · Home icon & text", substring = true)
        composeRule.onNodeWithTag("facet_appearance_row").performClick()
        assertEquals(true, navigatedFromAppearance != null)
    }

    @Test
    fun applyFacetButtonActivatesThisFacetAndInvokesOnFacetApply() {
        // Given a fresh facet that isn't yet the active one
        var applied = false
        val facetRepository = setContent(onFacetApply = { applied = true })
        awaitFacetLoaded()
        val facetId = runBlocking { facetRepository.observeFacets().first().first().id }
        composeRule.onNodeWithTag("facet_settings_apply_button").assertIsEnabled()

        // When the header's "Apply facet" button is tapped
        composeRule.onNodeWithTag("facet_settings_apply_button").performClick()

        // Then this facet becomes the active one and the nav callback fires
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().activeFacetId } == facetId
        }
        assertEquals(true, applied)
    }

    @Test
    fun applyFacetButtonIsDisabledWhenThisFacetIsAlreadyActive() {
        // Given this facet is already the active one
        val facetRepository = setContent()
        awaitFacetLoaded()
        val facetId = runBlocking { facetRepository.observeFacets().first().first().id }
        runBlocking { settingsRepository.setActiveFacetId(facetId) }

        // Then the header's "Apply facet" button is disabled — nothing to apply
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("facet_settings_apply_button").assertIsNotEnabled() }.isSuccess
        }
    }
}
