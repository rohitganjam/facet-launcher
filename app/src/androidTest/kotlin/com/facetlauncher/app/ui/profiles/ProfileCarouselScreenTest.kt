package com.facetlauncher.app.ui.profiles

import android.content.pm.LauncherApps
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
import com.facetlauncher.app.data.ProfileDockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.ProfileRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.domain.ObserveProfilePreviewsUseCase
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProfileCarouselScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    /**
     * [seed] runs against real (throwaway, in-memory) repositories before the screen renders.
     * [seedApps] runs after, with access to the app-backed repositories, for tests that need to
     * populate favorites and/or the dock (both source from the same real installed-apps list, so
     * they're seeded together rather than via a second [ProfileRepository]/[SettingsRepository]-only hook).
     */
    private fun setContent(
        onProfileApplied: () -> Unit = {},
        onEditProfile: (Long) -> Unit = {},
        onReorderProfiles: () -> Unit = {},
        onNavigateToSettings: () -> Unit = {},
        onDismissDrag: (Float) -> Unit = {},
        onDismissDragEnd: () -> Unit = {},
        seed: suspend (ProfileRepository, SettingsRepository) -> Unit = { _, _ -> },
        seedApps: suspend (AppRepository, FavoriteAppRepository, DockAppRepository) -> Unit = { _, _, _ -> },
    ) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val profileRepository = ProfileRepository(database.profileDao())
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "profile-carousel-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                val appRepository = AppRepository(launcherApps)
                val favoriteAppRepository = FavoriteAppRepository(database.favoriteAppDao(), appRepository)
                val defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), appRepository)
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
                val dockAppRepository = DockAppRepository(database.dockAppDao(), appRepository)
                val profileDockAppRepository = ProfileDockAppRepository(database.profileDockAppDao(), appRepository)
                val wallpaperRepository = com.facetlauncher.app.data.WallpaperRepository(
                    android.app.WallpaperManager.getInstance(context),
                )
                runBlocking {
                    seed(profileRepository, settingsRepository)
                }
                runBlocking { seedApps(appRepository, favoriteAppRepository, dockAppRepository) }
                ProfileCarouselViewModel(
                    profileRepository,
                    settingsRepository,
                    wallpaperRepository,
                    ObserveProfilePreviewsUseCase(
                        profileRepository,
                        settingsRepository,
                        favoriteAppRepository,
                        defaultFavoriteAppRepository,
                        profileDockAppRepository,
                        dockAppRepository,
                        usageStatsRepository,
                        usageAccessRepository,
                        calendarPermissionRepository,
                        calendarRepository,
                    ),
                )
            }
            FacetLauncherTheme {
                ProfileCarouselScreen(
                    onProfileApplied = onProfileApplied,
                    onEditProfile = onEditProfile,
                    onReorderProfiles = onReorderProfiles,
                    onNavigateToSettings = onNavigateToSettings,
                    onDismissDrag = onDismissDrag,
                    onDismissDragEnd = onDismissDragEnd,
                    viewModel = viewModel,
                )
            }
        }
        composeRule.waitForIdle()
        // ProfileCarouselViewModel's uiState now combines three flows, one of which
        // (ObserveProfilePreviewsUseCase) does a real LauncherApps query — real async work that
        // doesn't necessarily land inside waitForIdle()'s window (which mainly waits for
        // already-pending recomposition, not for a StateFlow update that hasn't arrived yet).
        // ProfileCarouselContent renders nothing at all until uiState.profiles is non-empty, so
        // wait explicitly for that first real frame rather than assuming a single waitForIdle()
        // call caught it.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("profile_carousel_screen").fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * A single fast [androidx.compose.ui.test.swipeLeft] can carry enough fling velocity to
     * skip two pages at once (landing on the trailing Add-profile page instead of the
     * intended neighbor) — this instead drags a controlled ~55% of the pager's width over
     * many small steps, low enough velocity to just settle on the very next page.
     */
    private fun swipeToNextPage() {
        val pager = composeRule.onNodeWithTag("profile_carousel_pager")
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
        // Given two profiles, the first one active
        lateinit var settingsRepository: SettingsRepository
        var secondProfileId = 0L
        var applied = false
        setContent(
            onProfileApplied = { applied = true },
            seed = { profileRepository, settings ->
                val first = profileRepository.addProfile()
                val second = profileRepository.addProfile()
                secondProfileId = second.id
                settings.setActiveProfileId(first.id)
                settingsRepository = settings
            },
        )

        // When swiping to center the second page and tapping it — recent-apps style, tapping a
        // card applies it immediately, there's no separate Select step.
        swipeToNextPage()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("profile_page_name_$secondProfileId", useUnmergedTree = true).assertTextEquals("Profile 2")
        composeRule.onNodeWithTag("profile_page_$secondProfileId").performClick()

        // Then it becomes the active profile and onProfileApplied fires (the caller returns Home)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().activeProfileId == secondProfileId }
        }
        assertEquals(true, applied)
    }

    @Test
    fun swipingToACardWithoutTappingLeavesTheActiveProfileUnchanged() {
        // Given two profiles, the first one active
        lateinit var settingsRepository: SettingsRepository
        var firstProfileId = 0L
        var secondProfileId = 0L
        setContent(
            seed = { profileRepository, settings ->
                val first = profileRepository.addProfile()
                val second = profileRepository.addProfile()
                firstProfileId = first.id
                secondProfileId = second.id
                settings.setActiveProfileId(first.id)
                settingsRepository = settings
            },
        )

        // When browsing to the second page without tapping it — browsing never applies
        swipeToNextPage()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("profile_page_name_$secondProfileId", useUnmergedTree = true).assertTextEquals("Profile 2")

        // Then the active profile is unchanged
        assertEquals(firstProfileId, runBlocking { settingsRepository.settings.first().activeProfileId })
    }

    @Test
    fun swipingRightOnTheFirstProfileCardForwardsTheDismissDrag() {
        // Given two profiles, the first one (page 0) active — this screen doesn't own opening or
        // closing itself any more (its host, HomeDrawerRoute, does via a follow-finger axis —
        // see chat history); it only forwards the raw drag deltas it's uniquely positioned to
        // catch, since the pager would otherwise eat a rightward drag on page 0 as a dead
        // overscroll before anything else saw it.
        var draggedTotal = 0f
        var dragEnded = false
        var applied = false
        setContent(
            onProfileApplied = { applied = true },
            onDismissDrag = { draggedTotal += it },
            onDismissDragEnd = { dragEnded = true },
            seed = { profileRepository, settings ->
                val first = profileRepository.addProfile()
                profileRepository.addProfile()
                settings.setActiveProfileId(first.id)
            },
        )

        // When swiping right on the card itself — there's no previous profile from page 0
        val pager = composeRule.onNodeWithTag("profile_carousel_pager")
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
    fun swipingRightOnANonFirstProfileCardBrowsesInsteadOfForwardingADismissDrag() {
        // Given two profiles, browsed to the second page
        var draggedTotal = 0f
        var dragEnded = false
        var firstProfileId = 0L
        setContent(
            onDismissDrag = { draggedTotal += it },
            onDismissDragEnd = { dragEnded = true },
            seed = { profileRepository, settings ->
                val first = profileRepository.addProfile()
                profileRepository.addProfile()
                firstProfileId = first.id
                settings.setActiveProfileId(first.id)
            },
        )
        swipeToNextPage()
        composeRule.waitForIdle()

        // When swiping right on the card from page 1 — this browses back to page 0, it does not
        // forward a dismiss drag (only the first page has nowhere to browse to)
        val pager = composeRule.onNodeWithTag("profile_carousel_pager")
        val widthPx = pager.fetchSemanticsNode().size.width.toFloat()
        pager.performTouchInput {
            down(centerLeft)
            repeat(15) { step -> moveTo(centerLeft + Offset(widthPx * 0.55f * (step + 1) / 15f, 0f)) }
            up()
        }
        composeRule.waitForIdle()

        // Then it's back on the first profile's card and nothing was forwarded
        composeRule.onNodeWithTag("profile_page_name_$firstProfileId", useUnmergedTree = true).assertTextEquals("Profile 1")
        assertEquals(0f, draggedTotal)
        assertEquals(false, dragEnded)
    }

    @Test
    fun reorderButtonNavigatesToManageProfiles() {
        // Given two profiles (so the button is enabled)
        var reorderRequested = false
        setContent(
            onReorderProfiles = { reorderRequested = true },
            seed = { profileRepository, settings ->
                val first = profileRepository.addProfile()
                profileRepository.addProfile()
                settings.setActiveProfileId(first.id)
            },
        )

        // When tapping the Reorder button
        composeRule.onNodeWithTag("profile_carousel_reorder").performClick()

        // Then the caller is asked to open Manage Profiles
        assertEquals(true, reorderRequested)
    }

    @Test
    fun reorderButtonIsDisabledWithOnlyOneProfile() {
        setContent(
            seed = { profileRepository, settings ->
                val only = profileRepository.addProfile()
                settings.setActiveProfileId(only.id)
            },
        )

        // Nothing to reorder with a single profile
        composeRule.onNodeWithTag("profile_carousel_reorder").assertIsNotEnabled()
    }

    @Test
    fun headerShowsTheTitleAndLiveProfileCountSingularAndPlural() {
        // Given a single profile (singular count wording)
        setContent(
            seed = { profileRepository, settings ->
                val only = profileRepository.addProfile()
                settings.setActiveProfileId(only.id)
            },
        )
        composeRule.onNodeWithText("Switch Profiles").assertExists()
        composeRule.onNodeWithText("1 profile available").assertExists()
    }

    @Test
    fun headerShowsThePluralCountWithMultipleProfiles() {
        // Given two profiles (plural count wording)
        setContent(
            seed = { profileRepository, settings ->
                val first = profileRepository.addProfile()
                profileRepository.addProfile()
                settings.setActiveProfileId(first.id)
            },
        )
        composeRule.onNodeWithText("2 profiles available").assertExists()
    }

    @Test
    fun addProfilePageIsHiddenOnceTheMaximumIsReached() {
        // Given the maximum of 3 profiles already exist
        setContent(
            seed = { profileRepository, settings ->
                repeat(ProfileRepository.MAX_PROFILES) { profileRepository.addProfile() }
                settings.setActiveProfileId(1L)
            },
        )

        // Then no add-profile page is offered
        composeRule.onNodeWithTag("profile_carousel_add_page").assertDoesNotExist()
    }

    @Test
    fun deleteIsDisabledWithOnlyOneProfileRemaining() {
        // Given a single profile
        var onlyProfileId = 0L
        setContent(
            seed = { profileRepository, settings ->
                val only = profileRepository.addProfile()
                onlyProfileId = only.id
                settings.setActiveProfileId(only.id)
            },
        )

        // Then the delete action is disabled — the last profile can't be removed
        composeRule.onNodeWithTag("profile_page_delete_$onlyProfileId", useUnmergedTree = true).assertIsNotEnabled()
    }

    @Test
    fun deletingAProfileRemovesItFromTheCarousel() {
        // Given two profiles, centered on the second
        var secondProfileId = 0L
        setContent(
            seed = { profileRepository, settings ->
                val first = profileRepository.addProfile()
                val second = profileRepository.addProfile()
                secondProfileId = second.id
                settings.setActiveProfileId(first.id)
            },
        )
        swipeToNextPage()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("profile_page_name_$secondProfileId", useUnmergedTree = true).assertTextEquals("Profile 2")

        // When deleting it and confirming
        composeRule.onNodeWithTag("profile_page_delete_$secondProfileId", useUnmergedTree = true).performClick()
        composeRule.onNode(hasText("Delete")).performClick()
        composeRule.waitForIdle()

        // Then it's gone — the deletion is a real async round trip (ViewModel -> Repository ->
        // Room -> StateFlow -> recomposition), which a single waitForIdle() doesn't reliably
        // catch, so poll instead of asserting immediately (see chat history / CLAUDE.md)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("Profile 2").assertDoesNotExist() }.isSuccess
        }
    }

    @Test
    fun previewCardShowsBothFavoritesAndDockAppsTogether() {
        // Given one profile with a favorite app, and a (profile-independent, shared) dock app
        var profileId = 0L
        var favoriteLabel = ""
        var dockLabel = ""
        setContent(
            seed = { profileRepository, settings ->
                val only = profileRepository.addProfile()
                profileId = only.id
                settings.setActiveProfileId(only.id)
                // Overriding its own favorites, rather than the launcher-wide default list, since
                // this test seeds a favorite specifically for this profile.
                profileRepository.setOverridingFavorites(only, true)
            },
            seedApps = { appRepository, favoriteAppRepository, dockAppRepository ->
                val installed = appRepository.getInstalledApps()
                val favoriteApp = installed[0]
                val dockApp = installed[1]
                favoriteLabel = favoriteApp.label
                dockLabel = dockApp.label
                favoriteAppRepository.addFavorite(profileId = profileId, app = favoriteApp, position = 0)
                dockAppRepository.addDockApp(dockApp, position = 0)
            },
        )

        // Then the preview card renders the favorite as a visible row and the dock app as an icon
        composeRule.onNodeWithText(favoriteLabel).assertExists()
        composeRule.onNodeWithContentDescription(dockLabel).assertExists()
    }

    @Test
    fun previewCardRendersTheWallpaperBehindItsContent() {
        // Given one profile
        var profileId = 0L
        setContent(
            seed = { profileRepository, settings ->
                val only = profileRepository.addProfile()
                profileId = only.id
                settings.setActiveProfileId(only.id)
            },
        )

        // Then the card paints a wallpaper backdrop, with the profile's own content still on top.
        // Unmerged tree: the card's own `clickable` merges its descendants' semantics, so the
        // backdrop's testTag is only visible as a standalone node in the unmerged tree.
        val wallpaperNodes = composeRule
            .onAllNodesWithTag("wallpaper_background", useUnmergedTree = true)
            .fetchSemanticsNodes().size
        assertEquals(true, wallpaperNodes > 0)
        composeRule.onNodeWithTag("profile_page_$profileId").assertExists()
    }

    @Test
    fun tappingAFavoriteRowInsideTheCardStillAppliesTheProfile() {
        // Given one profile with a favorite app
        var profileId = 0L
        var favoriteLabel = ""
        var applied = false
        setContent(
            onProfileApplied = { applied = true },
            seed = { profileRepository, settings ->
                val only = profileRepository.addProfile()
                profileId = only.id
                settings.setActiveProfileId(only.id)
                profileRepository.setOverridingFavorites(only, true)
            },
            seedApps = { appRepository, favoriteAppRepository, _ ->
                val favoriteApp = appRepository.getInstalledApps()[0]
                favoriteLabel = favoriteApp.label
                favoriteAppRepository.addFavorite(profileId = profileId, app = favoriteApp, position = 0)
            },
        )

        // When tapping directly on the favorite row's own label — the reused real AppRow's own
        // click used to consume this touch instead of letting it reach the card's own clickable
        // underneath, so the tap silently did nothing (see chat history).
        composeRule.onNodeWithText(favoriteLabel).performClick()

        // Then the profile is still applied
        assertEquals(true, applied)
    }

    @Test
    fun tappingADockIconInsideTheCardStillAppliesTheProfile() {
        // Given one profile and a (profile-independent, shared) dock app
        var profileId = 0L
        var dockLabel = ""
        var applied = false
        setContent(
            onProfileApplied = { applied = true },
            seed = { profileRepository, settings ->
                val only = profileRepository.addProfile()
                profileId = only.id
                settings.setActiveProfileId(only.id)
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

        // Then the profile is still applied
        assertEquals(true, applied)
    }

    @Test
    fun longPressingAFavoriteRowInsideTheCardDoesNotOpenTheAppContextMenu() {
        // Given one profile with a favorite app — this card is a read-only preview of a
        // profile's Home layout, not a place to manage apps, so the reused real AppRow's own
        // long-press-to-open-Uninstall/App-Info menu must not fire here (see chat history).
        var profileId = 0L
        var favoriteLabel = ""
        setContent(
            seed = { profileRepository, settings ->
                val only = profileRepository.addProfile()
                profileId = only.id
                settings.setActiveProfileId(only.id)
                profileRepository.setOverridingFavorites(only, true)
            },
            seedApps = { appRepository, favoriteAppRepository, _ ->
                val favoriteApp = appRepository.getInstalledApps()[0]
                favoriteLabel = favoriteApp.label
                favoriteAppRepository.addFavorite(profileId = profileId, app = favoriteApp, position = 0)
            },
        )

        // When long-pressing the favorite row
        composeRule.onNodeWithText(favoriteLabel).performTouchInput { longClick() }

        // Then no context menu appears
        composeRule.onNodeWithTag("app_context_menu").assertDoesNotExist()
    }

    @Test
    fun longPressingADockIconInsideTheCardDoesNotOpenTheAppContextMenu() {
        // Given one profile and a (profile-independent, shared) dock app — same
        // read-only-preview reasoning as the favorite row above.
        var dockLabel = ""
        setContent(
            seed = { profileRepository, settings ->
                val only = profileRepository.addProfile()
                settings.setActiveProfileId(only.id)
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
    fun profileSettingsIconOnTheCardNavigatesToEditProfile() {
        // Given a single profile
        var profileId = 0L
        var editedProfileId = -1L
        setContent(
            onEditProfile = { editedProfileId = it },
            seed = { profileRepository, settings ->
                val only = profileRepository.addProfile()
                profileId = only.id
                settings.setActiveProfileId(only.id)
            },
        )

        // When tapping the card's own Settings icon
        composeRule.onNodeWithTag("profile_page_settings_$profileId", useUnmergedTree = true).performClick()

        // Then the caller is asked to navigate to that profile's settings
        assertEquals(profileId, editedProfileId)
    }

    @Test
    fun launcherSettingsRowInvokesTheCallback() {
        // Given the carousel
        var navigatedToSettings = false
        setContent(
            onNavigateToSettings = { navigatedToSettings = true },
            seed = { profileRepository, settings ->
                val only = profileRepository.addProfile()
                settings.setActiveProfileId(only.id)
            },
        )

        // Then the row renders its title + subtitle
        composeRule.onNodeWithText("Launcher settings").assertExists()
        composeRule.onNodeWithText("Default clock, favorites, drawer, badges").assertExists()

        // When tapping the "Launcher settings" row below the carousel
        composeRule.onNodeWithTag("profile_carousel_launcher_settings").performClick()

        // Then the caller is asked to navigate there
        assertEquals(true, navigatedToSettings)
    }
}
