package com.facetlauncher.app.ui.navigation

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
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.ui.dock.DockAppPickerScreen
import com.facetlauncher.app.ui.facets.FacetSettingsScreen
import com.facetlauncher.app.ui.facets.FavoritesPickerScreen
import com.facetlauncher.app.ui.facets.ManageFacetsScreen
import com.facetlauncher.app.ui.home.clock.ClockStyleGalleryRoute
import com.facetlauncher.app.ui.home.clock.FacetClockStyleGalleryScreen
import com.facetlauncher.app.ui.launcher.HomeDrawerRoute
import com.facetlauncher.app.ui.launcher.LauncherViewModel
import com.facetlauncher.app.ui.pro.FacetProScreen
import com.facetlauncher.app.ui.settings.AboutScreen
import com.facetlauncher.app.ui.settings.AppDrawerSettingsScreen
import com.facetlauncher.app.ui.settings.AppearanceSettingsScreen
import com.facetlauncher.app.ui.settings.CalendarSettingsScreen
import com.facetlauncher.app.ui.settings.DockSettingsScreen
import com.facetlauncher.app.ui.settings.FolderAppPickerScreen
import com.facetlauncher.app.ui.settings.FolderDetailScreen
import com.facetlauncher.app.ui.settings.FoldersSettingsScreen
import com.facetlauncher.app.ui.settings.HomeAppsListSettingsScreen
import com.facetlauncher.app.ui.settings.NotificationAccessExplanationScreen
import com.facetlauncher.app.ui.settings.NotificationSettingsScreen
import com.facetlauncher.app.ui.settings.PermissionsScreen
import com.facetlauncher.app.ui.settings.SettingsScreen
import com.facetlauncher.app.ui.settings.UsageAccessExplanationScreen
import com.facetlauncher.app.ui.settings.automation.FacetAutomationScreen
import com.facetlauncher.app.ui.settings.backup.BackupRestoreScreen
import com.facetlauncher.app.ui.theme.FACET_TRANSITION_DURATION_MS
import com.facetlauncher.app.ui.theme.FacetTransitionEasing

object FacetDestinations {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val DOCK_PICKER = "dockPicker?facetId={facetId}"
    const val FACET_MANAGE = "facetManage"
    const val FACET_AUTOMATION = "facetAutomation"
    const val FACET_PRO = "facetPro/{reason}"
    const val FACET_SETTINGS = "facetSettings/{facetId}"
    const val FAVORITES_PICKER = "favoritesPicker?facetId={facetId}"
    const val CALENDAR_SETTINGS = "calendarSettings?facetId={facetId}"
    const val USAGE_ACCESS_EXPLANATION = "usageAccessExplanation"
    const val PERMISSIONS = "permissions"
    const val NOTIFICATION_SETTINGS = "notificationSettings"
    const val FOLDERS_SETTINGS = "foldersSettings"
    const val FOLDER_DETAIL = "folderDetail/{folderId}"
    const val FOLDER_APP_PICKER = "folderAppPicker/{folderId}"
    const val NOTIFICATION_ACCESS_EXPLANATION = "notificationAccessExplanation"
    const val CLOCK_STYLE_GALLERY = "clockStyleGallery"
    const val FACET_CLOCK_STYLE_GALLERY = "facetClockStyleGallery/{facetId}"
    const val BACKUP_RESTORE = "backupRestore"
    const val APPEARANCE_SETTINGS = "appearanceSettings?facetId={facetId}"
    const val DOCK_SETTINGS = "dockSettings?facetId={facetId}"
    const val HOME_APPS_LIST_SETTINGS = "homeAppsListSettings?facetId={facetId}"
    const val APP_DRAWER_SETTINGS = "appDrawerSettings"
    const val ABOUT = "about"

    fun facetClockStyleGallery(facetId: Long) = "facetClockStyleGallery/$facetId"

    fun facetSettings(facetId: Long) = "facetSettings/$facetId"

    fun facetPro(reason: ProReason) = "facetPro/${reason.name}"

    fun folderDetail(folderId: Long) = "folderDetail/$folderId"

    fun folderAppPicker(folderId: Long) = "folderAppPicker/$folderId"

    /** Omitting [facetId] (or passing [NO_ACTIVE_FACET_ID]) opens the launcher-wide default Favorites list. */
    fun favoritesPicker(facetId: Long? = null) =
        if (facetId != null && facetId != NO_ACTIVE_FACET_ID) "favoritesPicker?facetId=$facetId" else "favoritesPicker"

    /** Omitting [facetId] (or passing [NO_ACTIVE_FACET_ID]) opens the launcher-wide default dock. */
    fun dockPicker(facetId: Long? = null) =
        if (facetId != null && facetId != NO_ACTIVE_FACET_ID) "dockPicker?facetId=$facetId" else "dockPicker"

    /** Omitting [facetId] (or passing [NO_ACTIVE_FACET_ID]) opens the launcher-wide default Home apps list. */
    fun homeAppsListSettings(facetId: Long? = null) =
        if (facetId != null && facetId != NO_ACTIVE_FACET_ID) "homeAppsListSettings?facetId=$facetId" else "homeAppsListSettings"

    /** Omitting [facetId] (or passing [NO_ACTIVE_FACET_ID]) opens the launcher-wide default dock. */
    fun dockSettings(facetId: Long? = null) =
        if (facetId != null && facetId != NO_ACTIVE_FACET_ID) "dockSettings?facetId=$facetId" else "dockSettings"

    /** Omitting [facetId] (or passing [NO_ACTIVE_FACET_ID]) opens the global (non-facet-scoped) Appearance screen. */
    fun appearanceSettings(facetId: Long? = null) =
        if (facetId != null && facetId != NO_ACTIVE_FACET_ID) "appearanceSettings?facetId=$facetId" else "appearanceSettings"

    /** Omitting [facetId] (or passing [NO_ACTIVE_FACET_ID]) opens the global (non-facet-scoped) entry point. */
    fun calendarSettings(facetId: Long? = null) =
        if (facetId != null && facetId != NO_ACTIVE_FACET_ID) "calendarSettings?facetId=$facetId" else "calendarSettings"
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
 * Facets) stays a real `NavHost` destination, reached from Home the same way as before.
 */
@Composable
fun FacetNavHost(
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    launcherViewModel: LauncherViewModel = hiltViewModel(),
) {
    val animationSpec = tween<IntOffset>(durationMillis = FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)

    LaunchedEffect(launcherViewModel) {
        launcherViewModel.homePressedEvent.collect {
            navController.popBackStack(FacetDestinations.HOME, inclusive = false)
        }
    }

    NavHost(
        navController = navController,
        startDestination = FacetDestinations.HOME,
        modifier = modifier,
        // Pure slide, no crossfade: the window shows the wallpaper through any partially-transparent
        // layer, so a fading-out screen briefly reveals it — reads as a flash of Home. Two opaque
        // screens sliding past each other always cover the full width, so there's nothing to bleed.
        // (Switch Facets no longer lives here — it's a follow-finger panel inside HomeDrawerRoute
        // itself now, like the Hub; see chat history.)
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = animationSpec) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = animationSpec) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = animationSpec) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = animationSpec) },
        // Back gesture: without these Navigation defaults to a scale-down "zoom out" for every screen.
        predictivePopEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = animationSpec) },
        predictivePopExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = animationSpec) },
    ) {
        composable(FacetDestinations.HOME) {
            HomeDrawerRoute(
                apps = apps,
                onAppClick = onAppClick,
                onNavigateToSettings = { navController.navigate(FacetDestinations.SETTINGS) },
                onNavigateToFacetSettings = { facetId -> navController.navigate(FacetDestinations.facetSettings(facetId)) },
                onNavigateToManageFacets = { navController.navigate(FacetDestinations.FACET_MANAGE) },
                onNavigateToFacetPro = { reason -> navController.navigate(FacetDestinations.facetPro(reason)) },
                onNavigateToUsageAccessExplanation = {
                    navController.navigate(FacetDestinations.USAGE_ACCESS_EXPLANATION)
                },
                onNavigateToClockStyleGallery = { facetId ->
                    val dest = if (facetId != null) FacetDestinations.facetClockStyleGallery(facetId) else FacetDestinations.CLOCK_STYLE_GALLERY
                    navController.navigate(dest)
                },
                launcherViewModel = launcherViewModel,
            )
        }
        composable(FacetDestinations.USAGE_ACCESS_EXPLANATION) {
            UsageAccessExplanationScreen(onBack = { navController.popBackStackSafely() })
        }
        composable(FacetDestinations.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStackSafely() },
                onViewFacets = { navController.navigate(FacetDestinations.FACET_MANAGE) },
                onViewFacetAutomation = { navController.navigate(FacetDestinations.FACET_AUTOMATION) },
                onViewFacetPro = { navController.navigate(FacetDestinations.facetPro(ProReason.ABOUT)) },
                onNavigateToAppearance = { navController.navigate(FacetDestinations.appearanceSettings()) },
                onNavigateToCalendarSettings = { navController.navigate(FacetDestinations.calendarSettings()) },
                onNavigateToDockSettings = { navController.navigate(FacetDestinations.dockSettings()) },
                onNavigateToHomeAppsListSettings = { navController.navigate(FacetDestinations.homeAppsListSettings()) },
                onNavigateToAppDrawerSettings = { navController.navigate(FacetDestinations.APP_DRAWER_SETTINGS) },
                onNavigateToNotificationSettings = { navController.navigate(FacetDestinations.NOTIFICATION_SETTINGS) },
                onNavigateToFolders = { navController.navigate(FacetDestinations.FOLDERS_SETTINGS) },
                onNavigateToPermissions = { navController.navigate(FacetDestinations.PERMISSIONS) },
                onNavigateToBackupRestore = { navController.navigate(FacetDestinations.BACKUP_RESTORE) },
                onNavigateToAbout = { navController.navigate(FacetDestinations.ABOUT) },
            )
        }
        composable(FacetDestinations.BACKUP_RESTORE) {
            BackupRestoreScreen(onBack = { navController.popBackStackSafely() })
        }
        composable(FacetDestinations.ABOUT) {
            AboutScreen(onBack = { navController.popBackStackSafely() })
        }
        composable(
            FacetDestinations.APPEARANCE_SETTINGS,
            arguments = listOf(navArgument("facetId") { type = NavType.LongType; defaultValue = NO_ACTIVE_FACET_ID }),
        ) { backStackEntry ->
            val facetId = backStackEntry.arguments?.getLong("facetId") ?: NO_ACTIVE_FACET_ID
            AppearanceSettingsScreen(
                onBack = { navController.popBackStackSafely() },
                onNavigateToClockStyleGallery = {
                    val dest = if (facetId != NO_ACTIVE_FACET_ID) FacetDestinations.facetClockStyleGallery(facetId) else FacetDestinations.CLOCK_STYLE_GALLERY
                    navController.navigate(dest)
                },
            )
        }
        composable(
            FacetDestinations.DOCK_SETTINGS,
            arguments = listOf(navArgument("facetId") { type = NavType.LongType; defaultValue = NO_ACTIVE_FACET_ID }),
        ) { backStackEntry ->
            val facetId = backStackEntry.arguments?.getLong("facetId")?.takeIf { it != NO_ACTIVE_FACET_ID }
            DockSettingsScreen(
                onBack = { navController.popBackStackSafely() },
                onAddDockApp = { navController.navigate(FacetDestinations.dockPicker(facetId)) },
            )
        }
        composable(
            FacetDestinations.HOME_APPS_LIST_SETTINGS,
            arguments = listOf(navArgument("facetId") { type = NavType.LongType; defaultValue = NO_ACTIVE_FACET_ID }),
        ) { backStackEntry ->
            val facetId = backStackEntry.arguments?.getLong("facetId")?.takeIf { it != NO_ACTIVE_FACET_ID }
            HomeAppsListSettingsScreen(
                onBack = { navController.popBackStackSafely() },
                onEditFavorites = { navController.navigate(FacetDestinations.favoritesPicker(facetId)) },
            )
        }
        composable(FacetDestinations.APP_DRAWER_SETTINGS) {
            AppDrawerSettingsScreen(onBack = { navController.popBackStackSafely() })
        }
        composable(FacetDestinations.CLOCK_STYLE_GALLERY) {
            ClockStyleGalleryRoute(onBack = { navController.popBackStackSafely() })
        }
        composable(
            FacetDestinations.FACET_CLOCK_STYLE_GALLERY,
            arguments = listOf(navArgument("facetId") { type = NavType.LongType }),
        ) {
            FacetClockStyleGalleryScreen(onBack = { navController.popBackStackSafely() })
        }
        composable(FacetDestinations.PERMISSIONS) {
            PermissionsScreen(
                onBack = { navController.popBackStackSafely() },
                onNavigateToUsageAccessExplanation = { navController.navigate(FacetDestinations.USAGE_ACCESS_EXPLANATION) },
                onNavigateToNotificationAccessExplanation = {
                    navController.navigate(FacetDestinations.NOTIFICATION_ACCESS_EXPLANATION)
                },
            )
        }
        composable(FacetDestinations.NOTIFICATION_SETTINGS) {
            NotificationSettingsScreen(
                onBack = { navController.popBackStackSafely() },
                onNavigateToNotificationAccessExplanation = {
                    navController.navigate(FacetDestinations.NOTIFICATION_ACCESS_EXPLANATION)
                },
            )
        }
        composable(FacetDestinations.NOTIFICATION_ACCESS_EXPLANATION) {
            NotificationAccessExplanationScreen(onBack = { navController.popBackStackSafely() })
        }
        composable(FacetDestinations.FOLDERS_SETTINGS) {
            FoldersSettingsScreen(
                onBack = { navController.popBackStackSafely() },
                onOpenFolder = { folderId -> navController.navigate(FacetDestinations.folderDetail(folderId)) },
            )
        }
        composable(
            FacetDestinations.FOLDER_DETAIL,
            arguments = listOf(navArgument("folderId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val folderId = checkNotNull(backStackEntry.arguments?.getLong("folderId"))
            FolderDetailScreen(
                onBack = { navController.popBackStackSafely() },
                onAddToFolder = { navController.navigate(FacetDestinations.folderAppPicker(folderId)) },
            )
        }
        composable(
            FacetDestinations.FOLDER_APP_PICKER,
            arguments = listOf(navArgument("folderId") { type = NavType.LongType }),
        ) {
            FolderAppPickerScreen(
                onDone = { navController.popBackStackSafely() },
                onNavigateToUsageAccessExplanation = { navController.navigate(FacetDestinations.USAGE_ACCESS_EXPLANATION) },
            )
        }
        composable(
            FacetDestinations.DOCK_PICKER,
            arguments = listOf(navArgument("facetId") { type = NavType.LongType; defaultValue = NO_ACTIVE_FACET_ID }),
        ) {
            DockAppPickerScreen(
                onDone = { navController.popBackStackSafely() },
                onNavigateToUsageAccessExplanation = { navController.navigate(FacetDestinations.USAGE_ACCESS_EXPLANATION) },
            )
        }
        composable(FacetDestinations.FACET_AUTOMATION) {
            FacetAutomationScreen(
                onBack = { navController.popBackStackSafely() },
                onOpenFacetPro = { reason -> navController.navigate(FacetDestinations.facetPro(reason)) },
            )
        }
        composable(
            FacetDestinations.FACET_PRO,
            arguments = listOf(navArgument("reason") { type = NavType.StringType }),
        ) { entry ->
            val reason = entry.arguments?.getString("reason")?.let { name -> ProReason.entries.firstOrNull { it.name == name } } ?: ProReason.ABOUT
            FacetProScreen(
                reason = reason,
                onBack = { navController.popBackStackSafely() },
                onAddFacet = { navController.navigate(FacetDestinations.FACET_MANAGE) },
            )
        }
        composable(FacetDestinations.FACET_MANAGE) {
            // Settings → Facets (and the carousel's Reorder button): a normal settings screen.
            ManageFacetsScreen(
                onBack = { navController.popBackStackSafely() },
                onEditFacet = { facetId -> navController.navigate(FacetDestinations.facetSettings(facetId)) },
                onFacetApply = { navController.popBackStack(FacetDestinations.HOME, inclusive = false) },
                onOpenFacetPro = { reason -> navController.navigate(FacetDestinations.facetPro(reason)) },
                onOpenFacetAutomation = { navController.navigate(FacetDestinations.FACET_AUTOMATION) },
            )
        }
        composable(
            FacetDestinations.FACET_SETTINGS,
            arguments = listOf(navArgument("facetId") { type = NavType.LongType }),
        ) {
            FacetSettingsScreen(
                onBack = { navController.popBackStackSafely() },
                onNavigateToAppsList = { facetId -> navController.navigate(FacetDestinations.homeAppsListSettings(facetId)) },
                onNavigateToDockSettings = { facetId -> navController.navigate(FacetDestinations.dockSettings(facetId)) },
                onNavigateToCalendarSettings = { facetId ->
                    navController.navigate(FacetDestinations.calendarSettings(facetId))
                },
                onNavigateToAppearance = { facetId ->
                    navController.navigate(FacetDestinations.appearanceSettings(facetId))
                },
                onFacetApply = { navController.popBackStack(FacetDestinations.HOME, inclusive = false) },
            )
        }
        composable(
            FacetDestinations.FAVORITES_PICKER,
            arguments = listOf(navArgument("facetId") { type = NavType.LongType; defaultValue = NO_ACTIVE_FACET_ID }),
        ) {
            FavoritesPickerScreen(
                onDone = { navController.popBackStackSafely() },
                onNavigateToUsageAccessExplanation = { navController.navigate(FacetDestinations.USAGE_ACCESS_EXPLANATION) },
            )
        }
        composable(
            FacetDestinations.CALENDAR_SETTINGS,
            arguments = listOf(navArgument("facetId") { type = NavType.LongType; defaultValue = NO_ACTIVE_FACET_ID }),
        ) {
            CalendarSettingsScreen(onBack = { navController.popBackStackSafely() })
        }
    }
}
