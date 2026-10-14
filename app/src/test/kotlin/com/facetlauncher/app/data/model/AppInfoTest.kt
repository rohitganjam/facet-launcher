package com.facetlauncher.app.data.model

import android.os.Parcel
import android.os.UserHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppInfoTest {

    private fun userHandle(id: Int): UserHandle {
        val parcel = Parcel.obtain()
        parcel.writeInt(id)
        parcel.setDataPosition(0)
        return UserHandle(parcel).also { parcel.recycle() }
    }

    private fun app(handle: UserHandle) =
        AppInfo(packageName = "com.google.android.calendar", activityName = ".AllInOneActivity", label = "Calendar", icon = null, userHandle = handle)

    @Test
    fun `the same activity for two users has two different list keys`() {
        // Given the same package and activity installed for the personal user and a Work Profile
        val personal = app(userHandle(0))
        val work = app(userHandle(10))

        // Then their list keys differ, so a LazyColumn keyed by them cannot collide
        assertNotEquals(personal.listKey, work.listKey)
    }

    @Test
    fun `the same activity for the same user always has the same list key`() {
        assertEquals(app(userHandle(10)).listKey, app(userHandle(10)).listKey)
    }
}
