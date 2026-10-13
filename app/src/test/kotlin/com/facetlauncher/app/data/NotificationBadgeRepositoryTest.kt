package com.facetlauncher.app.data

import android.os.UserHandle
import com.facetlauncher.app.data.model.NotificationInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock

class NotificationBadgeRepositoryTest {

    private val personal = mock(UserHandle::class.java)
    private val work = mock(UserHandle::class.java)

    private fun listed(
        packageName: String = "com.chat",
        handle: UserHandle = personal,
        key: String = "k",
        title: String? = "Priya",
        text: String? = "Dinner tonight?",
        postTime: Long = 1L,
    ) = NotificationInfo(
        packageName = packageName,
        silent = false,
        key = key,
        userHandle = handle,
        title = title,
        text = text,
        postTime = postTime,
    )

    @Test
    fun `lists an app's notifications newest first`() = runTest {
        // Given three notifications for one app, posted out of order
        val repository = NotificationBadgeRepository()
        repository.setActiveNotifications(listOf(listed(key = "old", postTime = 1), listed(key = "new", postTime = 3), listed(key = "mid", postTime = 2)))

        // When listing them
        val result = repository.notificationsFor("com.chat", personal).first()

        // Then the newest comes first
        assertEquals(listOf("new", "mid", "old"), result.map { it.key })
    }

    @Test
    fun `lists only the requested app in the requested profile`() = runTest {
        // Given the same package in two profiles, plus another app
        val repository = NotificationBadgeRepository()
        repository.setActiveNotifications(
            listOf(
                listed(key = "personal-chat", handle = personal),
                listed(key = "work-chat", handle = work),
                listed(key = "other-app", packageName = "com.mail"),
            ),
        )

        // When listing the work copy of the chat app
        val result = repository.notificationsFor("com.chat", work).first()

        // Then only the work profile's notification is returned
        assertEquals(listOf("work-chat"), result.map { it.key })
    }

    @Test
    fun `leaves out silent media non-clearable secret and contentless notifications`() = runTest {
        // Given one good notification and one of each kind that must not be listed
        val repository = NotificationBadgeRepository()
        repository.setActiveNotifications(
            listOf(
                listed(key = "good"),
                listed(key = "silent").copy(silent = true),
                listed(key = "media").copy(isMedia = true),
                listed(key = "ongoing").copy(isClearable = false),
                listed(key = "secret").copy(isSecret = true),
                listed(key = "empty", title = " ", text = null),
            ),
        )

        // When listing the app's notifications
        val result = repository.notificationsFor("com.chat", personal).first()

        // Then only the good one is listed
        assertEquals(listOf("good"), result.map { it.key })
    }

    @Test
    fun `a notification with only a title or only a body is still listed`() = runTest {
        // Given a title-only and a text-only notification
        val repository = NotificationBadgeRepository()
        repository.setActiveNotifications(listOf(listed(key = "title", text = null, postTime = 2), listed(key = "text", title = null, postTime = 1)))

        // When listing them
        val result = repository.notificationsFor("com.chat", personal).first()

        // Then both are present
        assertEquals(listOf("title", "text"), result.map { it.key })
    }

    @Test
    fun `the list follows a new snapshot and empties when notifications go away`() = runTest {
        // Given a repository listing one notification
        val repository = NotificationBadgeRepository()
        repository.setActiveNotifications(listOf(listed(key = "a")))
        assertEquals(listOf("a"), repository.notificationsFor("com.chat", personal).first().map { it.key })

        // When the next snapshot replaces it, then when it is empty
        repository.setActiveNotifications(listOf(listed(key = "b")))
        assertEquals(listOf("b"), repository.notificationsFor("com.chat", personal).first().map { it.key })
        repository.setActiveNotifications(emptyList())

        // Then nothing is listed
        assertEquals(emptyList<NotificationInfo>(), repository.notificationsFor("com.chat", personal).first())
    }

    @Test
    fun `listing does not change the badge counts`() = runTest {
        // Given a notification that is counted but not listed (no content)
        val repository = NotificationBadgeRepository()
        repository.setActiveNotifications(listOf(listed(key = "empty", title = null, text = null)))

        // Then it still counts toward the badge and is absent from the list
        assertEquals(mapOf("com.chat" to 1), repository.badgeCounts.value)
        assertEquals(emptyList<NotificationInfo>(), repository.notificationsFor("com.chat", personal).first())
    }

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

    @Test
    fun `media notification packages follow the snapshot including silent ones`() {
        // Given a media notification (silent) and an ordinary one
        val repository = NotificationBadgeRepository()
        repository.setActiveNotifications(
            listOf(
                NotificationInfo(packageName = "com.music", silent = true, isMedia = true),
                NotificationInfo(packageName = "com.chat", silent = false),
            ),
        )

        // Then only the media package is listed
        assertEquals(setOf("com.music"), repository.mediaNotificationPackages.value)

        // And once it is gone from the snapshot the set empties
        repository.setActiveNotifications(listOf(NotificationInfo(packageName = "com.chat", silent = false)))
        assertEquals(emptySet<String>(), repository.mediaNotificationPackages.value)
    }
}
