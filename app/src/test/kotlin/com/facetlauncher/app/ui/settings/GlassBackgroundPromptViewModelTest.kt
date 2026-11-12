package com.facetlauncher.app.ui.settings

import android.app.WallpaperManager
import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class GlassBackgroundPromptViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Before
    fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private class FakeCopies(val stored: MutableStateFlow<Bitmap?>) : WallpaperCopyRepository(RuntimeEnvironment.getApplication(), mock(WallpaperManager::class.java)) {
        var systemId = 1
        override val copy: Flow<Bitmap?> get() = stored
        override fun currentSystemWallpaperId() = systemId
    }

    private fun settings(): SettingsRepository {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(produceFile = { tempFolder.newFile("t-${System.nanoTime()}.preferences_pb") })
        return SettingsRepository(dataStore)
    }

    private fun GlassBackgroundPromptViewModel.observe(scope: TestScope) =
        scope.backgroundScope.launch(UnconfinedTestDispatcher(scope.testScheduler)) { needsCopy.collect { } }

    private val bitmap get() = Bitmap.createBitmap(5, 5, Bitmap.Config.ARGB_8888)

    @Test
    fun `with no copy and no earlier refusal the prompt is needed`() = runTest {
        // Given no copy and nothing dismissed
        val viewModel = GlassBackgroundPromptViewModel(FakeCopies(MutableStateFlow(null)), settings())
        viewModel.observe(this)

        // Then a copy is needed
        assertTrue(viewModel.needsCopy.first { it })
    }

    @Test
    fun `with a copy that matches the wallpaper nothing is needed`() = runTest {
        // Given a copy stored for the current wallpaper
        val settings = settings().also { it.setWallpaperCopySystemId(1) }
        val viewModel = GlassBackgroundPromptViewModel(FakeCopies(MutableStateFlow(bitmap)), settings)
        viewModel.observe(this)
        settings.settings.first { it.wallpaperCopySystemId == 1 }

        // Then no copy is needed
        assertFalse(viewModel.needsCopy.value)
    }

    @Test
    fun `a stale copy needs replacing once the launcher resumes`() = runTest {
        // Given a copy stored for wallpaper 1
        val copies = FakeCopies(MutableStateFlow(bitmap))
        val settings = settings().also { it.setWallpaperCopySystemId(1) }
        val viewModel = GlassBackgroundPromptViewModel(copies, settings)
        viewModel.observe(this)

        // When the wallpaper changes and the launcher resumes
        copies.systemId = 2
        viewModel.onResumed()

        // Then a copy is needed again
        assertTrue(viewModel.needsCopy.first { it })
    }

    @Test
    fun `a copy stored for a wallpaper that was just set is not stale before the launcher resumes`() = runTest {
        // Given a copy for wallpaper 1 and a view model that has already read that
        val copies = FakeCopies(MutableStateFlow(bitmap))
        val settings = settings().also { it.setWallpaperCopySystemId(1) }
        val viewModel = GlassBackgroundPromptViewModel(copies, settings)
        viewModel.observe(this)
        settings.settings.first { it.wallpaperCopySystemId == 1 }
        assertFalse(viewModel.needsCopy.value)

        // When Facet sets a new wallpaper (the system id changes) and stores the copy for it, with no resume in between
        copies.systemId = 2
        settings.setWallpaperCopySystemId(2)
        settings.settings.first { it.wallpaperCopySystemId == 2 }

        // Then the new copy counts as current, so there is still nothing to ask for
        assertFalse(viewModel.needsCopy.value)
    }

    @Test
    fun `declining is remembered`() = runTest {
        // Given a prompt that is needed
        val settings = settings()
        val viewModel = GlassBackgroundPromptViewModel(FakeCopies(MutableStateFlow(null)), settings)
        viewModel.observe(this)
        viewModel.needsCopy.first { it }

        // When declined
        viewModel.decline()

        // Then it is no longer needed and the refusal is stored
        assertFalse(viewModel.needsCopy.first { !it })
        assertTrue(settings.settings.first { it.wallpaperPromptDismissed }.wallpaperPromptDismissed)
    }
}
