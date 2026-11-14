package com.facetlauncher.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SwipeActionsTest {

    @Test
    fun `the old defaults map to drawer up shade down facets left and hub right`() {
        // When the old switches are all on and not swapped
        val actions = SwipeActions.fromLegacy(leftEnabled = true, rightEnabled = true, downEnabled = true, swapSides = false)

        // Then it is the default mapping
        assertEquals(SwipeActions(), actions)
    }

    @Test
    fun `swapped sides put the hub on the left and facets on the right`() {
        val actions = SwipeActions.fromLegacy(leftEnabled = true, rightEnabled = true, downEnabled = true, swapSides = true)

        assertEquals(SwipeAction.HUB, actions.left)
        assertEquals(SwipeAction.SWITCH_FACETS, actions.right)
    }

    @Test
    fun `a switched off swipe becomes none whichever side it was on`() {
        val actions = SwipeActions.fromLegacy(leftEnabled = false, rightEnabled = true, downEnabled = false, swapSides = true)

        assertEquals(SwipeAction.NONE, actions.left)
        assertEquals(SwipeAction.SWITCH_FACETS, actions.right)
        assertEquals(SwipeAction.NONE, actions.down)
        assertEquals(SwipeAction.APP_DRAWER, actions.up)
    }

    @Test
    fun `an action can be assigned to a second swipe`() {
        // When swipe down is set to open the drawer too
        val actions = SwipeActions().assign(SwipeDirection.DOWN, SwipeAction.APP_DRAWER)

        // Then both swipes open it
        assertEquals(listOf(SwipeDirection.UP, SwipeDirection.DOWN), actions.directionsOf(SwipeAction.APP_DRAWER))
    }

    @Test
    fun `the app drawer cannot leave its only swipe but can leave one of two`() {
        // Given the drawer on swipe up only
        val single = SwipeActions()

        // Then swipe up cannot be set to anything else
        assertFalse(single.canAssign(SwipeDirection.UP, SwipeAction.NONE))
        assertFalse(single.canAssign(SwipeDirection.UP, SwipeAction.HUB))
        assertEquals(single, single.assign(SwipeDirection.UP, SwipeAction.NONE))

        // And once swipe down opens it too, swipe up can change
        val double = single.assign(SwipeDirection.DOWN, SwipeAction.APP_DRAWER)
        assertTrue(double.canAssign(SwipeDirection.UP, SwipeAction.NONE))
        assertEquals(SwipeAction.HUB, double.assign(SwipeDirection.UP, SwipeAction.HUB).up)
    }

    @Test
    fun `none is on no swipe`() {
        assertEquals(emptyList<SwipeDirection>(), SwipeActions(left = SwipeAction.NONE).directionsOf(SwipeAction.NONE))
    }

    @Test
    fun `a mapping without the app drawer is repaired onto swipe up`() {
        val broken = SwipeActions(up = SwipeAction.HUB, down = SwipeAction.NONE, left = SwipeAction.NONE, right = SwipeAction.NONE)

        assertEquals(SwipeAction.APP_DRAWER, broken.normalized().up)
        assertEquals(SwipeActions(down = SwipeAction.APP_DRAWER), SwipeActions(down = SwipeAction.APP_DRAWER).normalized())
    }
}
