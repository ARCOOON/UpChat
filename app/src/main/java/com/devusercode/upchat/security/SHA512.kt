package com.devusercode.upchat.security

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

class SHA512 {
    companion object {
        fun generate(file: File): String {
            val digest = MessageDigest.getInstance("SHA-512")
            val fis = FileInputStream(file)
            val byteArray = ByteArray(8192)
            var bytesRead: Int

            while (fis.read(byteArray).also { bytesRead = it } != -1) {
                digest.update(byteArray, 0, bytesRead)
            }
            fis.close()

            val hashBytes = digest.digest()
            val hexString = StringBuilder()

            for (hashByte in hashBytes) {
                val hex = Integer.toHexString(0xFF and hashByte.toInt())
                if (hex.length == 1) {
                    hexString.append('0')
                }
                hexString.append(hex)
            }

            return hexString.toString()
        }

        fun validate(messageSHA512: String, generatedSHA512: String): Boolean {
            return messageSHA512 == generatedSHA512
        }
    }
}