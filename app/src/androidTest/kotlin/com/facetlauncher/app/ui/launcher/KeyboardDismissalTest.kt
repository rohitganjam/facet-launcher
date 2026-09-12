package com.facetlauncher.app.ui.launcher

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.appwidget.AppWidgetManager
import android.content.pm.LauncherApps
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.espresso.Espresso
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.DefaultAppRepository
import com.facetlauncher.app.data.AppShortcutRepository
import com.facetlauncher.app.data.BatteryRepository
import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.ContactPermissionRepository
import com.facetlauncher.app.data.ContactRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.NotificationBadgeRepository
import com.facetlauncher.app.data.NextAlarmRepository
import com.facetlauncher.app.data.NotificationShadeRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.SystemSettingsRepository
import com.facetlauncher.app.data.UsageAccessRepository
import com.facetlauncher.app.data.UsageStatsRepository
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.data.widget.LauncherAppWidgetHost
import com.facetlauncher.app.domain.AddAppToDockUseCase
import com.facetlauncher.app.domain.AddAppToFavoritesUseCase
import com.facetlauncher.app.domain.ObserveQuickAddStateUseCase
import com.facetlauncher.app.domain.RankBySearchRelevanceUseCase
import com.facetlauncher.app.domain.CleanUpUninstalledAppsUseCase
import com.facetlauncher.app.domain.DeleteWidgetUseCase
import com.facetlauncher.app.domain.EnsureActiveFacetUseCase
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.ObserveClockAccessoriesUseCase
import com.facetlauncher.app.domain.ObserveHomeScreenStateUseCase
import com.facetlauncher.app.domain.ObserveHubStateUseCase
import com.facetlauncher.app.domain.ResolveWidgetResizeUseCase
import com.facetlauncher.app.domain.CompactWidgetsUseCase
import com.facetlauncher.app.domain.PlaceWidgetUseCase
import com.facetlauncher.app.domain.ResolveWidgetDropUseCase
import com.facetlauncher.app.domain.ObserveFacetPreviewsUseCase
import com.facetlauncher.app.domain.SeedDefaultDockUseCase
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.ui.drawer.DrawerViewModel
import com.facetlauncher.app.ui.home.HomeViewModel
import com.facetlauncher.app.ui.hub.HubViewModel
import com.facetlauncher.app.ui.hub.picker.HubWidgetPickerViewModel
import com.facetlauncher.app.ui.facets.FacetCarouselViewModel
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.time.Clock

class KeyboardDismissalTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val apps = listOf(
        AppInfo(packageName = "com.example.a", activityName = ".Main", label = "A App", icon = null)
    )

    private lateinit var launcherViewModel: LauncherViewModel

    private fun setContent() {
        composeRule.setContent {
            val context = LocalContext.current
            val homeViewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "test-settings-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                val appRepository = AppRepository(launcherApps)
                val dockAppRepository = DockAppRepository(database.dockAppDao(), appRepository)
                val facetDockAppRepository = FacetDockAppRepository(database.facetDockAppDao(), appRepository)
                val favoriteAppRepository = FavoriteAppRepository(database.favoriteAppDao(), appRepository)
                val defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), appRepository)
                val facetRepository = FacetRepository(database.facetDao())
                val usageStatsRepository = UsageStatsRepository(
                    context.getSystemService(UsageStatsManager::class.java),
                    appRepository,
                )
                val usageAccessRepository = UsageAccessRepository(context.getSystemService(AppOpsManager::class.java), context)
                val notificationBadgeRepository = NotificationBadgeRepository()
                val notificationAccessRepository = NotificationAccessRepository(context)
                HomeViewModel(
                    ObserveHomeScreenStateUseCase(
                        settingsRepository,
                        dockAppRepository,
                        facetDockAppRepository,
                        favoriteAppRepository,
                        defaultFavoriteAppRepository,
                        facetRepository,
                        usageStatsRepository,
                        usageAccessRepository,
                        CalendarPermissionRepository(context),
                        CalendarRepository(context.contentResolver),
                        notificationBadgeRepository,
                        notificationAccessRepository,
                        ObserveClockAccessoriesUseCase(BatteryRepository(context), NextAlarmRepository(context, Clock.systemDefaultZone())),
                    ),
                    NotificationShadeRepository(context),
                    settingsRepository,
                    facetRepository,
                )
            }
            launcherViewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "test-launcher-settings-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                val appRepository = AppRepository(launcherApps)
                val dockAppRepository = DockAppRepository(database.dockAppDao(), appRepository)
                val facetDockAppRepository = FacetDockAppRepository(database.facetDockAppDao(), appRepository)
                val favoriteAppRepository = FavoriteAppRepository(database.favoriteAppDao(), appRepository)
                val defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), appRepository)
                val facetRepository = FacetRepository(database.facetDao())
                LauncherViewModel(
                    GetInstalledAppsUseCase(appRepository),
                    EnsureActiveFacetUseCase(facetRepository, settingsRepository),
                    CleanUpUninstalledAppsUseCase(appRepository, dockAppRepository, facetDockAppRepository, favoriteAppRepository, defaultFavoriteAppRepository),
                    SeedDefaultDockUseCase(settingsRepository, DefaultAppRepository(context), dockAppRepository, GetInstalledAppsUseCase(appRepository)),
                    settingsRepository,
                )
            }
            val drawerViewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "test-drawer-settings-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java))
                val dockAppRepository = DockAppRepository(database.dockAppDao(), appRepository)
                val facetDockAppRepository = FacetDockAppRepository(database.facetDockAppDao(), appRepository)
                val favoriteAppRepository = FavoriteAppRepository(database.favoriteAppDao(), appRepository)
                val defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), appRepository)
                val facetRepository = FacetRepository(database.facetDao())
                DrawerViewModel(
                    settingsRepository,
                    ContactPermissionRepository(context),
                    ContactRepository(context.contentResolver, context),
                    SystemSettingsRepository(context),
                    AppShortcutRepository(context.getSystemService(LauncherApps::class.java)),
                    NotificationBadgeRepository(),
                    NotificationAccessRepository(context),
                    RankBySearchRelevanceUseCase(),
                    ObserveQuickAddStateUseCase(
                        settingsRepository,
                        facetRepository,
                        favoriteAppRepository,
                        defaultFavoriteAppRepository,
                        dockAppRepository,
                        facetDockAppRepository,
                    ),
                    AddAppToFavoritesUseCase(settingsRepository, facetRepository, favoriteAppRepository, defaultFavoriteAppRepository),
                    AddAppToDockUseCase(settingsRepository, facetRepository, dockAppRepository, facetDockAppRepository),
                )
            }
            val hubViewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val widgetPlacementRepository = WidgetPlacementRepository(database.widgetPlacementDao())
                val appWidgetRepository = AppWidgetRepository(
                    context,
                    AppWidgetManager.getInstance(context),
                    LauncherAppWidgetHost(context),
                )
                HubViewModel(
                    ObserveHubStateUseCase(widgetPlacementRepository, appWidgetRepository),
                    appWidgetRepository,
                    widgetPlacementRepository,
                    DeleteWidgetUseCase(widgetPlacementRepository, appWidgetRepository),
                    ResolveWidgetDropUseCase(),
                    ResolveWidgetResizeUseCase(),
                    CompactWidgetsUseCase(),
                )
            }
            val widgetPickerViewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val widgetPlacementRepository = WidgetPlacementRepository(database.widgetPlacementDao())
                val appWidgetRepository = AppWidgetRepository(
                    context,
                    AppWidgetManager.getInstance(context),
                    LauncherAppWidgetHost(context),
                )
                HubWidgetPickerViewModel(appWidgetRepository, widgetPlacementRepository, PlaceWidgetUseCase())
            }
            // A throwaway set of repos, same as this file's other view models — none of this
            // test's cases touch the carousel itself, so correctness here doesn't matter, only
            // that it constructs.
            val facetViewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "test-facet-settings-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                val appRepository = AppRepository(launcherApps)
                val facetRepository = FacetRepository(database.facetDao())
                val favoriteAppRepository = FavoriteAppRepository(database.favoriteAppDao(), appRepository)
                val defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), appRepository)
                val facetDockAppRepository = FacetDockAppRepository(database.facetDockAppDao(), appRepository)
                val dockAppRepository = DockAppRepository(database.dockAppDao(), appRepository)
                val wallpaperRepository = WallpaperRepository(android.app.WallpaperManager.getInstance(context))
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
                        UsageStatsRepository(context.getSystemService(UsageStatsManager::class.java), appRepository),
                        UsageAccessRepository(context.getSystemService(AppOpsManager::class.java), context),
                        CalendarPermissionRepository(context),
                        CalendarRepository(context.contentResolver),
                    ),
                )
            }

            FacetLauncherTheme {
                HomeDrawerRoute(
                    apps = apps,
                    onAppClick = {},
                    onNavigateToSettings = {},
                    onNavigateToFacetSettings = {},
                    onNavigateToManageFacets = {},
                    onNavigateToUsageAccessExplanation = {},
                    homeViewModel = homeViewModel,
                    drawerViewModel = drawerViewModel,
                    hubViewModel = hubViewModel,
                    widgetPickerViewModel = widgetPickerViewModel,
                    facetViewModel = facetViewModel,
                    launcherViewModel = launcherViewModel,
                )
            }
        }
        // HomeDrawerRoute renders nothing at all until HomeViewModel's first real state emission
        // arrives (see HomeViewModel's own doc) — the real repositories behind it here (Room/
        // DataStore, not fakes) make that a genuine, if normally tiny, async gap, so every test
        // needs to wait it out before interacting rather than assuming instant content.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("drawer_search_field").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun settleAnimation() {
        composeRule.mainClock.advanceTimeBy(500)
        composeRule.waitForIdle()
    }

    @Test
    fun homePressedClearsFocusAndKeyboard() {
        // Given drawer is open and search field has focus
        setContent()
        composeRule.onRoot().performTouchInput { swipeUp() }
        settleAnimation()
        composeRule.onNodeWithTag("drawer_search_field").performClick()
        composeRule.onNodeWithTag("drawer_search_field").assertIsFocused()

        // When home is pressed
        composeRule.runOnUiThread {
            launcherViewModel.onHomePressed()
        }
        settleAnimation()

        // Then focus is cleared
        composeRule.onNodeWithTag("drawer_search_field").assertIsNotFocused()
    }

    @Test
    fun backButtonClearsFocusAndKeyboard() {
        // Given drawer is open and search field has focus with query
        setContent()
        composeRule.onRoot().performTouchInput { swipeUp() }
        settleAnimation()
        composeRule.onNodeWithTag("drawer_search_field").performTextInput("test")
        composeRule.onNodeWithTag("drawer_search_field").assertIsFocused()

        // When back is pressed — with the IME visible, the first system back press is consumed by
        // the keyboard dismissal itself (standard OS behavior, handled below the Activity's
        // back-callback stack, before our BackHandler ever sees it), not by our own BackHandler.
        // Close the keyboard first so this exercises our handler specifically, matching what a
        // real user's *second* back press sees — see the identical, already-documented workaround
        // in AppDrawerScreenTest.searchResultsAreHiddenAndBrowseModeReturnsOnBackPress.
        Espresso.closeSoftKeyboard()
        composeRule.waitForIdle()
        Espresso.pressBack()
        settleAnimation()

        // Then focus is cleared
        composeRule.onNodeWithTag("drawer_search_field").assertIsNotFocused()
    }

    @Test
    fun swipingDrawerDownClearsFocusAndKeyboard() {
        // Given drawer is open and search field has focus
        setContent()
        composeRule.onRoot().performTouchInput { swipeUp() }
        settleAnimation()
        composeRule.onNodeWithTag("drawer_search_field").performClick()
        composeRule.onNodeWithTag("drawer_search_field").assertIsFocused()

        // When swiping drawer down
        composeRule.onNodeWithTag("drawer_list").performTouchInput { swipeDown() }
        settleAnimation()

        // Then focus is cleared
        composeRule.onNodeWithTag("drawer_search_field").assertIsNotFocused()
    }
}
