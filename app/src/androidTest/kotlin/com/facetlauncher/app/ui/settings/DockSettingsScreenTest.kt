package com.facetlauncher.app.ui.settings

import android.app.WallpaperManager
import android.content.pm.LauncherApps
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.ProfileDockAppRepository
import com.facetlauncher.app.data.ProfileRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DockSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        onBack: () -> Unit = {},
        onAddDockApp: () -> Unit = {},
        profileId: Long? = null,
        seed: suspend (AppRepository, DockAppRepository, ProfileDockAppRepository) -> Unit = { _, _, _ -> },
    ) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "dock-settings-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val profileRepository = ProfileRepository(database.profileDao())
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java))
                val dockAppRepository = DockAppRepository(database.dockAppDao(), appRepository)
                val profileDockAppRepository = ProfileDockAppRepository(database.profileDockAppDao(), appRepository)
                if (profileId != null) {
                    runBlocking { database.profileDao().upsert(com.facetlauncher.app.data.local.ProfileEntity(id = profileId, name = "P", position = 0)) }
                }
                runBlocking { seed(appRepository, dockAppRepository, profileDockAppRepository) }
                DockSettingsViewModel(
                    SavedStateHandle(profileId?.let { mapOf("profileId" to it) } ?: emptyMap()),
                    settingsRepository,
                    profileRepository,
                    dockAppRepository,
                    profileDockAppRepository,
                    WallpaperRepository(WallpaperManager.getInstance(context)),
                )
            }
            FacetLauncherTheme {
                DockSettingsScreen(onBack = onBack, onAddDockApp = onAddDockApp, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun headerAndPreviewRender() {
        setContent()
        composeRule.onNodeWithTag("back_button").assertIsDisplayed()
        composeRule.onNodeWithText("Dock").assertIsDisplayed()
        composeRule.onNodeWithTag("dock_settings_preview_card").assertExists()
    }

    @Test
    fun backButtonInvokesOnBack() {
        var backInvoked = false
        setContent(onBack = { backInvoked = true })
        composeRule.onNodeWithTag("back_button").performClick()
        assertEquals(true, backInvoked)
    }

    @Test
    fun dockDisplayStyleDropdownPersistsToTheGlobalSetting() {
        setContent()
        composeRule.onNodeWithText("Icons").assertExists()

        composeRule.onNodeWithTag("dock_display_style_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("dock_display_style_row_option_TEXT").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("Text").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun selectDockAppsRowIsClickable() {
        var navigated = false
        setContent(onAddDockApp = { navigated = true })
        composeRule.onNodeWithTag("add_dock_app_row").performClick()
        assertEquals(true, navigated)
    }

    @Test
    fun profileScopedScreenWritesTheDisplayStyleToThatProfilesRow() {
        lateinit var profileRepo: ProfileRepository
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "dock-profile-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                profileRepo = ProfileRepository(database.profileDao())
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java))
                val pid = runBlocking { profileRepo.addProfile().id }
                DockSettingsViewModel(
                    SavedStateHandle(mapOf("profileId" to pid)),
                    settingsRepository,
                    profileRepo,
                    DockAppRepository(database.dockAppDao(), appRepository),
                    ProfileDockAppRepository(database.profileDockAppDao(), appRepository),
                    WallpaperRepository(WallpaperManager.getInstance(context)),
                )
            }
            FacetLauncherTheme { DockSettingsScreen(onBack = {}, onAddDockApp = {}, viewModel = viewModel) }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("dock_display_style_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("dock_display_style_row_option_TEXT").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { profileRepo.observeProfiles().first().first().dockDisplayMode == DockDisplayMode.TEXT }
        }
    }
}
