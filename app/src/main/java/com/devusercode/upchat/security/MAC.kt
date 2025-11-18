package com.devusercode.upchat.security

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Provides HMAC-SHA256 signing with keys derived from a shared secret using HKDF.
 */
class MAC(
    private val sharedSecret: String,
    private val salt: String,
) {
    private val macAlgorithm = "HmacSHA256"
    private val keySize = 32
    private val derivedKey: SecretKeySpec by lazy {
        val keyMaterial =
            Hkdf.derive(
                sharedSecret.toByteArray(StandardCharsets.UTF_8),
                salt.toByteArray(StandardCharsets.UTF_8),
                "UpChat-MAC-Key".toByteArray(StandardCharsets.UTF_8),
                keySize,
            )
        SecretKeySpec(keyMaterial, macAlgorithm)
    }

    fun generate(payload: String): String {
        val macBytes = signToBytes(payload)
        return Base64.encodeToString(macBytes, Base64.NO_WRAP)
    }

    fun verify(
        payload: String,
        receivedMAC: String?,
    ): Boolean {
        if (receivedMAC.isNullOrEmpty()) {
            return false
        }

        val expected = signToBytes(payload)

        val received =
            try {
                Base64.decode(receivedMAC, Base64.DEFAULT)
            } catch (e: IllegalArgumentException) {
                return false
            }

        return MessageDigest.isEqual(received, expected)
    }

    private fun signToBytes(payload: String): ByteArray {
        val mac = Mac.getInstance(macAlgorithm)
        mac.init(derivedKey)
        return mac.doFinal(payload.toByteArray(StandardCharsets.UTF_8))
    }
}
