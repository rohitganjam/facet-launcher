package com.facetlauncher.app.data

import android.app.role.RoleManager
import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class DefaultLauncherRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val roleManager = context.getSystemService(RoleManager::class.java)
    private val shadowRoleManager = shadowOf(roleManager)

    private fun repository() = DefaultLauncherRepository(context)

    @Test
    fun `requests the ROLE_HOME role when available and not already held`() {
        // Given the role API is available and not yet held
        shadowRoleManager.addAvailableRole(RoleManager.ROLE_HOME)

        // When requesting the intent
        val intent = repository().requestDefaultLauncherIntent()

        // Then it's a real role-request intent, not the Settings fallback (the exact action string
        // is a hidden/system-API constant not exposed on the public RoleManager surface, so this
        // asserts the branch taken rather than pinning that string).
        assertNotEquals(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS, intent.action)
    }

    @Test
    fun `falls back to the Settings screen when the role is already held`() {
        // Given the role is available but Facet already holds it (reinstall case)
        shadowRoleManager.addAvailableRole(RoleManager.ROLE_HOME)
        shadowRoleManager.addHeldRole(RoleManager.ROLE_HOME)

        // When requesting the intent
        val intent = repository().requestDefaultLauncherIntent()

        // Then it falls back to the same Settings screen the app's own row has always used
        assertEquals(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS, intent.action)
    }

    @Test
    fun `falls back to the Settings screen when the role isn't available at all`() {
        // Given the role API reports it unavailable (older API level / OEM without it)
        // (nothing added to shadowRoleManager — no role is available by default)

        // When requesting the intent
        val intent = repository().requestDefaultLauncherIntent()

        // Then it falls back to the Settings screen
        assertEquals(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS, intent.action)
    }

    @Test
    fun `isDefaultLauncher is unchanged by the new request-intent method`() = runTest {
        // Given no role state changes at all
        // When checking default-launcher status
        val result = repository().isDefaultLauncher()

        // Then it still resolves via the plain HOME-intent check, independent of RoleManager
        assertEquals(false, result)
    }
}
