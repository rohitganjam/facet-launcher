package com.facetlauncher.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.facetlauncher.app.data.di.DefaultDispatcher
import com.facetlauncher.app.data.di.VaultDataStore
import com.facetlauncher.app.data.model.PinCheck
import com.facetlauncher.app.data.model.VAULT_FREE_ATTEMPTS
import com.facetlauncher.app.data.model.VAULT_PIN_LENGTH
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Clock
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * The App Vault: which apps are hidden from every list in Facet, and the PIN that opens the vault. All of it lives in the
 * `facet_vault` DataStore, which backups and device transfers exclude. Only a salted, Keystore-keyed hash of the PIN is stored.
 */
@Singleton
open class AppVaultRepository @Inject constructor(
    @VaultDataStore private val dataStore: DataStore<Preferences>,
    private val pinHasher: PinHasher,
    private val clock: Clock,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val random = SecureRandom()

    /** [vaultKey]s of every vaulted app, installed or not. */
    open val vaultedKeys: Flow<Set<String>> get() = dataStore.data.map { it[VAULTED] ?: emptySet() }.distinctUntilChanged()

    val hasPin: Flow<Boolean> get() = dataStore.data.map { it[PIN_HASH] != null }.distinctUntilChanged()

    suspend fun hasPinNow(): Boolean = hasPin.first()

    suspend fun add(keys: Collection<String>) {
        dataStore.edit { it[VAULTED] = (it[VAULTED] ?: emptySet()) + keys }
    }

    suspend fun remove(key: String) {
        dataStore.edit { it[VAULTED] = (it[VAULTED] ?: emptySet()) - key }
    }

    /** A whole profile was removed: its vaulted apps can never come back. */
    suspend fun removeByUserId(userId: Int) {
        dataStore.edit { prefs -> prefs[VAULTED] = (prefs[VAULTED] ?: emptySet()).filterNot { it.endsWith("|$userId") }.toSet() }
    }

    suspend fun setPin(pin: String) {
        require(pin.length == VAULT_PIN_LENGTH && pin.all { it.isDigit() }) { "A vault PIN is $VAULT_PIN_LENGTH digits" }
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val hash = withContext(defaultDispatcher) { pinHasher.hash(salt, pin) }
        dataStore.edit {
            it[PIN_SALT] = encode(salt)
            it[PIN_HASH] = encode(hash)
            it[FAILED_ATTEMPTS] = 0
            it.remove(LOCKED_UNTIL)
        }
    }

    suspend fun verifyPin(pin: String): PinCheck {
        val prefs = dataStore.data.first()
        val now = clock.millis()
        val lockedUntil = prefs[LOCKED_UNTIL] ?: 0L
        if (lockedUntil > now) return PinCheck.LockedOut(lockedUntil - now)
        val salt = prefs[PIN_SALT]?.let(::decode)
        val stored = prefs[PIN_HASH]?.let(::decode)
        val matches = salt != null && stored != null &&
            MessageDigest.isEqual(withContext(defaultDispatcher) { pinHasher.hash(salt, pin) }, stored)
        if (matches) {
            dataStore.edit {
                it[FAILED_ATTEMPTS] = 0
                it.remove(LOCKED_UNTIL)
            }
            return PinCheck.Success
        }
        var result: PinCheck = PinCheck.Wrong(attemptsLeft = 0)
        dataStore.edit {
            val failed = (it[FAILED_ATTEMPTS] ?: 0) + 1
            it[FAILED_ATTEMPTS] = failed
            val lockout = lockoutMillisFor(failed)
            result = if (lockout > 0) {
                it[LOCKED_UNTIL] = now + lockout
                PinCheck.LockedOut(lockout)
            } else {
                PinCheck.Wrong(attemptsLeft = VAULT_FREE_ATTEMPTS - failed)
            }
        }
        return result
    }

    /** Milliseconds left of a running lockout, or 0. */
    suspend fun lockoutRemainingMillis(): Long = ((dataStore.data.first()[LOCKED_UNTIL] ?: 0L) - clock.millis()).coerceAtLeast(0L)

    /** Forgot PIN, once the phone's screen lock has been confirmed: the apps stay hidden, the PIN and any lockout are cleared. */
    suspend fun clearPin() {
        dataStore.edit {
            it.remove(PIN_HASH)
            it.remove(PIN_SALT)
            it.remove(FAILED_ATTEMPTS)
            it.remove(LOCKED_UNTIL)
        }
    }

    /** Turns the vault off: every app is back in its lists and the PIN is gone. */
    suspend fun reset() {
        dataStore.edit { it.clear() }
    }

    private fun encode(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    private fun decode(text: String): ByteArray = Base64.getDecoder().decode(text)

    companion object {
        private val VAULTED = stringSetPreferencesKey("vaulted_apps")
        private val PIN_HASH = stringPreferencesKey("pin_hash")
        private val PIN_SALT = stringPreferencesKey("pin_salt")
        private val FAILED_ATTEMPTS = intPreferencesKey("failed_attempts")
        private val LOCKED_UNTIL = longPreferencesKey("locked_until")
        private const val SALT_BYTES = 16

        /** Nothing until the [VAULT_FREE_ATTEMPTS]th miss in a row, then 30 s, 1 min, 2 min, 5 min, and 15 min for every further one. */
        internal fun lockoutMillisFor(failedAttempts: Int): Long = when {
            failedAttempts < VAULT_FREE_ATTEMPTS -> 0L
            failedAttempts == VAULT_FREE_ATTEMPTS -> 30_000L
            failedAttempts == VAULT_FREE_ATTEMPTS + 1 -> 60_000L
            failedAttempts == VAULT_FREE_ATTEMPTS + 2 -> 120_000L
            failedAttempts == VAULT_FREE_ATTEMPTS + 3 -> 300_000L
            else -> 900_000L
        }
    }
}
