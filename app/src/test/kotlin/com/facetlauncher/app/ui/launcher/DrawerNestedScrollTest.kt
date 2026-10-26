package com.facetlauncher.app.ui.launcher

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import com.facetlauncher.app.data.model.DrawerPresentation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DrawerNestedScrollTest {

    private val scope = CoroutineScope(UnconfinedTestDispatcher())

    private fun connection(drawer: SwipeAxisState, swipeBlocked: () -> Boolean = { false }) = drawerNestedScroll(
        drawer = drawer,
        presentation = DrawerPresentation.LIST,
        isSearching = false,
        listState = LazyListState(),
        gridState = LazyGridState(),
        searchListState = LazyListState(),
        swipeBlocked = swipeBlocked,
    )

    private fun openDrawer() = SwipeAxisState({ 1000f }, scope).also { drawer -> kotlinx.coroutines.runBlocking { drawer.progress.snapTo(1f) } }

    @Test
    fun `a downward drag at the top of an open drawer pulls it closed`() = runTest {
        // Given an open drawer whose list is at the top
        val drawer = openDrawer()

        // When a finger drags down
        val consumed = connection(drawer).onPreScroll(Offset(0f, 40f), NestedScrollSource.UserInput)

        // Then the connection takes the whole drag
        assertEquals(Offset(0f, 40f), consumed)
    }

    @Test
    fun `a downward drag leaves the drawer alone while a sheet is open over it`() = runTest {
        // Given the same drawer, with a sheet open over it
        val drawer = openDrawer()

        // When a finger drags down on that sheet's own list
        val consumed = connection(drawer, swipeBlocked = { true }).onPreScroll(Offset(0f, 40f), NestedScrollSource.UserInput)

        // Then nothing is taken, so the sheet's list gets the drag, and the drawer is not being dragged
        assertEquals(Offset.Zero, consumed)
        assertFalse(drawer.dragActive)
    }

    @Test
    fun `an upward drag is never taken`() = runTest {
        assertEquals(Offset.Zero, connection(openDrawer()).onPreScroll(Offset(0f, -40f), NestedScrollSource.UserInput))
    }

    @Test
    fun `a closed drawer is never pulled`() = runTest {
        val closed = SwipeAxisState({ 1000f }, scope)
        assertEquals(Offset.Zero, connection(closed).onPreScroll(Offset(0f, 40f), NestedScrollSource.UserInput))
    }
}
