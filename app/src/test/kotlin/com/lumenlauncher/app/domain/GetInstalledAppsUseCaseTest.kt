package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.AppRepository
import com.lumenlauncher.app.data.model.AppInfo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class GetInstalledAppsUseCaseTest {

    @Test
    fun `delegates to the repository and returns its result unchanged`() = runTest {
        // Given a repository returning a known list
        val repository = mock(AppRepository::class.java)
        val apps = listOf(AppInfo("com.example.a", ".Main", "A", null))
        `when`(repository.getInstalledApps()).thenReturn(apps)

        // When invoking the use case
        val result = GetInstalledAppsUseCase(repository)()

        // Then it returns exactly what the repository provided
        assertEquals(apps, result)
    }
}
