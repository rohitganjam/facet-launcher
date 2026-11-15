package com.facetlauncher.app.data.model

import android.os.UserHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.mock

class IconOverrideTest {

    private val personal: UserHandle = mock(UserHandle::class.java)
    private val work: UserHandle = mock(UserHandle::class.java)

    private fun app(pkg: String, activity: String = "$pkg.Main", user: UserHandle = personal, profile: AppProfile = AppProfile.PERSONAL) =
        AppInfo(packageName = pkg, activityName = activity, label = pkg, icon = null, profile = profile, userHandle = user)

    private fun override(pkg: String, activity: String = "$pkg.Main", userId: Int = personal.hashCode(), profile: AppProfile = AppProfile.PERSONAL) =
        IconOverride(pkg, activity, userId, profile, IconOverrideSource.OWN)

    @Test
    fun `an override applies to its exact activity and profile only`() {
        // Given the same app in the personal and work profiles, and an override for the personal copy
        val personalCopy = app("com.mail")
        val workCopy = app("com.mail", user = work, profile = AppProfile.WORK)
        val custom = override("com.mail")

        // When matching
        val matched = listOf(custom).matchApps(listOf(personalCopy, workCopy))

        // Then only the personal copy takes it
        assertEquals(custom, matched[personalCopy.listKey])
        assertNull(matched[workCopy.listKey])
    }

    @Test
    fun `a restored row without a user matches by profile`() {
        // Given a restored work override whose profile wasn't found on restore
        val workCopy = app("com.mail", user = work, profile = AppProfile.WORK)
        val restored = override("com.mail", userId = IconOverride.UNRESOLVED_USER_ID, profile = AppProfile.WORK)

        // Then the work copy takes it
        assertEquals(restored, listOf(restored).matchApps(listOf(workCopy))[workCopy.listKey])
    }

    @Test
    fun `an app whose only launcher activity was renamed keeps its custom icon`() {
        // Given an override for an activity the app no longer has, and the app now with one launcher activity
        val renamed = app("com.notes", activity = "com.notes.NewMain")
        val custom = override("com.notes", activity = "com.notes.OldMain")

        // Then the app still takes it
        assertEquals(custom, listOf(custom).matchApps(listOf(renamed))[renamed.listKey])
    }

    @Test
    fun `a package with two launcher activities only matches exactly`() {
        // Given an app with two launcher activities and an override for a third, gone one
        val first = app("com.suite", activity = "com.suite.Docs")
        val second = app("com.suite", activity = "com.suite.Sheets")
        val stale = override("com.suite", activity = "com.suite.Slides")

        // Then neither takes it, since it can't tell which one was meant
        assertEquals(emptyMap<String, IconOverride>(), listOf(stale).matchApps(listOf(first, second)))
    }
}
