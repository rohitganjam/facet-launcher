package com.lumenlauncher.app.ui.settings

import android.app.WallpaperManager
import android.app.usage.UsageStatsManager
import android.content.pm.LauncherApps
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.lumenlauncher.app.data.AppRepository
import com.lumenlauncher.app.data.DefaultAppRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.WallpaperRepository
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.domain.GetInstalledAppsUseCase
import com.lumenlauncher.app.domain.SelectPreviewAppsUseCase
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeAppsListSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** [onProfileRepo] receives the screen's real [ProfileRepository]; when [profileScoped] the VM
     *  is scoped to a freshly-added profile in that same DB, whose id is passed back too. */
    private fun setContent(
        onBack: () -> Unit = {},
        onEditFavorites: () -> Unit = {},
        profileScoped: Boolean = false,
        onProfileRepo: (ProfileRepository, Long?) -> Unit = { _, _ -> },
    ) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "home-apps-list-settings-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java))
                val profileRepository = ProfileRepository(database.profileDao())
                val profileId = if (profileScoped) runBlocking { profileRepository.addProfile().id } else null
                onProfileRepo(profileRepository, profileId)
                HomeAppsListSettingsViewModel(
                    SavedStateHandle(profileId?.let { mapOf("profileId" to it) } ?: emptyMap()),
                    settingsRepository,
                    profileRepository,
                    FavoriteAppRepository(database.favoriteAppDao(), appRepository),
                    DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), appRepository),
                    WallpaperRepository(WallpaperManager.getInstance(context)),
                    DefaultAppRepository(context),
                    GetInstalledAppsUseCase(appRepository),
                    SelectPreviewAppsUseCase(),
                )
            }
            LumenLauncherTheme {
                HomeAppsListSettingsScreen(onBack = onBack, onEditFavorites = onEditFavorites, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun headerAndPreviewRender() {
        setContent()
        composeRule.onNodeWithText("Home Apps List").assertIsDisplayed()
        composeRule.onNodeWithTag("back_button").assertIsDisplayed()
        composeRule.onNodeWithTag("home_apps_list_preview_card").assertExists()
    }

    @Test
    fun backButtonInvokesOnBack() {
        var backInvoked = false
        setContent(onBack = { backInvoked = true })
        composeRule.onNodeWithTag("back_button").performClick()
        assertEquals(true, backInvoked)
    }

    @Test
    fun appRowPositionDropdownSwitchesBetweenLeftAndRight() {
        setContent()
        composeRule.onNodeWithTag("default_app_row_position_row").assertTextContains("Left")

        composeRule.onNodeWithTag("default_app_row_position_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("default_app_row_position_row_option_RIGHT").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("default_app_row_position_row").assertTextContains("Right") }.isSuccess
        }
    }

    @Test
    fun favoritesRowIsClickable() {
        var navigated = false
        setContent(onEditFavorites = { navigated = true })
        composeRule.onNodeWithTag("home_apps_list_settings_screen").performScrollToNode(hasTestTag("default_favorites_row"))
        composeRule.onNodeWithTag("default_favorites_row").performClick()
        assertEquals(true, navigated)
    }

    @Test
    fun profileScopedScreenWritesThePositionToThatProfilesRow() {
        lateinit var profileRepo: ProfileRepository
        setContent(profileScoped = true, onProfileRepo = { repo, _ -> profileRepo = repo })

        composeRule.onNodeWithTag("default_app_row_position_row").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("default_app_row_position_row_option_RIGHT").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { profileRepo.observeProfiles().first().any { it.appRowPosition == AppRowPosition.RIGHT } }
        }
    }
}
