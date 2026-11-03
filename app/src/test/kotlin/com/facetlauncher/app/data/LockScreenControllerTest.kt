package com.facetlauncher.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LockScreenControllerTest {

    @Test
    fun `lock is a no-op while no service is attached`() {
        // Given a controller nothing has attached to
        val controller = LockScreenController()

        // Then locking reports that nothing took the request
        assertFalse(controller.lock())
    }

    @Test
    fun `lock runs the attached service's action and reports its result`() {
        // Given a service attached whose lock action succeeds
        var calls = 0
        val controller = LockScreenController()
        controller.attach { calls++; true }

        // When locking
        val locked = controller.lock()

        // Then the action ran once and the result is passed back
        assertTrue(locked)
        assertEquals(1, calls)
    }

    @Test
    fun `lock after detach is a no-op again`() {
        // Given a service that attached and then disconnected
        var calls = 0
        val controller = LockScreenController()
        controller.attach { calls++; true }
        controller.detach()

        // When locking
        val locked = controller.lock()

        // Then nothing ran
        assertFalse(locked)
        assertEquals(0, calls)
    }

    @Test
    fun `a failing lock action is reported as not locked instead of throwing`() {
        // Given a service whose lock action throws
        val controller = LockScreenController()
        controller.attach { error("service died") }

        // Then locking returns false rather than propagating
        assertFalse(controller.lock())
    }
}
