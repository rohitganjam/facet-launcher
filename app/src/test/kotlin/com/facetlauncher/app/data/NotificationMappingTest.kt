package com.facetlauncher.app.data

import android.app.Notification
import android.os.Bundle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NotificationMappingTest {

    @Test
    fun `reads the title and text from the extras`() {
        // Given a notification with a title and a body
        val extras = Bundle().apply {
            putCharSequence(Notification.EXTRA_TITLE, "Priya")
            putCharSequence(Notification.EXTRA_TEXT, "Are we still on?")
        }

        // When mapping it, then both come through
        assertEquals("Priya" to "Are we still on?", notificationTitleAndText(extras))
    }

    @Test
    fun `prefers the compact text and falls back to the expanded one`() {
        // Given a notification whose compact text is missing but has expanded text and title
        val extras = Bundle().apply {
            putCharSequence(Notification.EXTRA_TITLE_BIG, "Big title")
            putCharSequence(Notification.EXTRA_BIG_TEXT, "Big body")
        }

        // When mapping it, then the expanded text is used
        assertEquals("Big title" to "Big body", notificationTitleAndText(extras))

        // And once the compact text is present it wins
        extras.putCharSequence(Notification.EXTRA_TITLE, "Short title")
        extras.putCharSequence(Notification.EXTRA_TEXT, "Short body")
        assertEquals("Short title" to "Short body", notificationTitleAndText(extras))
    }

    @Test
    fun `blank text counts as missing and the rest is trimmed`() {
        // Given a blank title and a padded body
        val extras = Bundle().apply {
            putCharSequence(Notification.EXTRA_TITLE, "   ")
            putCharSequence(Notification.EXTRA_TEXT, "  hello  ")
        }

        // When mapping it, then the title is null and the body is trimmed
        assertEquals(null to "hello", notificationTitleAndText(extras))
    }

    @Test
    fun `long text is cut to the maximum length`() {
        // Given an enormous body
        val extras = Bundle().apply { putCharSequence(Notification.EXTRA_TEXT, "x".repeat(MAX_NOTIFICATION_TEXT_LENGTH + 50)) }

        // When mapping it, then it is truncated
        assertEquals(MAX_NOTIFICATION_TEXT_LENGTH, notificationTitleAndText(extras).second?.length)
    }

    @Test
    fun `null extras give no title or text`() {
        // When there are no extras, then there is nothing to show
        assertEquals(null to null, notificationTitleAndText(null))
        assertNull(notificationTitleAndText(Bundle()).first)
    }
}
