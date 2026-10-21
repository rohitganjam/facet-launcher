package com.facetlauncher.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.model.SettingsSearchEntry
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/** Same [android.content.pm.PackageManager.resolveActivity] shadowing approach as [DefaultAppRepositoryTest]/[ContactRepositoryTest]. */
@RunWith(RobolectricTestRunner::class)
class SystemSettingsRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repository = SystemSettingsRepository(context)

    private fun resolveInfoFor(packageName: String) = ResolveInfo().apply {
        activityInfo = ActivityInfo().apply {
            this.packageName = packageName
            name = ".Settings"
        }
    }

    /** Registers a resolvable handler for every catalog action the tests below rely on, so results aren't dropped by the repository's own resolveActivity guard. */
    private fun registerAllCatalogActions() {
        listOf(
            Settings.ACTION_WIFI_SETTINGS,
            Settings.ACTION_BLUETOOTH_SETTINGS,
            Settings.ACTION_DISPLAY_SETTINGS,
        ).forEach { action ->
            shadowOf(context.packageManager).addResolveInfoForIntent(Intent(action), resolveInfoFor("com.android.settings"))
        }
    }

    @Test
    fun `search runs on the injected IO dispatcher`() = runTest {
        // Given a repository whose IO dispatcher is the test scheduler's
        registerAllCatalogActions()
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val injected = SystemSettingsRepository(context, testDispatcher)

        // When search starts, nothing runs until the scheduler is advanced
        val result = async(testDispatcher) { injected.search("wi-f") }
        assertFalse(result.isCompleted)
        advanceUntilIdle()

        // Then it completes deterministically on that dispatcher
        assertEquals(1, result.await().size)
    }

    @Test
    fun `blank query returns no results`() = runTest {
        registerAllCatalogActions()

        assertEquals(emptyList<SettingsSearchEntry>(), repository.search(""))
    }

    @Test
    fun `matches by label`() = runTest {
        registerAllCatalogActions()

        val result = repository.search("wi-f")

        assertEquals(listOf(SettingsSearchEntry(id = "wifi", label = "Wi-Fi", action = Settings.ACTION_WIFI_SETTINGS)), result)
    }

    @Test
    fun `matches by an alias keyword not in the label`() = runTest {
        registerAllCatalogActions()

        // "internet" isn't in the "Wi-Fi" label itself, only in its keyword list
        val result = repository.search("internet")

        assertEquals(listOf(SettingsSearchEntry(id = "wifi", label = "Wi-Fi", action = Settings.ACTION_WIFI_SETTINGS)), result)
    }

    @Test
    fun `an entry the device can't actually resolve is omitted even if its keywords match`() = runTest {
        // Given nothing registered at all — this device/ROM doesn't expose Settings.ACTION_WIFI_SETTINGS
        val result = repository.search("wifi")

        assertEquals(emptyList<SettingsSearchEntry>(), result)
    }

    @Test
    fun `no catalog entry matches an unrelated query`() = runTest {
        registerAllCatalogActions()

        assertEquals(emptyList<SettingsSearchEntry>(), repository.search("xyzzy"))
    }
}
