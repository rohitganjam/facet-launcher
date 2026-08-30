package com.lumenlauncher.app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

/**
 * `StatusBarManager.expandNotificationsPanel()` is `@hide`d and invoked here via reflection, and
 * this project's Robolectric version has no shadow method for it — so these tests can't assert
 * the shade actually opened. What they cover instead is the contract [NotificationShadeRepository]
 * promises its caller (Home's swipe-down gesture): [NotificationShadeRepository.expand] never
 * throws, whether the system service is missing entirely or the reflective call itself fails.
 */
@RunWith(RobolectricTestRunner::class)
class NotificationShadeRepositoryTest {

    @Test
    fun `expand does not throw when the statusbar service is unavailable`() = runTest {
        // Given a context with no "statusbar" system service
        val context = mock(Context::class.java)
        `when`(context.getSystemService("statusbar")).thenReturn(null)
        val repository = NotificationShadeRepository(context)

        // When expanding the shade, then it completes without throwing
        repository.expand()
    }

    @Test
    fun `expand does not throw when the reflective call fails`() = runTest {
        // Given the real (Robolectric-stubbed) statusbar service
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = NotificationShadeRepository(context)

        // When expanding the shade, then any underlying reflection/binder failure is swallowed
        // rather than propagated
        repository.expand()
    }
}
