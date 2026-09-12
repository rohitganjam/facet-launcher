package com.facetlauncher.app.data

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
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

    private fun registerResolvableSettingsScreen() {
        val resolveInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                packageName = "com.android.settings"
                name = ".Settings"
            }
        }
        shadowOf(context.packageManager)
            .addResolveInfoForIntent(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS), resolveInfo)
    }

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
        // Given the role is available but Facet already holds it (reinstall case), and the
        // fallback Settings screen resolves to a real activity
        shadowRoleManager.addAvailableRole(RoleManager.ROLE_HOME)
        shadowRoleManager.addHeldRole(RoleManager.ROLE_HOME)
        registerResolvableSettingsScreen()

        // When requesting the intent
        val intent = repository().requestDefaultLauncherIntent()

        // Then it falls back to the same Settings screen the app's own row has always used
        assertEquals(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS, intent.action)
    }

    @Test
    fun `falls back to the Settings screen when the role isn't available at all`() {
        // Given the role API reports it unavailable (older API level / OEM without it), and the
        // fallback Settings screen resolves to a real activity
        // (nothing added to shadowRoleManager — no role is available by default)
        registerResolvableSettingsScreen()

        // When requesting the intent
        val intent = repository().requestDefaultLauncherIntent()

        // Then it falls back to the Settings screen
        assertEquals(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS, intent.action)
    }

    @Test
    fun `falls back to top-level Settings when even the fallback screen can't resolve`() {
        // Given the role isn't available, and the OEM build doesn't expose the default-apps
        // screen either (nothing registered to resolve ACTION_MANAGE_DEFAULT_APPS_SETTINGS)

        // When requesting the intent
        val intent = repository().requestDefaultLauncherIntent()

        // Then it falls back further, so the row's click always launches something
        assertEquals(Settings.ACTION_SETTINGS, intent.action)
    }

    @Test
    fun `isDefaultLauncher is true when the ROLE_HOME role is held`() = runTest {
        // Given the role is available and held by Facet
        shadowRoleManager.addAvailableRole(RoleManager.ROLE_HOME)
        shadowRoleManager.addHeldRole(RoleManager.ROLE_HOME)

        // When checking default-launcher status
        val result = repository().isDefaultLauncher()

        // Then it reports true
        assertEquals(true, result)
    }

    @Test
    fun `isDefaultLauncher is false when the role is available but not held`() = runTest {
        // Given the role is available but Facet doesn't hold it
        shadowRoleManager.addAvailableRole(RoleManager.ROLE_HOME)

        // When checking default-launcher status
        val result = repository().isDefaultLauncher()

        // Then it reports false — not the stale/unreliable plain HOME-intent resolution
        assertEquals(false, result)
    }

    @Test
    fun `isDefaultLauncher falls back to the HOME-intent check when the role isn't available`() = runTest {
        // Given the role API reports it unavailable (older API level / OEM without it), and
        // nothing registered to resolve the HOME intent in Robolectric's fake PackageManager

        // When checking default-launcher status
        val result = repository().isDefaultLauncher()

        // Then it falls back to the plain resolveActivity check, which finds no match
        assertEquals(false, result)
    }
}
