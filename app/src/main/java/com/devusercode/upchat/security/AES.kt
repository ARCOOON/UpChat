package com.devusercode.upchat.security

import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import android.util.Log

class AES(private val secretKey: String) {
    private val TAG = "AES"
    private val algorithm = "AES/CBC/PKCS5Padding"
    private val keySize = 16 // 128-bit key
    private val ivSize = 16 // 128-bit IV

    fun encrypt(plainText: String): String {
        val keyData = secretKey.toByteArray(StandardCharsets.UTF_8).copyOf(keySize)
        val ivData = ByteArray(ivSize)

        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyData, "AES"), IvParameterSpec(ivData))

        val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    fun decrypt(encryptedText: String): String {
        return try {
            val keyData = secretKey.toByteArray(StandardCharsets.UTF_8).copyOf(keySize)
            val ivData = ByteArray(ivSize)

            val cipher = Cipher.getInstance(algorithm)
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(keyData, "AES"), IvParameterSpec(ivData))

            val encryptedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(encryptedBytes)

            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            if (e !is IllegalArgumentException) {
                Log.e(TAG, "${e.javaClass.name} | ${e.message}")
                Log.i(TAG, "text -> $encryptedText")
            }
            encryptedText
        }
    }
}