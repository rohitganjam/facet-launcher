package com.facetlauncher.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.facetlauncher.app.data.model.ProQueryResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Optional

class PlayEntitlementRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val dataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create(produceFile = { tempFolder.newFile("entitlement-${System.nanoTime()}.preferences_pb") })
    }

    private val updates = MutableSharedFlow<ProQueryResult>(extraBufferCapacity = 4)

    private fun billing(answer: ProQueryResult) = mock(BillingRepository::class.java).also {
        runBlocking { `when`(it.queryPro()).thenReturn(answer) }
        `when`(it.updates).thenReturn(updates)
    }

    /** A fresh repository over the same DataStore, like a new process start; [answer] is what Play says when asked. */
    private fun TestScope.start(answer: ProQueryResult): PlayEntitlementRepository =
        PlayEntitlementRepository(billing(answer), dataStore, CoroutineScope(backgroundScope.coroutineContext), Optional.empty())

    private suspend fun PlayEntitlementRepository.settled(): Boolean {
        awaitLoaded()
        refresh()
        return isPro.value
    }

    @Test
    fun `a user with nothing cached and no answer from play is free`() = runTest {
        assertEquals(false, start(ProQueryResult.Unavailable).settled())
    }

    @Test
    fun `an owned purchase turns pro on`() = runTest {
        assertEquals(true, start(ProQueryResult.Owned).settled())
    }

    @Test
    fun `pro survives a restart while play cannot answer`() = runTest {
        start(ProQueryResult.Owned).settled()

        assertEquals(true, start(ProQueryResult.Unavailable).settled())
    }

    @Test
    fun `a cold start already reads the cached value before play is asked`() = runTest {
        start(ProQueryResult.Owned).settled()
        val restarted = start(ProQueryResult.Unavailable)

        restarted.awaitLoaded()

        assertEquals(true, restarted.isPro.value)
    }

    @Test
    fun `an explicit no purchase answer turns pro off and stays off after a restart`() = runTest {
        start(ProQueryResult.Owned).settled()

        assertEquals("refunded", false, start(ProQueryResult.NotOwned).settled())
        assertEquals("and stays off while play is unreachable", false, start(ProQueryResult.Unavailable).settled())
    }

    @Test
    fun `play being unreachable never turns pro off`() = runTest {
        start(ProQueryResult.Owned).settled()

        assertEquals(true, start(ProQueryResult.Unavailable).settled())
    }

    @Test
    fun `a pending purchase does not grant pro`() = runTest {
        assertEquals(false, start(ProQueryResult.Pending).settled())
    }

    @Test
    fun `a pending purchase does not revoke pro either`() = runTest {
        start(ProQueryResult.Owned).settled()

        assertEquals(true, start(ProQueryResult.Pending).settled())
    }

    @Test
    fun `a purchase that completes while the app runs turns pro on and is remembered`() = runTest {
        val repository = start(ProQueryResult.NotOwned)
        repository.settled()

        updates.emit(ProQueryResult.Owned)
        repository.isPro.first { it }

        assertEquals(true, start(ProQueryResult.Unavailable).settled())
    }

    private val forced = kotlinx.coroutines.flow.MutableStateFlow<Boolean?>(null)

    private fun TestScope.startWithOverride(answer: ProQueryResult): PlayEntitlementRepository =
        PlayEntitlementRepository(
            billing(answer),
            dataStore,
            CoroutineScope(backgroundScope.coroutineContext),
            Optional.of(object : EntitlementOverride { override val forced = this@PlayEntitlementRepositoryTest.forced }),
        )

    @Test
    fun `refresh returns what play said so restore can report it`() = runTest {
        val repository = start(ProQueryResult.NotOwned)

        assertEquals(ProQueryResult.NotOwned, repository.refresh())
    }

    @Test
    fun `a debug override can force pro on a free user`() = runTest {
        forced.value = true
        val repository = startWithOverride(ProQueryResult.NotOwned)

        assertEquals(true, repository.settled())
    }

    @Test
    fun `a debug override can force free on a pro user without forgetting the purchase`() = runTest {
        start(ProQueryResult.Owned).settled()
        forced.value = false
        val repository = startWithOverride(ProQueryResult.Unavailable)

        assertEquals("forced free", false, repository.settled())

        forced.value = null
        repository.isPro.first { it }
        assertEquals("following play again", true, repository.isPro.value)
    }
}
