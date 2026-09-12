package com.facetlauncher.app.ui.facets

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetDatabase
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
        onFacetApplied: () -> Unit = {},
        seed: suspend (FacetRepository, SettingsRepository) -> Unit = { _, _ -> },
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
                ManageFacetsViewModel(facetRepository, settingsRepository)
            }
            FacetLauncherTheme {
                ManageFacetsScreen(
                    onBack = onBack,
                    onEditFacet = onEditFacet,
                    onFacetApplied = onFacetApplied,
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
                repeat(FacetRepository.MAX_FACETS) { facetRepository.addFacet() }
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
    fun applyFacetFromTheOverflowMenuActivatesTheFacetAndNavigatesHome() {
        var secondId = 0L
        var applied = false
        val settingsRepository = setContent(
            onFacetApplied = { applied = true },
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
}
