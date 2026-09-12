package com.facetlauncher.app.data

import com.facetlauncher.app.data.model.NotificationInfo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationBadgeRepositoryTest {

    @Test
    fun `groups non-silent notifications by package and counts them`() = runTest {
        // Given several notifications, two from the same package
        val repository = NotificationBadgeRepository()

        // When setting the active notifications
        repository.setActiveNotifications(
            listOf(
                NotificationInfo(packageName = "com.example.a", silent = false),
                NotificationInfo(packageName = "com.example.a", silent = false),
                NotificationInfo(packageName = "com.example.b", silent = false),
            ),
        )

        // Then each package's count reflects how many notifications it has
        assertEquals(mapOf("com.example.a" to 2, "com.example.b" to 1), repository.badgeCounts.value)
    }

    @Test
    fun `excludes silent notifications from the counts`() = runTest {
        // Given a mix of silent and non-silent notifications from the same package
        val repository = NotificationBadgeRepository()

        // When setting the active notifications
        repository.setActiveNotifications(
            listOf(
                NotificationInfo(packageName = "com.example.a", silent = true),
                NotificationInfo(packageName = "com.example.a", silent = false),
                NotificationInfo(packageName = "com.example.b", silent = true),
            ),
        )

        // Then only the non-silent notification is counted, and the all-silent package is absent
        assertEquals(mapOf("com.example.a" to 1), repository.badgeCounts.value)
    }

    @Test
    fun `replaces the previous snapshot wholesale rather than accumulating`() = runTest {
        // Given a repository already holding counts from an earlier snapshot
        val repository = NotificationBadgeRepository()
        repository.setActiveNotifications(listOf(NotificationInfo(packageName = "com.example.a", silent = false)))

        // When a new snapshot arrives without that package
        repository.setActiveNotifications(listOf(NotificationInfo(packageName = "com.example.b", silent = false)))

        // Then the old package's count is gone, not merged with the new one
        assertEquals(mapOf("com.example.b" to 1), repository.badgeCounts.value)
    }

    @Test
    fun `an empty notification list clears all counts`() = runTest {
        // Given a repository holding counts
        val repository = NotificationBadgeRepository()
        repository.setActiveNotifications(listOf(NotificationInfo(packageName = "com.example.a", silent = false)))

        // When the listener reports no active notifications
        repository.setActiveNotifications(emptyList())

        // Then the counts are empty
        assertEquals(emptyMap<String, Int>(), repository.badgeCounts.value)
    }
}
