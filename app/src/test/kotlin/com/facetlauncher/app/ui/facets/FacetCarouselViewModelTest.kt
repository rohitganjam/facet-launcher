package com.facetlauncher.app.ui.facets

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.facetlauncher.app.data.AutomationStateRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.domain.ActivateFacetByIdUseCase
import com.facetlauncher.app.domain.RefreshAutomationStateUseCase
import com.facetlauncher.app.domain.DeleteFacetUseCase
import com.facetlauncher.app.domain.ObserveFacetPreviewsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class FacetCarouselViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `selectFacet activates the facet and records it as a manual switch`() = runTest(dispatcher) {
        // Given a second facet and a rule the evaluator has marked active
        val second = FacetEntity(id = 2L, name = "Work", position = 1)
        val facetRepository = mock(FacetRepository::class.java).also {
            `when`(it.observeFacets()).thenReturn(flowOf(emptyList()))
            `when`(it.getById(second.id)).thenReturn(second)
        }
        val settingsRepository = mock(SettingsRepository::class.java).also {
            `when`(it.settings).thenReturn(MutableStateFlow(LauncherSettings()))
        }
        val wallpaperRepository = mock(WallpaperRepository::class.java).also {
            `when`(it.currentHomeWallpaper()).thenReturn(HomeWallpaper.Unavailable)
        }
        val observeFacetPreviews = mock(ObserveFacetPreviewsUseCase::class.java).also {
            `when`(it()).thenReturn(flowOf(emptyMap()))
        }
        val automationState = AutomationStateRepository(
            PreferenceDataStoreFactory.create(scope = backgroundScope, produceFile = { tempFolder.newFile("automation-${System.nanoTime()}.preferences_pb") }),
        )
        automationState.update { it.copy(activeRuleIds = listOf(10L)) }
        val viewModel = FacetCarouselViewModel(
            facetRepository,
            settingsRepository,
            wallpaperRepository,
            DeleteFacetUseCase(facetRepository, mock(com.facetlauncher.app.data.widget.AppWidgetRepository::class.java)),
            observeFacetPreviews,
            ActivateFacetByIdUseCase(facetRepository, settingsRepository, automationState, mock(RefreshAutomationStateUseCase::class.java)),
        )

        // When the user picks it in the carousel
        viewModel.selectFacet(second.id)
        dispatcher.scheduler.advanceUntilIdle()

        // Then it becomes active, the baseline, and overrides the active rule
        verify(settingsRepository).setActiveFacetId(second.id)
        assertEquals(second.id, automationState.get().baselineFacetId)
        assertEquals(setOf(10L), automationState.get().suppressedRuleIds)
    }
}
