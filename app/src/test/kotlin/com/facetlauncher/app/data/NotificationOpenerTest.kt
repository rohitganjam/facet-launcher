package com.facetlauncher.app.data

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.isNull
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NotificationOpenerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `a notification with no intent is not opened`() {
        // Given a notification that carries no content intent, when opening it, then nothing is sent
        assertFalse(NotificationOpener(context).open(null))
    }

    @Test
    fun `sends the content intent and reports success`() {
        // Given a notification's content intent
        val pendingIntent = mock(PendingIntent::class.java)

        // When opening it
        val opened = NotificationOpener(context).open(pendingIntent)

        // Then the intent is sent from the launcher's context
        assertTrue(opened)
        verify(pendingIntent).send(eq(context), eq(0), isNull<Intent>(), isNull(), isNull(), isNull<String>(), any<Bundle>())
    }

    @Test
    fun `an intent the app has since cancelled is reported as not opened`() {
        // Given a content intent whose app has cancelled it
        val pendingIntent = mock(PendingIntent::class.java)
        doThrow(PendingIntent.CanceledException()).`when`(pendingIntent)
            .send(eq(context), eq(0), isNull<Intent>(), isNull(), isNull(), isNull<String>(), any<Bundle>())

        // When opening it, then the failure is contained and reported
        assertFalse(NotificationOpener(context).open(pendingIntent))
    }
}
