package com.facetlauncher.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.facetlauncher.app.data.model.PinCheck
import com.facetlauncher.app.data.model.VAULT_FREE_ATTEMPTS
import java.security.MessageDigest
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** A hasher that needs no Keystore: SHA-256 over salt and PIN. */
class FakePinHasher : PinHasher {
    override fun hash(salt: ByteArray, pin: String): ByteArray = MessageDigest.getInstance("SHA-256").digest(salt + pin.toByteArray())
}

class MutableTestClock(var now: Instant = Instant.parse("2026-11-15T10:00:00Z")) : Clock() {
    override fun getZone(): ZoneId = ZoneOffset.UTC
    override fun withZone(zone: ZoneId): Clock = this
    override fun instant(): Instant = now
    fun advanceSeconds(seconds: Long) {
        now = now.plusSeconds(seconds)
    }
}

class AppVaultRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val clock = MutableTestClock()
    private val file by lazy { tempFolder.newFile("vault-${System.nanoTime()}.preferences_pb") }

    private fun TestScope.dataStore(): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = backgroundScope, produceFile = { file })

    private fun TestScope.repository(store: DataStore<Preferences> = dataStore()) =
        AppVaultRepository(store, FakePinHasher(), clock, StandardTestDispatcher(testScheduler))

    @Test
    fun `an empty vault has no apps and no PIN`() = runTest {
        val repository = repository()

        assertEquals(emptySet<String>(), repository.vaultedKeys.first())
        assertFalse(repository.hasPinNow())
    }

    @Test
    fun `apps added to the vault are listed and removing one leaves the rest`() = runTest {
        // Given two vaulted apps
        val repository = repository()
        repository.add(listOf("a|0", "b|0"))

        // When one is removed
        repository.remove("a|0")

        // Then only the other stays
        assertEquals(setOf("b|0"), repository.vaultedKeys.first())
    }

    @Test
    fun `the vault list and the PIN survive a new repository on the same store`() = runTest {
        // Given a vault with an app and a PIN
        val store = dataStore()
        repository(store).apply {
            add(listOf("a|0"))
            setPin("1234")
        }

        // When another repository reads the same store (a process restart)
        val reopened = repository(store)

        // Then both are still there and the PIN still checks out
        assertEquals(setOf("a|0"), reopened.vaultedKeys.first())
        assertTrue(reopened.hasPinNow())
        assertEquals(PinCheck.Success, reopened.verifyPin("1234"))
    }

    @Test
    fun `the PIN is never stored in the clear`() = runTest {
        val repository = repository()
        repository.setPin("4321")

        assertFalse(String(file.readBytes(), Charsets.ISO_8859_1).contains("4321"))
    }

    @Test
    fun `a PIN that is not four digits is refused`() = runTest {
        val repository = repository()

        for (bad in listOf("123", "12345", "12a4", "")) {
            val refused = runCatching { repository.setPin(bad) }.isFailure
            assertTrue("'$bad' should be refused", refused)
        }
        assertFalse(repository.hasPinNow())
    }

    @Test
    fun `a wrong PIN counts down the free attempts and the right one resets them`() = runTest {
        // Given a PIN and two misses
        val repository = repository()
        repository.setPin("1234")
        assertEquals(PinCheck.Wrong(VAULT_FREE_ATTEMPTS - 1), repository.verifyPin("0000"))
        assertEquals(PinCheck.Wrong(VAULT_FREE_ATTEMPTS - 2), repository.verifyPin("0000"))

        // When the right PIN is entered, the count starts over
        assertEquals(PinCheck.Success, repository.verifyPin("1234"))
        assertEquals(PinCheck.Wrong(VAULT_FREE_ATTEMPTS - 1), repository.verifyPin("0000"))
    }

    @Test
    fun `the fifth wrong PIN locks the vault for 30 seconds and even the right PIN is refused`() = runTest {
        // Given four misses
        val repository = repository()
        repository.setPin("1234")
        repeat(VAULT_FREE_ATTEMPTS - 1) { repository.verifyPin("0000") }

        // When the fifth misses
        val fifth = repository.verifyPin("0000")

        // Then it locks for 30 s, and the right PIN is refused meanwhile
        assertEquals(PinCheck.LockedOut(30_000), fifth)
        assertEquals(PinCheck.LockedOut(30_000), repository.verifyPin("1234"))
        assertEquals(30_000L, repository.lockoutRemainingMillis())

        // And after it ends the right PIN works
        clock.advanceSeconds(31)
        assertEquals(0L, repository.lockoutRemainingMillis())
        assertEquals(PinCheck.Success, repository.verifyPin("1234"))
    }

    @Test
    fun `each further miss after a lockout waits longer`() = runTest {
        val repository = repository()
        repository.setPin("1234")
        repeat(VAULT_FREE_ATTEMPTS) { repository.verifyPin("0000") }

        val waits = mutableListOf<Long>()
        repeat(4) {
            clock.advanceSeconds(1000)
            (repository.verifyPin("0000") as PinCheck.LockedOut).let { waits += it.remainingMillis }
        }

        assertEquals(listOf(60_000L, 120_000L, 300_000L, 900_000L), waits)
    }

    @Test
    fun `a lockout survives a restart`() = runTest {
        val store = dataStore()
        val first = repository(store)
        first.setPin("1234")
        repeat(VAULT_FREE_ATTEMPTS) { first.verifyPin("0000") }

        assertEquals(PinCheck.LockedOut(30_000), repository(store).verifyPin("1234"))
    }

    @Test
    fun `lockout lengths follow the ladder`() {
        val ladder = (1..9).map { AppVaultRepository.lockoutMillisFor(it) }

        assertEquals(listOf(0L, 0L, 0L, 0L, 30_000L, 60_000L, 120_000L, 300_000L, 900_000L), ladder)
    }

    @Test
    fun `clearing the PIN keeps the vaulted apps and ends any lockout`() = runTest {
        // Given a locked-out vault with an app
        val repository = repository()
        repository.add(listOf("a|0"))
        repository.setPin("1234")
        repeat(VAULT_FREE_ATTEMPTS) { repository.verifyPin("0000") }

        // When the PIN is cleared after the screen lock was confirmed
        repository.clearPin()

        // Then the app stays vaulted, there is no PIN, and no lockout
        assertEquals(setOf("a|0"), repository.vaultedKeys.first())
        assertFalse(repository.hasPinNow())
        assertEquals(0L, repository.lockoutRemainingMillis())
    }

    @Test
    fun `a reset turns the vault off completely`() = runTest {
        val repository = repository()
        repository.add(listOf("a|0"))
        repository.setPin("1234")

        repository.reset()

        assertEquals(emptySet<String>(), repository.vaultedKeys.first())
        assertFalse(repository.hasPinNow())
    }

    @Test
    fun `removing a profile clears only that user's vaulted apps`() = runTest {
        val repository = repository()
        repository.add(listOf("a|0", "a|10", "b|10", "c|100"))

        repository.removeByUserId(10)

        assertEquals(setOf("a|0", "c|100"), repository.vaultedKeys.first())
    }
}
