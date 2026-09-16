package com.facetlauncher.app.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.LauncherUserInfo
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
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
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
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class AppRepositoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    /** Robolectric's broadcast delivery is queued on the main looper, not synchronous — pump both the virtual test-dispatcher queue and the looper. */
    private fun TestScope.idle() {
        advanceUntilIdle()
        shadowOf(android.os.Looper.getMainLooper()).idle()
        advanceUntilIdle()
    }

    /** A [UserManager] reporting only the personal profile — the common, no-Work-Profile case every other test in this file assumes. */
    private fun personalOnlyUserManager(): UserManager {
        val userManager = mock(UserManager::class.java)
        `when`(userManager.userProfiles).thenReturn(listOf(Process.myUserHandle()))
        return userManager
    }

    /** Stubs [handle] to positively resolve as a real Work Profile via `getLauncherUserInfo`, matching [AppRepository.profileFor]'s real API 35+ check. */
    private fun stubAsManagedProfile(launcherApps: LauncherApps, handle: UserHandle) {
        val info = mock(LauncherUserInfo::class.java)
        `when`(info.userType).thenReturn(UserManager.USER_TYPE_PROFILE_MANAGED)
        `when`(launcherApps.getLauncherUserInfo(handle)).thenReturn(info)
    }

    /** Stubs [handle] to positively resolve as a real Private Space via `getLauncherUserInfo` — the positive case, once `ACCESS_HIDDEN_PROFILES` is held. */
    private fun stubAsPrivateProfile(launcherApps: LauncherApps, handle: UserHandle) {
        val info = mock(LauncherUserInfo::class.java)
        `when`(info.userType).thenReturn(UserManager.USER_TYPE_PROFILE_PRIVATE)
        `when`(launcherApps.getLauncherUserInfo(handle)).thenReturn(info)
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
        // backgroundScope, not the constructor's default real scope — observeInstalledApps() is
        // now shareIn'd (see AppRepository's own doc), and its sharing coroutine needs to live in
        // a scope this test actually controls so it's deterministically torn down (including the
        // registerCallback/registerReceiver cleanup in observeInstalledAppsUncached's awaitClose)
        // when runTest ends, instead of leaking a real Dispatchers.Default coroutine that can fire
        // its cleanup — and crash — during some unrelated, later-running test.
        val repository = AppRepository(launcherApps, personalOnlyUserManager(), context, backgroundScope)

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
    @Config(sdk = [35])
    fun `tags apps from a second user profile as WORK, leaving the primary user's apps PERSONAL`() = runTest {
        // Given a UserManager reporting a Work Profile alongside the primary user, each with its own app
        val launcherApps = mock(LauncherApps::class.java)
        val personalHandle = Process.myUserHandle()
        val workHandle = mock(UserHandle::class.java)
        val userManager = mock(UserManager::class.java)
        `when`(userManager.userProfiles).thenReturn(listOf(personalHandle, workHandle))
        stubAsManagedProfile(launcherApps, workHandle)

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
    @Config(sdk = [35])
    fun `resolveUserHandle finds the Work Profile handle, and returns null when there isn't one`() {
        // Given a Work Profile alongside the primary user
        val personalHandle = Process.myUserHandle()
        val workHandle = mock(UserHandle::class.java)
        val userManager = mock(UserManager::class.java)
        `when`(userManager.userProfiles).thenReturn(listOf(personalHandle, workHandle))
        val launcherApps = mock(LauncherApps::class.java)
        stubAsManagedProfile(launcherApps, workHandle)
        val repository = AppRepository(launcherApps, userManager, context)

        // Then WORK resolves to the non-primary handle, and PERSONAL to the primary one
        assertEquals(workHandle, repository.resolveUserHandle(AppProfile.WORK))
        assertEquals(personalHandle, repository.resolveUserHandle(AppProfile.PERSONAL))

        // And when no Work Profile exists at all, WORK resolves to nothing
        `when`(userManager.userProfiles).thenReturn(listOf(personalHandle))
        assertEquals(null, repository.resolveUserHandle(AppProfile.WORK))
    }

    @Test
    @Config(sdk = [35])
    fun `does not classify a non-Work profile handle as WORK — regression for the Private Space false positive`() {
        // Given a second profile handle that exists (like Android 15+'s Private Space) but isn't
        // a real Work Profile — getLauncherUserInfo returns null for it without ACCESS_HIDDEN_PROFILES,
        // confirmed live against a real Private Space profile on an API 36 emulator (see chat history):
        // UserManager.getUserProfiles() includes it, but getLauncherUserInfo comes back null.
        val personalHandle = Process.myUserHandle()
        val otherHandle = mock(UserHandle::class.java)
        val userManager = mock(UserManager::class.java)
        `when`(userManager.userProfiles).thenReturn(listOf(personalHandle, otherHandle))
        val launcherApps = mock(LauncherApps::class.java)
        `when`(launcherApps.getLauncherUserInfo(otherHandle)).thenReturn(null)
        val repository = AppRepository(launcherApps, userManager, context)

        // Then WorkProfileRepository-style resolution (via resolveUserHandle) finds no Work
        // Profile at all — the other handle is never mistaken for one, so hasWorkProfile() (and
        // the Drawer's Work tab, and the Settings row) stay correctly hidden.
        assertEquals(null, repository.resolveUserHandle(AppProfile.WORK))
        assertEquals(personalHandle, repository.resolveUserHandle(AppProfile.PERSONAL))
    }

    @Test
    @Config(sdk = [35])
    fun `tags a real Private Space handle as PRIVATE, not PERSONAL or WORK`() = runTest {
        // Given a UserManager reporting a Private Space alongside the primary user, with
        // ACCESS_HIDDEN_PROFILES held (getLauncherUserInfo positively resolves userType)
        val launcherApps = mock(LauncherApps::class.java)
        val personalHandle = Process.myUserHandle()
        val privateHandle = mock(UserHandle::class.java)
        val userManager = mock(UserManager::class.java)
        `when`(userManager.userProfiles).thenReturn(listOf(personalHandle, privateHandle))
        stubAsPrivateProfile(launcherApps, privateHandle)

        val personalApp = fakeActivity("com.example.notes", ".Main", "Notes")
        val privateApp = fakeActivity("com.example.notes", ".Main", "Notes")
        `when`(launcherApps.getActivityList(null, personalHandle)).thenReturn(listOf(personalApp))
        `when`(launcherApps.getActivityList(null, privateHandle)).thenReturn(listOf(privateApp))

        // When fetching installed apps across every profile
        val result = AppRepository(launcherApps, userManager, context).getInstalledApps()

        // Then the same package+activity appears twice, tagged by the profile it came from
        assertEquals(2, result.size)
        assertEquals(setOf(AppProfile.PERSONAL, AppProfile.PRIVATE), result.map { it.profile }.toSet())
    }

    @Test
    @Config(sdk = [34])
    fun `profileFor falls back to OTHER, not WORK, for a non-primary handle below API 35`() {
        // Given a non-primary handle on an OS version with no getLauncherUserInfo API at all
        // (below API 35) — the old behavior guessed "not primary = WORK", which misclassified an
        // OEM clone/dual-app profile as a genuine Work Profile (see chat history: this both
        // crashed calling isQuietModeEnabled on it and hid its apps behind a Work tab).
        val launcherApps = mock(LauncherApps::class.java)
        val otherHandle = mock(UserHandle::class.java)
        val repository = AppRepository(launcherApps, personalOnlyUserManager(), context)

        // Then it's classified OTHER, never WORK, and never PERSONAL
        assertEquals(AppProfile.OTHER, repository.profileFor(otherHandle))
    }

    @Test
    fun `getInstalledApps carries each app's real UserHandle`() = runTest {
        // Given an app enumerated from a specific (mocked, non-primary) handle
        val launcherApps = mock(LauncherApps::class.java)
        val workHandle = mock(UserHandle::class.java)
        val userManager = mock(UserManager::class.java)
        `when`(userManager.userProfiles).thenReturn(listOf(workHandle))
        val activity = fakeActivity("com.example.work", ".Main", "Work App")
        `when`(launcherApps.getActivityList(null, workHandle)).thenReturn(listOf(activity))

        // When fetching installed apps
        val result = AppRepository(launcherApps, userManager, context).getInstalledApps()

        // Then the resulting AppInfo carries that exact real handle, not just a display category
        assertEquals(workHandle, result.single().userHandle)
    }

    @Test
    fun `getInstalledApps does not crash when one profile's getActivityList throws`() = runTest {
        // Given two profiles, one of which throws when queried (a device-specific LauncherApps
        // quirk, e.g. an OEM clone profile not fully honoring the contract)
        val launcherApps = mock(LauncherApps::class.java)
        val personalHandle = Process.myUserHandle()
        val brokenHandle = mock(UserHandle::class.java)
        val userManager = mock(UserManager::class.java)
        `when`(userManager.userProfiles).thenReturn(listOf(personalHandle, brokenHandle))
        val goodApp = fakeActivity("com.example.good", ".Main", "Good")
        `when`(launcherApps.getActivityList(null, personalHandle)).thenReturn(listOf(goodApp))
        `when`(launcherApps.getActivityList(null, brokenHandle)).thenThrow(IllegalStateException("broken profile"))

        // When fetching installed apps across every profile
        val result = AppRepository(launcherApps, userManager, context).getInstalledApps()

        // Then the other handle's apps still come back, rather than the whole fetch failing
        assertEquals(listOf("Good"), result.map { it.label })
    }

    @Test
    fun `getInstalledApps drops a single entry whose fields throw BadParcelableException, without losing the rest`() = runTest {
        // Given getActivityList() itself succeeds, but one returned LauncherActivityInfo throws
        // on field access — a real crash confirmed on a Techno Spark 20 Pro: the OS/OEM's
        // LauncherApps implementation for some profile (e.g. an OEM clone profile) can hand back
        // an entry whose embedded Parcelable fields don't unmarshal cleanly in this process, even
        // though the getActivityList() call that returned it didn't throw at all.
        val launcherApps = mock(LauncherApps::class.java)
        val goodApp = fakeActivity("com.example.good", ".Main", "Good")
        val brokenApp = mock(LauncherActivityInfo::class.java)
        `when`(brokenApp.applicationInfo).thenThrow(android.os.BadParcelableException("corrupt"))
        `when`(launcherApps.getActivityList(eq(null), any())).thenReturn(listOf(goodApp, brokenApp))

        // When fetching installed apps
        val result = AppRepository(launcherApps, personalOnlyUserManager(), context).getInstalledApps()

        // Then the broken entry is silently dropped, not crashing the whole fetch
        assertEquals(listOf("Good"), result.map { it.label })
    }

    @Test
    fun `observeProfileRemoved emits the removed handle from EXTRA_USER`() = runTest {
        // Given a live subscription to observeProfileRemoved
        val launcherApps = mock(LauncherApps::class.java)
        val repository = AppRepository(launcherApps, personalOnlyUserManager(), context)
        val emissions = mutableListOf<UserHandle>()
        val collectJob = launch { repository.observeProfileRemoved().collect { emissions.add(it) } }
        idle()

        // When a real ACTION_MANAGED_PROFILE_REMOVED broadcast arrives, carrying the removed handle
        val removedHandle = Process.myUserHandle()
        val intent = android.content.Intent(android.content.Intent.ACTION_MANAGED_PROFILE_REMOVED)
            .putExtra(android.content.Intent.EXTRA_USER, removedHandle)
        context.sendBroadcast(intent)
        idle()
        collectJob.cancel()

        // Then it emits exactly that handle
        assertEquals(listOf(removedHandle), emissions)
    }

    @Test
    @Config(sdk = [35])
    fun `resolveUserHandle finds the Private Space handle, distinct from Work Profile`() {
        // Given both a Work Profile and a Private Space alongside the primary user
        val personalHandle = Process.myUserHandle()
        val workHandle = mock(UserHandle::class.java)
        val privateHandle = mock(UserHandle::class.java)
        val userManager = mock(UserManager::class.java)
        `when`(userManager.userProfiles).thenReturn(listOf(personalHandle, workHandle, privateHandle))
        val launcherApps = mock(LauncherApps::class.java)
        stubAsManagedProfile(launcherApps, workHandle)
        stubAsPrivateProfile(launcherApps, privateHandle)
        val repository = AppRepository(launcherApps, userManager, context)

        // Then each profile resolves to its own distinct handle
        assertEquals(workHandle, repository.resolveUserHandle(AppProfile.WORK))
        assertEquals(privateHandle, repository.resolveUserHandle(AppProfile.PRIVATE))
        assertEquals(personalHandle, repository.resolveUserHandle(AppProfile.PERSONAL))
    }

    @Test
    @Config(sdk = [35])
    fun `classifies and keeps distinct a Work Profile, a Private Space, and an unclassifiable third profile all present at once`() = runTest {
        // Given the real-world stress case this whole identity redesign exists to survive: an
        // MDM-managed device where the user also has Private Space configured AND an OEM
        // clone/dual-app profile — four handles total (primary + three non-primary), each with its
        // own installed copy of the same package+activity.
        val personalHandle = Process.myUserHandle()
        val workHandle = mock(UserHandle::class.java)
        val privateHandle = mock(UserHandle::class.java)
        val cloneHandle = mock(UserHandle::class.java)
        val userManager = mock(UserManager::class.java)
        `when`(userManager.userProfiles).thenReturn(listOf(personalHandle, workHandle, privateHandle, cloneHandle))
        val launcherApps = mock(LauncherApps::class.java)
        stubAsManagedProfile(launcherApps, workHandle)
        stubAsPrivateProfile(launcherApps, privateHandle)
        // The clone profile reports its own distinct, unrecognized userType — getLauncherUserInfo
        // doesn't come back null (unlike the pre-35 case), it just isn't MANAGED or PRIVATE.
        val cloneInfo = mock(LauncherUserInfo::class.java)
        `when`(cloneInfo.userType).thenReturn("com.oem.clone_profile")
        `when`(launcherApps.getLauncherUserInfo(cloneHandle)).thenReturn(cloneInfo)

        val personalApp = fakeActivity("com.example.chat", ".Main", "Chat")
        val workApp = fakeActivity("com.example.chat", ".Main", "Chat")
        val privateApp = fakeActivity("com.example.chat", ".Main", "Chat")
        val cloneApp = fakeActivity("com.example.chat", ".Main", "Chat")
        `when`(launcherApps.getActivityList(null, personalHandle)).thenReturn(listOf(personalApp))
        `when`(launcherApps.getActivityList(null, workHandle)).thenReturn(listOf(workApp))
        `when`(launcherApps.getActivityList(null, privateHandle)).thenReturn(listOf(privateApp))
        `when`(launcherApps.getActivityList(null, cloneHandle)).thenReturn(listOf(cloneApp))
        val repository = AppRepository(launcherApps, userManager, context)

        // 1. profileFor classifies all three non-primary handles correctly and distinctly
        assertEquals(AppProfile.PERSONAL, repository.profileFor(personalHandle))
        assertEquals(AppProfile.WORK, repository.profileFor(workHandle))
        assertEquals(AppProfile.PRIVATE, repository.profileFor(privateHandle))
        assertEquals(AppProfile.OTHER, repository.profileFor(cloneHandle))

        // 2. getInstalledApps returns all four copies, each carrying its own real handle and the
        // matching display profile — none collapsed or confused with another
        val apps = repository.getInstalledApps()
        assertEquals(4, apps.size)
        assertEquals(setOf(personalHandle, workHandle, privateHandle, cloneHandle), apps.map { it.userHandle }.toSet())
        val profileByHandle = apps.associate { it.userHandle to it.profile }
        assertEquals(AppProfile.PERSONAL, profileByHandle[personalHandle])
        assertEquals(AppProfile.WORK, profileByHandle[workHandle])
        assertEquals(AppProfile.PRIVATE, profileByHandle[privateHandle])
        assertEquals(AppProfile.OTHER, profileByHandle[cloneHandle])

        // 3. resolveUserHandle finds only its own profile's handle — never the clone (OTHER) handle,
        // and WORK/PRIVATE are never confused with each other
        assertEquals(workHandle, repository.resolveUserHandle(AppProfile.WORK))
        assertEquals(privateHandle, repository.resolveUserHandle(AppProfile.PRIVATE))

        // 4. handlesFor(OTHER) finds exactly the clone handle — not WORK or PRIVATE
        assertEquals(listOf(cloneHandle), repository.handlesFor(AppProfile.OTHER))
    }
}

private fun <T> eq(value: T): T = org.mockito.Mockito.eq(value) ?: value
private fun <T> any(): T = org.mockito.Mockito.any()
