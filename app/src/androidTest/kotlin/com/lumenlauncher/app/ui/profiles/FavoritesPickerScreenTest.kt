package com.lumenlauncher.app.ui.profiles

import android.content.pm.LauncherApps
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.lumenlauncher.app.data.AppRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.domain.GetInstalledAppsUseCase
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class FavoritesPickerScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var profileId = 0L

    /** [seed] runs against real (throwaway, in-memory) repositories before the screen renders. */
    private fun setContent(onDone: () -> Unit = {}, seed: suspend (AppRepository, FavoriteAppRepository) -> Unit = { _, _ -> }) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                val appRepository = AppRepository(launcherApps)
                val favoriteAppRepository = FavoriteAppRepository(database.favoriteAppDao(), appRepository)
                val defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), appRepository)
                runBlocking {
                    profileId = database.profileDao().upsert(ProfileEntity(name = "Profile 1", position = 0))
                    seed(appRepository, favoriteAppRepository)
                }
                FavoritesPickerViewModel(
                    SavedStateHandle(mapOf("profileId" to profileId)),
                    GetInstalledAppsUseCase(appRepository),
                    favoriteAppRepository,
                    defaultFavoriteAppRepository,
                )
            }
            LumenLauncherTheme {
                FavoritesPickerScreen(onDone = onDone, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun checkingAnAppAddsItToFavorites() {
        // Given the picker, searched down to some real installed app
        var targetApp: AppInfo? = null
        setContent { appRepository, _ -> targetApp = runBlocking { appRepository.getInstalledApps() }.first() }
        val app = requireNotNull(targetApp)
        val rowTag = "favorites_picker_row_${app.packageName}"

        // The ViewModel's installed-apps list comes from a real suspend fetch (init { launch { ... } }),
        // which isn't reliably caught by a single waitForIdle() — poll for the row before interacting
        // (same async-I/O-vs-idling gap as elsewhere, see IMPLEMENTATION_PLAN.md)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("favorites_picker_search").performTextInput(app.label)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(rowTag).assertIsOff()

        // When it's checked
        composeRule.onNodeWithTag(rowTag).performClick()

        // Then its checkbox reflects the new favorite membership
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertIsOn() }.isSuccess
        }
    }

    @Test
    fun headerStaysVisibleAfterScrollingToAllApps() {
        // Given two real apps already favorited, so both "FAVORITES" and "ALL APPS" render
        setContent { appRepository, favoriteAppRepository ->
            val installed = runBlocking { appRepository.getInstalledApps() }
            installed.take(2).forEachIndexed { index, app -> favoriteAppRepository.addFavorite(profileId, app, index) }
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("ALL APPS").assertExists() }.isSuccess
        }

        // When scrolling down to the "ALL APPS" section
        composeRule.onNodeWithText("ALL APPS").performScrollTo()

        // Then the pinned header (title + Done) is still on screen, not scrolled away
        composeRule.onNodeWithText("Favorites").assertIsDisplayed()
        composeRule.onNodeWithTag("favorites_picker_done").assertIsDisplayed()
    }

    @Test
    fun favoritedAppsRenderFirstUnderTheirOwnSectionHeader() {
        // Given two real apps already favorited
        var favorites: List<AppInfo> = emptyList()
        setContent { appRepository, favoriteAppRepository ->
            val installed = runBlocking { appRepository.getInstalledApps() }
            favorites = installed.take(2)
            favorites.forEachIndexed { index, app -> favoriteAppRepository.addFavorite(profileId, app, index) }
        }

        // Then both section headers render, with the favorited apps' rows present under "FAVORITES"
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("FAVORITES").assertExists() }.isSuccess
        }
        composeRule.onNodeWithText("ALL APPS").assertExists()
        favorites.forEach { app ->
            composeRule.onNodeWithTag("favorites_picker_row_${app.packageName}").assertIsOn()
        }
    }

    @Test
    fun checkingIsBlockedAtTheMaxFavoritesCap() {
        // Given favorites already at the cap
        var overflowApp: AppInfo? = null
        setContent { appRepository, favoriteAppRepository ->
            val installed = runBlocking { appRepository.getInstalledApps() }
            val capApps = installed.take(FavoriteAppRepository.MAX_FAVORITES)
            overflowApp = installed.getOrNull(FavoriteAppRepository.MAX_FAVORITES)
            runBlocking { capApps.forEachIndexed { index, app -> favoriteAppRepository.addFavorite(profileId, app, index) } }
        }
        val app = requireNotNull(overflowApp) {
            "test needs at least ${FavoriteAppRepository.MAX_FAVORITES + 1} installed apps visible to the test APK"
        }
        val rowTag = "favorites_picker_row_${app.packageName}"

        // See checkingAnAppAddsItToFavorites — wait for the real installed-apps fetch to land
        // before interacting, rather than trusting a single waitForIdle() caught it.
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("favorites_picker_search").performTextInput(app.label)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(rowTag).assertIsOff()

        // When trying to check one more app past the cap
        composeRule.onNodeWithTag(rowTag).performClick()
        composeRule.waitForIdle()
        Thread.sleep(500) // let a (correctly) blocked write's absence settle before asserting

        // Then it stays unchecked — the cap blocks the addition
        composeRule.onNodeWithTag(rowTag).assertIsOff()
    }

    /** No `profileId` — the launcher-wide default Favorites list, reached from Settings. */
    private fun setContentForDefaultFavorites(): Pair<AppRepository, DefaultFavoriteAppRepository> {
        lateinit var appRepository: AppRepository
        lateinit var defaultFavoriteAppRepository: DefaultFavoriteAppRepository
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                appRepository = AppRepository(launcherApps)
                val favoriteAppRepository = FavoriteAppRepository(database.favoriteAppDao(), appRepository)
                defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), appRepository)
                FavoritesPickerViewModel(SavedStateHandle(), GetInstalledAppsUseCase(appRepository), favoriteAppRepository, defaultFavoriteAppRepository)
            }
            LumenLauncherTheme {
                FavoritesPickerScreen(onDone = {}, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
        return appRepository to defaultFavoriteAppRepository
    }

    @Test
    fun withNoProfileIdTheScreenEditsTheLauncherWideDefaultList() {
        // Given the picker reached with no profileId (Settings' own "Default favorites" row)
        val (appRepository, _) = setContentForDefaultFavorites()

        // Then the header reads "Default favorites", not "Favorites" — uiState.isProfileScoped
        // defaults to true until the ViewModel's real combine() emits, so a single waitForIdle()
        // in the setup helper isn't guaranteed to have caught it yet (same StateFlow-vs-idling
        // gap already documented elsewhere in this suite)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("Default favorites").assertExists() }.isSuccess
        }

        // When checking a real installed app — poll for its row, since the ViewModel's
        // installed-apps list comes from a real suspend fetch (same async-I/O-vs-idling gap as
        // checkingAnAppAddsItToFavorites above)
        val app = runBlocking { appRepository.getInstalledApps() }.first()
        val rowTag = "favorites_picker_row_${app.packageName}"
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag(rowTag).performClick()

        // Then it's reflected as checked, via the real production write path — with no
        // profileId, the ViewModel routes reads/writes through DefaultFavoriteAppRepository
        // rather than FavoriteAppRepository, so this proves it landed in the launcher-wide
        // default list, not a per-profile one. Deliberately not asserted by calling
        // defaultFavoriteAppRepository.observeDefaultFavorites() directly from the test: that
        // flow chains into AppRepository.observeInstalledApps()'s live LauncherApps.registerCallback,
        // which needs a Looper-bound thread — fine when collected via the ViewModel's own
        // viewModelScope (Main), but calling it via a bare runBlocking on the instrumentation
        // test's own thread (no Looper.prepare()) crashes the instrumentation process.
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertIsOn() }.isSuccess
        }
    }
}
