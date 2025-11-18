package com.devusercode.upchat.security

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal object Hkdf {
    private const val HKDF_ALGORITHM = "HmacSHA256"

    fun derive(
        secret: ByteArray,
        salt: ByteArray,
        info: ByteArray,
        size: Int,
    ): ByteArray {
        val mac = Mac.getInstance(HKDF_ALGORITHM)
        val actualSalt = if (salt.isNotEmpty()) salt else ByteArray(mac.macLength)
        mac.init(SecretKeySpec(actualSalt, HKDF_ALGORITHM))
        val prk = mac.doFinal(secret)

        mac.init(SecretKeySpec(prk, HKDF_ALGORITHM))

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
}
