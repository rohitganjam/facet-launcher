package com.facetlauncher.app.data

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

// Robolectric: the permission check goes through ContextCompat, which reads the process and user ids.
@RunWith(RobolectricTestRunner::class)
class LocationRepositoryTest {

    private val pune = Location(LocationManager.NETWORK_PROVIDER).apply { latitude = 18.52; longitude = 73.86 }
    private val manager = mock(LocationManager::class.java)
    private val context = mock(Context::class.java)

    private fun repository(granted: Boolean, dispatcher: CoroutineDispatcher): LocationRepository {
        `when`(context.getSystemService(LocationManager::class.java)).thenReturn(manager)
        `when`(context.checkPermission(anyString(), anyInt(), anyInt()))
            .thenReturn(if (granted) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED)
        return LocationRepository(context, dispatcher)
    }

    @Test
    fun `without the permission there is no location`() = runTest {
        assertNull(repository(granted = false, UnconfinedTestDispatcher(testScheduler)).currentLocation())
    }

    @Test
    fun `a permission taken away during the fix falls back to the last known position`() = runTest {
        // Given the provider on, the fresh fix throwing as if the grant was revoked, and a last known position
        `when`(manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true)
        doThrow(SecurityException("revoked")).`when`(manager).getCurrentLocation(
            org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
        )
        `when`(manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)).thenReturn(pune)

        // When asking for the location
        val location = repository(granted = true, UnconfinedTestDispatcher(testScheduler)).currentLocation()

        // Then it does not crash and uses the last known position
        assertNotNull(location)
        assertEquals(18.52, location!!.latitude, 0.0)
        // And it did not sit out the ten second wait for a fix that would never come
        assertEquals(0L, testScheduler.currentTime)
    }
}
