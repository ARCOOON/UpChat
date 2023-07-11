package com.devusercode.upchat.utils;

import android.os.Build;

import java.util.Objects;
import java.util.UUID;

public class DeviceId {
    private static final String TAG = DeviceId.class.getSimpleName();

    public DeviceId() {
    }

    @SuppressWarnings("deprecated")
    public static String getId() {
        int cpu_abi = 0;

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            cpu_abi = (Build.CPU_ABI.length() % 10);
        } else {
            cpu_abi = getCpuAbi();
        }

        String m_szDevIDShort = "35" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10) + cpu_abi + (Build.DEVICE.length() % 10) + (Build.MANUFACTURER.length() % 10) + (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10);

        String serial;
        try {
            serial = Objects.requireNonNull(Build.class.getField("SERIAL").get(null)).toString();
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            serial = "";
        }
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }

    // Function to map supported ABIs to integer value
    private static int getCpuAbi() {
        for (String abi : Build.SUPPORTED_ABIS) {
            switch (abi) {
                case "armeabi":
                    return 1;
                case "armeabi-v7a":
                    return 2;
                case "arm64-v8a":
                    return 3;
                case "x86":
                    return 4;
                case "x86_64":
                    return 5;
                default:
                    break;
                // Ignore other custom ABIs
            }
        }
        // Unknown ABI or no supported ABIs found
        return 0;
    }

}
