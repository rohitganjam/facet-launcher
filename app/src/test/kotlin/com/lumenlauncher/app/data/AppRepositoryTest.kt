package com.lumenlauncher.app.data

import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.os.Process
import com.lumenlauncher.app.data.model.AppInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppRepositoryTest {

    private fun fakeActivity(packageName: String, className: String, label: String): LauncherActivityInfo {
        val appInfo = ApplicationInfo().apply { this.packageName = packageName }
        val activity = mock(LauncherActivityInfo::class.java)
        `when`(activity.applicationInfo).thenReturn(appInfo)
        `when`(activity.componentName).thenReturn(android.content.ComponentName(packageName, className))
        `when`(activity.label).thenReturn(label as CharSequence)
        return activity
    }

    @Test
    fun `maps LauncherActivityInfo entries to AppInfo with matching package, activity, and label`() = runTest {
        // Given a LauncherApps that reports two activities
        val launcherApps = mock(LauncherApps::class.java)
        val one = fakeActivity("com.example.one", ".MainActivity", "One")
        val two = fakeActivity("com.example.two", ".MainActivity", "Two")
        `when`(launcherApps.getActivityList(eq(null), any())).thenReturn(listOf(one, two))

        // When fetching installed apps
        val result = AppRepository(launcherApps).getInstalledApps()

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
        val result = AppRepository(launcherApps).getInstalledApps()

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
        val repository = AppRepository(launcherApps)

        // When collecting the live flow into a channel — genuine suspension on receive() (unlike
        // advanceUntilIdle(), which only pumps the virtual test-dispatcher queue) correctly waits
        // out getInstalledApps()'s real withContext(Dispatchers.Default) hop each time
        val emissions = Channel<List<AppInfo>>(Channel.UNLIMITED)
        val collectJob = launch { repository.observeInstalledApps().collect { emissions.send(it) } }

        // Then it emits immediately on subscription, and again once the registered
        // LauncherApps.Callback fires — simulating an uninstall while Lumen is in the foreground
        assertEquals(listOf("One", "Two"), emissions.receive().map { it.label })

        val callbackCaptor = ArgumentCaptor.forClass(LauncherApps.Callback::class.java)
        verify(launcherApps).registerCallback(callbackCaptor.capture())
        callbackCaptor.value.onPackageRemoved("com.example.two", Process.myUserHandle())

        assertEquals(listOf("One"), emissions.receive().map { it.label })
        collectJob.cancel()
    }
}

private fun <T> eq(value: T): T = org.mockito.Mockito.eq(value) ?: value
private fun <T> any(): T = org.mockito.Mockito.any()
