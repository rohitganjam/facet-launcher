package com.facetlauncher.app.ui.home

import android.app.WallpaperManager
import com.facetlauncher.app.data.ScreenCaptureRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.model.ScreenCopy
import com.facetlauncher.app.data.model.ScreenSize
import com.facetlauncher.app.domain.ObserveGlassInUseUseCase
import com.facetlauncher.app.domain.ObserveWallpaperScreensUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WallpaperCopyPillViewModelTest {

    @Before
    fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private class FakeCopies(val screenList: MutableStateFlow<List<ScreenCopy>>) :
        WallpaperCopyRepository(RuntimeEnvironment.getApplication(), mock(WallpaperManager::class.java)) {
        var systemId = 1
        val checking = MutableStateFlow(0)
        override val screens: Flow<List<ScreenCopy>> get() = kotlinx.coroutines.flow.combine(screenList, checking) { list, _ -> list }
        override val dismissedPrompts: Flow<Set<String>> get() = flowOf(emptySet())
        override val currentScreen: Flow<ScreenSize> get() = flowOf(ScreenSize(1080, 2340))
        override fun currentSystemWallpaperId() = systemId
    }

    private class FakeCapture : ScreenCaptureRepository(RuntimeEnvironment.getApplication()) {
        val running = MutableStateFlow(false)
        override val capturing: StateFlow<Boolean> get() = running
    }

    private class Setup(val copies: FakeCopies, val capture: FakeCapture, val model: WallpaperCopyPillViewModel)

    private fun setup(glass: Boolean = true, outdated: Boolean = true): Setup {
        val view = ScreenSize(1080, 2340)
        val copies = FakeCopies(MutableStateFlow(listOf(ScreenCopy(view, hasCopy = !outdated, outdated = outdated))))
        val glassInUse = mock(ObserveGlassInUseUseCase::class.java)
        org.mockito.Mockito.`when`(glassInUse()).thenReturn(flowOf(glass))
        val capture = FakeCapture()
        return Setup(copies, capture, WallpaperCopyPillViewModel(ObserveWallpaperScreensUseCase(copies), glassInUse, capture))
    }

    private fun kotlinx.coroutines.test.TestScope.observe(model: WallpaperCopyPillViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.offer.collect { } }
    }

    @Test
    fun `an out of date copy is offered once for its wallpaper`() = runTest {
        // Given a copy that no longer matches the wallpaper
        val s = setup()
        observe(s.model)
        val key = s.model.offer.value?.key
        assertEquals("1080x2340@1", key)

        // When the pill has been shown
        s.model.onShown(key!!)

        // Then it is not offered again for that wallpaper
        assertNull(s.model.offer.value)
    }

    @Test
    fun `a new wallpaper is offered again after the first offer was shown`() = runTest {
        // Given the offer for wallpaper 1 was shown
        val s = setup()
        observe(s.model)
        s.model.onShown(s.model.offer.value!!.key)

        // When the system wallpaper changes and Home checks again
        s.copies.systemId = 2
        s.copies.checking.value++

        // Then the view is offered for the new wallpaper
        assertEquals("1080x2340@2", s.model.offer.value?.key)
    }

    @Test
    fun `nothing is offered while a capture is being made`() = runTest {
        // Given an out of date copy and a capture in progress
        val s = setup()
        observe(s.model)
        s.capture.running.value = true

        // Then there is no offer, and it comes back if the capture ends without a copy
        assertNull(s.model.offer.value)
        s.capture.running.value = false
        assertEquals("1080x2340@1", s.model.offer.value?.key)
    }

    @Test
    fun `nothing is offered when the view already has a copy for the wallpaper`() = runTest {
        // Given a copy that matches the wallpaper
        val s = setup(outdated = false)
        observe(s.model)

        // Then there is no offer
        assertNull(s.model.offer.value)
    }

    @Test
    fun `nothing is offered without glass`() = runTest {
        // Given an out of date copy but no glass anywhere
        val s = setup(glass = false)
        observe(s.model)

        // Then there is no offer
        assertNull(s.model.offer.value)
    }
}
