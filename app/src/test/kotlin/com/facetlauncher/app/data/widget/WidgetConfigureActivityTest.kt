package com.facetlauncher.app.data.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** The trampoline is what actually fires an external widget's configure activity, so it is tested with the repository stubbed. */
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class WidgetConfigureActivityTest {

    @get:Rule val hiltRule = HiltAndroidRule(this)

    @BindValue @JvmField val repository: AppWidgetRepository = mock(AppWidgetRepository::class.java)

    @Before fun setUp() = hiltRule.inject()

    private val placeholder = mock(Activity::class.java)

    /** Mockito's `any()` returns null, which Kotlin rejects for a non-null Activity parameter. */
    private fun anyActivity(): Activity = any(Activity::class.java) ?: placeholder

    private fun launch(appWidgetId: Int?): WidgetConfigureActivity {
        val intent = Intent(ApplicationProvider.getApplicationContext(), WidgetConfigureActivity::class.java)
        if (appWidgetId != null) intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        return Robolectric.buildActivity(WidgetConfigureActivity::class.java, intent).create().get()
    }

    private fun WidgetConfigureActivity.deliverResult(resultCode: Int, data: Intent?) {
        val method = Activity::class.java.getDeclaredMethod("onActivityResult", Int::class.java, Int::class.java, Intent::class.java)
        method.isAccessible = true
        method.invoke(this, 1, resultCode, data)
    }

    @Test
    fun `launching it fires the configure activity for the widget id and stays open for the result`() {
        // Given the host can start the external widget's configure activity
        `when`(repository.startConfigureActivity(anyActivity(), eq(7), anyInt())).thenReturn(true)

        // When the trampoline opens for widget 7
        val activity = launch(7)

        // Then the host was asked to start widget 7's configure activity, and the trampoline waits for its result
        verify(repository).startConfigureActivity(eq(activity) ?: activity, eq(7), eq(1))
        assertTrue(!activity.isFinishing)
    }

    @Test
    fun `the configure activity's result is passed back to the caller`() {
        // Given a trampoline waiting on widget 7's configure activity
        `when`(repository.startConfigureActivity(anyActivity(), eq(7), anyInt())).thenReturn(true)
        val activity = launch(7)
        val data = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 7)

        // When the configure activity finishes OK
        activity.deliverResult(Activity.RESULT_OK, data)

        // Then the same result goes back and the trampoline closes
        assertTrue(activity.isFinishing)
        assertEquals(Activity.RESULT_OK, shadowOf(activity).resultCode)
        assertEquals(data, shadowOf(activity).resultIntent)
    }

    @Test
    fun `a cancelled configure activity cancels the add`() {
        // Given a trampoline waiting on widget 7's configure activity
        `when`(repository.startConfigureActivity(anyActivity(), eq(7), anyInt())).thenReturn(true)
        val activity = launch(7)

        // When the user backs out of it
        activity.deliverResult(Activity.RESULT_CANCELED, null)

        // Then the caller gets RESULT_CANCELED
        assertTrue(activity.isFinishing)
        assertEquals(Activity.RESULT_CANCELED, shadowOf(activity).resultCode)
    }

    @Test
    fun `it cancels at once when the host can't start the configure activity`() {
        // Given a host that fails to start it
        `when`(repository.startConfigureActivity(anyActivity(), eq(7), anyInt())).thenReturn(false)

        // When the trampoline opens
        val activity = launch(7)

        // Then it closes with RESULT_CANCELED instead of hanging
        assertTrue(activity.isFinishing)
        assertEquals(Activity.RESULT_CANCELED, shadowOf(activity).resultCode)
    }

    @Test
    fun `it cancels without calling the host when no widget id was passed`() {
        // When opened with no widget id
        val activity = launch(null)

        // Then nothing is fired and the caller gets RESULT_CANCELED
        verify(repository, never()).startConfigureActivity(anyActivity(), anyInt(), anyInt())
        assertTrue(activity.isFinishing)
        assertEquals(Activity.RESULT_CANCELED, shadowOf(activity).resultCode)
        assertNull(shadowOf(activity).resultIntent)
    }
}
