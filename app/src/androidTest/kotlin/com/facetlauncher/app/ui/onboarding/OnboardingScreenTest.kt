package com.facetlauncher.app.ui.onboarding

import android.app.WallpaperManager
import android.content.pm.LauncherApps
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.VerificationModes
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DefaultLauncherRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.ProfileDockAppRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.ui.dock.DockAppPickerViewModel
import com.facetlauncher.app.ui.profiles.FavoritesPickerViewModel
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.anyOf
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OnboardingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    /** [seed] runs against real (throwaway, in-memory/temp-file) repositories before the screen renders. */
    private fun setContent(
        onFinish: () -> Unit = {},
        seed: suspend (AppRepository, DockAppRepository, DefaultFavoriteAppRepository) -> Unit = { _, _, _ -> },
    ) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModels = remember {
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java))
                val defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), appRepository)
                val dockAppRepository = DockAppRepository(database.dockAppDao(), appRepository)
                val favoriteAppRepository = FavoriteAppRepository(database.favoriteAppDao(), appRepository)
                val profileDockAppRepository = ProfileDockAppRepository(database.profileDockAppDao(), appRepository)
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "onboarding-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val wallpaperRepository = WallpaperRepository(WallpaperManager.getInstance(context))
                val defaultLauncherRepository = DefaultLauncherRepository(context)
                runBlocking { seed(appRepository, dockAppRepository, defaultFavoriteAppRepository) }
                val getInstalledApps = GetInstalledAppsUseCase(appRepository)
                Triple(
                    OnboardingViewModel(
                        defaultFavoriteAppRepository,
                        dockAppRepository,
                        settingsRepository,
                        wallpaperRepository,
                        defaultLauncherRepository,
                    ),
                    // Onboarding embeds these full-screen pickers as-is (same as Settings); this
                    // non-Hilt test host builds them by hand, same pattern as DockAppPickerScreenTest/
                    // FavoritesPickerScreenTest — an empty SavedStateHandle means the launcher-wide
                    // default dock/favorites, matching onboarding's own scope.
                    DockAppPickerViewModel(SavedStateHandle(), getInstalledApps, dockAppRepository, profileDockAppRepository),
                    FavoritesPickerViewModel(SavedStateHandle(), getInstalledApps, favoriteAppRepository, defaultFavoriteAppRepository),
                )
            }
            FacetLauncherTheme {
                OnboardingScreen(
                    onFinish = onFinish,
                    viewModel = viewModels.first,
                    dockPickerViewModel = viewModels.second,
                    favoritesPickerViewModel = viewModels.third,
                )
            }
        }
        composeRule.waitForIdle()
    }

    /**
     * Waits for the set-default step to fully render, then taps whichever primary action is
     * present — "Later" normally, or "Done" if this test APK happens to already hold the `HOME`
     * role on this device/emulator (the already-default variant). [DefaultLauncherRepository.isDefaultLauncher]
     * is a real, async `PackageManager` query, not caught by a single `waitForIdle()`.
     */
    private fun finishFromSetDefaultStep() {
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onNodeWithTag("onboarding_later").let { runCatching { it.assertExists() }.isSuccess } ||
                composeRule.onNodeWithTag("onboarding_done").let { runCatching { it.assertExists() }.isSuccess }
        }
        val laterExists = runCatching { composeRule.onNodeWithTag("onboarding_later").assertExists() }.isSuccess
        if (laterExists) {
            composeRule.onNodeWithTag("onboarding_later").performClick()
        } else {
            composeRule.onNodeWithTag("onboarding_done").performClick()
        }
    }

    @Test
    fun `first run shows the intro step`() {
        setContent()

        composeRule.onNodeWithTag("onboarding_intro_page").assertExists()
    }

    @Test
    fun `Next advances intro through home setup and profiles to set-default`() {
        setContent()

        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_home_setup_page").assertExists()

        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_profiles_page").assertExists()

        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_set_default_page").assertExists()
    }

    @Test
    fun `Back on the home-setup step returns to intro`() {
        setContent()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_back").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_intro_page").assertExists()
    }

    @Test
    fun `Back on the profiles step returns to home setup`() {
        setContent()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_back").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_home_setup_page").assertExists()
    }

    @Test
    fun `the set-default step has no Back button or swipe-back`() {
        // Given onboarding has advanced to the final set-default sheet
        setContent()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_set_default_page").assertExists()

        // Then there's no "Back" affordance on the sheet itself
        composeRule.onNodeWithTag("onboarding_back").assertDoesNotExist()

        // When swiping right (the usual "go back" gesture elsewhere in onboarding)
        composeRule.onNodeWithTag("onboarding_screen").performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        // Then it does nothing — still on the set-default sheet, not back on Profiles
        composeRule.onNodeWithTag("onboarding_set_default_page").assertExists()
    }

    @Test
    fun `system back on the set-default step dismisses it like Later`() {
        // Given onboarding has advanced to the final set-default sheet
        var finished = false
        setContent(onFinish = { finished = true })
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_set_default_page").assertExists()

        // When pressing the system back gesture, instead of navigating anywhere
        pressBack()

        // Then it dismisses the sheet and finishes onboarding, the same as "Later"
        composeRule.waitUntil(timeoutMillis = 3_000) { finished }
        assertTrue(finished)
    }

    @Test
    fun `swiping left advances to the next step`() {
        setContent()

        composeRule.onNodeWithTag("onboarding_screen").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_home_setup_page").assertExists()
    }

    @Test
    fun `swiping right returns to the previous step`() {
        setContent()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_home_setup_page").assertExists()

        composeRule.onNodeWithTag("onboarding_screen").performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_intro_page").assertExists()
    }

    @Test
    fun `swiping right on the first step does nothing`() {
        setContent()

        composeRule.onNodeWithTag("onboarding_screen").performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_intro_page").assertExists()
    }

    @Test
    fun `swiping left on the last step does nothing`() {
        setContent()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_set_default_page").assertExists()

        composeRule.onNodeWithTag("onboarding_screen").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_set_default_page").assertExists()
    }

    @Test
    fun `tapping Favorites opens the full-screen favorites picker`() {
        setContent()

        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_edit_favorites").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("favorites_picker_screen").assertExists()
    }

    @Test
    fun `Done on the favorites picker returns to home setup`() {
        setContent()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_edit_favorites").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("favorites_picker_done").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_home_setup_page").assertExists()
    }

    @Test
    fun `tapping Manage dock apps opens the full-screen dock picker`() {
        var seededApp: AppInfo? = null
        setContent { appRepository, dockAppRepository, _ ->
            val app = runBlocking { appRepository.getInstalledApps() }.first()
            seededApp = app
            dockAppRepository.addDockApp(app, 0)
        }
        requireNotNull(seededApp)

        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onNodeWithText("1 of ${DockAppRepository.MAX_APPS}").let { runCatching { it.assertExists() }.isSuccess }
        }

        composeRule.onNodeWithTag("onboarding_manage_dock").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("dock_app_picker_screen").assertExists()
    }

    @Test
    fun `Clear all removes every dock app after confirming`() {
        var seededApp: AppInfo? = null
        setContent { appRepository, dockAppRepository, _ ->
            val app = runBlocking { appRepository.getInstalledApps() }.first()
            seededApp = app
            dockAppRepository.addDockApp(app, 0)
        }
        requireNotNull(seededApp)

        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onNodeWithTag("onboarding_clear_dock").let { runCatching { it.assertExists() }.isSuccess }
        }

        // When tapping Clear all and confirming the destructive dialog
        composeRule.onNodeWithTag("onboarding_clear_dock").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("confirm_dialog_confirm").performClick()
        composeRule.waitForIdle()

        // Then the dock is empty and the Clear all action disappears with it
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onNodeWithText("0 of ${DockAppRepository.MAX_APPS}").let { runCatching { it.assertExists() }.isSuccess }
        }
        composeRule.onNodeWithTag("onboarding_clear_dock").assertDoesNotExist()
    }

    @Test
    fun `Clear all removes every favorite after confirming`() {
        var seededApp: AppInfo? = null
        setContent { appRepository, _, defaultFavoriteAppRepository ->
            val app = runBlocking { appRepository.getInstalledApps() }.first()
            seededApp = app
            defaultFavoriteAppRepository.addFavorite(app, 0)
        }
        requireNotNull(seededApp)

        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onNodeWithTag("onboarding_clear_favorites").let { runCatching { it.assertExists() }.isSuccess }
        }

        // When tapping Clear all and confirming the destructive dialog
        composeRule.onNodeWithTag("onboarding_clear_favorites").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("confirm_dialog_confirm").performClick()
        composeRule.waitForIdle()

        // Then the favorites list is empty and the Clear all action disappears with it
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("onboarding_favorite_reorder_row_${seededApp?.packageName}").fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithTag("onboarding_clear_favorites").assertDoesNotExist()
    }

    @Test
    fun `Later finishes onboarding`() {
        var finished = false
        setContent(onFinish = { finished = true })

        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()

        finishFromSetDefaultStep()

        composeRule.waitUntil(timeoutMillis = 3_000) { finished }
        assertTrue(finished)
    }

    @Test
    fun `tapping outside the set-default sheet finishes onboarding the same as Later`() {
        var finished = false
        setContent(onFinish = { finished = true })

        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("onboarding_set_default_scrim").assertExists() }.isSuccess
        }

        // Tapping the dimmed area outside the sheet, not either of its own buttons
        composeRule.onNodeWithTag("onboarding_set_default_scrim").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) { finished }
        assertTrue(finished)
    }

    @Test
    fun `completing onboarding requests no runtime permission`() {
        setContent()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        finishFromSetDefaultStep()
        composeRule.waitForIdle()

        Intents.intended(
            anyOf(
                hasAction("android.content.pm.action.REQUEST_PERMISSIONS"),
                hasAction(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS),
            ),
            VerificationModes.times(0),
        )
    }

    @Test
    fun `system back from home setup returns to the intro step`() {
        setContent()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_home_setup_page").assertExists()

        pressBack()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_intro_page").assertExists()
    }
}
