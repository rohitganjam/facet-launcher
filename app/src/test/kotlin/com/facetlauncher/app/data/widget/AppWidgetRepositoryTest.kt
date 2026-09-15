package com.facetlauncher.app.data.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.IntentSender
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Bundle
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import android.util.SizeF
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.model.AppProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class AppWidgetRepositoryTest {

    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()

    /** A [UserManager] reporting only the personal profile — the common, no-Work-Profile case every other test in this file assumes. */
    private fun personalOnlyUserManager(): UserManager {
        val userManager = mock(UserManager::class.java)
        `when`(userManager.userProfiles).thenReturn(listOf(Process.myUserHandle()))
        return userManager
    }

    /**
     * [AppWidgetProviderInfo.loadLabel]/`loadIcon`/`loadPreviewImage` all read a private
     * `providerInfo: ActivityInfo` field that's normally populated by the real system service —
     * a bare `AppWidgetProviderInfo()` in a test leaves it null and NPEs the moment
     * `getWidgetProviderOptions()` calls `loadLabel`. Set via reflection (mirroring
     * `LauncherAppWidgetHost.configureIntentSender`'s own existing reflection use for a similarly
     * unreachable-by-public-API field), matching [applicationInfo]'s package.
     */
    private fun AppWidgetProviderInfo.withResolvableLabel(applicationInfo: ApplicationInfo): AppWidgetProviderInfo = apply {
        // nonLocalizedLabel set directly — sidesteps needing a real resource-backed label
        // resolvable through the (shadow) PackageManager for loadLabel() to return non-null.
        val activityInfo = ActivityInfo().apply {
            packageName = applicationInfo.packageName
            this.applicationInfo = applicationInfo
            nonLocalizedLabel = "Widget Label"
        }
        AppWidgetProviderInfo::class.java.getDeclaredField("providerInfo").apply { isAccessible = true }.set(this, activityInfo)
    }

    @Test
    fun `allocateAppWidgetId returns the host-issued id`() {
        // Given a host that hands out id 42
        val host = mock(LauncherAppWidgetHost::class.java)
        `when`(host.allocateAppWidgetId()).thenReturn(42)
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), host, personalOnlyUserManager())

        // Then the repository passes it through unchanged
        assertEquals(42, repository.allocateAppWidgetId())
    }

    @Test
    fun `createBindIntent carries the right action and extras`() {
        // Given a provider component
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), mock(LauncherAppWidgetHost::class.java), personalOnlyUserManager())
        val provider = ComponentName("com.example.widgets", ".MyWidgetProvider")

        // When building the bind intent
        val intent = repository.createBindIntent(appWidgetId = 7, provider = provider, profile = AppProfile.PERSONAL)

        // Then it's a real ACTION_APPWIDGET_BIND intent addressed at this widget id/provider
        assertEquals(AppWidgetManager.ACTION_APPWIDGET_BIND, intent.action)
        assertEquals(7, intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1))
        assertEquals(provider, intent.getParcelableExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER))
    }

    @Test
    fun `createConfigureIntentSender returns null when the provider declares no configure activity`() {
        // Given a provider with no configure component
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), mock(LauncherAppWidgetHost::class.java), personalOnlyUserManager())
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
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), host, personalOnlyUserManager())
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
        val repository = AppWidgetRepository(context, appWidgetManager, mock(LauncherAppWidgetHost::class.java), personalOnlyUserManager())

        // Then the repository surfaces that as null (the orphan signal), not a crash
        assertNull(repository.getAppWidgetInfo(99))
    }

    @Test
    fun `defaultSpanFor prefers the provider's target cell size over minWidth math`() {
        // Given a modern provider that declares 0dp minWidth but a real 4x2-cell target
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), mock(LauncherAppWidgetHost::class.java), personalOnlyUserManager())
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
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), mock(LauncherAppWidgetHost::class.java), personalOnlyUserManager())
        val info = AppWidgetProviderInfo().apply { targetCellWidth = 12; targetCellHeight = 3 }

        // 12 columns can't fit a 5-column grid
        assertEquals(5 to 3, repository.defaultSpanFor(info))
    }

    @Test
    fun `defaultSpanFor falls back to minWidth for a provider with no target cells`() {
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), mock(LauncherAppWidgetHost::class.java), personalOnlyUserManager())
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
        val repository = AppWidgetRepository(context, appWidgetManager, mock(LauncherAppWidgetHost::class.java), personalOnlyUserManager())

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
        val repository = AppWidgetRepository(context, appWidgetManager, mock(LauncherAppWidgetHost::class.java), personalOnlyUserManager())

        // When the tile hasn't been measured yet (0 x 0)
        repository.updateWidgetSize(appWidgetId = 7, widthDp = 0, heightDp = 0)

        // Then nothing is pushed — a 0-size SizeF is as broken as an empty list
        verify(appWidgetManager, never()).updateAppWidgetOptions(anyInt(), org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `deleteAppWidgetId releases the id through the host`() {
        // Given a host
        val host = mock(LauncherAppWidgetHost::class.java)
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), host, personalOnlyUserManager())

        // When deleting a widget id
        repository.deleteAppWidgetId(7)

        // Then it's released through the host, not just silently dropped
        org.mockito.Mockito.verify(host).deleteAppWidgetId(7)
    }

    @Test
    fun `getWidgetProviderOptions tags a Work Profile provider as WORK, leaving the primary user's as PERSONAL`() {
        // Given a resolvable owning app (getWidgetProviderOptions filters out anything whose
        // package the PackageManager can't resolve a label for) with a Work Profile alongside the
        // primary user, each with its own provider
        val appInfo = ApplicationInfo().apply { packageName = "com.example.widgets" }
        val packageInfo = PackageInfo().apply { packageName = "com.example.widgets"; applicationInfo = appInfo }
        shadowOf(context.packageManager).installPackage(packageInfo)
        val personalHandle = Process.myUserHandle()
        val workHandle = mock(UserHandle::class.java)
        val userManager = mock(UserManager::class.java)
        `when`(userManager.userProfiles).thenReturn(listOf(personalHandle, workHandle))
        val appWidgetManager = mock(AppWidgetManager::class.java)
        val personalProvider = AppWidgetProviderInfo().apply { provider = ComponentName("com.example.widgets", ".Clock") }.withResolvableLabel(appInfo)
        val workProvider = AppWidgetProviderInfo().apply { provider = ComponentName("com.example.widgets", ".Clock") }.withResolvableLabel(appInfo)
        `when`(appWidgetManager.getInstalledProvidersForProfile(personalHandle)).thenReturn(listOf(personalProvider))
        `when`(appWidgetManager.getInstalledProvidersForProfile(workHandle)).thenReturn(listOf(workProvider))
        val repository = AppWidgetRepository(context, appWidgetManager, mock(LauncherAppWidgetHost::class.java), userManager)

        // When fetching provider options across every profile
        val options = repository.getWidgetProviderOptions()

        // Then the same provider appears twice, tagged by the profile it came from
        assertEquals(2, options.size)
        assertEquals(setOf(AppProfile.PERSONAL, AppProfile.WORK), options.map { it.profile }.toSet())
    }

    @Test
    fun `bindAppWidgetIdIfAllowed resolves the Work Profile handle and binds against it`() {
        // Given a Work Profile alongside the primary user
        val personalHandle = Process.myUserHandle()
        val workHandle = mock(UserHandle::class.java)
        val userManager = mock(UserManager::class.java)
        `when`(userManager.userProfiles).thenReturn(listOf(personalHandle, workHandle))
        val appWidgetManager = mock(AppWidgetManager::class.java)
        val provider = ComponentName("com.example.widgets", ".Clock")
        `when`(appWidgetManager.bindAppWidgetIdIfAllowed(7, workHandle, provider, null)).thenReturn(true)
        val repository = AppWidgetRepository(context, appWidgetManager, mock(LauncherAppWidgetHost::class.java), userManager)

        // When binding a Work Profile widget
        val bound = repository.bindAppWidgetIdIfAllowed(appWidgetId = 7, provider = provider, profile = AppProfile.WORK)

        // Then it resolved to the Work Profile's own handle, not the primary user's
        assertTrue(bound)
    }

    @Test
    fun `bindAppWidgetIdIfAllowed returns false when WORK is requested but no Work Profile exists`() {
        // Given no Work Profile on this device
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), mock(LauncherAppWidgetHost::class.java), personalOnlyUserManager())

        // Then there's no handle to bind against — false, not a crash
        assertFalse(repository.bindAppWidgetIdIfAllowed(appWidgetId = 7, provider = ComponentName("com.example.widgets", ".Clock"), profile = AppProfile.WORK))
    }

    @Test
    fun `createBindIntent for a Work Profile widget carries EXTRA_APPWIDGET_PROVIDER_PROFILE`() {
        // Given a Work Profile alongside the primary user
        val personalHandle = Process.myUserHandle()
        val workHandle = mock(UserHandle::class.java)
        val userManager = mock(UserManager::class.java)
        `when`(userManager.userProfiles).thenReturn(listOf(personalHandle, workHandle))
        val repository = AppWidgetRepository(context, mock(AppWidgetManager::class.java), mock(LauncherAppWidgetHost::class.java), userManager)
        val provider = ComponentName("com.example.widgets", ".Clock")

        // When building the bind intent for a Work Profile provider
        val intent = repository.createBindIntent(appWidgetId = 7, provider = provider, profile = AppProfile.WORK)

        // Then the system's own bind-permission dialog is told which profile this is
        assertEquals(workHandle, intent.getParcelableExtra<UserHandle>(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE))
    }
}
