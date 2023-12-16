package com.devusercode.upchat.utils

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import java.util.Objects
import java.util.UUID

object DeviceId {
    private const val TAG = "DeviceId"

    @RequiresApi(Build.VERSION_CODES.O)
    @Suppress("DEPRECATION")
    fun getId(): String {
        val cpuABI: Int = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Build.CPU_ABI.length % 10
        } else {
            getCpuAbi()
        }

        val devIdShort =
            "35${Build.BOARD.length % 10}${Build.BRAND.length % 10}${cpuABI}${Build.DEVICE.length % 10}${Build.MANUFACTURER.length % 10}${Build.MODEL.length % 10}${Build.PRODUCT.length % 10}"

        val serial: String? = try {
            // Build::class.java.getField("SERIAL").get(null)?.toString()
            Build.getSerial()
        } catch (exception: Exception) {
            ""
        }

        return UUID(devIdShort.hashCode().toLong(), serial.hashCode().toLong()).toString()
    }

    private fun getCpuAbi(): Int {
        for (abi in Build.SUPPORTED_ABIS) {
            when (abi) {
                "armeabi" -> return 1
                "armeabi-v7a" -> return 2
                "arm64-v8a" -> return 3
                "x86" -> return 4
                "x86_64" -> return 5
                // Ignore other custom ABIs
            }
        }
        // Unknown ABI or no supported ABIs found
        return 0
    }
}
