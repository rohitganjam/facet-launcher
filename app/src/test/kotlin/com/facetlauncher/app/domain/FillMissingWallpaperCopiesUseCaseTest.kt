package com.facetlauncher.app.domain

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.model.ScreenCopy
import com.facetlauncher.app.data.model.ScreenSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class FillMissingWallpaperCopiesUseCaseTest {

    private val outer = ScreenSize(50, 100)
    private val inner = ScreenSize(100, 80)

    /** A repository with a fixed set of views and a kept original, decoding it to a photo that is red on the left and blue on the right. */
    private class FakeCopies(views: List<ScreenCopy>, var kept: StoredOriginal?, var systemId: Int = 7) :
        WallpaperCopyRepository(RuntimeEnvironment.getApplication(), mock(WallpaperManager::class.java)) {
        val saved = mutableMapOf<ScreenSize, Bitmap>()
        private val viewList = MutableStateFlow(views)
        override val screens: Flow<List<ScreenCopy>> get() = viewList
        override suspend fun knownScreens() = viewList.value.map { it.screen }.distinct()
        var deleted = false
        override suspend fun original(): StoredOriginal? = kept
        override suspend fun originalSystemId(): Int? = kept?.systemId
        override suspend fun deleteOriginal() { deleted = true; kept = null }
        override fun currentSystemWallpaperId() = systemId
        override suspend fun decodeFile(file: java.io.File, maxSide: Int): Bitmap? =
            Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888).also { bitmap ->
                for (x in 0 until 200) for (y in 0 until 100) bitmap.setPixel(x, y, if (x < 100) Color.RED else Color.BLUE)
            }
        override suspend fun save(bitmap: Bitmap, size: ScreenSize): Boolean { saved[size] = bitmap; return true }
    }

    private fun original(systemId: Int = 7) = WallpaperCopyRepository.StoredOriginal(java.io.File("original.bin"), RectF(0.375f, 0f, 0.625f, 1f), systemId)

    private fun useCase(copies: FakeCopies) = FillMissingWallpaperCopiesUseCase(copies, UnconfinedTestDispatcher())

    @Test
    fun `a screen first seen later gets copies built from the original`() = runTest {
        // Given the outer screen has its copies and the inner screen, never seen before, has none
        val views = listOf(
            ScreenCopy(inner, false), ScreenCopy(inner.turned, false, screen = inner),
            ScreenCopy(outer, true), ScreenCopy(outer.turned, true, screen = outer),
        )
        val copies = FakeCopies(views, original())

        // When the missing copies are filled
        val built = useCase(copies)()

        // Then both views of the inner screen were built, at their own sizes, and the others left alone
        assertTrue(built)
        assertEquals(setOf(inner, inner.turned), copies.saved.keys)
        assertEquals(100, copies.saved[inner]!!.width)
        assertEquals(80, copies.saved[inner]!!.height)
        assertEquals(Color.RED, copies.saved[inner]!!.getPixel(1, 40))
        assertEquals(Color.BLUE, copies.saved[inner]!!.getPixel(98, 40))
    }

    @Test
    fun `nothing is built when no copy is missing`() = runTest {
        // Given every view has a copy
        val copies = FakeCopies(listOf(ScreenCopy(outer, true), ScreenCopy(outer.turned, true, screen = outer)), original())

        // Then there is nothing to build
        assertFalse(useCase(copies)())
        assertTrue(copies.saved.isEmpty())
    }

    @Test
    fun `nothing is built without an original`() = runTest {
        // Given a missing view but a wallpaper that was captured, so no original is kept
        val copies = FakeCopies(listOf(ScreenCopy(outer, true), ScreenCopy(outer.turned, false, screen = outer)), kept = null)

        // Then it has to be captured, not built
        assertFalse(useCase(copies)())
        assertTrue(copies.saved.isEmpty())
    }

    @Test
    fun `nothing is built once the wallpaper was changed elsewhere`() = runTest {
        // Given an original from wallpaper 7, and the system now showing wallpaper 8
        val copies = FakeCopies(listOf(ScreenCopy(outer, false, outdated = true), ScreenCopy(outer.turned, false, screen = outer)), original(systemId = 7), systemId = 8)

        // Then the old photo is not drawn, and it is deleted, since it can never be the wallpaper again
        assertFalse(useCase(copies)())
        assertTrue(copies.saved.isEmpty())
        assertTrue(copies.deleted)
    }

    @Test
    fun `an original of the wallpaper now set is kept even when no copy is missing`() = runTest {
        // Given every view has a copy and the original is of the wallpaper set
        val copies = FakeCopies(listOf(ScreenCopy(outer, true), ScreenCopy(outer.turned, true, screen = outer)), original())

        // Then nothing is built and the original stays
        assertFalse(useCase(copies)())
        assertFalse(copies.deleted)
    }
}
