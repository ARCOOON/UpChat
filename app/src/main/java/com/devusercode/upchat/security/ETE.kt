package com.devusercode.upchat.security

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher

class ETE(
    private val encryptionKey: PublicKey,
    private val decryptionKey: PrivateKey
) {
    companion object {
        fun generateKeyPair(): Pair<PublicKey, PrivateKey> {
            val keyGen = KeyPairGenerator.getInstance("RSA")
            keyGen.initialize(2048)
            val keyPair = keyGen.generateKeyPair()
            return Pair(keyPair.public, keyPair.private)
        }
    }

    fun encrypt(message: String): String {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey)

        val encryptedBytes = cipher.doFinal(message.toByteArray(StandardCharsets.UTF_8))

        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    fun decrypt(encryptedMessage: String): String {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.DECRYPT_MODE, decryptionKey)

        val encryptedBytes = Base64.decode(encryptedMessage, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return String(decryptedBytes, StandardCharsets.UTF_8)
    }
}