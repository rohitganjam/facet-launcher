package com.facetlauncher.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationActionControllerTest {

    @Test
    fun `dismiss sends the keys to the attached listener`() {
        // Given a controller with a listener attached
        val controller = NotificationActionController()
        val received = mutableListOf<List<String>>()
        controller.attach { received += it }

        // When dismissing two notifications
        val dispatched = controller.dismiss(listOf("a", "b"))

        // Then the listener got both keys and the call reports success
        assertTrue(dispatched)
        assertEquals(listOf(listOf("a", "b")), received)
    }

    @Test
    fun `dismiss does nothing while no listener is attached`() {
        // Given a controller that was never attached
        val controller = NotificationActionController()

        // When dismissing, then nothing is dispatched
        assertFalse(controller.dismiss(listOf("a")))
    }

    @Test
    fun `dismiss does nothing after the listener detaches`() {
        // Given a listener that attached and then disconnected
        val controller = NotificationActionController()
        val received = mutableListOf<List<String>>()
        controller.attach { received += it }
        controller.detach()

        // When dismissing, then the old listener is not called
        assertFalse(controller.dismiss(listOf("a")))
        assertTrue(received.isEmpty())
    }

    @Test
    fun `dismiss with no keys sends nothing`() {
        // Given an attached listener
        val controller = NotificationActionController()
        val received = mutableListOf<List<String>>()
        controller.attach { received += it }

        // When dismissing an empty list, then no request is made
        assertFalse(controller.dismiss(emptyList()))
        assertTrue(received.isEmpty())
    }

    @Test
    fun `a listener that throws is reported as not dispatched`() {
        // Given a listener that fails, as when the service has just been unbound
        val controller = NotificationActionController()
        controller.attach { throw SecurityException("not bound") }

        // When dismissing, then the failure is contained and reported
        assertFalse(controller.dismiss(listOf("a")))
    }
}
