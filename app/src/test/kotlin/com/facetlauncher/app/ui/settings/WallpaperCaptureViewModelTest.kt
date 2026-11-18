package com.facetlauncher.app.ui.settings

import android.app.Activity
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.facetlauncher.app.data.CapturedFrames
import com.facetlauncher.app.data.ScreenCaptureRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.domain.CaptureWallpaperUseCase
import com.facetlauncher.app.domain.UseScreenshotAsWallpaperCopyUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
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
class WallpaperCaptureViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After fun tearDown() = Dispatchers.resetMain()

    private class FakeCopies : WallpaperCopyRepository(RuntimeEnvironment.getApplication(), mock(WallpaperManager::class.java)) {
        override fun currentSystemWallpaperId() = 5
        override fun screenSize() = 50 to 100
        override suspend fun save(bitmap: Bitmap) = true
        override suspend fun save(bitmap: Bitmap, size: com.facetlauncher.app.data.model.ScreenSize) = true
    }

    private class FakeCapture(private val frame: Bitmap?) : ScreenCaptureRepository(RuntimeEnvironment.getApplication()) {
        override fun consentIntent() = Intent("consent")
        override suspend fun capture(resultCode: Int, consent: Intent, barsHidden: () -> Boolean, rotateTo: (suspend (landscape: Boolean) -> Boolean)?): CapturedFrames? = frame?.let { CapturedFrames(it) }
    }

    private fun viewModel(frame: Bitmap?): WallpaperCaptureViewModel {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(produceFile = { tempFolder.newFile("t-${System.nanoTime()}.preferences_pb") })
        val capture = FakeCapture(frame)
        val copies = FakeCopies()
        val store = UseScreenshotAsWallpaperCopyUseCase(copies, SettingsRepository(dataStore), UnconfinedTestDispatcher())
        return WallpaperCaptureViewModel(capture, CaptureWallpaperUseCase(capture, store, copies))
    }

    private val portrait get() = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888)

    @Test
    fun `asking to capture clears the card before the dialog opens`() {
        // Given the prompt is showing
        val viewModel = viewModel(portrait)
        assertEquals(CapturePhase.Prompt, viewModel.uiState.value.phase)

        // When the user asks to capture
        viewModel.onCaptureRequested()

        // Then nothing is drawn, so the frame is just the wallpaper
        assertEquals(CapturePhase.Capturing, viewModel.uiState.value.phase)
    }

    @Test
    fun `declining the dialog shows the cancelled card`() {
        // Given a capture was requested
        val viewModel = viewModel(portrait)
        viewModel.onCaptureRequested()

        // When the dialog is declined
        viewModel.onConsentResult(Activity.RESULT_CANCELED, null)

        // Then the cancelled card is showing
        assertEquals(CapturePhase.Failed, viewModel.uiState.value.phase)
    }

    @Test
    fun `agreeing stores the copy and reports it`() = runTest {
        // Given a capture was requested and a frame will be taken
        val viewModel = viewModel(portrait)
        viewModel.onCaptureRequested()

        // When the user agrees
        viewModel.onConsentResult(Activity.RESULT_OK, Intent())

        // Then the capture is reported done
        assertEquals(WallpaperCaptureUiEvent.Captured, viewModel.uiEvents.first())
    }

    @Test
    fun `agreeing but getting no frame shows the cancelled card`() {
        // Given the capture will return nothing
        val viewModel = viewModel(null)
        viewModel.onCaptureRequested()

        // When the user agrees
        viewModel.onConsentResult(Activity.RESULT_OK, Intent())

        // Then the cancelled card is showing, so they can try again
        assertEquals(CapturePhase.Failed, viewModel.uiState.value.phase)
    }

    @Test
    fun `the manual route and back move between the two cards`() {
        // Given the prompt
        val viewModel = viewModel(portrait)

        // When the manual route is chosen and then Back
        viewModel.onManualRoute()
        val manual = viewModel.uiState.value.phase
        viewModel.onBackToPrompt()

        // Then it showed the manual card, then the prompt
        assertEquals(CapturePhase.Manual, manual)
        assertEquals(CapturePhase.Prompt, viewModel.uiState.value.phase)
    }
}
