package com.facetlauncher.app.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppRepositoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    /** A [UserManager] reporting only the personal profile — the common, no-Work-Profile case every other test in this file assumes. */
    private fun personalOnlyUserManager(): UserManager {
        val userManager = mock(UserManager::class.java)
        `when`(userManager.userProfiles).thenReturn(listOf(Process.myUserHandle()))
        return userManager
    }

    private fun fakeActivity(packageName: String, className: String, label: String): LauncherActivityInfo {
        val appInfo = ApplicationInfo().apply { this.packageName = packageName }
        val activity = mock(LauncherActivityInfo::class.java)
        `when`(activity.applicationInfo).thenReturn(appInfo)
        `when`(activity.componentName).thenReturn(android.content.ComponentName(packageName, className))
        `when`(activity.label).thenReturn(label as CharSequence)
        return activity
    }

    @Test
    fun `flattens an adaptive icon without the OS's own corner masking`() = runTest {
        // Given an app whose icon is an AdaptiveIconDrawable with fully opaque background and
        // foreground layers — if the OS's own default masking were still in play (calling
        // AdaptiveIconDrawable.toBitmap() directly, instead of drawing its layers ourselves),
        // the resulting bitmap's corners would be masked out (transparent); our own flattening
        // draws the layers past their safe-zone bounds instead, so nothing masks the corners
        // except AppIcon's own clip() downstream — see AppRepository.flattenIcon's own doc.
        val launcherApps = mock(LauncherApps::class.java)
        val activity = fakeActivity("com.example.adaptive", ".Main", "Adaptive")
        val adaptiveIcon = AdaptiveIconDrawable(ColorDrawable(Color.RED), ColorDrawable(Color.BLUE))
        `when`(activity.getIcon(0)).thenReturn(adaptiveIcon)
        `when`(launcherApps.getActivityList(eq(null), any())).thenReturn(listOf(activity))

        // When fetching installed apps
        val result = AppRepository(launcherApps, personalOnlyUserManager(), context).getInstalledApps()

        // Then the flattened icon's corner pixel is opaque, not masked away
        val bitmap = result[0].icon!!.asAndroidBitmap()
        assertNotEquals(0, Color.alpha(bitmap.getPixel(0, 0)))
    }

    @Test
    fun `maps LauncherActivityInfo entries to AppInfo with matching package, activity, and label`() = runTest {
        // Given a LauncherApps that reports two activities
        val launcherApps = mock(LauncherApps::class.java)
        val one = fakeActivity("com.example.one", ".MainActivity", "One")
        val two = fakeActivity("com.example.two", ".MainActivity", "Two")
        `when`(launcherApps.getActivityList(eq(null), any())).thenReturn(listOf(one, two))

        // When fetching installed apps
        val result = AppRepository(launcherApps, personalOnlyUserManager(), context).getInstalledApps()

        // Then both are mapped with their package/activity/label preserved
        assertEquals(2, result.size)
        assertEquals("com.example.one", result[0].packageName)
        assertEquals(".MainActivity", result[0].activityName)
        assertEquals("One", result[0].label)
    }

    @Test
    fun `sorts apps alphabetically by label, case-insensitive`() = runTest {
        // Given activities reported out of order with mixed case labels
        val launcherApps = mock(LauncherApps::class.java)
        val zebra = fakeActivity("com.example.zebra", ".Main", "zebra")
        val apple = fakeActivity("com.example.apple", ".Main", "Apple")
        val mango = fakeActivity("com.example.mango", ".Main", "mango")
        `when`(launcherApps.getActivityList(eq(null), any())).thenReturn(listOf(zebra, apple, mango))

        // When fetching installed apps
        val result = AppRepository(launcherApps, personalOnlyUserManager(), context).getInstalledApps()

        // Then they come back sorted case-insensitively: Apple, mango, zebra
        assertEquals(listOf("Apple", "mango", "zebra"), result.map { it.label })
    }

    @Test
    fun `observeInstalledApps re-emits after a package is removed`() = runTest {
        // Given a LauncherApps that reports two apps, then one after a simulated removal
        val launcherApps = mock(LauncherApps::class.java)
        val one = fakeActivity("com.example.one", ".Main", "One")
        val two = fakeActivity("com.example.two", ".Main", "Two")
        `when`(launcherApps.getActivityList(eq(null), any()))
            .thenReturn(listOf(one, two), listOf(one))
        val repository = AppRepository(launcherApps, personalOnlyUserManager(), context)

        // When collecting the live flow into a channel — genuine suspension on receive() (unlike
        // advanceUntilIdle(), which only pumps the virtual test-dispatcher queue) correctly waits
        // out getInstalledApps()'s real withContext(Dispatchers.Default) hop each time
        val emissions = Channel<List<AppInfo>>(Channel.UNLIMITED)
        val collectJob = launch { repository.observeInstalledApps().collect { emissions.send(it) } }

        // Then it emits immediately on subscription, and again once the registered
        // LauncherApps.Callback fires — simulating an uninstall while Facet is in the foreground
        assertEquals(listOf("One", "Two"), emissions.receive().map { it.label })

        val callbackCaptor = ArgumentCaptor.forClass(LauncherApps.Callback::class.java)
        verify(launcherApps).registerCallback(callbackCaptor.capture())
        callbackCaptor.value.onPackageRemoved("com.example.two", Process.myUserHandle())

        assertEquals(listOf("One"), emissions.receive().map { it.label })
        collectJob.cancel()
    }

    @Test
    fun `tags apps from a second user profile as WORK, leaving the primary user's apps PERSONAL`() = runTest {
        // Given a UserManager reporting a Work Profile alongside the primary user, each with its own app
        val launcherApps = mock(LauncherApps::class.java)
        val personalHandle = Process.myUserHandle()
        val workHandle = mock(UserHandle::class.java)
        val userManager = mock(UserManager::class.java)
        `when`(userManager.userProfiles).thenReturn(listOf(personalHandle, workHandle))

        val personalApp = fakeActivity("com.example.chat", ".Main", "Chat")
        val workApp = fakeActivity("com.example.chat", ".Main", "Chat")
        `when`(launcherApps.getActivityList(null, personalHandle)).thenReturn(listOf(personalApp))
        `when`(launcherApps.getActivityList(null, workHandle)).thenReturn(listOf(workApp))

        // When fetching installed apps across every profile
        val result = AppRepository(launcherApps, userManager, context).getInstalledApps()

        // Then the same package+activity appears twice, tagged by the profile it came from
        assertEquals(2, result.size)
        assertEquals(setOf(AppProfile.PERSONAL, AppProfile.WORK), result.map { it.profile }.toSet())
    }

    @Test
    fun `resolveUserHandle finds the Work Profile handle, and returns null when there isn't one`() {
        // Given a Work Profile alongside the primary user
        val personalHandle = Process.myUserHandle()
        val workHandle = mock(UserHandle::class.java)
        val userManager = mock(UserManager::class.java)
        `when`(userManager.userProfiles).thenReturn(listOf(personalHandle, workHandle))
        val repository = AppRepository(mock(LauncherApps::class.java), userManager, context)

        // Then WORK resolves to the non-primary handle, and PERSONAL to the primary one
        assertEquals(workHandle, repository.resolveUserHandle(AppProfile.WORK))
        assertEquals(personalHandle, repository.resolveUserHandle(AppProfile.PERSONAL))

        // And when no Work Profile exists at all, WORK resolves to nothing
        `when`(userManager.userProfiles).thenReturn(listOf(personalHandle))
        assertEquals(null, repository.resolveUserHandle(AppProfile.WORK))
    }
}

private fun <T> eq(value: T): T = org.mockito.Mockito.eq(value) ?: value
private fun <T> any(): T = org.mockito.Mockito.any()
