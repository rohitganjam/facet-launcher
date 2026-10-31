package com.facetlauncher.app.ui.launcher

import com.facetlauncher.app.data.AppVersion
import com.facetlauncher.app.data.ReleaseNotesRepository
import com.facetlauncher.app.domain.GroupAppsByLetterUseCase
import com.facetlauncher.app.domain.ObserveWeatherUseCase
import com.facetlauncher.app.domain.RefreshWeatherUseCase
import com.facetlauncher.app.ui.drawer.DrawerListUseCases
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.appwidget.AppWidgetManager
import android.content.pm.LauncherApps
import android.os.UserManager
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
import com.facetlauncher.app.data.BatteryRepository
import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.ContactPermissionRepository
import com.facetlauncher.app.data.ContactRepository
import com.facetlauncher.app.data.DefaultAppRepository
import com.facetlauncher.app.data.DefaultLauncherRepository
import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FacetShortcutRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.NextAlarmRepository
import com.facetlauncher.app.data.NotificationActionController
import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.MediaSessionRepository
import com.facetlauncher.app.data.NotificationBadgeRepository
import com.facetlauncher.app.data.NotificationOpener
import com.facetlauncher.app.data.NotificationShadeRepository
import com.facetlauncher.app.data.PrivateSpaceRepository
import com.facetlauncher.app.data.SecureFolderRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.SystemSettingsRepository
import com.facetlauncher.app.data.UsageAccessRepository
import com.facetlauncher.app.data.UsageStatsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.WorkProfileRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.data.widget.LauncherAppWidgetHost
import com.facetlauncher.app.domain.ActivateFacetByIdUseCase
import com.facetlauncher.app.domain.AddFacetUseCase
import com.facetlauncher.app.domain.CleanUpUninstalledAppsUseCase
import com.facetlauncher.app.domain.CompactWidgetsUseCase
import com.facetlauncher.app.domain.FlowPackWidgetsUseCase
import com.facetlauncher.app.domain.LandscapeHubUseCase
import com.facetlauncher.app.domain.OrderWidgetsForLandscapeUseCase
import com.facetlauncher.app.domain.ReorderLandscapeWidgetUseCase
import com.facetlauncher.app.domain.DeleteFacetUseCase
import com.facetlauncher.app.domain.DeleteWidgetUseCase
import com.facetlauncher.app.domain.EnsureActiveFacetUseCase
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.ObserveAppNotificationsUseCase
import com.facetlauncher.app.domain.ObserveClockAccessoriesUseCase
import com.facetlauncher.app.domain.ObserveFacetPreviewsUseCase
import com.facetlauncher.app.domain.ObserveHomeScreenStateUseCase
import com.facetlauncher.app.domain.ObserveHubStateUseCase
import com.facetlauncher.app.domain.ObserveQuickAddStateUseCase
import com.facetlauncher.app.domain.PlaceWidgetUseCase
import com.facetlauncher.app.domain.RankBySearchRelevanceUseCase
import com.facetlauncher.app.domain.RecentlyInstalledAppsUseCase
import com.facetlauncher.app.domain.RepairOrphanedProfileRowsUseCase
import com.facetlauncher.app.domain.ResolveWidgetDropUseCase
import com.facetlauncher.app.domain.ResolveWidgetResizeUseCase
import com.facetlauncher.app.domain.SeedDefaultDockUseCase
import com.facetlauncher.app.domain.SelectableFacetsUseCase
import com.facetlauncher.app.domain.AddClockStackItemUseCase
import com.facetlauncher.app.domain.ReplaceClockStackItemUseCase
import com.facetlauncher.app.domain.SyncFacetShortcutsUseCase
import com.facetlauncher.app.ui.drawer.DrawerViewModel
import com.facetlauncher.app.ui.drawer.PrivateSpaceViewModel
import com.facetlauncher.app.ui.facets.FacetCarouselViewModel
import com.facetlauncher.app.ui.home.ClockWidgetFacetController
import com.facetlauncher.app.ui.home.ClockWidgetHostController
import com.facetlauncher.app.ui.home.HomeViewModel
import com.facetlauncher.app.ui.home.WhatsNewPillViewModel
import com.facetlauncher.app.ui.home.weather.WeatherViewModel
import com.facetlauncher.app.ui.home.widget.ClockWidgetPickerViewModel
import com.facetlauncher.app.ui.hub.HubViewModel
import com.facetlauncher.app.ui.hub.picker.HubWidgetPickerViewModel
import com.facetlauncher.app.ui.testAutomation
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import java.time.Clock
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.domain.ObservePlacedItemsUseCase
import com.facetlauncher.app.domain.AddToActiveListUseCase
import com.facetlauncher.app.domain.RemoveFromActiveListUseCase
import com.facetlauncher.app.domain.ResolveActivePlacementTargetUseCase
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

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
                val appRepository = AppRepository(launcherApps, context.getSystemService(UserManager::class.java), context)
                val placementRepository = PlacementRepository(database.appPlacementDao(), database.folderPlacementDao())
                val observePlacedItems = ObservePlacedItemsUseCase(placementRepository, FolderRepository(database.folderDao(), appRepository), appRepository)
                val facetRepository = FacetRepository(database.facetDao(), database.clockStackItemDao())
                val clockStackRepository = ClockStackRepository(database, database.clockStackItemDao())
                val usageStatsRepository = UsageStatsRepository(
                    context.getSystemService(UsageStatsManager::class.java),
                    appRepository,
                )
                val usageAccessRepository = UsageAccessRepository(context.getSystemService(AppOpsManager::class.java), context)
                val notificationBadgeRepository = NotificationBadgeRepository()
                val notificationAccessRepository = NotificationAccessRepository(context)
                val appWidgetRepository = AppWidgetRepository(
                    context,
                    AppWidgetManager.getInstance(context),
                    LauncherAppWidgetHost(context),
                    context.getSystemService(UserManager::class.java),
                    appRepository,
                )
                HomeViewModel(
                    ObserveHomeScreenStateUseCase(
                        settingsRepository,
                        observePlacedItems,
                        facetRepository,
                        usageStatsRepository,
                        usageAccessRepository,
                        CalendarPermissionRepository(context),
                        CalendarRepository(context.contentResolver),
                        notificationBadgeRepository,
                        notificationAccessRepository,
                        ObserveClockAccessoriesUseCase(BatteryRepository(context), NextAlarmRepository(context, Clock.systemDefaultZone())),
                        MediaSessionRepository(context, notificationBadgeRepository),
                    ),
                    NotificationShadeRepository(context),
                    settingsRepository,
                    facetRepository,
                    DefaultLauncherRepository(context),
                    ObserveQuickAddStateUseCase(),
                    MediaSessionRepository(context, notificationBadgeRepository),
                    ClockWidgetHostController(appWidgetRepository),
                    ClockWidgetFacetController(clockStackRepository, ReplaceClockStackItemUseCase(clockStackRepository, appWidgetRepository), EntitlementRepository()),
                    EntitlementRepository(),
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
                val appRepository = AppRepository(launcherApps, context.getSystemService(UserManager::class.java), context)
                val placementRepository = PlacementRepository(database.appPlacementDao(), database.folderPlacementDao())
                val observePlacedItems = ObservePlacedItemsUseCase(placementRepository, FolderRepository(database.folderDao(), appRepository), appRepository)
                val facetRepository = FacetRepository(database.facetDao(), database.clockStackItemDao())
                val clockStackRepository = ClockStackRepository(database, database.clockStackItemDao())
                LauncherViewModel(
                    GetInstalledAppsUseCase(appRepository),
                    EnsureActiveFacetUseCase(facetRepository, settingsRepository, SelectableFacetsUseCase(facetRepository, EntitlementRepository())),
                    CleanUpUninstalledAppsUseCase(
                        appRepository, placementRepository,
                        FolderRepository(database.folderDao(), appRepository),
                    ),
                    RepairOrphanedProfileRowsUseCase(
                        appRepository, placementRepository,
                        FolderRepository(database.folderDao(), appRepository),
                    ),
                    SeedDefaultDockUseCase(settingsRepository, DefaultAppRepository(context), placementRepository, GetInstalledAppsUseCase(appRepository)),
                    SyncFacetShortcutsUseCase(SelectableFacetsUseCase(facetRepository, EntitlementRepository()), FacetShortcutRepository(context)),
                    testAutomation(context, database, facetRepository, settingsRepository).activate,
                    testAutomation(context, database, facetRepository, settingsRepository).run,
                    settingsRepository,
                    WorkProfileRepository(context.getSystemService(UserManager::class.java), appRepository, context),
                )
            }
            val drawerViewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "test-drawer-settings-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java), context.getSystemService(UserManager::class.java), context)
                val placementRepository = PlacementRepository(database.appPlacementDao(), database.folderPlacementDao())
                val observePlacedItems = ObservePlacedItemsUseCase(placementRepository, FolderRepository(database.folderDao(), appRepository), appRepository)
                val facetRepository = FacetRepository(database.facetDao(), database.clockStackItemDao())
                val clockStackRepository = ClockStackRepository(database, database.clockStackItemDao())
                val notificationBadgeRepository = NotificationBadgeRepository()
                val notificationAccessRepository = NotificationAccessRepository(context)
                DrawerViewModel(
                    appRepository,
                    settingsRepository,
                    ContactPermissionRepository(context),
                    ContactRepository(context.contentResolver, context),
                    SystemSettingsRepository(context),
                    notificationBadgeRepository,
                    notificationAccessRepository,
                    RankBySearchRelevanceUseCase(),
                    AddToActiveListUseCase(ResolveActivePlacementTargetUseCase(settingsRepository, facetRepository), placementRepository, observePlacedItems),
                    RemoveFromActiveListUseCase(ResolveActivePlacementTargetUseCase(settingsRepository, facetRepository), placementRepository),
                    FolderRepository(database.folderDao(), appRepository),
                    SecureFolderRepository(context),
                    PrivateSpaceRepository(context.getSystemService(UserManager::class.java), appRepository, context),
                    NotificationActionController(),
                    NotificationOpener(context),
                    ObserveAppNotificationsUseCase(settingsRepository, notificationBadgeRepository, notificationAccessRepository),
                    DrawerListUseCases(RankBySearchRelevanceUseCase(), GroupAppsByLetterUseCase(), RecentlyInstalledAppsUseCase()),
                )
            }
            val privateSpaceViewModel = remember {
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java), context.getSystemService(UserManager::class.java), context)
                val privateSpaceRepository = PrivateSpaceRepository(context.getSystemService(UserManager::class.java), appRepository, context)
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "test-settings-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                PrivateSpaceViewModel(privateSpaceRepository, RankBySearchRelevanceUseCase(), RecentlyInstalledAppsUseCase(), settingsRepository)
            }
            val hubViewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val widgetPlacementRepository = WidgetPlacementRepository(database.widgetPlacementDao())
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java), context.getSystemService(UserManager::class.java), context)
                val appWidgetRepository = AppWidgetRepository(
                    context,
                    AppWidgetManager.getInstance(context),
                    LauncherAppWidgetHost(context),
                    context.getSystemService(UserManager::class.java),
                    appRepository,
                )
                HubViewModel(
                    ObserveHubStateUseCase(widgetPlacementRepository, appWidgetRepository),
                    appWidgetRepository,
                    widgetPlacementRepository,
                    DeleteWidgetUseCase(widgetPlacementRepository, appWidgetRepository),
                    ResolveWidgetDropUseCase(),
                    ResolveWidgetResizeUseCase(),
                    CompactWidgetsUseCase(),
                    LandscapeHubUseCase(OrderWidgetsForLandscapeUseCase(), FlowPackWidgetsUseCase(), ReorderLandscapeWidgetUseCase()),
                )
            }
            val widgetPickerViewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val widgetPlacementRepository = WidgetPlacementRepository(database.widgetPlacementDao())
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java), context.getSystemService(UserManager::class.java), context)
                val appWidgetRepository = AppWidgetRepository(
                    context,
                    AppWidgetManager.getInstance(context),
                    LauncherAppWidgetHost(context),
                    context.getSystemService(UserManager::class.java),
                    appRepository,
                )
                HubWidgetPickerViewModel(appWidgetRepository, widgetPlacementRepository, PlaceWidgetUseCase())
            }
            val clockWidgetPickerViewModel = remember {
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java), context.getSystemService(UserManager::class.java), context)
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val placementRepository = PlacementRepository(database.appPlacementDao(), database.folderPlacementDao())
                val observePlacedItems = ObservePlacedItemsUseCase(placementRepository, FolderRepository(database.folderDao(), appRepository), appRepository)
                val facetRepository = FacetRepository(database.facetDao(), database.clockStackItemDao())
                val clockStackRepository = ClockStackRepository(database, database.clockStackItemDao())
                val appWidgetRepository = AppWidgetRepository(
                    context,
                    AppWidgetManager.getInstance(context),
                    LauncherAppWidgetHost(context),
                    context.getSystemService(UserManager::class.java),
                    appRepository,
                )
                ClockWidgetPickerViewModel(
                    appWidgetRepository,
                    ReplaceClockStackItemUseCase(clockStackRepository, appWidgetRepository),
                    AddClockStackItemUseCase(clockStackRepository, EntitlementRepository()),
                )
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
                val appRepository = AppRepository(launcherApps, context.getSystemService(UserManager::class.java), context)
                val placementRepository = PlacementRepository(database.appPlacementDao(), database.folderPlacementDao())
                val observePlacedItems = ObservePlacedItemsUseCase(placementRepository, FolderRepository(database.folderDao(), appRepository), appRepository)
                val facetRepository = FacetRepository(database.facetDao(), database.clockStackItemDao())
                val clockStackRepository = ClockStackRepository(database, database.clockStackItemDao())
                val wallpaperRepository = WallpaperRepository(android.app.WallpaperManager.getInstance(context))
                FacetCarouselViewModel(
                    facetRepository,
                    settingsRepository,
                    wallpaperRepository,
                    DeleteFacetUseCase(
                        facetRepository,
                        AppWidgetRepository(
                            context,
                            AppWidgetManager.getInstance(context),
                            LauncherAppWidgetHost(context),
                            context.getSystemService(UserManager::class.java),
                            appRepository,
                        ),
                    ),
                    ObserveFacetPreviewsUseCase(
                        facetRepository,
                        settingsRepository,
                        observePlacedItems,
                        UsageStatsRepository(context.getSystemService(UsageStatsManager::class.java), appRepository),
                        UsageAccessRepository(context.getSystemService(AppOpsManager::class.java), context),
                        CalendarPermissionRepository(context),
                        CalendarRepository(context.contentResolver),
                    ),
                    testAutomation(context, database, facetRepository, settingsRepository).activate,
                    AddFacetUseCase(facetRepository, EntitlementRepository()),
                    EntitlementRepository(),
                )
            }

            // The weather sheet's and the What's new pill's state: nothing to show here, but HomeDrawerRoute needs both.
            val extraSettings = remember {
                SettingsRepository(PreferenceDataStoreFactory.create(produceFile = { File(context.cacheDir, "test-extra-${System.nanoTime()}.preferences_pb") }))
            }
            val weatherViewModel = remember {
                val observeWeather = mock(ObserveWeatherUseCase::class.java)
                `when`(observeWeather()).thenReturn(flowOf(null))
                WeatherViewModel(observeWeather, extraSettings, mock(RefreshWeatherUseCase::class.java))
            }
            val whatsNewPillViewModel = remember { WhatsNewPillViewModel(extraSettings, ReleaseNotesRepository(context), AppVersion()) }

            FacetLauncherTheme {
                HomeDrawerRoute(
                    apps = apps.toImmutableList(),
                    onAppClick = {},
                    onNavigateToSettings = {},
                    onNavigateToFacetSettings = {},
                    onNavigateToManageFacets = {},
                    onNavigateToFacetPro = {},
                    onNavigateToUsageAccessExplanation = {},
                    homeViewModel = homeViewModel,
                    weatherViewModel = weatherViewModel,
                    whatsNewPillViewModel = whatsNewPillViewModel,
                    drawerViewModel = drawerViewModel,
                    privateSpaceViewModel = privateSpaceViewModel,
                    hubViewModel = hubViewModel,
                    widgetPickerViewModel = widgetPickerViewModel,
                    clockWidgetPickerViewModel = clockWidgetPickerViewModel,
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
