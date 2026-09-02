package com.lumenlauncher.app.ui.profiles

import android.content.pm.LauncherApps
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.lumenlauncher.app.data.AppRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.domain.ObserveProfilePreviewsUseCase
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
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
        onBack: () -> Unit = {},
        onProfileApplied: () -> Unit = {},
        onEditProfile: (Long) -> Unit = {},
        seed: suspend (ProfileRepository, SettingsRepository) -> Unit = { _, _ -> },
        seedApps: suspend (AppRepository, FavoriteAppRepository, DockAppRepository) -> Unit = { _, _, _ -> },
    ) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
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
                val usageStatsRepository = com.lumenlauncher.app.data.UsageStatsRepository(
                    context.getSystemService(android.app.usage.UsageStatsManager::class.java),
                    appRepository
                )
                val usageAccessRepository = com.lumenlauncher.app.data.UsageAccessRepository(
                    context.getSystemService(android.app.AppOpsManager::class.java),
                    context
                )
                val dockAppRepository = DockAppRepository(database.dockAppDao(), appRepository)
                runBlocking { seed(profileRepository, settingsRepository) }
                runBlocking { seedApps(appRepository, favoriteAppRepository, dockAppRepository) }
                ProfileCarouselViewModel(
                    profileRepository,
                    settingsRepository,
                    dockAppRepository,
                    ObserveProfilePreviewsUseCase(
                        profileRepository, 
                        settingsRepository, 
                        favoriteAppRepository, 
                        defaultFavoriteAppRepository,
                        usageStatsRepository,
                        usageAccessRepository
                    ),
                )
            }
            LumenLauncherTheme {
                ProfileCarouselScreen(
                    onBack = onBack,
                    onProfileApplied = onProfileApplied,
                    onEditProfile = onEditProfile,
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
        var backInvoked = false
        setContent(
            onBack = { backInvoked = true },
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
        composeRule.onNodeWithText("Profile 2").assertExists()
        composeRule.onNodeWithText("Profile 2").performClick()

        // Then it becomes the active profile, and onProfileApplied fires — not onBack, so the
        // caller can always return to Home regardless of how the carousel was reached
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().activeProfileId == secondProfileId }
        }
        assertEquals(true, applied)
        assertEquals(false, backInvoked)
    }

    @Test
    fun backWithoutTappingACardLeavesTheActiveProfileUnchanged() {
        // Given two profiles, the first one active
        lateinit var settingsRepository: SettingsRepository
        var firstProfileId = 0L
        var backInvoked = false
        setContent(
            onBack = { backInvoked = true },
            seed = { profileRepository, settings ->
                val first = profileRepository.addProfile()
                profileRepository.addProfile()
                firstProfileId = first.id
                settings.setActiveProfileId(first.id)
                settingsRepository = settings
            },
        )

        // When browsing to the second page but pressing back instead of tapping it
        swipeToNextPage()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Profile 2").assertExists()
        composeRule.onNodeWithTag("back_button").performClick()

        // Then back was invoked and the active profile is unchanged
        assertEquals(true, backInvoked)
        assertEquals(firstProfileId, runBlocking { settingsRepository.settings.first().activeProfileId })
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
        setContent(
            seed = { profileRepository, settings ->
                val only = profileRepository.addProfile()
                settings.setActiveProfileId(only.id)
            },
        )

        // Then the trash action is disabled — the last profile can't be removed
        composeRule.onNodeWithTag("profile_carousel_trash").assertIsNotEnabled()
    }

    @Test
    fun deletingAProfileRemovesItFromTheCarousel() {
        // Given two profiles, centered on the second
        setContent(
            seed = { profileRepository, settings ->
                val first = profileRepository.addProfile()
                profileRepository.addProfile()
                settings.setActiveProfileId(first.id)
            },
        )
        swipeToNextPage()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Profile 2").assertExists()

        // When deleting it and confirming
        composeRule.onNodeWithTag("profile_carousel_trash").performClick()
        composeRule.onNode(hasText("Delete")).performClick()
        composeRule.waitForIdle()

        // Then it's gone
        composeRule.onNodeWithText("Profile 2").assertDoesNotExist()
    }

    @Test
    fun reorderLinkOpensAReorderableListAndBackReturnsToTheCarousel() {
        // Given two profiles
        setContent(
            seed = { profileRepository, settings ->
                val first = profileRepository.addProfile()
                profileRepository.addProfile()
                settings.setActiveProfileId(first.id)
            },
        )

        // When opening reorder mode
        composeRule.onNodeWithTag("profile_carousel_reorder").performClick()
        composeRule.waitForIdle()

        // Then the list shows both profiles plus an Add row, and the pager is gone
        composeRule.onNodeWithTag("profile_reorder_list").assertExists()
        composeRule.onNodeWithTag("profile_carousel_pager").assertDoesNotExist()
        composeRule.onNodeWithText("Profile 1").assertExists()
        composeRule.onNodeWithText("Profile 2").assertExists()
        composeRule.onNodeWithTag("profile_reorder_add_row").assertExists()
        // No redundant "Done" affordance — every drag-release already autosaves via onCommit.
        composeRule.onNodeWithTag("profile_carousel_reorder").assertDoesNotExist()

        // When tapping the back button
        composeRule.onNodeWithTag("back_button").performClick()
        composeRule.waitForIdle()

        // Then it's back to the carousel, with the "Reorder" entry point visible again
        composeRule.onNodeWithTag("profile_carousel_pager").assertExists()
        composeRule.onNodeWithTag("profile_reorder_list").assertDoesNotExist()
        composeRule.onNodeWithTag("profile_carousel_reorder").assertExists()
    }

    @Test
    fun addRowInTheReorderListIsHiddenOnceTheMaximumIsReached() {
        // Given the maximum of 3 profiles already exist
        setContent(
            seed = { profileRepository, settings ->
                repeat(ProfileRepository.MAX_PROFILES) { profileRepository.addProfile() }
                settings.setActiveProfileId(1L)
            },
        )

        // When opening reorder mode
        composeRule.onNodeWithTag("profile_carousel_reorder").performClick()
        composeRule.waitForIdle()

        // Then no add row is offered
        composeRule.onNodeWithTag("profile_reorder_add_row").assertDoesNotExist()
    }

    @Test
    fun deletingFromTheReorderListRemovesTheRow() {
        // Given two profiles
        var secondProfileId = 0L
        setContent(
            seed = { profileRepository, settings ->
                val first = profileRepository.addProfile()
                val second = profileRepository.addProfile()
                secondProfileId = second.id
                settings.setActiveProfileId(first.id)
            },
        )
        composeRule.onNodeWithTag("profile_carousel_reorder").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Profile 2").assertExists()

        // When opening the second row's overflow menu, choosing Delete, and confirming
        composeRule.onNodeWithTag("profile_reorder_menu_$secondProfileId").performClick()
        composeRule.onNode(hasText("Delete")).performClick()
        composeRule.onNode(hasText("Delete")).performClick()
        composeRule.waitForIdle()

        // Then it's gone from the list
        composeRule.onNodeWithText("Profile 2").assertDoesNotExist()
    }

    @Test
    fun profileSettingsFromTheReorderListNavigatesToEditProfile() {
        // Given two profiles
        var firstProfileId = 0L
        var editedProfileId = -1L
        setContent(
            onEditProfile = { editedProfileId = it },
            seed = { profileRepository, settings ->
                val first = profileRepository.addProfile()
                firstProfileId = first.id
                profileRepository.addProfile()
                settings.setActiveProfileId(first.id)
            },
        )
        composeRule.onNodeWithTag("profile_carousel_reorder").performClick()
        composeRule.waitForIdle()

        // When opening the first row's overflow menu and choosing Profile settings
        composeRule.onNodeWithTag("profile_reorder_menu_$firstProfileId").performClick()
        composeRule.onNode(hasText("Profile settings")).performClick()

        // Then the caller is asked to navigate to that profile's settings
        assertEquals(firstProfileId, editedProfileId)
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
}
