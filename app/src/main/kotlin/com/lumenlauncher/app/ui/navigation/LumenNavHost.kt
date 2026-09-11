package com.lumenlauncher.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.NO_ACTIVE_PROFILE_ID
import com.lumenlauncher.app.ui.dock.DockAppPickerScreen
import com.lumenlauncher.app.ui.home.clock.ClockStyleGalleryRoute
import com.lumenlauncher.app.ui.home.clock.ProfileClockStyleGalleryScreen
import com.lumenlauncher.app.ui.launcher.HomeDrawerRoute
import com.lumenlauncher.app.ui.launcher.LauncherViewModel
import com.lumenlauncher.app.ui.profiles.FavoritesPickerScreen
import com.lumenlauncher.app.ui.profiles.ManageProfilesScreen
import com.lumenlauncher.app.ui.profiles.ProfileSettingsScreen
import com.lumenlauncher.app.ui.settings.AppDrawerSettingsScreen
import com.lumenlauncher.app.ui.settings.AppearanceSettingsScreen
import com.lumenlauncher.app.ui.settings.CalendarSettingsScreen
import com.lumenlauncher.app.ui.settings.DockSettingsScreen
import com.lumenlauncher.app.ui.settings.HomeAppsListSettingsScreen
import com.lumenlauncher.app.ui.settings.backup.BackupRestoreScreen
import com.lumenlauncher.app.ui.settings.NotificationAccessExplanationScreen
import com.lumenlauncher.app.ui.settings.NotificationSettingsScreen
import com.lumenlauncher.app.ui.settings.PermissionsScreen
import com.lumenlauncher.app.ui.settings.SettingsScreen
import com.lumenlauncher.app.ui.settings.UsageAccessExplanationScreen
import com.lumenlauncher.app.ui.theme.LUMEN_TRANSITION_DURATION_MS
import com.lumenlauncher.app.ui.theme.LumenTransitionEasing

object LumenDestinations {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val DOCK_PICKER = "dockPicker?profileId={profileId}"
    const val PROFILE_MANAGE = "profileManage"
    const val PROFILE_SETTINGS = "profileSettings/{profileId}"
    const val FAVORITES_PICKER = "favoritesPicker?profileId={profileId}"
    const val CALENDAR_SETTINGS = "calendarSettings?profileId={profileId}"
    const val USAGE_ACCESS_EXPLANATION = "usageAccessExplanation"
    const val PERMISSIONS = "permissions"
    const val NOTIFICATION_SETTINGS = "notificationSettings"
    const val NOTIFICATION_ACCESS_EXPLANATION = "notificationAccessExplanation"
    const val CLOCK_STYLE_GALLERY = "clockStyleGallery"
    const val PROFILE_CLOCK_STYLE_GALLERY = "profileClockStyleGallery/{profileId}"
    const val BACKUP_RESTORE = "backupRestore"
    const val APPEARANCE_SETTINGS = "appearanceSettings"
    const val DOCK_SETTINGS = "dockSettings?profileId={profileId}"
    const val HOME_APPS_LIST_SETTINGS = "homeAppsListSettings?profileId={profileId}"
    const val APP_DRAWER_SETTINGS = "appDrawerSettings"

    fun profileClockStyleGallery(profileId: Long) = "profileClockStyleGallery/$profileId"

    fun profileSettings(profileId: Long) = "profileSettings/$profileId"

    /** Omitting [profileId] (or passing [NO_ACTIVE_PROFILE_ID]) opens the launcher-wide default Favorites list. */
    fun favoritesPicker(profileId: Long? = null) =
        if (profileId != null && profileId != NO_ACTIVE_PROFILE_ID) "favoritesPicker?profileId=$profileId" else "favoritesPicker"

    /** Omitting [profileId] (or passing [NO_ACTIVE_PROFILE_ID]) opens the launcher-wide default dock. */
    fun dockPicker(profileId: Long? = null) =
        if (profileId != null && profileId != NO_ACTIVE_PROFILE_ID) "dockPicker?profileId=$profileId" else "dockPicker"

    /** Omitting [profileId] (or passing [NO_ACTIVE_PROFILE_ID]) opens the launcher-wide default Home apps list. */
    fun homeAppsListSettings(profileId: Long? = null) =
        if (profileId != null && profileId != NO_ACTIVE_PROFILE_ID) "homeAppsListSettings?profileId=$profileId" else "homeAppsListSettings"

    /** Omitting [profileId] (or passing [NO_ACTIVE_PROFILE_ID]) opens the launcher-wide default dock. */
    fun dockSettings(profileId: Long? = null) =
        if (profileId != null && profileId != NO_ACTIVE_PROFILE_ID) "dockSettings?profileId=$profileId" else "dockSettings"

    /** Omitting [profileId] (or passing [NO_ACTIVE_PROFILE_ID]) opens the global (non-profile-scoped) entry point. */
    fun calendarSettings(profileId: Long? = null) =
        if (profileId != null && profileId != NO_ACTIVE_PROFILE_ID) "calendarSettings?profileId=$profileId" else "calendarSettings"
}

/**
 * [NavHostController.popBackStack] pops the current (start) destination too once nothing else
 * remains, leaving `currentDestination == null` rather than safely no-op'ing — a real hazard
 * here since a single swipe-to-close gesture can cross the close threshold more than once
 * (many synthetic/physical touch-move events per gesture), firing this callback repeatedly.
 * Guard by only popping when there's actually somewhere to go back to.
 */
fun NavHostController.popBackStackSafely() {
    if (previousBackStackEntry != null) popBackStack()
}

/**
 * Single navigation graph for the whole app. Home and Drawer are merged into one route
 * ([HomeDrawerRoute]) rather than two separate destinations — their open/close transition is
 * a continuous follow-finger drag, which `NavHost`'s declarative enter/exit transitions can't
 * drive; see `HomeDrawerRoute` for the full rationale. Every other screen (Settings, pickers,
 * Profiles) stays a real `NavHost` destination, reached from Home the same way as before.
 */
@Composable
fun LumenNavHost(
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    launcherViewModel: LauncherViewModel = hiltViewModel(),
) {
    val animationSpec = tween<IntOffset>(durationMillis = LUMEN_TRANSITION_DURATION_MS, easing = LumenTransitionEasing)

    LaunchedEffect(launcherViewModel) {
        launcherViewModel.homePressedEvent.collect {
            navController.popBackStack(LumenDestinations.HOME, inclusive = false)
        }
    }

    NavHost(
        navController = navController,
        startDestination = LumenDestinations.HOME,
        modifier = modifier,
        // Pure slide, no crossfade: the window shows the wallpaper through any partially-transparent
        // layer, so a fading-out screen briefly reveals it — reads as a flash of Home. Two opaque
        // screens sliding past each other always cover the full width, so there's nothing to bleed.
        // (Switch Profiles no longer lives here — it's a follow-finger panel inside HomeDrawerRoute
        // itself now, like the Hub; see chat history.)
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = animationSpec) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = animationSpec) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = animationSpec) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = animationSpec) },
    ) {
        composable(LumenDestinations.HOME) {
            HomeDrawerRoute(
                apps = apps,
                onAppClick = onAppClick,
                onNavigateToSettings = { navController.navigate(LumenDestinations.SETTINGS) },
                onNavigateToProfileSettings = { profileId -> navController.navigate(LumenDestinations.profileSettings(profileId)) },
                onNavigateToManageProfiles = { navController.navigate(LumenDestinations.PROFILE_MANAGE) },
                onNavigateToUsageAccessExplanation = {
                    navController.navigate(LumenDestinations.USAGE_ACCESS_EXPLANATION)
                },
                onNavigateToClockStyleGallery = { profileId ->
                    val dest = if (profileId != null) LumenDestinations.profileClockStyleGallery(profileId) else LumenDestinations.CLOCK_STYLE_GALLERY
                    navController.navigate(dest)
                },
                launcherViewModel = launcherViewModel,
            )
        }
        composable(LumenDestinations.USAGE_ACCESS_EXPLANATION) {
            UsageAccessExplanationScreen(onBack = { navController.popBackStackSafely() })
        }
        composable(LumenDestinations.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStackSafely() },
                onViewProfiles = { navController.navigate(LumenDestinations.PROFILE_MANAGE) },
                onNavigateToAppearance = { navController.navigate(LumenDestinations.APPEARANCE_SETTINGS) },
                onNavigateToClockStyleGallery = { navController.navigate(LumenDestinations.CLOCK_STYLE_GALLERY) },
                onNavigateToCalendarSettings = { navController.navigate(LumenDestinations.calendarSettings()) },
                onNavigateToDockSettings = { navController.navigate(LumenDestinations.dockSettings()) },
                onNavigateToHomeAppsListSettings = { navController.navigate(LumenDestinations.homeAppsListSettings()) },
                onNavigateToAppDrawerSettings = { navController.navigate(LumenDestinations.APP_DRAWER_SETTINGS) },
                onNavigateToNotificationSettings = { navController.navigate(LumenDestinations.NOTIFICATION_SETTINGS) },
                onNavigateToPermissions = { navController.navigate(LumenDestinations.PERMISSIONS) },
                onNavigateToBackupRestore = { navController.navigate(LumenDestinations.BACKUP_RESTORE) },
            )
        }
        composable(LumenDestinations.BACKUP_RESTORE) {
            BackupRestoreScreen(onBack = { navController.popBackStackSafely() })
        }
        composable(LumenDestinations.APPEARANCE_SETTINGS) {
            AppearanceSettingsScreen(onBack = { navController.popBackStackSafely() })
        }
        composable(
            LumenDestinations.DOCK_SETTINGS,
            arguments = listOf(navArgument("profileId") { type = NavType.LongType; defaultValue = NO_ACTIVE_PROFILE_ID }),
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId")?.takeIf { it != NO_ACTIVE_PROFILE_ID }
            DockSettingsScreen(
                onBack = { navController.popBackStackSafely() },
                onAddDockApp = { navController.navigate(LumenDestinations.dockPicker(profileId)) },
            )
        }
        composable(
            LumenDestinations.HOME_APPS_LIST_SETTINGS,
            arguments = listOf(navArgument("profileId") { type = NavType.LongType; defaultValue = NO_ACTIVE_PROFILE_ID }),
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId")?.takeIf { it != NO_ACTIVE_PROFILE_ID }
            HomeAppsListSettingsScreen(
                onBack = { navController.popBackStackSafely() },
                onEditFavorites = { navController.navigate(LumenDestinations.favoritesPicker(profileId)) },
            )
        }
        composable(LumenDestinations.APP_DRAWER_SETTINGS) {
            AppDrawerSettingsScreen(onBack = { navController.popBackStackSafely() })
        }
        composable(LumenDestinations.CLOCK_STYLE_GALLERY) {
            ClockStyleGalleryRoute(onBack = { navController.popBackStackSafely() })
        }
        composable(
            LumenDestinations.PROFILE_CLOCK_STYLE_GALLERY,
            arguments = listOf(navArgument("profileId") { type = NavType.LongType }),
        ) {
            ProfileClockStyleGalleryScreen(onBack = { navController.popBackStackSafely() })
        }
        composable(LumenDestinations.PERMISSIONS) {
            PermissionsScreen(
                onBack = { navController.popBackStackSafely() },
                onNavigateToUsageAccessExplanation = { navController.navigate(LumenDestinations.USAGE_ACCESS_EXPLANATION) },
                onNavigateToNotificationAccessExplanation = {
                    navController.navigate(LumenDestinations.NOTIFICATION_ACCESS_EXPLANATION)
                },
            )
        }
        composable(LumenDestinations.NOTIFICATION_SETTINGS) {
            NotificationSettingsScreen(
                onBack = { navController.popBackStackSafely() },
                onNavigateToNotificationAccessExplanation = {
                    navController.navigate(LumenDestinations.NOTIFICATION_ACCESS_EXPLANATION)
                },
            )
        }
        composable(LumenDestinations.NOTIFICATION_ACCESS_EXPLANATION) {
            NotificationAccessExplanationScreen(onBack = { navController.popBackStackSafely() })
        }
        composable(
            LumenDestinations.DOCK_PICKER,
            arguments = listOf(navArgument("profileId") { type = NavType.LongType; defaultValue = NO_ACTIVE_PROFILE_ID }),
        ) {
            DockAppPickerScreen(onDone = { navController.popBackStackSafely() })
        }
        composable(LumenDestinations.PROFILE_MANAGE) {
            // Settings → Profiles (and the carousel's Reorder button): a normal settings screen.
            ManageProfilesScreen(
                onBack = { navController.popBackStackSafely() },
                onEditProfile = { profileId -> navController.navigate(LumenDestinations.profileSettings(profileId)) },
            )
        }
        composable(
            LumenDestinations.PROFILE_SETTINGS,
            arguments = listOf(navArgument("profileId") { type = NavType.LongType }),
        ) {
            ProfileSettingsScreen(
                onBack = { navController.popBackStackSafely() },
                onNavigateToAppsList = { profileId -> navController.navigate(LumenDestinations.homeAppsListSettings(profileId)) },
                onNavigateToDockSettings = { profileId -> navController.navigate(LumenDestinations.dockSettings(profileId)) },
                onNavigateToCalendarSettings = { profileId ->
                    navController.navigate(LumenDestinations.calendarSettings(profileId))
                },
                onNavigateToClockStyleGallery = { profileId ->
                    navController.navigate(LumenDestinations.profileClockStyleGallery(profileId))
                },
            )
        }
        composable(
            LumenDestinations.FAVORITES_PICKER,
            arguments = listOf(navArgument("profileId") { type = NavType.LongType; defaultValue = NO_ACTIVE_PROFILE_ID }),
        ) {
            FavoritesPickerScreen(onDone = { navController.popBackStackSafely() })
        }
        composable(
            LumenDestinations.CALENDAR_SETTINGS,
            arguments = listOf(navArgument("profileId") { type = NavType.LongType; defaultValue = NO_ACTIVE_PROFILE_ID }),
        ) {
            CalendarSettingsScreen(onBack = { navController.popBackStackSafely() })
        }
    }
}
