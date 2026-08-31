package com.lumenlauncher.app.data.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppWidgetRepositoryTest {

    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun `allocateAppWidgetId returns the host-issued id`() {
        // Given a host that hands out id 42
        val host = mock(LauncherAppWidgetHost::class.java)
        `when`(host.allocateAppWidgetId()).thenReturn(42)
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), host)

        // Then the repository passes it through unchanged
        assertEquals(42, repository.allocateAppWidgetId())
    }

    @Test
    fun `createBindIntent carries the right action and extras`() {
        // Given a provider component
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), mock(LauncherAppWidgetHost::class.java))
        val provider = ComponentName("com.example.widgets", ".MyWidgetProvider")

        // When building the bind intent
        val intent = repository.createBindIntent(appWidgetId = 7, provider = provider)

        // Then it's a real ACTION_APPWIDGET_BIND intent addressed at this widget id/provider
        assertEquals(AppWidgetManager.ACTION_APPWIDGET_BIND, intent.action)
        assertEquals(7, intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1))
        assertEquals(provider, intent.getParcelableExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER))
    }

    @Test
    fun `createConfigureIntent returns null when the provider declares no configure activity`() {
        // Given a provider with no configure component
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), mock(LauncherAppWidgetHost::class.java))
        val provider = AppWidgetProviderInfo().apply { configure = null }

        // Then no configure step is needed
        assertNull(repository.createConfigureIntent(appWidgetId = 7, provider = provider))
    }

    @Test
    fun `createConfigureIntent targets the provider's own configure activity when declared`() {
        // Given a provider that declares a configure activity
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), mock(LauncherAppWidgetHost::class.java))
        val configureComponent = ComponentName("com.example.widgets", ".ConfigureActivity")
        val provider = AppWidgetProviderInfo().apply { configure = configureComponent }

        // When building the configure intent
        val intent = repository.createConfigureIntent(appWidgetId = 7, provider = provider)

        // Then it targets that exact activity, carrying the widget id
        assertEquals(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE, intent?.action)
        assertEquals(configureComponent, intent?.component)
        assertEquals(7, intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1))
    }

    @Test
    fun `getAppWidgetInfo returns null for an unknown id`() {
        // Given a manager that has no record of this widget id (e.g. its provider was uninstalled)
        val appWidgetManager = mock(AppWidgetManager::class.java)
        `when`(appWidgetManager.getAppWidgetInfo(99)).thenReturn(null)
        val repository = AppWidgetRepository(context, appWidgetManager, mock(LauncherAppWidgetHost::class.java))

        // Then the repository surfaces that as null (the orphan signal), not a crash
        assertNull(repository.getAppWidgetInfo(99))
    }

    @Test
    fun `deleteAppWidgetId releases the id through the host`() {
        // Given a host
        val host = mock(LauncherAppWidgetHost::class.java)
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), host)

        // When deleting a widget id
        repository.deleteAppWidgetId(7)

        // Then it's released through the host, not just silently dropped
        org.mockito.Mockito.verify(host).deleteAppWidgetId(7)
    }
}
