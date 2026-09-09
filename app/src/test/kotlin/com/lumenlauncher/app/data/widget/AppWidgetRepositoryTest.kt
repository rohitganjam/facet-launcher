package com.lumenlauncher.app.data.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.IntentSender
import android.os.Bundle
import android.util.SizeF
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
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
    fun `defaultSpanFor prefers the provider's target cell size over minWidth math`() {
        // Given a modern provider that declares 0dp minWidth but a real 4x2-cell target
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), mock(LauncherAppWidgetHost::class.java))
        val info = AppWidgetProviderInfo().apply {
            minWidth = 0
            minHeight = 0
            targetCellWidth = 4
            targetCellHeight = 2
        }

        // Then it's placed at 4x2, not 1x1 (which is what the legacy minWidth math would give)
        assertEquals(4 to 2, repository.defaultSpanFor(info))
    }

    @Test
    fun `defaultSpanFor clamps the column span to the Hub's own column count`() {
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), mock(LauncherAppWidgetHost::class.java))
        val info = AppWidgetProviderInfo().apply { targetCellWidth = 12; targetCellHeight = 3 }

        // 12 columns can't fit a 5-column grid
        assertEquals(5 to 3, repository.defaultSpanFor(info))
    }

    @Test
    fun `defaultSpanFor falls back to minWidth for a provider with no target cells`() {
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), mock(LauncherAppWidgetHost::class.java))
        val info = AppWidgetProviderInfo().apply {
            minWidth = 300
            minHeight = 60
            targetCellWidth = 0
            targetCellHeight = 0
        }

        // Legacy path still works: at least 1 cell, never below
        val (cols, rows) = repository.defaultSpanFor(info)
        assertEquals(true, cols in 1..5)
        assertEquals(true, rows >= 1)
    }

    @Test
    fun `updateWidgetSize sets a non-empty OPTION_APPWIDGET_SIZES`() {
        // Given a repository over a mock AppWidgetManager
        val appWidgetManager = mock(AppWidgetManager::class.java)
        val repository = AppWidgetRepository(context, appWidgetManager, mock(LauncherAppWidgetHost::class.java))

        // When pushing a tile's on-screen size
        repository.updateWidgetSize(appWidgetId = 7, widthDp = 200, heightDp = 120)

        // Then the widget's options carry the min/max dims AND a real SizeF list — an empty list is
        // exactly what makes Glance/RemoteViews widgets fail with "Can't show content"
        val options = ArgumentCaptor.forClass(Bundle::class.java)
        verify(appWidgetManager).updateAppWidgetOptions(eq(7), options.capture())
        val captured = options.value
        assertEquals(200, captured.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH))
        assertEquals(120, captured.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT))
        val sizes = captured.getParcelableArrayList<SizeF>(AppWidgetManager.OPTION_APPWIDGET_SIZES)
        assertEquals(listOf(SizeF(200f, 120f)), sizes)
    }

    @Test
    fun `updateWidgetSize ignores a zero-size tile`() {
        // Given a repository over a mock AppWidgetManager
        val appWidgetManager = mock(AppWidgetManager::class.java)
        val repository = AppWidgetRepository(context, appWidgetManager, mock(LauncherAppWidgetHost::class.java))

        // When the tile hasn't been measured yet (0 x 0)
        repository.updateWidgetSize(appWidgetId = 7, widthDp = 0, heightDp = 0)

        // Then nothing is pushed — a 0-size SizeF is as broken as an empty list
        verify(appWidgetManager, never()).updateAppWidgetOptions(anyInt(), org.mockito.ArgumentMatchers.any())
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
