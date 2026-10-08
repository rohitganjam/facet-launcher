package com.facetlauncher.app.ui.facets

import android.appwidget.AppWidgetManager
import android.os.UserManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.model.FacetLimits
import com.facetlauncher.app.data.model.ProReason
import com.facetlauncher.app.data.widget.AppWidgetRepository
import com.facetlauncher.app.data.widget.LauncherAppWidgetHost
import com.facetlauncher.app.domain.ActivateFacetByIdUseCase
import com.facetlauncher.app.domain.AddFacetUseCase
import com.facetlauncher.app.domain.DeleteFacetUseCase
import com.facetlauncher.app.ui.FakeEntitlementRepository
import com.facetlauncher.app.ui.testAutomation
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ManageFacetsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        onBack: () -> Unit = {},
        onEditFacet: (Long) -> Unit = {},
        onFacetApply: () -> Unit = {},
        seed: suspend (FacetRepository, SettingsRepository) -> Unit = { _, _ -> },
        entitlement: EntitlementRepository = EntitlementRepository(),
        onOpenFacetPro: (ProReason) -> Unit = {},
    ): SettingsRepository {
        lateinit var settingsRepository: SettingsRepository
        composeRule.setContent {
            val context = LocalContext.current
            val viewModel = remember {
                val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
                val facetRepository = FacetRepository(database.facetDao())
                settingsRepository = SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.cacheDir, "manage-facets-test-${System.nanoTime()}.preferences_pb") },
                    ),
                )
                runBlocking { seed(facetRepository, settingsRepository) }
                val appRepository = AppRepository(
                    context.getSystemService(android.content.pm.LauncherApps::class.java),
                    context.getSystemService(UserManager::class.java),
                    context,
                )
                val appWidgetRepository = AppWidgetRepository(
                    context,
                    AppWidgetManager.getInstance(context),
                    LauncherAppWidgetHost(context),
                    context.getSystemService(UserManager::class.java),
                    appRepository,
                )
                ManageFacetsViewModel(
                    facetRepository,
                    settingsRepository,
                    DeleteFacetUseCase(facetRepository, appWidgetRepository),
                    testAutomation(context, database, facetRepository, settingsRepository, entitlement).activate,
                    AddFacetUseCase(facetRepository, entitlement),
                    entitlement,
                )
            }
            FacetLauncherTheme {
                ManageFacetsScreen(
                    onBack = onBack,
                    onEditFacet = onEditFacet,
                    onFacetApply = onFacetApply,
                    onOpenFacetPro = onOpenFacetPro,
                    viewModel = viewModel,
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("manage_facets_screen").fetchSemanticsNodes().isNotEmpty()
        }
        return settingsRepository
    }

    @Test
    fun listShowsTheFacetsTheHintAndTheAddRow() {
        setContent(
            seed = { facetRepository, _ ->
                facetRepository.addFacet()
                facetRepository.addFacet()
            },
        )

        composeRule.onNodeWithText("Manage Facets").assertExists()
        composeRule.onNodeWithText("Drag to re-order facets").assertExists()
        composeRule.onNodeWithText("Facet 1").assertExists()
        composeRule.onNodeWithText("Facet 2").assertExists()
        composeRule.onNodeWithTag("facet_reorder_add_row").assertExists()
    }

    @Test
    fun addRowIsHiddenOnceTheMaximumIsReached() {
        setContent(
            seed = { facetRepository, _ ->
                repeat(FacetLimits.PRO_MAX_FACETS) { facetRepository.addFacet() }
            },
        )

        composeRule.onNodeWithTag("facet_reorder_add_row").assertDoesNotExist()
    }

    @Test
    fun deleteIsDisabledWithOnlyOneFacetRemaining() {
        var onlyId = 0L
        setContent(
            seed = { facetRepository, _ -> onlyId = facetRepository.addFacet().id },
        )

        composeRule.onNodeWithTag("facet_reorder_menu_$onlyId").performClick()
        composeRule.onNode(hasText("Delete")).assertIsNotEnabled()
    }

    @Test
    fun deletingFromTheOverflowMenuRemovesTheRow() {
        var secondId = 0L
        setContent(
            seed = { facetRepository, _ ->
                facetRepository.addFacet()
                secondId = facetRepository.addFacet().id
            },
        )
        composeRule.onNodeWithText("Facet 2").assertExists()

        composeRule.onNodeWithTag("facet_reorder_menu_$secondId").performClick()
        composeRule.onNode(hasText("Delete")).performClick()
        composeRule.onNode(hasText("Delete")).performClick() // confirm dialog

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithText("Facet 2").assertDoesNotExist() }.isSuccess
        }
    }

    @Test
    fun facetSettingsFromTheOverflowMenuNavigatesToEditFacet() {
        var firstId = 0L
        var editedId = -1L
        setContent(
            onEditFacet = { editedId = it },
            seed = { facetRepository, _ ->
                firstId = facetRepository.addFacet().id
                facetRepository.addFacet()
            },
        )

        composeRule.onNodeWithTag("facet_reorder_menu_$firstId").performClick()
        composeRule.onNode(hasText("Facet settings")).performClick()

        assertEquals(firstId, editedId)
    }

    @Test
    fun tappingTheRowNavigatesToEditFacet() {
        // The drag handle and the "..." overflow menu are the only other interactive elements on
        // this row — tapping anywhere else on it should open that facet's own settings directly.
        var firstId = 0L
        var editedId = -1L
        setContent(
            onEditFacet = { editedId = it },
            seed = { facetRepository, _ ->
                firstId = facetRepository.addFacet().id
                facetRepository.addFacet()
            },
        )

        composeRule.onNodeWithTag("facet_reorder_row_$firstId").performClick()

        assertEquals(firstId, editedId)
    }

    @Test
    fun applyFacetFromTheOverflowMenuActivatesTheFacetAndNavigatesHome() {
        var secondId = 0L
        var applied = false
        val settingsRepository = setContent(
            onFacetApply = { applied = true },
            seed = { facetRepository, settingsRepository ->
                val first = facetRepository.addFacet()
                secondId = facetRepository.addFacet().id
                settingsRepository.setActiveFacetId(first.id)
            },
        )

        composeRule.onNodeWithTag("facet_reorder_menu_$secondId").performClick()
        composeRule.onNode(hasText("Apply facet")).performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runBlocking { settingsRepository.settings.first().activeFacetId == secondId }
        }
        assertEquals(true, applied)
    }

    @Test
    fun applyFacetIsDisabledForTheActiveFacet() {
        var firstId = 0L
        setContent(
            seed = { facetRepository, settingsRepository ->
                firstId = facetRepository.addFacet().id
                facetRepository.addFacet()
                settingsRepository.setActiveFacetId(firstId)
            },
        )

        composeRule.onNodeWithTag("facet_reorder_menu_$firstId").performClick()
        composeRule.onNode(hasText("Apply facet")).assertIsNotEnabled()
    }

    @Test
    fun backButtonInvokesTheCallback() {
        var backPressed = false
        setContent(
            onBack = { backPressed = true },
            seed = { facetRepository, _ -> facetRepository.addFacet() },
        )

        composeRule.onNodeWithTag("back_button").performClick()

        assertEquals(true, backPressed)
    }

    private fun freeWith(count: Int, onOpenFacetPro: (ProReason) -> Unit = {}) = setContent(
        onOpenFacetPro = onOpenFacetPro,
        seed = { facetRepository, settings ->
            repeat(count) { facetRepository.addFacet() }
            settings.setActiveFacetId(1L)
        },
        entitlement = FakeEntitlementRepository(initial = false),
    )

    @Test
    fun aFreeUsersFourthFacetIsLockedAndTappingItOpensTheUpgradeInsteadOfItsSettings() {
        var edited: Long? = null
        var opened: ProReason? = null
        setContent(
            onEditFacet = { edited = it },
            onOpenFacetPro = { opened = it },
            seed = { facetRepository, settings ->
                repeat(4) { facetRepository.addFacet() }
                settings.setActiveFacetId(1L)
            },
            entitlement = FakeEntitlementRepository(initial = false),
        )

        composeRule.onNodeWithTag("facet_reorder_lock_4", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("facet_reorder_lock_1", useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithTag("facet_reorder_row_4").performClick()

        assertEquals(ProReason.FACET_LOCKED, opened)
        assertEquals(null, edited)
    }

    @Test
    fun aProUserSeesNoLocks() {
        setContent(seed = { facetRepository, _ -> repeat(4) { facetRepository.addFacet() } })

        composeRule.onNodeWithTag("facet_reorder_lock_4", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun theAddRowStaysAtTheFreeLimitWithAProPillAndOpensTheUpgrade() {
        var opened: ProReason? = null
        freeWith(3) { opened = it }

        composeRule.onNodeWithTag("facet_reorder_add_row").assertExists().performClick()

        assertEquals(ProReason.FACET_LIMIT, opened)
        composeRule.onAllNodesWithText("Pro").assertCountEquals(1)
    }

    @Test
    fun aLockedFacetCannotBeAppliedOrOpenedButCanStillBeDeleted() {
        freeWith(4)

        composeRule.onNodeWithTag("facet_reorder_menu_4").performClick()
        composeRule.onNode(hasText("Facet settings")).assertIsNotEnabled()
        composeRule.onNode(hasText("Apply facet")).assertIsNotEnabled()
        composeRule.onNode(hasText("Delete")).assertIsEnabled().performClick()

        composeRule.onNodeWithTag("confirm_dialog").assertExists()
    }
}
