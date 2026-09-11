package com.lumenlauncher.app.ui.onboarding

import android.app.WallpaperManager
import android.content.pm.LauncherApps
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.VerificationModes
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import com.lumenlauncher.app.data.AppRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DefaultLauncherRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileDockAppRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.WallpaperRepository
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.domain.GetInstalledAppsUseCase
import com.lumenlauncher.app.ui.dock.DockAppPickerViewModel
import com.lumenlauncher.app.ui.profiles.FavoritesPickerViewModel
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
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
    private fun setContent(onFinish: () -> Unit = {}, seed: suspend (AppRepository, DockAppRepository) -> Unit = { _, _ -> }) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModels = remember {
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
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
                runBlocking { seed(appRepository, dockAppRepository) }
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
            LumenLauncherTheme {
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
    fun `Skip on the home-setup step jumps to profiles`() {
        setContent()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_skip").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_profiles_page").assertExists()
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
        setContent { appRepository, dockAppRepository ->
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
