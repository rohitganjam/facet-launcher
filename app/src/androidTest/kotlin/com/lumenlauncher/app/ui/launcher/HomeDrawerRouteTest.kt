package com.lumenlauncher.app.ui.launcher

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.appwidget.AppWidgetManager
import android.content.pm.LauncherApps
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.center
import androidx.compose.ui.test.down
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.moveTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.swipeWithVelocity
import androidx.compose.ui.test.topCenter
import androidx.compose.ui.test.up
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
import com.lumenlauncher.app.domain.CleanUpUninstalledAppsUseCase
import com.lumenlauncher.app.domain.DeleteWidgetUseCase
import com.lumenlauncher.app.domain.EnsureActiveProfileUseCase
import com.lumenlauncher.app.domain.GetInstalledAppsUseCase
import com.lumenlauncher.app.domain.ObserveHomeScreenStateUseCase
import com.lumenlauncher.app.domain.ObserveHubStateUseCase
import com.lumenlauncher.app.ui.drawer.DrawerViewModel
import com.lumenlauncher.app.ui.home.HomeViewModel
import com.lumenlauncher.app.ui.hub.HubViewModel
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import java.io.File
import org.junit.Rule
import org.junit.Test

/**
 * Home and Drawer are one merged composable now ([HomeDrawerRoute]) rather than two NavHost
 * destinations — covers its follow-finger open/close drag (commits past halfway, springs back
 * otherwise) and the long-press sheet it hosts. Real (throwaway, per-test) repositories back
 * the ViewModels rather than Hilt, matching how other ViewModel tests in this codebase
 * construct their dependencies directly.
 */
class HomeDrawerRouteTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** Enough apps (one per letter) that the drawer list can actually scroll. */
    private val apps = ('A'..'Z').map { letter ->
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)
    }

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
            val launcherViewModel = remember {
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
                    ContactRepository(context.contentResolver),
                    AppShortcutRepository(context.getSystemService(LauncherApps::class.java)),
                    NotificationBadgeRepository(),
                    NotificationAccessRepository(context),
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
                    DeleteWidgetUseCase(widgetPlacementRepository, appWidgetRepository),
                )
            }

            LumenLauncherTheme {
                HomeDrawerRoute(
                    apps = apps,
                    onAppClick = {},
                    onNavigateToSettings = {},
                    onNavigateToProfileCarousel = {},
                    onNavigateToEditProfile = {},
                    onNavigateToUsageAccessExplanation = {},
                    onNavigateToHubWidgetPicker = {},
                    homeViewModel = homeViewModel,
                    drawerViewModel = drawerViewModel,
                    hubViewModel = hubViewModel,
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
    fun swipingUpPastHalfwayCommitsTheDrawerOpen() {
        // Given the launcher starts on Home
        setContent()
        composeRule.onNodeWithText("FAVORITES").assertExists()

        // When swiping up (Compose test's default swipeUp() travels most of the node's height,
        // well past the halfway commit point)
        composeRule.onRoot().performTouchInput { swipeUp() }
        settleAnimation()

        // Then the drawer commits fully open — the rail is always composed (offset off-screen
        // when closed, per the follow-finger drag design), so displayed-ness is what matters
        composeRule.onNodeWithTag("alphabet_rail").assertIsDisplayed()
    }

    @Test
    fun releasingBeforeHalfwaySpringsBackClosed() {
        // Given the launcher starts on Home
        setContent()

        // When dragging up only a small fraction of the screen and releasing
        composeRule.onRoot().performTouchInput {
            down(bottomCenter)
            moveTo(bottomCenter - Offset(0f, 40f))
            up()
        }
        settleAnimation()

        // Then it springs back to Home rather than opening
        composeRule.onNodeWithText("FAVORITES").assertExists()
        composeRule.onNodeWithTag("alphabet_rail").assertIsNotDisplayed()
    }

    @Test
    fun swipingDownAtDrawerTopReturnsToHome() {
        // Given the drawer is open, scrolled to the top
        setContent()
        composeRule.onRoot().performTouchInput { swipeUp() }
        settleAnimation()
        composeRule.onNodeWithTag("alphabet_rail").assertIsDisplayed()

        // When swiping down
        composeRule.onNodeWithTag("drawer_list").performTouchInput { swipeDown() }
        settleAnimation()

        // Then it returns to Home
        composeRule.onNodeWithText("FAVORITES").assertExists()
        composeRule.onNodeWithTag("alphabet_rail").assertIsNotDisplayed()
    }

    @Test
    fun lowVelocitySwipePastTwentyPercentStillClosesTheDrawer() {
        // Given the drawer is open, scrolled to the top
        setContent()
        composeRule.onRoot().performTouchInput { swipeUp() }
        settleAnimation()
        composeRule.onNodeWithTag("alphabet_rail").assertIsDisplayed()

        // When dragging down past the 20% commit threshold but releasing at a speed well under
        // VELOCITY_THRESHOLD_PX (1000px/s) — regression test for a bug where the drawer list's
        // own post-release fling deceleration re-hijacked the settle animation (onPreScroll
        // wasn't filtering by NestedScrollSource), stranding the drawer partway open instead of
        // collapsing it once the threshold had already been crossed.
        composeRule.onNodeWithTag("drawer_list").performTouchInput {
            swipeWithVelocity(
                start = topCenter,
                end = topCenter + Offset(0f, visibleSize.height * 0.35f),
                endVelocity = 200f,
                durationMillis = 600,
            )
        }
        settleAnimation()

        // Then it still fully closes back to Home, not left partially open
        composeRule.onNodeWithText("FAVORITES").assertExists()
        composeRule.onNodeWithTag("alphabet_rail").assertIsNotDisplayed()
    }

    @Test
    fun swipingDownWhileScrolledDoesNotCloseTheDrawer() {
        // Given the drawer is open and has been scrolled well away from the top
        setContent()
        composeRule.onRoot().performTouchInput { swipeUp() }
        settleAnimation()
        composeRule.onNodeWithTag("alphabet_rail").assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_list").performTouchInput { swipeUp() }

        // When swiping down by an amount too small to scroll the list all the way back to the
        // top (a full-distance swipeDown() would reach the top *and then* keep going, which is
        // a different, deliberate case — reaching top mid-gesture and continuing on to close)
        composeRule.onNodeWithTag("drawer_list").performTouchInput {
            down(bottomCenter)
            moveTo(bottomCenter + Offset(0f, 150f))
            up()
        }
        settleAnimation()

        // Then this was purely a scroll — the drawer is still fully open, not closed at all
        composeRule.onNodeWithTag("alphabet_rail").assertIsDisplayed()
    }

    @Test
    fun systemBackClosesAnOpenDrawer() {
        // Given the drawer is open
        setContent()
        composeRule.onRoot().performTouchInput { swipeUp() }
        settleAnimation()
        composeRule.onNodeWithTag("alphabet_rail").assertIsDisplayed()

        // When the system back gesture fires
        Espresso.pressBack()
        settleAnimation()

        // Then it closes back to Home
        composeRule.onNodeWithText("FAVORITES").assertExists()
        composeRule.onNodeWithTag("alphabet_rail").assertIsNotDisplayed()
    }

    @Test
    fun swipingRightPastThresholdCommitsTheHubOpen() {
        // Given the launcher starts on Home
        setContent()
        composeRule.onNodeWithTag("hub_screen").assertIsNotDisplayed()

        // When swiping right (Compose test's default swipeRight() travels most of the node's
        // width, well past the halfway commit point)
        composeRule.onRoot().performTouchInput { swipeRight() }
        settleAnimation()

        // Then the Hub commits fully open
        composeRule.onNodeWithTag("hub_screen").assertIsDisplayed()
    }

    @Test
    fun releasingHubSwipeBeforeThresholdSpringsBackClosed() {
        // Given the launcher starts on Home
        setContent()

        // When dragging right only a small fraction of the screen and releasing
        composeRule.onRoot().performTouchInput {
            down(centerLeft)
            moveTo(centerLeft + Offset(40f, 0f))
            up()
        }
        settleAnimation()

        // Then it springs back to Home rather than opening
        composeRule.onNodeWithTag("hub_screen").assertIsNotDisplayed()
    }

    @Test
    fun swipingLeftOnHubReturnsToHome() {
        // Given the Hub is open
        setContent()
        composeRule.onRoot().performTouchInput { swipeRight() }
        settleAnimation()
        composeRule.onNodeWithTag("hub_screen").assertIsDisplayed()

        // When swiping left on the Hub itself
        composeRule.onNodeWithTag("hub_screen").performTouchInput { swipeLeft() }
        settleAnimation()

        // Then it closes back to Home
        composeRule.onNodeWithTag("hub_screen").assertIsNotDisplayed()
    }

    @Test
    fun systemBackClosesAnOpenHub() {
        // Given the Hub is open
        setContent()
        composeRule.onRoot().performTouchInput { swipeRight() }
        settleAnimation()
        composeRule.onNodeWithTag("hub_screen").assertIsDisplayed()

        // When the system back gesture fires
        Espresso.pressBack()
        settleAnimation()

        // Then it closes back to Home
        composeRule.onNodeWithTag("hub_screen").assertIsNotDisplayed()
    }

    @Test
    fun aDiagonalSwipeMostlyRightOpensOnlyTheHubNotTheDrawer() {
        // Given the launcher starts on Home
        setContent()

        // When dragging mostly right with a small vertical component (a diagonal swipe, but
        // dominated by the horizontal axis) — regression guard for the axis-lock: this must
        // commit to the Hub alone, never both surfaces at once
        composeRule.onRoot().performTouchInput {
            swipeWithVelocity(
                start = centerLeft,
                end = centerLeft + Offset(visibleSize.width * 0.6f, visibleSize.height * 0.1f),
                endVelocity = 200f,
                durationMillis = 300,
            )
        }
        settleAnimation()

        // Then the Hub opened and the Drawer did not
        composeRule.onNodeWithTag("hub_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("alphabet_rail").assertIsNotDisplayed()
    }

    @Test
    fun aDiagonalSwipeMostlyUpOpensOnlyTheDrawerNotTheHub() {
        // Given the launcher starts on Home
        setContent()

        // When dragging mostly up with a small horizontal component — the mirror-image
        // regression guard, dominated by the vertical axis this time
        composeRule.onRoot().performTouchInput {
            swipeWithVelocity(
                start = bottomCenter,
                end = bottomCenter + Offset(visibleSize.width * 0.1f, -visibleSize.height * 0.6f),
                endVelocity = 200f,
                durationMillis = 300,
            )
        }
        settleAnimation()

        // Then the Drawer opened and the Hub did not
        composeRule.onNodeWithTag("alphabet_rail").assertIsDisplayed()
        composeRule.onNodeWithTag("hub_screen").assertIsNotDisplayed()
    }

    @Test
    fun holdingStillForTheFullDurationOpensTheSheet() {
        // Given Home rendered
        setContent()

        // When the finger goes down and stays still past the platform long-press timeout —
        // advance Compose's own test clock rather than Thread.sleep, since the pointerInput
        // coroutine's internal timer is driven by that clock, not real wall-clock time
        composeRule.onRoot().performTouchInput { down(center) }
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.waitForIdle()

        // Then the long-press sheet opens
        composeRule.onNodeWithTag("long_press_sheet").assertExists()
        composeRule.onRoot().performTouchInput { up() }
    }

    @Test
    fun releasingBeforeTheDurationDoesNotOpenTheSheet() {
        // Given Home rendered
        setContent()

        // When the finger lifts well before the long-press timeout
        composeRule.onRoot().performTouchInput { down(center) }
        composeRule.mainClock.advanceTimeBy(100)
        composeRule.onRoot().performTouchInput { up() }
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.waitForIdle()

        // Then the sheet never opens
        composeRule.onNodeWithTag("long_press_sheet").assertDoesNotExist()
    }
}
