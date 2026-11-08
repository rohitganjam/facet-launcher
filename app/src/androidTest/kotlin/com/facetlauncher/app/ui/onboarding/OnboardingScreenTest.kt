package com.facetlauncher.app.ui.onboarding

import android.app.AppOpsManager
import android.app.WallpaperManager
import android.app.usage.UsageStatsManager
import android.content.pm.LauncherApps
import android.os.UserManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
import com.facetlauncher.app.data.getTestableApps
import com.facetlauncher.app.data.DefaultAppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.UsageAccessRepository
import com.facetlauncher.app.data.UsageStatsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.domain.ApplyFacetDefaultsUseCase
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.SelectPreviewAppsUseCase
import com.facetlauncher.app.domain.SortAppsForPickerUseCase
import com.facetlauncher.app.ui.dock.DockAppPickerViewModel
import com.facetlauncher.app.ui.facets.FavoritesPickerViewModel
import com.facetlauncher.app.ui.settings.FakeNotificationAccessRepository
import com.facetlauncher.app.ui.settings.NotificationSettingsViewModel
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.anyOf
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.domain.ObservePlacedItemsUseCase
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget

private class OnboardingViewModels(
    val onboarding: OnboardingViewModel,
    val dockPicker: DockAppPickerViewModel,
    val favoritesPicker: FavoritesPickerViewModel,
    val notificationSettings: NotificationSettingsViewModel,
)

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

    // A fresh in-memory database numbers its first facet 1.
    private val firstFacetDock = PlacementTarget(PlacementList.DOCK, 1L)
    private val firstFacetFavorites = PlacementTarget(PlacementList.FAVORITES, 1L)

    /** [seed] runs against real (throwaway, in-memory/temp-file) repositories before the screen renders. */
    private fun setContent(
        onFinish: () -> Unit = {},
        seed: suspend (AppRepository, PlacementRepository) -> Unit = { _, _ -> },
    ) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModels = remember {
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java), context.getSystemService(UserManager::class.java), context)
                val folderRepository = FolderRepository(database.folderDao(), appRepository)
                val placementRepository = PlacementRepository(database.appPlacementDao(), database.folderPlacementDao())
                val observePlacedItems = ObservePlacedItemsUseCase(placementRepository, folderRepository, appRepository)
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "onboarding-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val facetRepository = FacetRepository(database.facetDao(), database.clockStackItemDao())
                // The first facet exists, and is active, before onboarding shows; its lists are the ones set up here.
                runBlocking {
                    settingsRepository.setActiveFacetId(facetRepository.addFacet().id)
                    seed(appRepository, placementRepository)
                }
                val getInstalledApps = GetInstalledAppsUseCase(appRepository)
                val sortAppsForPicker = SortAppsForPickerUseCase(UsageStatsRepository(context.getSystemService(UsageStatsManager::class.java), appRepository))
                val usageAccessRepository = UsageAccessRepository(context.getSystemService(AppOpsManager::class.java), context)
                OnboardingViewModels(
                    OnboardingViewModel(
                        placementRepository,
                        observePlacedItems,
                        settingsRepository,
                        ApplyFacetDefaultsUseCase(facetRepository, com.facetlauncher.app.domain.SeedFacetDefaultsUseCase(com.facetlauncher.app.data.DefaultAppRepository(context), placementRepository, com.facetlauncher.app.domain.GetInstalledAppsUseCase(appRepository)), settingsRepository),
                        appRepository,
                    ),
                    // Onboarding embeds these full-screen pickers as-is (same as Settings); this
                    // non-Hilt test host builds them by hand, same pattern as DockAppPickerScreenTest/
                    // FavoritesPickerScreenTest — an empty SavedStateHandle means no facet route, so
                    // OnboardingScreen points them at the first facet.
                    DockAppPickerViewModel(SavedStateHandle(), getInstalledApps, placementRepository, observePlacedItems, folderRepository, sortAppsForPicker, usageAccessRepository),
                    FavoritesPickerViewModel(SavedStateHandle(), getInstalledApps, placementRepository, observePlacedItems, folderRepository, sortAppsForPicker, usageAccessRepository),
                    NotificationSettingsViewModel(
                        settingsRepository,
                        FakeNotificationAccessRepository(context, granted = false),
                        observePlacedItems,
                        WallpaperRepository(WallpaperManager.getInstance(context)),
                        DefaultAppRepository(context),
                        getInstalledApps,
                        SelectPreviewAppsUseCase(),
                    ),
                )
            }
            FacetLauncherTheme {
                OnboardingScreen(
                    onFinish = onFinish,
                    viewModel = viewModels.onboarding,
                    dockPickerViewModel = viewModels.dockPicker,
                    favoritesPickerViewModel = viewModels.favoritesPicker,
                    notificationSettingsViewModel = viewModels.notificationSettings,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun goToHomeSetup() {
        repeat(2) {
            composeRule.onNodeWithTag("onboarding_next").performClick()
            composeRule.waitForIdle()
        }
        composeRule.onNodeWithTag("onboarding_home_setup_page").assertExists()
    }

    private fun goToDrawerSetup() {
        goToHomeSetup()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_drawer_setup_page").assertExists()
    }

    private fun goToNotifications() {
        goToDrawerSetup()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_notifications_page").assertExists()
    }

    @Test
    fun `first run shows the intro step`() {
        setContent()

        composeRule.onNodeWithTag("onboarding_intro_page").assertExists()
    }

    @Test
    fun `Next advances intro through facets and home setup to notifications then finishes onboarding`() {
        var finished = false
        setContent(onFinish = { finished = true })

        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_facets_page").assertExists()

        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_home_setup_page").assertExists()

        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_drawer_setup_page").assertExists()

        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_notifications_page").assertExists()

        // The notifications step's "Continue" is the flow's last, real action — the "make Facet your
        // home screen" prompt itself now lives on the real Home screen instead of a further step here
        // (see SetDefaultLauncherSheet's own doc), so reaching it just finishes onboarding directly.
        composeRule.onNodeWithTag("onboarding_next").performClick()
        // Finishing first copies the choices onto the facets, which is asynchronous
        composeRule.waitUntil(timeoutMillis = 3_000) { finished }

        assertTrue(finished)
    }

    @Test
    fun `dock and favorites pickers reached from onboarding skip the Folders tab`() {
        // No folder can exist yet this early in first-run (they're only ever created via the
        // Drawer/Search's own "Add to folder" long-press flow, unreachable from onboarding), so
        // both pickers should go straight to the apps list rather than offering a dead-end tab.
        setContent()
        goToHomeSetup()

        composeRule.onNodeWithTag("onboarding_manage_dock").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("dock_picker_tab_folders").assertDoesNotExist()
        composeRule.onNodeWithTag("dock_picker_search").assertExists()
        composeRule.onNodeWithTag("back_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_edit_favorites").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("favorites_picker_tab_folders").assertDoesNotExist()
        composeRule.onNodeWithTag("favorites_picker_search").assertExists()
    }

    @Test
    fun `Back on the home-setup step returns to facets`() {
        setContent()
        goToHomeSetup()

        composeRule.onNodeWithTag("onboarding_back").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_facets_page").assertExists()
    }

    @Test
    fun `Back on the notifications step returns to drawer setup`() {
        setContent()
        goToNotifications()

        composeRule.onNodeWithTag("onboarding_back").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_drawer_setup_page").assertExists()
    }

    @Test
    fun `Back on the drawer setup step returns to home setup`() {
        setContent()
        goToDrawerSetup()

        composeRule.onNodeWithTag("onboarding_back").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_home_setup_page").assertExists()
    }

    @Test
    fun `Back on the facets step returns to intro`() {
        setContent()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_back").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_intro_page").assertExists()
    }

    @Test
    fun `swiping left advances to the next step`() {
        setContent()

        composeRule.onNodeWithTag("onboarding_screen").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_facets_page").assertExists()
    }

    @Test
    fun `swiping right returns to the previous step`() {
        setContent()
        goToHomeSetup()
        composeRule.onNodeWithTag("onboarding_home_setup_page").assertExists()

        composeRule.onNodeWithTag("onboarding_screen").performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_facets_page").assertExists()
    }

    @Test
    fun `swiping right on the first step does nothing`() {
        setContent()

        composeRule.onNodeWithTag("onboarding_screen").performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_intro_page").assertExists()
    }

    @Test
    fun `swiping left on the last step finishes onboarding`() {
        var finished = false
        setContent(onFinish = { finished = true })
        goToNotifications()

        composeRule.onNodeWithTag("onboarding_screen").performTouchInput { swipeLeft() }
        composeRule.waitUntil(timeoutMillis = 3_000) { finished }

        assertTrue(finished)
    }

    @Test
    fun `tapping Favorites opens the full-screen favorites picker`() {
        setContent()

        goToHomeSetup()

        composeRule.onNodeWithTag("onboarding_edit_favorites").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("favorites_picker_screen").assertExists()
    }

    @Test
    fun `Done on the favorites picker returns to home setup`() {
        setContent()
        goToHomeSetup()
        composeRule.onNodeWithTag("onboarding_edit_favorites").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("favorites_picker_done").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_home_setup_page").assertExists()
    }

    @Test
    fun `tapping Manage dock apps opens the full-screen dock picker`() {
        var seededApp: AppInfo? = null
        setContent { appRepository, placementRepository ->
            val app = runBlocking { appRepository.getTestableApps() }.first()
            seededApp = app
            placementRepository.addApp(firstFacetDock, app, 0)
        }
        requireNotNull(seededApp)

        goToHomeSetup()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onNodeWithText("1 of ${AppListLimits.MAX_DOCK_APPS}").let { runCatching { it.assertExists() }.isSuccess }
        }

        composeRule.onNodeWithTag("onboarding_manage_dock").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("dock_app_picker_screen").assertExists()
    }

    @Test
    fun `Clear all removes every dock app after confirming`() {
        var seededApp: AppInfo? = null
        setContent { appRepository, placementRepository ->
            val app = runBlocking { appRepository.getTestableApps() }.first()
            seededApp = app
            placementRepository.addApp(firstFacetDock, app, 0)
        }
        requireNotNull(seededApp)

        goToHomeSetup()
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
            composeRule.onNodeWithText("0 of ${AppListLimits.MAX_DOCK_APPS}").let { runCatching { it.assertExists() }.isSuccess }
        }
        composeRule.onNodeWithTag("onboarding_clear_dock").assertDoesNotExist()
    }

    @Test
    fun `Clear all removes every favorite after confirming`() {
        var seededApp: AppInfo? = null
        setContent { appRepository, placementRepository ->
            val app = runBlocking { appRepository.getTestableApps() }.first()
            seededApp = app
            placementRepository.addApp(firstFacetFavorites, app, 0)
        }
        requireNotNull(seededApp)

        goToHomeSetup()
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
    fun `drawer setup shows the preview and offers search bar position and both search toggles and folders in drawer`() {
        setContent()
        goToDrawerSetup()

        composeRule.onNodeWithTag("app_drawer_preview").assertExists()
        composeRule.onNodeWithTag("onboarding_search_bar_position_row").performScrollTo().assertExists()
        composeRule.onNodeWithTag("onboarding_search_contacts_toggle").performScrollTo().assertIsOff()
        composeRule.onNodeWithTag("onboarding_search_settings_toggle").performScrollTo().assertIsOff()
        composeRule.onNodeWithTag("onboarding_drawer_folder_display_mode_row").performScrollTo().assertExists()
    }

    @Test
    fun `turning on Search settings in drawer setup switches it on`() {
        setContent()
        goToDrawerSetup()

        composeRule.onNodeWithTag("onboarding_search_settings_toggle").performScrollTo().performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("onboarding_search_settings_toggle").assertIsOn() }.isSuccess
        }
    }

    @Test
    fun `completing onboarding requests no runtime permission`() {
        setContent()
        goToNotifications()
        composeRule.onNodeWithTag("onboarding_next").performClick()
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
    fun `system back from home setup returns to the facets step`() {
        setContent()
        goToHomeSetup()
        composeRule.onNodeWithTag("onboarding_home_setup_page").assertExists()

        pressBack()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("onboarding_facets_page").assertExists()
    }

    @Test
    fun `Skip on the intro step finishes onboarding`() {
        var finished = false
        setContent(onFinish = { finished = true })

        composeRule.onNodeWithTag("onboarding_skip").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) { finished }
        assertTrue(finished)
    }

    @Test
    fun `Skip on the home-setup step finishes onboarding`() {
        var finished = false
        setContent(onFinish = { finished = true })
        goToHomeSetup()
        composeRule.onNodeWithTag("onboarding_home_setup_page").assertExists()

        composeRule.onNodeWithTag("onboarding_skip").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) { finished }
        assertTrue(finished)
    }

    @Test
    fun `Skip on the facets step finishes onboarding`() {
        var finished = false
        setContent(onFinish = { finished = true })
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("onboarding_facets_page").assertExists()

        composeRule.onNodeWithTag("onboarding_skip").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) { finished }
        assertTrue(finished)
    }
}
