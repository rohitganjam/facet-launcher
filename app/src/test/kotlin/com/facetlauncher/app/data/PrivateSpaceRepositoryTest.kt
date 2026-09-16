package com.facetlauncher.app.data

import android.content.Context
import android.os.UserHandle
import android.os.UserManager
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.model.AppProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PrivateSpaceRepositoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `state is NotConfigured when no Private Space handle exists`() = runTest {
        val userManager = mock(UserManager::class.java)
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.resolveUserHandle(AppProfile.PRIVATE)).thenReturn(null)
        val repository = PrivateSpaceRepository(userManager, appRepository, context)

        assertEquals(PrivateSpaceState.NotConfigured, repository.observePrivateSpaceState().first())
    }

    @Test
    fun `state is Locked when the handle exists and quiet mode is enabled`() = runTest {
        val handle = mock(UserHandle::class.java)
        val userManager = mock(UserManager::class.java)
        `when`(userManager.isQuietModeEnabled(handle)).thenReturn(true)
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.resolveUserHandle(AppProfile.PRIVATE)).thenReturn(handle)
        val repository = PrivateSpaceRepository(userManager, appRepository, context)

        assertEquals(PrivateSpaceState.Locked, repository.observePrivateSpaceState().first())
    }

    @Test
    fun `state is Unlocked when the handle exists and quiet mode is disabled`() = runTest {
        val handle = mock(UserHandle::class.java)
        val userManager = mock(UserManager::class.java)
        `when`(userManager.isQuietModeEnabled(handle)).thenReturn(false)
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.resolveUserHandle(AppProfile.PRIVATE)).thenReturn(handle)
        val repository = PrivateSpaceRepository(userManager, appRepository, context)

        assertEquals(PrivateSpaceState.Unlocked, repository.observePrivateSpaceState().first())
    }

    @Test
    fun `isQuietModeEnabled throwing does not crash observePrivateSpaceState`() = runTest {
        // Given a resolved Private Space handle, but the system call to check its quiet-mode
        // state throws (e.g. this launcher isn't recognized as eligible to query it on some OEM
        // build) — mirrors the runCatching guard AppWidgetRepository/WorkProfileRepository already
        // use around the same isQuietModeEnabled call.
        val handle = mock(UserHandle::class.java)
        val userManager = mock(UserManager::class.java)
        `when`(userManager.isQuietModeEnabled(handle)).thenThrow(IllegalStateException("not eligible"))
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.resolveUserHandle(AppProfile.PRIVATE)).thenReturn(handle)
        val repository = PrivateSpaceRepository(userManager, appRepository, context)

        // Then it degrades to "not quiet mode" (Unlocked) — runCatching { }.getOrDefault(false) —
        // rather than propagating the exception
        assertEquals(PrivateSpaceState.Unlocked, repository.observePrivateSpaceState().first())
    }

    @Test
    fun `requestUnlock calls requestQuietModeEnabled false against the resolved handle`() {
        val handle = mock(UserHandle::class.java)
        val userManager = mock(UserManager::class.java)
        `when`(userManager.requestQuietModeEnabled(false, handle)).thenReturn(true)
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.resolveUserHandle(AppProfile.PRIVATE)).thenReturn(handle)
        val repository = PrivateSpaceRepository(userManager, appRepository, context)

        assertEquals(true, repository.requestUnlock())
    }

    @Test
    fun `requestUnlock is a no-op when Private Space isn't configured`() {
        val userManager = mock(UserManager::class.java)
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.resolveUserHandle(AppProfile.PRIVATE)).thenReturn(null)
        val repository = PrivateSpaceRepository(userManager, appRepository, context)

        assertFalse(repository.requestUnlock())
        // resolveUserHandle returns null before ever reaching UserManager — no call was attempted.
        verifyNoInteractions(userManager)
    }
}
