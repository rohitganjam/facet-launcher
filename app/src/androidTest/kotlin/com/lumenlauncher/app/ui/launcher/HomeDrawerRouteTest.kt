package com.lumenlauncher.app.ui.launcher

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.appwidget.AppWidgetManager
import android.content.pm.LauncherApps
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.center
import androidx.compose.ui.test.click
import androidx.compose.ui.test.down
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.moveTo
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
import com.lumenlauncher.app.data.DefaultAppRepository
import com.lumenlauncher.app.data.AppShortcutRepository
import com.lumenlauncher.app.data.CalendarPermissionRepository
import com.lumenlauncher.app.data.CalendarRepository
import com.lumenlauncher.app.data.ContactPermissionRepository
import com.lumenlauncher.app.data.ContactRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileDockAppRepository
import com.lumenlauncher.app.data.NotificationAccessRepository
import com.lumenlauncher.app.data.NotificationBadgeRepository
import com.lumenlauncher.app.data.NotificationShadeRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.UsageAccessRepository
import com.lumenlauncher.app.data.UsageStatsRepository
import com.lumenlauncher.app.data.WidgetPlacementRepository
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.data.local.WidgetPlacementEntity
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.widget.AppWidgetRepository
import com.lumenlauncher.app.data.widget.LauncherAppWidgetHost
import com.lumenlauncher.app.domain.AddAppToDockUseCase
import com.lumenlauncher.app.domain.AddAppToFavoritesUseCase
import com.lumenlauncher.app.domain.ObserveQuickAddStateUseCase
import com.lumenlauncher.app.domain.RankBySearchRelevanceUseCase
import com.lumenlauncher.app.domain.CleanUpUninstalledAppsUseCase
import com.lumenlauncher.app.domain.DeleteWidgetUseCase
import com.lumenlauncher.app.domain.EnsureActiveProfileUseCase
import com.lumenlauncher.app.domain.GetInstalledAppsUseCase
import com.lumenlauncher.app.domain.ObserveHomeScreenStateUseCase
import com.lumenlauncher.app.domain.ObserveHubStateUseCase
import com.lumenlauncher.app.domain.ResolveWidgetResizeUseCase
import com.lumenlauncher.app.domain.CompactWidgetsUseCase
import com.lumenlauncher.app.domain.ResolveWidgetDropUseCase
import com.lumenlauncher.app.domain.PlaceWidgetUseCase
import com.lumenlauncher.app.domain.ObserveProfilePreviewsUseCase
import com.lumenlauncher.app.domain.SeedDefaultDockUseCase
import com.lumenlauncher.app.data.WallpaperRepository
import com.lumenlauncher.app.ui.drawer.DrawerViewModel
import com.lumenlauncher.app.ui.home.HomeViewModel
import com.lumenlauncher.app.ui.hub.HubViewModel
import com.lumenlauncher.app.ui.hub.picker.HubWidgetPickerViewModel
import com.lumenlauncher.app.ui.profiles.ProfileCarouselViewModel
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

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

    private fun setContent(
        seedHubWithOneWidget: Boolean = false,
        onNavigateToProfileSettings: (Long) -> Unit = {},
        onNavigateToManageProfiles: () -> Unit = {},
        onboardingCompleted: Boolean = false,
    ) {
        composeRule.setContent {
            val context = LocalContext.current
            // Every repository below is a real @Singleton in production (Hilt hands every
            // ViewModel the same instance) — HomeViewModel and LauncherViewModel must share
            // these same instances here too, not each get their own throwaway copy, or
            // LauncherViewModel's EnsureActiveProfileUseCase (which creates the profile and
            // sets activeProfileId) writes to a database/DataStore HomeViewModel never sees,
            // leaving its own activeProfile permanently null and its app list permanently
            // empty (see chat history: this is why "FAVORITES" never rendered no matter how
            // long a test waited for it).
            val database = remember {
                Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
            }
            val settingsRepository = remember {
                SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "test-settings-${System.nanoTime()}.preferences_pb") },
                    ),
                ).also { repo -> if (onboardingCompleted) runBlocking { repo.setOnboardingCompleted(true) } }
            }
            val profileRepository = remember { ProfileRepository(database.profileDao()) }
            // A real AppRepository here would hit the actual on-device LauncherApps service —
            // AppRepositoryTest already covers that integration (mapping/sorting/icon-flatten/
            // live re-emission) against a mocked LauncherApps at the unit level, so there's no
            // correctness value in re-exercising the real system call in every UI test here too,
            // only real cost: LauncherApps.getActivityList() triggers a full package-visibility
            // pass across every package on the AVD (200+ BLOCKED entries in logcat per call) plus
            // real icon decoding, measured at 18+ real seconds on a loaded machine (see chat
            // history). Mocking AppRepository itself to return this test's own fake `apps` list
            // keeps everything downstream of it (Room, DataStore, the actual gesture/ViewModel
            // logic under test) real, while cutting out only the expensive, already-tested-
            // elsewhere system call.
            val appRepository = remember {
                mock(AppRepository::class.java).also { repo ->
                    `when`(repo.observeInstalledApps()).thenReturn(flowOf(apps))
                    runBlocking { `when`(repo.getInstalledApps()).thenReturn(apps) }
                    // Every Flow-returning method needs a stub — an unstubbed mock method
                    // returns null, and CleanUpUninstalledAppsUseCase collecting a null Flow
                    // NPEs immediately. No real uninstalls happen in these tests, so an empty,
                    // never-emitting Flow is the correct fake here.
                    `when`(repo.observeUninstalledPackages()).thenReturn(emptyFlow())
                }
            }
            val dockAppRepository = remember { DockAppRepository(database.dockAppDao(), appRepository) }
            val profileDockAppRepository = remember { ProfileDockAppRepository(database.profileDockAppDao(), appRepository) }
            val favoriteAppRepository = remember { FavoriteAppRepository(database.favoriteAppDao(), appRepository) }
            val defaultFavoriteAppRepository = remember {
                DefaultFavoriteAppRepository(database.defaultFavoriteAppDao(), appRepository).also { repo ->
                    // Never seeded otherwise — observeDefaultFavorites() combines DB rows against
                    // installed apps, so with zero rows the list (and "FAVORITES") stays
                    // permanently empty regardless of how fast/slow the installed-apps side is.
                    runBlocking { repo.addFavorite(apps.first(), position = 0) }
                }
            }
            val homeViewModel = remember {
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
                        profileDockAppRepository,
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
                    settingsRepository,
                    profileRepository,
                )
            }
            val launcherViewModel = remember {
                LauncherViewModel(
                    GetInstalledAppsUseCase(appRepository),
                    EnsureActiveProfileUseCase(profileRepository, settingsRepository),
                    CleanUpUninstalledAppsUseCase(appRepository, dockAppRepository, profileDockAppRepository, favoriteAppRepository, defaultFavoriteAppRepository),
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
                DrawerViewModel(
                    settingsRepository,
                    ContactPermissionRepository(context),
                    ContactRepository(context.contentResolver, context),
                    AppShortcutRepository(context.getSystemService(LauncherApps::class.java)),
                    NotificationBadgeRepository(),
                    NotificationAccessRepository(context),
                    RankBySearchRelevanceUseCase(),
                    ObserveQuickAddStateUseCase(
                        settingsRepository,
                        profileRepository,
                        favoriteAppRepository,
                        defaultFavoriteAppRepository,
                        dockAppRepository,
                        profileDockAppRepository,
                    ),
                    AddAppToFavoritesUseCase(settingsRepository, profileRepository, favoriteAppRepository, defaultFavoriteAppRepository),
                    AddAppToDockUseCase(settingsRepository, profileRepository, dockAppRepository, profileDockAppRepository),
                )
            }
            val hubViewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                val widgetPlacementRepository = WidgetPlacementRepository(database.widgetPlacementDao())
                if (seedHubWithOneWidget) {
                    runBlocking {
                        widgetPlacementRepository.upsert(
                            WidgetPlacementEntity(
                                appWidgetId = 1,
                                providerPackageName = "com.example.widgets",
                                providerClassName = ".Provider",
                                row = 0,
                                col = 0,
                                colSpan = 1,
                                rowSpan = 1,
                            ),
                        )
                    }
                }
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
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                val widgetPlacementRepository = WidgetPlacementRepository(database.widgetPlacementDao())
                val appWidgetRepository = AppWidgetRepository(
                    context,
                    AppWidgetManager.getInstance(context),
                    LauncherAppWidgetHost(context),
                )
                HubWidgetPickerViewModel(appWidgetRepository, widgetPlacementRepository, PlaceWidgetUseCase())
            }
            // Shares the same profileRepository/settingsRepository/app-backed repos as
            // homeViewModel/launcherViewModel above — same reasoning as that block's own doc: a
            // separate throwaway copy here would observe a different activeProfileId than what
            // EnsureActiveProfileUseCase actually wrote.
            val profileViewModel = remember {
                val wallpaperRepository = WallpaperRepository(android.app.WallpaperManager.getInstance(context))
                ProfileCarouselViewModel(
                    profileRepository,
                    settingsRepository,
                    wallpaperRepository,
                    ObserveProfilePreviewsUseCase(
                        profileRepository,
                        settingsRepository,
                        favoriteAppRepository,
                        defaultFavoriteAppRepository,
                        profileDockAppRepository,
                        dockAppRepository,
                        UsageStatsRepository(context.getSystemService(UsageStatsManager::class.java), appRepository),
                        UsageAccessRepository(context.getSystemService(AppOpsManager::class.java), context),
                        CalendarPermissionRepository(context),
                        CalendarRepository(context.contentResolver),
                    ),
                )
            }

            LumenLauncherTheme {
                HomeDrawerRoute(
                    apps = apps,
                    onAppClick = {},
                    onNavigateToSettings = {},
                    onNavigateToProfileSettings = onNavigateToProfileSettings,
                    onNavigateToManageProfiles = onNavigateToManageProfiles,
                    onNavigateToUsageAccessExplanation = {},
                    homeViewModel = homeViewModel,
                    drawerViewModel = drawerViewModel,
                    hubViewModel = hubViewModel,
                    widgetPickerViewModel = widgetPickerViewModel,
                    profileViewModel = profileViewModel,
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
        // drawer_search_field only tells you DrawerViewModel is up — Home's own favorites list
        // depends on a separate chain through the now-shared profileRepository/settingsRepository:
        // LauncherViewModel's EnsureActiveProfileUseCase creates the profile (Room insert) and
        // writes activeProfileId (DataStore), which HomeViewModel's ObserveHomeScreenStateUseCase
        // must observe before it resolves a non-null active profile. With AppRepository mocked
        // (see above), the installed-apps side is instant, so this is just Room/DataStore's own
        // ordinary emission latency, not a real system call — a short timeout is enough.
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("FAVORITES").fetchSemanticsNodes().isNotEmpty()
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
    fun swipingUpStartingOnAnAppIconStillOpensTheDrawer() {
        // Given the launcher starts on Home, with a favorite app icon rendered in the swipe area
        // (regression test: HomeDrawerRoute's swipe detector used to run on Compose's default
        // Main pass, so a finger coming down on the icon's own combinedClickable claimed the
        // touch first and the drag never started — only empty space between icons worked)
        setContent()
        val iconBounds = composeRule.onNodeWithTag("home_app_icon_com.example.A", useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
        val rootHeight = composeRule.onRoot().fetchSemanticsNode().boundsInRoot.height

        // When swiping up starting exactly on top of that icon, well past the halfway commit point
        composeRule.onRoot().performTouchInput {
            down(iconBounds.center)
            moveTo(iconBounds.center - Offset(0f, rootHeight * 0.6f))
            up()
        }
        settleAnimation()

        // Then the drawer still commits open
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
    fun swipingLeftPastThresholdOpensTheProfileCarousel() {
        // Given Home rendered — a left swipe on Home is the way into Switch Profiles, a
        // follow-finger panel to Home's right (mirroring the Hub, to Home's left; see chat
        // history — this replaced the old empty-space long-press, which was too hard to land
        // between the clock, app list and dock, and before that a NavHost destination).
        setContent()
        composeRule.onNodeWithTag("profile_carousel_screen").assertIsNotDisplayed()

        // When swiping left across most of the screen (Compose's default swipeLeft() travels
        // well past the commit fraction)
        composeRule.onRoot().performTouchInput { swipeLeft() }
        settleAnimation()

        // Then the carousel commits fully open
        composeRule.onNodeWithTag("profile_carousel_screen").assertIsDisplayed()
    }

    @Test
    fun releasingALeftDragBeforeThresholdDoesNotOpenTheProfileCarousel() {
        // Given Home rendered
        setContent()

        // When dragging left only a small fraction of the screen and releasing
        composeRule.onRoot().performTouchInput {
            down(centerRight)
            moveTo(centerRight - Offset(40f, 0f))
            up()
        }
        settleAnimation()

        // Then it springs back closed
        composeRule.onNodeWithTag("profile_carousel_screen").assertIsNotDisplayed()
    }

    @Test
    fun swipingLeftOpensTheProfileCarouselNotTheHub() {
        // Given Home rendered — regression guard for the horizontal axis split: rightward is the
        // Hub, leftward is Switch Profiles, and one gesture must never do both.
        setContent()

        // When swiping left
        composeRule.onRoot().performTouchInput { swipeLeft() }
        settleAnimation()

        // Then the carousel opened and the Hub stayed closed
        composeRule.onNodeWithTag("profile_carousel_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("hub_screen").assertIsNotDisplayed()
    }

    @Test
    fun aDiagonalSwipeMostlyLeftOpensTheProfileCarouselNotTheDrawer() {
        // Given Home rendered
        setContent()

        // When dragging mostly left with a small vertical component — the axis-lock must commit
        // to the horizontal (Switch Profiles) gesture alone, never also the Drawer
        composeRule.onRoot().performTouchInput {
            swipeWithVelocity(
                start = centerRight,
                end = centerRight + Offset(-visibleSize.width * 0.6f, visibleSize.height * 0.1f),
                endVelocity = 200f,
                durationMillis = 300,
            )
        }
        settleAnimation()

        // Then the carousel opened and the Drawer did not
        composeRule.onNodeWithTag("profile_carousel_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("alphabet_rail").assertIsNotDisplayed()
    }

    @Test
    fun swipingRightInEmptySpaceOnTheOpenCarouselReturnsToHome() {
        // Given the carousel is open
        setContent()
        composeRule.onRoot().performTouchInput { swipeLeft() }
        settleAnimation()
        composeRule.onNodeWithTag("profile_carousel_screen").assertIsDisplayed()

        // When swiping right low on the screen, below the card cluster (the trailing spacer) —
        // the carousel's own host-level Box (mirroring the Hub's) catches this, not the pager
        composeRule.onNodeWithTag("profile_carousel_screen").performTouchInput {
            val startX = width * 0.15f
            val y = height * 0.94f
            down(Offset(startX, y))
            repeat(15) { step -> moveTo(Offset(startX + width * 0.7f * (step + 1) / 15f, y)) }
            up()
        }
        settleAnimation()

        // Then it closes back to Home
        composeRule.onNodeWithTag("profile_carousel_screen").assertIsNotDisplayed()
    }

    @Test
    fun swipingRightOnTheFirstProfileCardWhileOpenReturnsToHome() {
        // Given the carousel is open, on the only (first) profile's page — nowhere to browse to
        setContent()
        composeRule.onRoot().performTouchInput { swipeLeft() }
        settleAnimation()
        composeRule.onNodeWithTag("profile_carousel_screen").assertIsDisplayed()

        // When swiping right on the pager itself
        val pager = composeRule.onNodeWithTag("profile_carousel_pager")
        val pagerWidth = pager.fetchSemanticsNode().size.width.toFloat()
        pager.performTouchInput {
            down(centerLeft)
            repeat(15) { step -> moveTo(centerLeft + Offset(pagerWidth * 0.6f * (step + 1) / 15f, 0f)) }
            up()
        }
        settleAnimation()

        // Then it closes back to Home — ProfileCarouselScreen forwarded the drag through
        // onDismissDrag/onDismissDragEnd into the very same axis the empty-space swipe above uses
        composeRule.onNodeWithTag("profile_carousel_screen").assertIsNotDisplayed()
    }

    @Test
    fun systemBackClosesAnOpenProfileCarousel() {
        // Given the carousel is open
        setContent()
        composeRule.onRoot().performTouchInput { swipeLeft() }
        settleAnimation()
        composeRule.onNodeWithTag("profile_carousel_screen").assertIsDisplayed()

        // When the system back gesture fires
        Espresso.pressBack()
        settleAnimation()

        // Then it closes back to Home
        composeRule.onNodeWithTag("profile_carousel_screen").assertIsNotDisplayed()
    }

    @Test
    fun draggingAGrabbedHubWidgetDoesNotAlsoCloseTheHub() {
        // Given the Hub is open with one widget placed
        setContent(seedHubWithOneWidget = true)
        composeRule.onRoot().performTouchInput { swipeRight() }
        settleAnimation()
        composeRule.onNodeWithTag("hub_screen").assertIsDisplayed()

        // When that widget is long-pressed and dragged left — the same direction a swipe-to-close
        // would use — regression guard: the tile's own gesture must consume the pointer stream so
        // the ancestor Home<->Hub swipe-axis never also reacts to it
        composeRule.onNodeWithTag("hub_widget_tile_1").performTouchInput { down(center) }
        composeRule.mainClock.advanceTimeBy(600)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("hub_widget_tile_1").performTouchInput {
            moveTo(center - Offset(80f, 0f))
            up()
        }
        settleAnimation()

        // Then the Hub is still open — the drag moved/bounced the widget, not the surface
        composeRule.onNodeWithTag("hub_screen").assertIsDisplayed()
    }

    @Test
    fun addingAWidgetOverlaysTheHubAndReturnsToItStillOpen() {
        // Given the Hub is open
        setContent()
        composeRule.onRoot().performTouchInput { swipeRight() }
        settleAnimation()
        composeRule.onNodeWithTag("hub_screen").assertIsDisplayed()

        // When Add is tapped — the picker must load as an overlay ON TOP of Hub (a real NavHost
        // destination here would tear down this whole composable instead, resetting hubAxis and
        // losing Hub's own open state — see chat history)
        composeRule.onNodeWithTag("hub_add_button").performClick()
        settleAnimation()

        // Then the picker is showing, and Hub is still composed underneath it (not replaced)
        composeRule.onNodeWithTag("hub_widget_picker_overlay").assertIsDisplayed()
        composeRule.onNodeWithTag("hub_screen").assertExists()

        // When the picker is dismissed via its own back button
        composeRule.onNodeWithTag("back_button").performClick()
        settleAnimation()

        // Then it's back to the Hub, which is still open (not reset to closed)
        composeRule.onNodeWithTag("hub_widget_picker_overlay").assertDoesNotExist()
        composeRule.onNodeWithTag("hub_screen").assertIsDisplayed()
    }

    @Test
    fun closingDrawerResetsSearchQueryAndScrollPosition() {
        // Given the drawer is open with a search query and scrolled down
        setContent()
        composeRule.onRoot().performTouchInput { swipeUp() }
        settleAnimation()
        
        val query = "TestQuery"
        composeRule.onNodeWithTag("drawer_search_field").performTextInput(query)
        composeRule.onNodeWithTag("drawer_search_results").performTouchInput { swipeUp() }
        
        // When the drawer is closed (using swipe down since pressBack is flaky in this env)
        composeRule.onNodeWithTag("drawer_search_results").performTouchInput { swipeDown() }
        settleAnimation()
        
        // Then it resets state when opened again
        composeRule.onRoot().performTouchInput { swipeUp() }
        settleAnimation()
        
        composeRule.onNodeWithText(query).assertDoesNotExist()
        // Verify it's back at the top - "A App" header should be visible
        composeRule.onNodeWithTag("header_A").assertIsDisplayed()
    }

    @Test
    fun launchingAppFromDrawerClosesDrawerAndResetsState() {
        // Given the drawer is open with a search query
        setContent()
        composeRule.onRoot().performTouchInput { swipeUp() }
        settleAnimation()
        
        // Search for "A" so "A App" is visible and clickable
        val query = "A"
        composeRule.onNodeWithTag("drawer_search_field").performTextInput(query)
        
        // When an app is clicked
        composeRule.onNodeWithTag("drawer_app_row_com.example.A").performClick()
        settleAnimation()
        
        // Then the drawer is closed
        composeRule.onNodeWithTag("alphabet_rail").assertIsNotDisplayed()
        
        // And it resets state when opened again
        composeRule.onRoot().performTouchInput { swipeUp() }
        settleAnimation()
        
        // The search field should not contain the query "A"
        // We check that the search field does not have the text "A" 
        // (but it might have the placeholder "Search apps", so we check for exact text match of the query)
        // Using assertDoesNotExist on the query string itself is risky if it's "A", 
        // so we check the field's semantics.
        composeRule.onNodeWithTag("drawer_search_field").assert(hasText(""))
        composeRule.onNodeWithTag("header_A").assertIsDisplayed()
    }

    @Test
    fun gestureHintShowsOnceOnboardingHasCompletedAndDismissesOnGotIt() {
        // Given onboarding has just completed
        setContent(onboardingCompleted = true)
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("FAVORITES").fetchSemanticsNodes().isNotEmpty()
        }

        // Then the one-time gesture hint overlay shows over Home (HomeViewModel's own combine is
        // real async work, same as everywhere else in this file — not reliably caught by a single
        // waitForIdle())
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("gesture_hint_overlay").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("gesture_hint_overlay").assertIsDisplayed()

        // When tapping "Got it"
        composeRule.onNodeWithTag("gesture_hint_got_it").performClick()

        // Then it's dismissed and doesn't come back
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("gesture_hint_overlay").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun gestureHintDoesNotShowBeforeOnboardingHasCompleted() {
        // Given onboarding has not completed (the default for every other test in this file)
        setContent(onboardingCompleted = false)
        composeRule.onNodeWithText("FAVORITES").assertExists()

        // Then no gesture hint overlay renders
        composeRule.onNodeWithTag("gesture_hint_overlay").assertDoesNotExist()
    }
}
