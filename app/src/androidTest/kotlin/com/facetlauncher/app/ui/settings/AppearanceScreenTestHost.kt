package com.facetlauncher.app.ui.settings

import android.content.pm.LauncherApps
import android.os.UserManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.facetlauncher.app.data.PlacementRepository

/** Builds the real [AppearanceSettingsViewModel] over an in-memory DB and a throwaway DataStore, and hosts whichever Appearance screen a test hands it. Shared by the three Appearance screens' tests. */
internal class AppearanceScreenTestHost(private val composeRule: ComposeContentTestRule) {


    /** Fakes calendar *data* — no real Calendar Provider reads. Mirrors `CalendarSettingsScreenTest`'s own local `FakeCalendarRepository`, just overriding `getUpcomingEvents` instead of `getCalendars`. */
    private class FakeCalendarRepository(
        contentResolver: android.content.ContentResolver,
        private val events: List<CalendarEvent>,
    ) : CalendarRepository(contentResolver) {
        override suspend fun getUpcomingEvents(calendarIds: Set<String>?, includeAllDay: Boolean): List<CalendarEvent> = events
    }

    /**
     * [facetId] non-null builds a facet-scoped screen — seeds a real facet with that id in the
     * same in-memory DB. [seed] runs against [FacetRepository] before the screen renders (facet
     * override flags etc.); [seedApps] runs against the real favorite/dock repositories, given the
     * device's own real installed apps — mirrors `FacetCarouselScreenTest`'s own `seed`/`seedApps`
     * split. Real installed apps, not a synthetic [AppInfo], are required here:
     * [DefaultFavoriteAppRepository]/[DockAppRepository] hydrate every stored row against
     * [AppRepository]'s live installed-app list (same uninstall-collapse pattern as Home itself),
     * so a fake package name is silently filtered out of what the preview ever renders — the
     * preview must reflect this scope's real, actual content, not mock/sample data (see chat
     * history). [calendarGranted]/[calendarEvents] fake the calendar permission grant and its
     * data via [FakeCalendarPermissionRepository]/[FakeCalendarRepository] — no real OS-level
     * grant needed (see that fake's own doc for why).
     */
    lateinit var screenViewModel: AppearanceSettingsViewModel

    /** The `facets` flow is a real Room query, so a facet-scoped screen first renders unscoped until it emits. */
    fun awaitFacetScoped() {
        composeRule.waitUntil(timeoutMillis = 5_000) { screenViewModel.uiState.value.isFacetScoped }
        composeRule.waitForIdle()
    }

    fun setContent(
        facetId: Long? = null,
        calendarGranted: Boolean = false,
        notificationAccessGranted: Boolean = true,
        calendarEvents: List<CalendarEvent> = emptyList(),
        seed: suspend (FacetRepository, Long?) -> Unit = { _, _ -> },
        seedApps: suspend (AppRepository, PlacementRepository, Long?) -> Unit = { _, _, _ -> },
        screen: @Composable (AppearanceSettingsViewModel) -> Unit,
    ): SettingsRepository {
        lateinit var settingsRepository: SettingsRepository
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "appearance-settings-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                val appRepository = AppRepository(context.getSystemService(LauncherApps::class.java), context.getSystemService(UserManager::class.java), context)
                val wallpaperRepository = com.facetlauncher.app.data.WallpaperRepository(
                    android.app.WallpaperManager.getInstance(context),
                )
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val facetRepository = FacetRepository(database.facetDao())
                val folderRepository = FolderRepository(database.folderDao(), appRepository)
                val placementRepository = PlacementRepository(database.appPlacementDao(), database.folderPlacementDao(), folderRepository, appRepository)
                if (facetId != null) {
                    runBlocking { database.facetDao().insert(com.facetlauncher.app.data.local.FacetEntity(id = facetId, name = "P", position = 0)) }
                }
                runBlocking { seed(facetRepository, facetId) }
                runBlocking { seedApps(appRepository, placementRepository, facetId) }
                AppearanceSettingsViewModel(
                    SavedStateHandle(facetId?.let { mapOf("facetId" to it) } ?: emptyMap()),
                    settingsRepository,
                    facetRepository,
                    placementRepository,
                    FakeCalendarRepository(context.contentResolver, calendarEvents),
                    FakeCalendarPermissionRepository(context, granted = calendarGranted),
                    wallpaperRepository,
                    FakeNotificationAccessRepository(context, granted = notificationAccessGranted),
                ).also { screenViewModel = it }
            }
            FacetLauncherTheme { screen(viewModel) }
        }
        return settingsRepository
    }
}
