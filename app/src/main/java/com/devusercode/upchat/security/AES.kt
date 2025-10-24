package com.devusercode.upchat.security

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AES(private val sharedSecret: String, private val salt: String) {
    private val algorithm = "AES/CBC/PKCS5Padding"
    private val hkdfAlgorithm = "HmacSHA256"
    private val keySize = 16 // 128-bit key
    private val ivSize = 16 // 128-bit IV
    private val secureRandom = SecureRandom()
    private val derivedKey: SecretKeySpec by lazy {
        val keyMaterial = hkdf(
            sharedSecret.toByteArray(StandardCharsets.UTF_8),
            salt.toByteArray(StandardCharsets.UTF_8),
            "UpChat-AES-Key".toByteArray(StandardCharsets.UTF_8),
            keySize
        )
        SecretKeySpec(keyMaterial, "AES")
    }

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(algorithm)
        val iv = ByteArray(ivSize)
        secureRandom.nextBytes(iv)

        cipher.init(Cipher.ENCRYPT_MODE, derivedKey, IvParameterSpec(iv))

        val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
        val ivAndCiphertext = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, ivAndCiphertext, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, ivAndCiphertext, iv.size, encryptedBytes.size)

        return Base64.encodeToString(ivAndCiphertext, Base64.NO_WRAP)
    }

    fun decrypt(encryptedText: String): String {
        return try {
            val ivAndCiphertext = Base64.decode(encryptedText, Base64.DEFAULT)

            if (ivAndCiphertext.size < ivSize) {
                throw IllegalArgumentException("Ciphertext too short")
            }

            val iv = ivAndCiphertext.copyOfRange(0, ivSize)
            val ciphertext = ivAndCiphertext.copyOfRange(ivSize, ivAndCiphertext.size)

            val cipher = Cipher.getInstance(algorithm)
            cipher.init(Cipher.DECRYPT_MODE, derivedKey, IvParameterSpec(iv))

            val decryptedBytes = cipher.doFinal(ciphertext)

            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            if (e !is IllegalArgumentException && e !is BadPaddingException && e !is IllegalBlockSizeException) {
                throw e
            } else {
                encryptedText
            }
        }
    }

    private fun hkdf(secret: ByteArray, salt: ByteArray, info: ByteArray, size: Int): ByteArray {
        val mac = Mac.getInstance(hkdfAlgorithm)

        val actualSalt = if (salt.isNotEmpty()) salt else ByteArray(mac.macLength)
        mac.init(SecretKeySpec(actualSalt, hkdfAlgorithm))
        val prk = mac.doFinal(secret)

        mac.init(SecretKeySpec(prk, hkdfAlgorithm))

        val result = ByteArray(size)
        var previous = ByteArray(0)
        var bytesGenerated = 0
        var counter = 1

        while (bytesGenerated < size) {
            mac.update(previous)
            mac.update(info)
            mac.update(counter.toByte())

            val output = mac.doFinal()
            val bytesToCopy = minOf(output.size, size - bytesGenerated)
            System.arraycopy(output, 0, result, bytesGenerated, bytesToCopy)

            previous = output
            bytesGenerated += bytesToCopy
            counter++
        }

        return result
    }

    companion object {
        fun buildSharedSecret(vararg identifiers: String?): String {
            val nonNullIdentifiers = identifiers.filterNotNull()

            require(nonNullIdentifiers.isNotEmpty()) { "At least one identifier is required" }

            return nonNullIdentifiers.sorted().joinToString(":")
        }
    }
}
