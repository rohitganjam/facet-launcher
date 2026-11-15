package com.facetlauncher.app.ui.launcher

import android.app.WallpaperManager
import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.WallpaperPaletteRepository
import com.facetlauncher.app.data.model.WallpaperPalette
import com.facetlauncher.app.data.wallpaperPaletteFrom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WallpaperBackdropViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Before
    fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private class FakeCopies(copy: Flow<Bitmap?>) : WallpaperCopyRepository(org.robolectric.RuntimeEnvironment.getApplication(), mock(WallpaperManager::class.java)) {
        var systemId = 1
        override val copy: Flow<Bitmap?> = copy
        override fun currentSystemWallpaperId() = systemId
    }

    /** Computes a palette for any copy and none without one, and counts how often it was asked. */
    private class FakePalette : WallpaperPaletteRepository(mock(WallpaperManager::class.java), Dispatchers.Unconfined) {
        val changes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        var requests = 0
        override suspend fun paletteFor(copy: Bitmap?): WallpaperPalette? {
            requests++
            return copy?.let { wallpaperPaletteFrom(0xFF2563EB.toInt()) }
        }
        override val systemColorChanges: Flow<Unit> = changes
    }

    private fun settings(): SettingsRepository {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(produceFile = { tempFolder.newFile("t-${System.nanoTime()}.preferences_pb") })
        return SettingsRepository(dataStore)
    }

    private fun WallpaperBackdropViewModel.observe(scope: kotlinx.coroutines.test.TestScope) =
        scope.backgroundScope.launch(UnconfinedTestDispatcher(scope.testScheduler)) { copy.collect { } }

    @Test
    fun `there is no copy until the repository has one`() = runTest {
        // Given a repository with no stored copy
        val viewModel = WallpaperBackdropViewModel(FakeCopies(MutableStateFlow(null)), settings(), FakePalette())

        // Then the view model shows none
        assertNull(viewModel.copy.value)
    }

    @Test
    fun `the copy follows the repository as it is stored and removed`() = runTest {
        // Given a repository whose copy can change and a copy recorded for the current system wallpaper
        val stored = MutableStateFlow<Bitmap?>(null)
        val settings = settings().also { it.setWallpaperCopySystemId(1) }
        val viewModel = WallpaperBackdropViewModel(FakeCopies(stored), settings, FakePalette())
        viewModel.observe(this)
        val bitmap = Bitmap.createBitmap(10, 20, Bitmap.Config.ARGB_8888)

        // When a copy is stored
        stored.value = bitmap

        // Then the view model shows that same bitmap
        assertSame(bitmap, viewModel.copy.first { it != null })

        // And when it is removed, none
        stored.value = null
        assertNull(viewModel.copy.first { it == null })
    }

    @Test
    fun `a copy stored for a wallpaper that was just set keeps showing before the launcher resumes`() = runTest {
        // Given a copy for wallpaper 1 being shown
        val copies = FakeCopies(MutableStateFlow(Bitmap.createBitmap(10, 20, Bitmap.Config.ARGB_8888)))
        val settings = settings().also { it.setWallpaperCopySystemId(1) }
        val viewModel = WallpaperBackdropViewModel(copies, settings, FakePalette())
        viewModel.observe(this)
        assertNotNull(viewModel.copy.first { it != null })

        // When Facet sets a new wallpaper and stores the copy for it, with no resume in between
        copies.systemId = 2
        settings.setWallpaperCopySystemId(2)
        settings.settings.first { it.wallpaperCopySystemId == 2 }

        // Then the copy is still shown
        assertNotNull(viewModel.copy.value)
    }

    @Test
    fun `a copy is dropped once the system wallpaper has changed since it was stored`() = runTest {
        // Given a copy stored when the system id was 1
        val copies = FakeCopies(MutableStateFlow(Bitmap.createBitmap(10, 20, Bitmap.Config.ARGB_8888)))
        val settings = settings().also { it.setWallpaperCopySystemId(1) }
        val viewModel = WallpaperBackdropViewModel(copies, settings, FakePalette())
        viewModel.observe(this)
        assertNotNull(viewModel.copy.first { it != null })

        // When the system wallpaper changes and the launcher resumes
        copies.systemId = 2
        viewModel.onResumed()

        // Then glass falls back to the tint: no copy
        assertNull(viewModel.copy.first { it == null })
    }

    @Test
    fun `the palette follows the copy and Android's colour changes`() = runTest {
        // Given no copy yet
        val stored = MutableStateFlow<Bitmap?>(null)
        val palette = FakePalette()
        val viewModel = WallpaperBackdropViewModel(FakeCopies(stored), settings(), palette)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.palette.collect { } }

        // Then there is no palette of Facet's own
        assertNull(viewModel.palette.first())

        // When Facet stores a copy
        stored.value = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)

        // Then the palette is computed from it
        val computed = viewModel.palette.first { it != null }
        assertNotNull(computed)

        // When Android reports new wallpaper colours
        val before = palette.requests
        palette.changes.tryEmit(Unit)

        // Then the palette is computed again
        assertTrue(palette.requests > before)
    }
}
