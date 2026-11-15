package com.facetlauncher.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    fun `available entries load on the injected IO dispatcher`() = runTest {
        // Given a repository whose IO dispatcher is the test scheduler's
        registerAllCatalogActions()
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val injected = SystemSettingsRepository(context, testDispatcher)

        // When loading starts, nothing runs until the scheduler is advanced
        val result = async(testDispatcher) { injected.availableEntries() }
        assertFalse(result.isCompleted)
        advanceUntilIdle()

        // Then it completes deterministically on that dispatcher
        assertEquals(3, result.await().size)
    }

    @Test
    fun `each available entry carries its label, action and alias keywords`() = runTest {
        // Given a device that resolves Wi-Fi, Bluetooth and Display settings
        registerAllCatalogActions()

        // When the available entries load
        val wifi = repository.availableEntries().single { it.id == "wifi" }

        // Then Wi-Fi keeps the aliases search matches on, like "internet"
        assertEquals("Wi-Fi", wifi.label)
        assertEquals(Settings.ACTION_WIFI_SETTINGS, wifi.action)
        assertTrue("internet" in wifi.keywords)
    }

    @Test
    fun `an entry the device can't actually resolve is omitted`() = runTest {
        // Given only Bluetooth resolvable: this ROM doesn't expose Settings.ACTION_WIFI_SETTINGS
        shadowOf(context.packageManager).addResolveInfoForIntent(
            Intent(Settings.ACTION_BLUETOOTH_SETTINGS), resolveInfoFor("com.android.settings"),
        )

        // When the available entries load
        val ids = repository.availableEntries().map { it.id }

        // Then only Bluetooth is offered
        assertEquals(listOf("bluetooth"), ids)
    }
}
