package com.lumenlauncher.app.ui.dock

import android.content.pm.LauncherApps
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import com.lumenlauncher.app.data.AppRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.local.LumenDatabase
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.domain.GetInstalledAppsUseCase
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class DockAppPickerScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** [seed] runs against real (throwaway, in-memory) repositories before the screen renders. */
    private fun setContent(onDone: () -> Unit = {}, seed: (AppRepository, DockAppRepository) -> Unit = { _, _ -> }) {
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, LumenDatabase::class.java).allowMainThreadQueries().build()
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                val appRepository = AppRepository(launcherApps)
                val dockAppRepository = DockAppRepository(database.dockAppDao(), appRepository)
                seed(appRepository, dockAppRepository)
                DockAppPickerViewModel(GetInstalledAppsUseCase(appRepository), dockAppRepository)
            }
            LumenLauncherTheme {
                DockAppPickerScreen(onDone = onDone, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun backButtonInvokesOnDone() {
        // Given the picker
        var doneInvoked = false
        setContent(onDone = { doneInvoked = true })

        // When tapping the back button
        composeRule.onNodeWithTag("back_button").performClick()

        // Then it navigates back, same as tapping "Done"
        assert(doneInvoked)
    }

    @Test
    fun checkingAnAppAddsItToTheDock() {
        // Given the picker, searched down to some real installed app (fetched from the same
        // repository the ViewModel uses, rather than assuming a specific package is visible —
        // package-visibility rules can differ between this test APK and the app under test)
        var targetApp: AppInfo? = null
        setContent { appRepository, _ -> targetApp = runBlocking { appRepository.getInstalledApps() }.first() }
        val app = requireNotNull(targetApp)
        val rowTag = "dock_picker_row_${app.packageName}"

        // The ViewModel's installed-apps list comes from a real suspend fetch (init { launch { ... } }),
        // which isn't reliably caught by a single waitForIdle() — poll for the row before interacting
        // (same async-I/O-vs-idling gap as elsewhere, see IMPLEMENTATION_PLAN.md)
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("dock_picker_search").performTextInput(app.label)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(rowTag).assertIsOff()

        // When it's checked
        composeRule.onNodeWithTag(rowTag).performClick()

        // Then its checkbox reflects the new dock membership
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertIsOn() }.isSuccess
        }
    }

    @Test
    fun headerStaysVisibleAfterScrollingToAllApps() {
        // Given two real apps already in the dock, so both "IN DOCK" and "ALL APPS" render
        setContent { appRepository, dockAppRepository ->
            val installed = runBlocking { appRepository.getInstalledApps() }
            installed.take(2).forEachIndexed { index, app -> runBlocking { dockAppRepository.addDockApp(app, index) } }
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("ALL APPS").assertExists() }.isSuccess
        }

        // When scrolling down to the "ALL APPS" section
        composeRule.onNodeWithText("ALL APPS").performScrollTo()

        // Then the pinned header (title + Done) is still on screen, not scrolled away
        composeRule.onNodeWithText("Dock").assertIsDisplayed()
        composeRule.onNodeWithTag("dock_picker_done").assertIsDisplayed()
    }

    @Test
    fun inDockAppsRenderFirstUnderTheirOwnSectionHeader() {
        // Given two real apps already in the dock
        var dockApps: List<AppInfo> = emptyList()
        setContent { appRepository, dockAppRepository ->
            val installed = runBlocking { appRepository.getInstalledApps() }
            dockApps = installed.take(2)
            dockApps.forEachIndexed { index, app -> runBlocking { dockAppRepository.addDockApp(app, index) } }
        }

        // Then both section headers render, with the in-dock apps' rows present under "IN DOCK"
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("IN DOCK").assertExists() }.isSuccess
        }
        composeRule.onNodeWithText("ALL APPS").assertExists()
        dockApps.forEach { app ->
            composeRule.onNodeWithTag("dock_picker_row_${app.packageName}").assertIsOn()
        }
    }

    @Test
    fun uncheckingIsAllowedDownToAnEmptyDock() {
        // Given a dock with a single app — no floor blocks removing it (MIN_APPS = 0)
        var onlyDockApp: AppInfo? = null
        setContent { appRepository, dockAppRepository ->
            val app = runBlocking { appRepository.getInstalledApps() }.first()
            onlyDockApp = app
            runBlocking { dockAppRepository.addDockApp(app, 0) }
        }
        val app = requireNotNull(onlyDockApp)
        val rowTag = "dock_picker_row_${app.packageName}"

        // See checkingAnAppAddsItToTheDock — wait for the real installed-apps fetch to land
        // before interacting, rather than trusting a single waitForIdle() caught it.
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("dock_picker_search").performTextInput(app.label)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(rowTag).assertIsOn()

        // When unchecking the dock's only remaining app
        composeRule.onNodeWithTag(rowTag).performClick()

        // Then it's removed, leaving the dock empty
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag(rowTag).assertIsOff() }.isSuccess
        }
    }
}
