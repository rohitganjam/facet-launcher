package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.data.LicenseRepository
import com.facetlauncher.app.data.model.LicenseCatalog
import com.facetlauncher.app.data.model.LicenseSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class LicenseTextViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `a known id loads its licence text`() = runTest {
        // Given a repository that returns text for the GeoNames entry
        val sections = listOf(LicenseSection(heading = null, text = "Attribution 4.0 International"))
        val repository = mock(LicenseRepository::class.java)
        `when`(repository.sections(LicenseCatalog.find("geonames")!!)).thenReturn(sections)
        val viewModel = LicenseTextViewModel(SavedStateHandle(mapOf("licenseId" to "geonames")), repository)

        // Then before anything is collected the state is the loading default
        assertTrue(viewModel.uiState.value.isLoading)

        // When collected
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then it carries the entry and its text
        val state = viewModel.uiState.value
        assertEquals("geonames", state.info?.id)
        assertEquals(sections, state.sections)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `an unknown id loads nothing`() = runTest {
        // Given an id that is not in the catalogue
        val viewModel = LicenseTextViewModel(SavedStateHandle(mapOf("licenseId" to "nope")), mock(LicenseRepository::class.java))

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then there is no entry, no text, and loading has ended
        val state = viewModel.uiState.value
        assertNull(state.info)
        assertTrue(state.sections.isEmpty())
        assertEquals(false, state.isLoading)
    }
}
