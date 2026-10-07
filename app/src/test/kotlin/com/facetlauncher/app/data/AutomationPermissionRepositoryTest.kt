package com.facetlauncher.app.data

import android.Manifest
import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.model.AutomationPermission
import com.facetlauncher.app.data.model.AutomationTrigger
import com.facetlauncher.app.data.model.BatteryDirection
import com.facetlauncher.app.data.model.BatteryLevelCondition
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.time.DayOfWeek

@RunWith(RobolectricTestRunner::class)
class AutomationPermissionRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repository = AutomationPermissionRepository(context)
    private val shadow = shadowOf(context as Application)

    @Test
    fun `a permission is reported as granted once the system grants it`() {
        shadow.denyPermissions(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION)
        assertFalse(repository.isGranted(AutomationPermission.BLUETOOTH_CONNECT))
        assertFalse(repository.isGranted(AutomationPermission.LOCATION))

        shadow.grantPermissions(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION)

        assertTrue(repository.isGranted(AutomationPermission.BLUETOOTH_CONNECT))
        assertTrue(repository.isGranted(AutomationPermission.LOCATION))
    }

    @Test
    fun `a revoked permission is noticed on the next read`() {
        shadow.grantPermissions(Manifest.permission.BLUETOOTH_CONNECT)
        assertTrue(repository.isGranted(AutomationPermission.BLUETOOTH_CONNECT))

        shadow.denyPermissions(Manifest.permission.BLUETOOTH_CONNECT)

        assertFalse(repository.isGranted(AutomationPermission.BLUETOOTH_CONNECT))
    }

    @Test
    fun `a bluetooth rule is usable only with the bluetooth permission`() {
        val rule = AutomationTrigger.Bluetooth("AA:BB", "Car")

        shadow.denyPermissions(Manifest.permission.BLUETOOTH_CONNECT)
        assertFalse(repository.isUsable(rule))
        shadow.grantPermissions(Manifest.permission.BLUETOOTH_CONNECT)
        assertTrue(repository.isUsable(rule))
    }

    @Test
    fun `a named wifi rule needs location but any network does not`() {
        shadow.denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        assertFalse(repository.isUsable(AutomationTrigger.Wifi(ssid = "Home")))
        assertTrue(repository.isUsable(AutomationTrigger.Wifi(ssid = null)))

        shadow.grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        assertTrue(repository.isUsable(AutomationTrigger.Wifi(ssid = "Home")))
    }

    @Test
    fun `schedule headphones and battery rules are always usable`() {
        shadow.denyPermissions(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION)

        assertTrue(repository.isUsable(AutomationTrigger.Schedule(setOf(DayOfWeek.MONDAY), 0, 1)))
        assertTrue(repository.isUsable(AutomationTrigger.Headphones()))
        assertTrue(repository.isUsable(AutomationTrigger.Battery(true, BatteryLevelCondition(BatteryDirection.BELOW, 50))))
    }
}
