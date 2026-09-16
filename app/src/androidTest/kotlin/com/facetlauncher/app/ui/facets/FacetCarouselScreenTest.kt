package com.facetlauncher.app.ui.facets

import android.content.pm.LauncherApps
import android.os.UserManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.domain.ObserveFacetPreviewsUseCase
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FacetCarouselScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    /**
     * [seed] runs against real (throwaway, in-memory) repositories before the screen renders.
     * [seedApps] runs after, with access to the app-backed repositories, for tests that need to
     * populate favorites and/or the dock (both source from the same real installed-apps list, so
     * they're seeded together rather than via a second [FacetRepository]/[SettingsRepository]-only hook).
     */
    private fun setContent(
        onFacetApplied: () -> Unit = {},
        onEditFacet: (Long) -> Unit = {},
        onReorderFacets: () -> Unit = {},
        onNavigateToSettings: () -> Unit = {},
        onDismissDrag: (Float) -> Unit = {},
        onDismissDragEnd: () -> Unit = {},
        seed: suspend (FacetRepository, SettingsRepository) -> Unit = { _, _ -> },
        seedApps: suspend (AppRepository, FavoriteAppRepository, DockAppRepository) -> Unit = { _, _, _ -> },
    ) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val facetRepository = FacetRepository(database.facetDao())
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "facet-carousel-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                val appRepository = AppRepository(launcherApps, context.getSystemService(UserManager::class.java), context)
                val favoriteAppRepository = FavoriteAppRepository(database.favoriteAppDao(), database.favoriteFolderPlacementDao(), FolderRepository(database.folderDao(), appRepository), appRepository)
                val defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), database.defaultFavoriteFolderPlacementDao(), FolderRepository(database.folderDao(), appRepository), appRepository)
                val usageStatsRepository = com.facetlauncher.app.data.UsageStatsRepository(
                    context.getSystemService(android.app.usage.UsageStatsManager::class.java),
                    appRepository
                )
                val usageAccessRepository = com.facetlauncher.app.data.UsageAccessRepository(
                    context.getSystemService(android.app.AppOpsManager::class.java),
                    context
                )
                val calendarPermissionRepository = com.facetlauncher.app.data.CalendarPermissionRepository(context)
                val calendarRepository = com.facetlauncher.app.data.CalendarRepository(context.contentResolver)
                val dockAppRepository = DockAppRepository(database.dockAppDao(), database.dockFolderPlacementDao(), FolderRepository(database.folderDao(), appRepository), appRepository)
                val facetDockAppRepository = FacetDockAppRepository(database.facetDockAppDao(), database.facetDockFolderPlacementDao(), FolderRepository(database.folderDao(), appRepository), appRepository)
                val wallpaperRepository = com.facetlauncher.app.data.WallpaperRepository(
                    android.app.WallpaperManager.getInstance(context),
                )
                runBlocking {
                    seed(facetRepository, settingsRepository)
                }
                runBlocking { seedApps(appRepository, favoriteAppRepository, dockAppRepository) }
                FacetCarouselViewModel(
                    facetRepository,
                    settingsRepository,
                    wallpaperRepository,
                    ObserveFacetPreviewsUseCase(
                        facetRepository,
                        settingsRepository,
                        favoriteAppRepository,
                        defaultFavoriteAppRepository,
                        facetDockAppRepository,
                        dockAppRepository,
                        usageStatsRepository,
                        usageAccessRepository,
                        calendarPermissionRepository,
                        calendarRepository,
                    ),
                )
            }
            FacetLauncherTheme {
                FacetCarouselScreen(
                    onFacetApplied = onFacetApplied,
                    onEditFacet = onEditFacet,
                    onReorderFacets = onReorderFacets,
                    onNavigateToSettings = onNavigateToSettings,
                    onDismissDrag = onDismissDrag,
                    onDismissDragEnd = onDismissDragEnd,
                    viewModel = viewModel,
                )
            }
        }
        composeRule.waitForIdle()
        // FacetCarouselViewModel's uiState now combines three flows, one of which
        // (ObserveFacetPreviewsUseCase) does a real LauncherApps query — real async work that
        // doesn't necessarily land inside waitForIdle()'s window (which mainly waits for
        // already-pending recomposition, not for a StateFlow update that hasn't arrived yet).
        // FacetCarouselContent renders nothing at all until uiState.facets is non-empty, so
        // wait explicitly for that first real frame rather than assuming a single waitForIdle()
        // call caught it.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("facet_carousel_screen").fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * A single fast [androidx.compose.ui.test.swipeLeft] can carry enough fling velocity to
     * skip two pages at once (landing on the trailing Add-facet page instead of the
     * intended neighbor) — this instead drags a controlled ~55% of the pager's width over
     * many small steps, low enough velocity to just settle on the very next page.
     */
    private fun swipeToNextPage() {
        val pager = composeRule.onNodeWithTag("facet_carousel_pager")
        val widthPx = pager.fetchSemanticsNode().size.width.toFloat()
        pager.performTouchInput {
            down(centerRight)
            repeat(15) { step ->
                moveTo(centerRight - Offset(widthPx * 0.55f * (step + 1) / 15f, 0f))
            }
            up()
        }
    }

    @Test
    fun tappingACardAppliesItImmediately() {
        // Given two facets, the first one active
        lateinit var settingsRepository: SettingsRepository
        var secondFacetId = 0L
        var applied = false
        setContent(
            onFacetApplied = { applied = true },
            seed = { facetRepository, settings ->
                val first = facetRepository.addFacet()
                val second = facetRepository.addFacet()
                secondFacetId = second.id
                settings.setActiveFacetId(first.id)
                settingsRepository = settings
            },
        )

        // When swiping to center the second page and tapping it — recent-apps style, tapping a
        // card applies it immediately, there's no separate Select step.
        swipeToNextPage()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("facet_page_name_$secondFacetId", useUnmergedTree = true).assertTextEquals("Facet 2")
        composeRule.onNodeWithTag("facet_page_$secondFacetId").performClick()

        // Then it becomes the active facet and onFacetApplied fires (the caller returns Home)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().activeFacetId == secondFacetId }
        }
        assertEquals(true, applied)
    }

    @Test
    fun onlyTheActiveFacetShowsTheActiveIndicator() {
        // Given two facets, the first one active
        var firstFacetId = 0L
        var secondFacetId = 0L
        setContent(
            seed = { facetRepository, settings ->
                val first = facetRepository.addFacet()
                val second = facetRepository.addFacet()
                firstFacetId = first.id
                secondFacetId = second.id
                settings.setActiveFacetId(first.id)
            },
        )

        // Then the first facet's card shows the checkmark, and swiping to the second shows it
        // has none.
        composeRule.onNodeWithTag("facet_page_active_$firstFacetId", useUnmergedTree = true).assertExists()
        swipeToNextPage()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("facet_page_active_$secondFacetId", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun swipingToACardWithoutTappingLeavesTheActiveFacetUnchanged() {
        // Given two facets, the first one active
        lateinit var settingsRepository: SettingsRepository
        var firstFacetId = 0L
        var secondFacetId = 0L
        setContent(
            seed = { facetRepository, settings ->
                val first = facetRepository.addFacet()
                val second = facetRepository.addFacet()
                firstFacetId = first.id
                secondFacetId = second.id
                settings.setActiveFacetId(first.id)
                settingsRepository = settings
            },
        )

        // When browsing to the second page without tapping it — browsing never applies
        swipeToNextPage()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("facet_page_name_$secondFacetId", useUnmergedTree = true).assertTextEquals("Facet 2")

        // Then the active facet is unchanged
        assertEquals(firstFacetId, runBlocking { settingsRepository.settings.first().activeFacetId })
    }

    @Test
    fun swipingRightOnTheFirstFacetCardForwardsTheDismissDrag() {
        // Given two facets, the first one (page 0) active — this screen doesn't own opening or
        // closing itself any more (its host, HomeDrawerRoute, does via a follow-finger axis —
        // see chat history); it only forwards the raw drag deltas it's uniquely positioned to
        // catch, since the pager would otherwise eat a rightward drag on page 0 as a dead
        // overscroll before anything else saw it.
        var draggedTotal = 0f
        var dragEnded = false
        var applied = false
        setContent(
            onFacetApplied = { applied = true },
            onDismissDrag = { draggedTotal += it },
            onDismissDragEnd = { dragEnded = true },
            seed = { facetRepository, settings ->
                val first = facetRepository.addFacet()
                facetRepository.addFacet()
                settings.setActiveFacetId(first.id)
            },
        )

        // When swiping right on the card itself — there's no previous facet from page 0
        val pager = composeRule.onNodeWithTag("facet_carousel_pager")
        val pagerWidth = pager.fetchSemanticsNode().size.width.toFloat()
        pager.performTouchInput {
            down(centerLeft)
            repeat(15) { step -> moveTo(centerLeft + Offset(pagerWidth * 0.6f * (step + 1) / 15f, 0f)) }
            up()
        }
        composeRule.waitForIdle()

        // Then the drag was forwarded and the gesture's end was reported, without applying anything
        assertEquals(true, draggedTotal > 0f)
        assertEquals(true, dragEnded)
        assertEquals(false, applied)
    }

    @Test
    fun swipingRightOnANonFirstFacetCardBrowsesInsteadOfForwardingADismissDrag() {
        // Given two facets, browsed to the second page
        var draggedTotal = 0f
        var dragEnded = false
        var firstFacetId = 0L
        setContent(
            onDismissDrag = { draggedTotal += it },
            onDismissDragEnd = { dragEnded = true },
            seed = { facetRepository, settings ->
                val first = facetRepository.addFacet()
                facetRepository.addFacet()
                firstFacetId = first.id
                settings.setActiveFacetId(first.id)
            },
        )
        swipeToNextPage()
        composeRule.waitForIdle()

        // When swiping right on the card from page 1 — this browses back to page 0, it does not
        // forward a dismiss drag (only the first page has nowhere to browse to)
        val pager = composeRule.onNodeWithTag("facet_carousel_pager")
        val widthPx = pager.fetchSemanticsNode().size.width.toFloat()
        pager.performTouchInput {
            down(centerLeft)
            repeat(15) { step -> moveTo(centerLeft + Offset(widthPx * 0.55f * (step + 1) / 15f, 0f)) }
            up()
        }
        composeRule.waitForIdle()

        // Then it's back on the first facet's card and nothing was forwarded
        composeRule.onNodeWithTag("facet_page_name_$firstFacetId", useUnmergedTree = true).assertTextEquals("Facet 1")
        assertEquals(0f, draggedTotal)
        assertEquals(false, dragEnded)
    }

    @Test
    fun reorderButtonNavigatesToManageFacets() {
        // Given two facets (so the button is enabled)
        var reorderRequested = false
        setContent(
            onReorderFacets = { reorderRequested = true },
            seed = { facetRepository, settings ->
                val first = facetRepository.addFacet()
                facetRepository.addFacet()
                settings.setActiveFacetId(first.id)
            },
        )

        // When tapping the Reorder button
        composeRule.onNodeWithTag("facet_carousel_reorder").performClick()

        // Then the caller is asked to open Manage Facets
        assertEquals(true, reorderRequested)
    }

    @Test
    fun reorderButtonIsDisabledWithOnlyOneFacet() {
        setContent(
            seed = { facetRepository, settings ->
                val only = facetRepository.addFacet()
                settings.setActiveFacetId(only.id)
            },
        )

        // Nothing to reorder with a single facet
        composeRule.onNodeWithTag("facet_carousel_reorder").assertIsNotEnabled()
    }

    @Test
    fun headerShowsTheTitleAndLiveFacetCountSingularAndPlural() {
        // Given a single facet (singular count wording)
        setContent(
            seed = { facetRepository, settings ->
                val only = facetRepository.addFacet()
                settings.setActiveFacetId(only.id)
            },
        )
        composeRule.onNodeWithText("Switch Facets").assertExists()
        composeRule.onNodeWithText("1 facet available").assertExists()
    }

    @Test
    fun headerShowsThePluralCountWithMultipleFacets() {
        // Given two facets (plural count wording)
        setContent(
            seed = { facetRepository, settings ->
                val first = facetRepository.addFacet()
                facetRepository.addFacet()
                settings.setActiveFacetId(first.id)
            },
        )
        composeRule.onNodeWithText("2 facets available").assertExists()
    }

    @Test
    fun addFacetPageIsHiddenOnceTheMaximumIsReached() {
        // Given the maximum of 3 facets already exist
        setContent(
            seed = { facetRepository, settings ->
                repeat(FacetRepository.MAX_FACETS) { facetRepository.addFacet() }
                settings.setActiveFacetId(1L)
            },
        )

        // Then no add-facet page is offered
        composeRule.onNodeWithTag("facet_carousel_add_page").assertDoesNotExist()
    }

    @Test
    fun deleteIsDisabledWithOnlyOneFacetRemaining() {
        // Given a single facet
        var onlyFacetId = 0L
        setContent(
            seed = { facetRepository, settings ->
                val only = facetRepository.addFacet()
                onlyFacetId = only.id
                settings.setActiveFacetId(only.id)
            },
        )

        // Then the delete action is disabled — the last facet can't be removed
        composeRule.onNodeWithTag("facet_page_delete_$onlyFacetId", useUnmergedTree = true).assertIsNotEnabled()
    }

    @Test
    fun deletingAFacetRemovesItFromTheCarousel() {
        // Given two facets, centered on the second
        var secondFacetId = 0L
        setContent(
            seed = { facetRepository, settings ->
                val first = facetRepository.addFacet()
                val second = facetRepository.addFacet()
                secondFacetId = second.id
                settings.setActiveFacetId(first.id)
            },
        )
        swipeToNextPage()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("facet_page_name_$secondFacetId", useUnmergedTree = true).assertTextEquals("Facet 2")

        // When deleting it and confirming
        composeRule.onNodeWithTag("facet_page_delete_$secondFacetId", useUnmergedTree = true).performClick()
        composeRule.onNode(hasText("Delete")).performClick()
        composeRule.waitForIdle()

        // Then it's gone — the deletion is a real async round trip (ViewModel -> Repository ->
        // Room -> StateFlow -> recomposition), which a single waitForIdle() doesn't reliably
        // catch, so poll instead of asserting immediately (see chat history / CLAUDE.md)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("Facet 2").assertDoesNotExist() }.isSuccess
        }
    }

    @Test
    fun previewCardShowsBothFavoritesAndDockAppsTogether() {
        // Given one facet with a favorite app, and a (facet-independent, shared) dock app
        var facetId = 0L
        var favoriteLabel = ""
        var dockLabel = ""
        setContent(
            seed = { facetRepository, settings ->
                val only = facetRepository.addFacet()
                facetId = only.id
                settings.setActiveFacetId(only.id)
                // Overriding its own favorites, rather than the launcher-wide default list, since
                // this test seeds a favorite specifically for this facet.
                facetRepository.setOverridingFavorites(only, true)
            },
            seedApps = { appRepository, favoriteAppRepository, dockAppRepository ->
                val installed = appRepository.getInstalledApps()
                val favoriteApp = installed[0]
                val dockApp = installed[1]
                favoriteLabel = favoriteApp.label
                dockLabel = dockApp.label
                favoriteAppRepository.addFavorite(facetId = facetId, app = favoriteApp, position = 0)
                dockAppRepository.addDockApp(dockApp, position = 0)
            },
        )

        // Then the preview card renders the favorite as a visible row and the dock app as an icon
        composeRule.onNodeWithText(favoriteLabel).assertExists()
        composeRule.onNodeWithContentDescription(dockLabel).assertExists()
    }

    @Test
    fun previewCardRendersTheWallpaperBehindItsContent() {
        // Given one facet
        var facetId = 0L
        setContent(
            seed = { facetRepository, settings ->
                val only = facetRepository.addFacet()
                facetId = only.id
                settings.setActiveFacetId(only.id)
            },
        )

        // Then the card paints a wallpaper backdrop, with the facet's own content still on top.
        // Unmerged tree: the card's own `clickable` merges its descendants' semantics, so the
        // backdrop's testTag is only visible as a standalone node in the unmerged tree.
        val wallpaperNodes = composeRule
            .onAllNodesWithTag("wallpaper_background", useUnmergedTree = true)
            .fetchSemanticsNodes().size
        assertEquals(true, wallpaperNodes > 0)
        composeRule.onNodeWithTag("facet_page_$facetId").assertExists()
    }

    @Test
    fun tappingAFavoriteRowInsideTheCardStillAppliesTheFacet() {
        // Given one facet with a favorite app
        var facetId = 0L
        var favoriteLabel = ""
        var applied = false
        setContent(
            onFacetApplied = { applied = true },
            seed = { facetRepository, settings ->
                val only = facetRepository.addFacet()
                facetId = only.id
                settings.setActiveFacetId(only.id)
                facetRepository.setOverridingFavorites(only, true)
            },
            seedApps = { appRepository, favoriteAppRepository, _ ->
                val favoriteApp = appRepository.getInstalledApps()[0]
                favoriteLabel = favoriteApp.label
                favoriteAppRepository.addFavorite(facetId = facetId, app = favoriteApp, position = 0)
            },
        )

        // When tapping directly on the favorite row's own label — the reused real AppRow's own
        // click used to consume this touch instead of letting it reach the card's own clickable
        // underneath, so the tap silently did nothing (see chat history).
        composeRule.onNodeWithText(favoriteLabel).performClick()

        // Then the facet is still applied
        assertEquals(true, applied)
    }

    @Test
    fun tappingADockIconInsideTheCardStillAppliesTheFacet() {
        // Given one facet and a (facet-independent, shared) dock app
        var facetId = 0L
        var dockLabel = ""
        var applied = false
        setContent(
            onFacetApplied = { applied = true },
            seed = { facetRepository, settings ->
                val only = facetRepository.addFacet()
                facetId = only.id
                settings.setActiveFacetId(only.id)
            },
            seedApps = { appRepository, _, dockAppRepository ->
                val dockApp = appRepository.getInstalledApps()[0]
                dockLabel = dockApp.label
                dockAppRepository.addDockApp(dockApp, position = 0)
            },
        )

        // When tapping directly on the dock icon — same reused-composable touch-consumption gap
        // as the favorite row above.
        composeRule.onNodeWithContentDescription(dockLabel).performClick()

        // Then the facet is still applied
        assertEquals(true, applied)
    }

    @Test
    fun longPressingAFavoriteRowInsideTheCardDoesNotOpenTheAppContextMenu() {
        // Given one facet with a favorite app — this card is a read-only preview of a
        // facet's Home layout, not a place to manage apps, so the reused real AppRow's own
        // long-press-to-open-Uninstall/App-Info menu must not fire here (see chat history).
        var facetId = 0L
        var favoriteLabel = ""
        setContent(
            seed = { facetRepository, settings ->
                val only = facetRepository.addFacet()
                facetId = only.id
                settings.setActiveFacetId(only.id)
                facetRepository.setOverridingFavorites(only, true)
            },
            seedApps = { appRepository, favoriteAppRepository, _ ->
                val favoriteApp = appRepository.getInstalledApps()[0]
                favoriteLabel = favoriteApp.label
                favoriteAppRepository.addFavorite(facetId = facetId, app = favoriteApp, position = 0)
            },
        )

        // When long-pressing the favorite row
        composeRule.onNodeWithText(favoriteLabel).performTouchInput { longClick() }

        // Then no context menu appears
        composeRule.onNodeWithTag("app_context_menu").assertDoesNotExist()
    }

    @Test
    fun longPressingADockIconInsideTheCardDoesNotOpenTheAppContextMenu() {
        // Given one facet and a (facet-independent, shared) dock app — same
        // read-only-preview reasoning as the favorite row above.
        var dockLabel = ""
        setContent(
            seed = { facetRepository, settings ->
                val only = facetRepository.addFacet()
                settings.setActiveFacetId(only.id)
            },
            seedApps = { appRepository, _, dockAppRepository ->
                val dockApp = appRepository.getInstalledApps()[0]
                dockLabel = dockApp.label
                dockAppRepository.addDockApp(dockApp, position = 0)
            },
        )

        // When long-pressing the dock icon
        composeRule.onNodeWithContentDescription(dockLabel).performTouchInput { longClick() }

        // Then no context menu appears
        composeRule.onNodeWithTag("app_context_menu").assertDoesNotExist()
    }

    @Test
    fun facetSettingsIconOnTheCardNavigatesToEditFacet() {
        // Given a single facet
        var facetId = 0L
        var editedFacetId = -1L
        setContent(
            onEditFacet = { editedFacetId = it },
            seed = { facetRepository, settings ->
                val only = facetRepository.addFacet()
                facetId = only.id
                settings.setActiveFacetId(only.id)
            },
        )

        // When tapping the card's own Settings icon
        composeRule.onNodeWithTag("facet_page_settings_$facetId", useUnmergedTree = true).performClick()

        // Then the caller is asked to navigate to that facet's settings
        assertEquals(facetId, editedFacetId)
    }

    @Test
    fun launcherSettingsRowInvokesTheCallback() {
        // Given the carousel
        var navigatedToSettings = false
        setContent(
            onNavigateToSettings = { navigatedToSettings = true },
            seed = { facetRepository, settings ->
                val only = facetRepository.addFacet()
                settings.setActiveFacetId(only.id)
            },
        )

        // Then the row renders its title + subtitle
        composeRule.onNodeWithText("Launcher settings").assertExists()
        composeRule.onNodeWithText("Appearance, Default clock, favorites, drawer and more").assertExists()

        // When tapping the "Launcher settings" row below the carousel
        composeRule.onNodeWithTag("facet_carousel_launcher_settings").performClick()

        // Then the caller is asked to navigate there
        assertEquals(true, navigatedToSettings)
    }
}
