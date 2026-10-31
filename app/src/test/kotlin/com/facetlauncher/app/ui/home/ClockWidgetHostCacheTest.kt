package com.facetlauncher.app.ui.home

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ClockWidgetHostCacheTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private var created = 0
    private val gone = mutableSetOf<Int>()

    private val cache = ClockWidgetHostCache { viewContext, id ->
        if (id in gone) null else AppWidgetHostView(viewContext).also { created++ }
    }

    @Test
    fun `a page's view is built once and handed back every time`() {
        val first = cache.get(context, 7)
        val second = cache.get(context, 7)

        assertSame(first, second)
        assertEquals(1, created)
    }

    @Test
    fun `a view is taken out of the holder it was last in`() {
        val oldHolder = FrameLayout(context)
        val newHolder = FrameLayout(context)
        oldHolder.addView(cache.get(context, 7))

        newHolder.addView(cache.get(context, 7))

        assertEquals(0, oldHolder.childCount)
        assertEquals(1, newHolder.childCount)
    }

    @Test
    fun `a provider that is gone gets no view and nothing is kept`() {
        gone += 7

        assertNull(cache.get(context, 7))
        assertFalse(cache.has(7))
    }

    @Test
    fun `pages that left the stack are dropped and rebuilt if they return`() {
        val seven = cache.get(context, 7)
        cache.get(context, 8)

        cache.retainOnly(setOf(8))

        assertFalse(cache.has(7))
        assertTrue(cache.has(8))
        assertNotSame(seven, cache.get(context, 7))
        assertEquals(3, created)
    }

    @Test
    fun `clear drops every view`() {
        cache.get(context, 7)

        cache.clear()

        assertFalse(cache.has(7))
    }
}
