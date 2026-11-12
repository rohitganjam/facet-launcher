package com.facetlauncher.app.ui.launcher

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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
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

    private fun settings(): SettingsRepository {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(produceFile = { tempFolder.newFile("t-${System.nanoTime()}.preferences_pb") })
        return SettingsRepository(dataStore)
    }

    private fun WallpaperBackdropViewModel.observe(scope: kotlinx.coroutines.test.TestScope) =
        scope.backgroundScope.launch(UnconfinedTestDispatcher(scope.testScheduler)) { copy.collect { } }

    @Test
    fun `there is no copy until the repository has one`() = runTest {
        // Given a repository with no stored copy
        val viewModel = WallpaperBackdropViewModel(FakeCopies(MutableStateFlow(null)), settings())

        // Then the view model shows none
        assertNull(viewModel.copy.value)
    }

    @Test
    fun `the copy follows the repository as it is stored and removed`() = runTest {
        // Given a repository whose copy can change and a copy recorded for the current system wallpaper
        val stored = MutableStateFlow<Bitmap?>(null)
        val settings = settings().also { it.setWallpaperCopySystemId(1) }
        val viewModel = WallpaperBackdropViewModel(FakeCopies(stored), settings)
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
    fun `a copy is dropped once the system wallpaper has changed since it was stored`() = runTest {
        // Given a copy stored when the system id was 1
        val copies = FakeCopies(MutableStateFlow(Bitmap.createBitmap(10, 20, Bitmap.Config.ARGB_8888)))
        val settings = settings().also { it.setWallpaperCopySystemId(1) }
        val viewModel = WallpaperBackdropViewModel(copies, settings)
        viewModel.observe(this)
        assertNotNull(viewModel.copy.first { it != null })

        // When the system wallpaper changes and the launcher resumes
        copies.systemId = 2
        viewModel.onResumed()

        // Then glass falls back to the tint: no copy
        assertNull(viewModel.copy.first { it == null })
    }
}
