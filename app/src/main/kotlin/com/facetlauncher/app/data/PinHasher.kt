package com.facetlauncher.app.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/** Turns a PIN and its salt into the bytes that are stored and compared; never the PIN itself. */
interface PinHasher {
    fun hash(salt: ByteArray, pin: String): ByteArray
}

/**
 * HMAC-SHA256 keyed by a non-exportable Android Keystore key, so a copied `facet_vault` file can't be brute-forced off the phone
 * (a 4-digit PIN has only 10,000 values). Falls back to PBKDF2 on a phone whose Keystore refuses the key.
 */
@Singleton
class KeystorePinHasher @Inject constructor() : PinHasher {

    override fun hash(salt: ByteArray, pin: String): ByteArray = runCatching { hmac(salt, pin) }.getOrElse { pbkdf2(salt, pin) }

    private fun hmac(salt: ByteArray, pin: String): ByteArray {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(secretKey())
        mac.update(salt)
        return mac.doFinal(pin.toByteArray(Charsets.UTF_8))
    }

    private fun secretKey(): SecretKey {
        val store = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = javax.crypto.KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256, ANDROID_KEYSTORE)
        generator.init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_SIGN).build())
        return generator.generateKey()
    }

    private fun pbkdf2(salt: ByteArray, pin: String): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_BITS)).encoded

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "facet_vault_pin"
        const val HMAC_ALGORITHM = "HmacSHA256"
        const val PBKDF2_ITERATIONS = 120_000
        const val HASH_BITS = 256
    }
}
