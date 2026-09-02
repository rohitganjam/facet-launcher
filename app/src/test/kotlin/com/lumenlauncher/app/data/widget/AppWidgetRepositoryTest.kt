package com.lumenlauncher.app.data.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.IntentSender
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
    fun `createConfigureIntentSender returns null when the provider declares no configure activity`() {
        // Given a provider with no configure component
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), mock(LauncherAppWidgetHost::class.java))
        val provider = AppWidgetProviderInfo().apply { configure = null }

        // Then no configure step is needed
        assertNull(repository.createConfigureIntentSender(appWidgetId = 7, provider = provider))
    }

    @Test
    fun `createConfigureIntentSender delegates to the host's own permission-scoped sender when a configure activity is declared`() {
        // Given a provider that declares a configure activity, and the host's own IntentSender for it
        // (must come from AppWidgetHost, not a bare Intent — a configure Activity is very often not
        // exported, e.g. Slack's, and a bare Intent hits the OS's exported-Activity check and crashes)
        val host = mock(LauncherAppWidgetHost::class.java)
        val sender = mock(IntentSender::class.java)
        `when`(host.configureIntentSender(7)).thenReturn(sender)
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), host)
        val configureComponent = ComponentName("com.example.widgets", ".ConfigureActivity")
        val provider = AppWidgetProviderInfo().apply { configure = configureComponent }

        // Then it returns exactly the host's sender for this widget id
        assertEquals(sender, repository.createConfigureIntentSender(appWidgetId = 7, provider = provider))
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
