package com.facetlauncher.app.data.model

/** A vault PIN is exactly this many digits. */
const val VAULT_PIN_LENGTH = 4

/** Wrong PINs allowed in a row before the first lockout. */
const val VAULT_FREE_ATTEMPTS = 5

/** Identity of a vaulted app: its package in one profile, whatever its launcher activities. */
fun vaultKey(packageName: String, userId: Int): String = "$packageName|$userId"

/** The package and user id a [vaultKey] was made from, or null for a malformed one. */
fun parseVaultKey(key: String): Pair<String, Int>? {
    val at = key.lastIndexOf('|')
    if (at <= 0) return null
    return key.substring(at + 1).toIntOrNull()?.let { key.substring(0, at) to it }
}

val AppInfo.vaultKey: String get() = vaultKey(packageName, userHandle.hashCode())

/** How a PIN check came out. */
sealed interface PinCheck {
    data object Success : PinCheck

    /** Wrong PIN; [attemptsLeft] more are allowed before a lockout starts. */
    data class Wrong(val attemptsLeft: Int) : PinCheck

    /** Too many wrong PINs; nothing is accepted for [remainingMillis]. */
    data class LockedOut(val remainingMillis: Long) : PinCheck
}
