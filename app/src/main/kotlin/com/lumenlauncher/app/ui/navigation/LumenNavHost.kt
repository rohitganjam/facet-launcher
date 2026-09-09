package com.lumenlauncher.app.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import com.lumenlauncher.app.ui.profiles.ProfileCarouselScreen
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

object LumenDestinations {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val DOCK_PICKER = "dockPicker"
    const val PROFILE_CAROUSEL = "profileCarousel"
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
    const val DOCK_SETTINGS = "dockSettings"
    const val HOME_APPS_LIST_SETTINGS = "homeAppsListSettings"
    const val APP_DRAWER_SETTINGS = "appDrawerSettings"

    fun profileClockStyleGallery(profileId: Long) = "profileClockStyleGallery/$profileId"

    fun profileSettings(profileId: Long) = "profileSettings/$profileId"

    /** Omitting [profileId] (or passing [NO_ACTIVE_PROFILE_ID]) opens the launcher-wide default Favorites list. */
    fun favoritesPicker(profileId: Long? = null) =
        if (profileId != null && profileId != NO_ACTIVE_PROFILE_ID) "favoritesPicker?profileId=$profileId" else "favoritesPicker"

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
    val animationSpec = tween<IntOffset>(durationMillis = 340, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f))
    val fadeSpec = tween<Float>(durationMillis = 200)

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
        // Exception: the translucent Switch Profiles carousel (PROFILE_CAROUSEL) is meant to sit
        // *over* a stationary Home, so Home doesn't animate when it's the counterpart — the
        // carousel's own composable fades/scales instead.
        enterTransition = {
            if (initialState.destination.route == LumenDestinations.PROFILE_CAROUSEL) EnterTransition.None
            else slideInHorizontally(initialOffsetX = { it }, animationSpec = animationSpec)
        },
        exitTransition = {
            if (targetState.destination.route == LumenDestinations.PROFILE_CAROUSEL) ExitTransition.None
            else slideOutHorizontally(targetOffsetX = { -it }, animationSpec = animationSpec)
        },
        popEnterTransition = {
            if (initialState.destination.route == LumenDestinations.PROFILE_CAROUSEL) EnterTransition.None
            else slideInHorizontally(initialOffsetX = { -it }, animationSpec = animationSpec)
        },
        popExitTransition = {
            if (targetState.destination.route == LumenDestinations.PROFILE_CAROUSEL) ExitTransition.None
            else slideOutHorizontally(targetOffsetX = { it }, animationSpec = animationSpec)
        },
    ) {
        composable(LumenDestinations.HOME) {
            HomeDrawerRoute(
                apps = apps,
                onAppClick = onAppClick,
                onNavigateToSettings = { navController.navigate(LumenDestinations.SETTINGS) },
                onNavigateToProfileCarousel = { navController.navigate(LumenDestinations.PROFILE_CAROUSEL) },
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
                onNavigateToDockSettings = { navController.navigate(LumenDestinations.DOCK_SETTINGS) },
                onNavigateToHomeAppsListSettings = { navController.navigate(LumenDestinations.HOME_APPS_LIST_SETTINGS) },
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
        composable(LumenDestinations.DOCK_SETTINGS) {
            DockSettingsScreen(
                onBack = { navController.popBackStackSafely() },
                onAddDockApp = { navController.navigate(LumenDestinations.DOCK_PICKER) },
            )
        }
        composable(LumenDestinations.HOME_APPS_LIST_SETTINGS) {
            HomeAppsListSettingsScreen(
                onBack = { navController.popBackStackSafely() },
                onEditDefaultFavorites = { navController.navigate(LumenDestinations.favoritesPicker()) },
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
        composable(LumenDestinations.DOCK_PICKER) {
            DockAppPickerScreen(onDone = { navController.popBackStackSafely() })
        }
        composable(
            LumenDestinations.PROFILE_CAROUSEL,
            // A translucent overlay on Home — fades/scales in place rather than sliding, so it
            // reads as sitting on top of Home (which stays put, thanks to the None branches in
            // the NavHost-level transitions above).
            enterTransition = { fadeIn(fadeSpec) + scaleIn(initialScale = 0.92f, animationSpec = fadeSpec) },
            exitTransition = { fadeOut(fadeSpec) + scaleOut(targetScale = 0.92f, animationSpec = fadeSpec) },
            popEnterTransition = { fadeIn(fadeSpec) + scaleIn(initialScale = 0.92f, animationSpec = fadeSpec) },
            popExitTransition = { fadeOut(fadeSpec) + scaleOut(targetScale = 0.92f, animationSpec = fadeSpec) },
        ) {
            ProfileCarouselScreen(
                // Applying a profile always lands on Home — clears everything above Home off the
                // back stack rather than just popping one level.
                onProfileApplied = {
                    navController.navigate(LumenDestinations.HOME) {
                        popUpTo(LumenDestinations.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onEditProfile = { profileId -> navController.navigate(LumenDestinations.profileSettings(profileId)) },
                onReorderProfiles = { navController.navigate(LumenDestinations.PROFILE_MANAGE) },
                onNavigateToSettings = { navController.navigate(LumenDestinations.SETTINGS) },
            )
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
                onNavigateToFavorites = { profileId -> navController.navigate(LumenDestinations.favoritesPicker(profileId)) },
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
