package com.lumenlauncher.app.ui.launcher

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.appwidget.AppWidgetManager
import android.content.pm.LauncherApps
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.createComposeRule
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
import com.lumenlauncher.app.data.AppRepository
import com.lumenlauncher.app.data.AppShortcutRepository
import com.lumenlauncher.app.data.CalendarPermissionRepository
import com.lumenlauncher.app.data.CalendarRepository
import com.lumenlauncher.app.data.ContactPermissionRepository
import com.lumenlauncher.app.data.ContactRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.NotificationAccessRepository
import com.lumenlauncher.app.data.NotificationBadgeRepository
import com.lumenlauncher.app.data.NotificationShadeRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.UsageAccessRepository
import com.lumenlauncher.app.data.UsageStatsRepository
import com.lumenlauncher.app.data.WidgetPlacementRepository
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.widget.AppWidgetRepository
import com.lumenlauncher.app.data.widget.LauncherAppWidgetHost
import com.lumenlauncher.app.domain.RankBySearchRelevanceUseCase
import com.lumenlauncher.app.domain.CleanUpUninstalledAppsUseCase
import com.lumenlauncher.app.domain.DeleteWidgetUseCase
import com.lumenlauncher.app.domain.EnsureActiveProfileUseCase
import com.lumenlauncher.app.domain.GetInstalledAppsUseCase
import com.lumenlauncher.app.domain.ObserveHomeScreenStateUseCase
import com.lumenlauncher.app.domain.ObserveHubStateUseCase
import com.lumenlauncher.app.domain.ResizeWidgetUseCase
import com.lumenlauncher.app.domain.CompactWidgetsUseCase
import com.lumenlauncher.app.domain.PlaceWidgetUseCase
import com.lumenlauncher.app.domain.ResolveWidgetDropUseCase
import com.lumenlauncher.app.ui.drawer.DrawerViewModel
import com.lumenlauncher.app.ui.home.HomeViewModel
import com.lumenlauncher.app.ui.hub.HubViewModel
import com.lumenlauncher.app.ui.hub.picker.HubWidgetPickerViewModel
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import org.junit.Rule
import org.junit.Test
import java.io.File

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
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                val appRepository = AppRepository(launcherApps)
                val dockAppRepository = DockAppRepository(database.dockAppDao(), appRepository)
                val favoriteAppRepository = FavoriteAppRepository(database.favoriteAppDao(), appRepository)
                val defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), appRepository)
                val profileRepository = ProfileRepository(database.profileDao())
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
                        favoriteAppRepository,
                        defaultFavoriteAppRepository,
                        profileRepository,
                        usageStatsRepository,
                        usageAccessRepository,
                        CalendarPermissionRepository(context),
                        CalendarRepository(context.contentResolver),
                        notificationBadgeRepository,
                        notificationAccessRepository,
                    ),
                    NotificationShadeRepository(context),
                )
            }
            launcherViewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "test-launcher-settings-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                val appRepository = AppRepository(launcherApps)
                val dockAppRepository = DockAppRepository(database.dockAppDao(), appRepository)
                val favoriteAppRepository = FavoriteAppRepository(database.favoriteAppDao(), appRepository)
                val defaultFavoriteAppRepository = DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), appRepository)
                val profileRepository = ProfileRepository(database.profileDao())
                LauncherViewModel(
                    GetInstalledAppsUseCase(appRepository),
                    EnsureActiveProfileUseCase(profileRepository, settingsRepository),
                    CleanUpUninstalledAppsUseCase(appRepository, dockAppRepository, favoriteAppRepository, defaultFavoriteAppRepository),
                    settingsRepository,
                )
            }
            val drawerViewModel = remember {
                val settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "test-drawer-settings-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                DrawerViewModel(
                    settingsRepository,
                    ContactPermissionRepository(context),
                    ContactRepository(context.contentResolver, context),
                    AppShortcutRepository(context.getSystemService(LauncherApps::class.java)),
                    NotificationBadgeRepository(),
                    NotificationAccessRepository(context),
                    RankBySearchRelevanceUseCase(),
                )
            }
            val hubViewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
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
                    ResizeWidgetUseCase(),
                    CompactWidgetsUseCase(),
                )
            }
            val widgetPickerViewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                val widgetPlacementRepository = WidgetPlacementRepository(database.widgetPlacementDao())
                val appWidgetRepository = AppWidgetRepository(
                    context,
                    AppWidgetManager.getInstance(context),
                    LauncherAppWidgetHost(context),
                )
                HubWidgetPickerViewModel(context, appWidgetRepository, widgetPlacementRepository, PlaceWidgetUseCase())
            }

            LumenLauncherTheme {
                HomeDrawerRoute(
                    apps = apps,
                    onAppClick = {},
                    onNavigateToSettings = {},
                    onNavigateToProfileCarousel = {},
                    onNavigateToEditProfile = {},
                    onNavigateToUsageAccessExplanation = {},
                    homeViewModel = homeViewModel,
                    drawerViewModel = drawerViewModel,
                    hubViewModel = hubViewModel,
                    widgetPickerViewModel = widgetPickerViewModel,
                    launcherViewModel = launcherViewModel,
                )
            }
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

        // When back is pressed (first time clears query and focus)
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
