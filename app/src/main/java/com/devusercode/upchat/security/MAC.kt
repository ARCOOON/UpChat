package com.devusercode.upchat.security

import android.os.Build
import androidx.annotation.RequiresApi
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * **MessageAuthenticationCode**
 *
 * This uses the HMAC-SHA256 algorithm for generating and verifying the MAC.
 * It converts the MAC bytes to Base64 encoding for easy representation and comparison.
 * The the result from verify will be true if the MAC verification is successful,
 * indicating that the message integrity is maintained.
 */
class MAC(private val secretKey: String) {
    private val macAlgorithm = "HmacSHA256"

    @RequiresApi(Build.VERSION_CODES.O)
    fun generate(message: String): String {
        val hmacKey = SecretKeySpec(secretKey.toByteArray(), macAlgorithm)
        val mac = Mac.getInstance(macAlgorithm)

        mac.init(hmacKey)

        val macBytes = mac.doFinal(message.toByteArray())
        return Base64.getEncoder().encodeToString(macBytes)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun verify(message: String, receivedMAC: String): Boolean {
        val expectedMAC = generate(message)

        return MessageDigest.isEqual(
            Base64.getDecoder().decode(receivedMAC),
            Base64.getDecoder().decode(expectedMAC)
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun verifyMAC(receivedMAC: String, generatedMac: String): Boolean {
        return MessageDigest.isEqual(
            Base64.getDecoder().decode(receivedMAC),
            Base64.getDecoder().decode(generatedMac)
        )
    }
}