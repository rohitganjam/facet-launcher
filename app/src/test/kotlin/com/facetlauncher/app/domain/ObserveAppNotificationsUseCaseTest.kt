package com.facetlauncher.app.domain

import android.os.UserHandle
import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.NotificationBadgeRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NotificationInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ObserveAppNotificationsUseCaseTest {

    private val handle = mock(UserHandle::class.java)
    private val message = NotificationInfo(
        packageName = "com.chat",
        silent = false,
        key = "k",
        userHandle = handle,
        title = "Priya",
        text = "Dinner tonight?",
    )

    private val settings = MutableStateFlow(LauncherSettings(showNotificationsInAppMenu = true))
    private val badges = NotificationBadgeRepository().also { it.setActiveNotifications(listOf(message)) }

    private fun useCase(granted: Boolean): ObserveAppNotificationsUseCase {
        val settingsRepository = mock(SettingsRepository::class.java).also { `when`(it.settings).thenReturn(settings) }
        val access = mock(NotificationAccessRepository::class.java).also { `when`(it.isGranted()).thenReturn(granted) }
        return ObserveAppNotificationsUseCase(settingsRepository, badges, access)
    }

    @Test
    fun `lists the app's notifications when the setting is on and access is granted`() = runTest {
        // Given the setting on and access granted
        val result = useCase(granted = true)("com.chat", handle).first()

        // Then the app's notifications are listed and no access is needed
        assertEquals(AppNotificationsState(notifications = listOf(message)), result)
    }

    @Test
    fun `shows nothing when the setting is off even with access`() = runTest {
        // Given the setting turned off
        settings.value = LauncherSettings(showNotificationsInAppMenu = false)

        // When observing, then the zone is hidden: nothing is listed and no access is asked for
        assertEquals(AppNotificationsState(enabled = false), useCase(granted = true)("com.chat", handle).first())
    }

    @Test
    fun `asks for access when the setting is on but access is missing`() = runTest {
        // Given access not granted
        val result = useCase(granted = false)("com.chat", handle).first()

        // Then nothing is listed and the sheet is told access is needed
        assertEquals(AppNotificationsState(needsAccess = true), result)
    }

    @Test
    fun `does not ask for access when the setting is off`() = runTest {
        // Given the setting off and no access
        settings.value = LauncherSettings(showNotificationsInAppMenu = false)

        // Then the zone is hidden and the user is not nagged about access
        assertEquals(AppNotificationsState(enabled = false), useCase(granted = false)("com.chat", handle).first())
    }

    @Test
    fun `follows the setting being switched off while observing`() = runTest {
        // Given an observer collecting with the setting on
        val flow = useCase(granted = true)("com.chat", handle)
        assertEquals(listOf(message), flow.first().notifications)

        // When the setting is turned off, then a fresh read is empty
        settings.value = LauncherSettings(showNotificationsInAppMenu = false)
        assertEquals(AppNotificationsState(enabled = false), flow.first())
    }

    @Test
    fun `access granted with nothing to list still shows the zone as an empty state`() = runTest {
        // Given access is granted and the app has no notifications
        badges.setActiveNotifications(emptyList())

        // When observing, then the zone is shown, empty, and not asking for access
        assertEquals(
            AppNotificationsState(notifications = emptyList(), needsAccess = false, enabled = true),
            useCase(granted = true)("com.chat", handle).first(),
        )
    }

    @Test
    fun `the access prompt state keeps the zone shown`() = runTest {
        // Given access is missing, when observing, then the zone is shown with the access prompt
        val state = useCase(granted = false)("com.chat", handle).first()
        assertEquals(true, state.enabled)
        assertEquals(true, state.needsAccess)
    }

    @Test
    fun `another app's notifications are not listed`() = runTest {
        // Given only a different package has a notification
        badges.setActiveNotifications(listOf(message.copy(packageName = "com.mail")))

        // When observing the chat app, then its list is empty
        assertEquals(AppNotificationsState(), useCase(granted = true)("com.chat", handle).first())
    }
}
