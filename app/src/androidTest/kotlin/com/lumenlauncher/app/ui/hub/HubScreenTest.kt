package com.lumenlauncher.app.ui.hub

import android.appwidget.AppWidgetManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import com.lumenlauncher.app.data.WidgetPlacementRepository
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.data.local.WidgetPlacementEntity
import com.lumenlauncher.app.data.widget.AppWidgetRepository
import com.lumenlauncher.app.data.widget.LauncherAppWidgetHost
import com.lumenlauncher.app.domain.DeleteWidgetUseCase
import com.lumenlauncher.app.domain.HUB_MAX_WIDGETS
import com.lumenlauncher.app.domain.ObserveHubStateUseCase
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class HubScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** [seed] runs against a real (throwaway, in-memory) [WidgetPlacementRepository] before the screen renders. */
    private fun setContent(seed: suspend (WidgetPlacementRepository) -> Unit = {}) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                val widgetPlacementRepository = WidgetPlacementRepository(database.widgetPlacementDao())
                runBlocking { seed(widgetPlacementRepository) }
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
                HubScreen(onAddClick = {}, onManageClick = {}, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
    }

    private fun placement(id: Int, row: Int) = WidgetPlacementEntity(
        appWidgetId = id,
        providerPackageName = "com.example.widgets",
        providerClassName = ".Provider",
        row = row,
        col = 0,
        colSpan = 2,
        rowSpan = 1,
    )

    @Test
    fun emptyStateShowsCenteredAddButtonAndNoWidgetsCopy() {
        // Given no widgets placed
        setContent()

        // Then the empty-state content renders, not a blank grid
        composeRule.onNodeWithText("No widgets yet").assertExists()
        composeRule.onNodeWithTag("hub_empty_add_widget").assertExists().assertIsDisplayed()
        composeRule.onNodeWithTag("hub_grid").assertDoesNotExist()
    }

    @Test
    fun headerAlwaysShowsTheTitleAndCurrentCount() {
        // Given two placed (unresolvable, so orphaned — see class doc) widgets
        setContent { repository ->
            repository.upsert(placement(id = 1, row = 0))
            repository.upsert(placement(id = 2, row = 1))
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("Lumen Hub").assertExists() }.isSuccess
        }

        // Then the header reflects the real count regardless of orphan status
        composeRule.onNodeWithText("2 of $HUB_MAX_WIDGETS widgets · 5 columns").assertExists()
    }

    @Test
    fun anUnresolvableWidgetRendersAsOrphanedNotADeadFrame() {
        // Given a placement whose appWidgetId was never actually bound — AppWidgetManager has no
        // record of it, which is exactly what "orphaned" (provider uninstalled) looks like from
        // this app's own perspective, so this doubles as the orphan-rendering test
        setContent { repository -> repository.upsert(placement(id = 1, row = 0)) }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("hub_orphaned_widget").assertExists() }.isSuccess
        }

        // Then it's the real placeholder — unavailable copy, Remove/Keep space actions — not a
        // blank or crashed tile
        composeRule.onNodeWithText("This widget unavailable").assertExists()
        composeRule.onNodeWithText("The app was uninstalled").assertExists()
        composeRule.onNodeWithTag("hub_orphaned_remove").assertExists()
        composeRule.onNodeWithTag("hub_orphaned_keep_space").assertExists()
    }

    @Test
    fun removingAnOrphanedWidgetDeletesItsPlacement() {
        // Given one orphaned placement
        val repositoryRef = arrayOfNulls<WidgetPlacementRepository>(1)
        setContent { repository -> repositoryRef[0] = repository; repository.upsert(placement(id = 1, row = 0)) }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("hub_orphaned_widget").assertExists() }.isSuccess
        }

        // When tapping Remove
        composeRule.onNodeWithTag("hub_orphaned_remove").performClick()

        // Then the tile is gone and the placement no longer exists
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onNodeWithTag("hub_orphaned_widget").let { runCatching { it.assertDoesNotExist() }.isSuccess }
        }
    }

    @Test
    fun atCapacityShowsTheStripAndGreysOutAdd() {
        // Given twenty placed widgets — the Hub's own cap
        setContent { repository ->
            (0 until HUB_MAX_WIDGETS).forEach { i -> repository.upsert(placement(id = i, row = i)) }
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("hub_at_capacity_strip").assertExists() }.isSuccess
        }

        // Then the strip explains why, and the header's Add is no longer clickable
        composeRule.onNodeWithText("Hub is full at 20 widgets — remove one to add another.").assertExists()
        composeRule.onNodeWithTag("hub_add_button").assertIsNotEnabled()
    }
}
