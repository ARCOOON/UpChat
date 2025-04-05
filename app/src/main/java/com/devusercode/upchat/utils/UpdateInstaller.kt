package com.devusercode.upchat.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import java.io.File

class UpdateInstaller(private val context: Context) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun install(filePath: String) {
        if (context.packageManager.canRequestPackageInstalls()) {
            // Permissions are granted, proceed with installation
            performInstall(filePath)
        } else {
            // Request install permissions
            requestInstallPermissions().run { performInstall(filePath) }
            // -> Install permissions granted
            // -> Perform installation
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestInstallPermissions() {
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
        val packageName = String.format("package:%s", context.packageName)
        val packageURI = Uri.parse(packageName)

        intent.setData(packageURI)

        context.startActivity(intent)
    }

    private fun performInstall(filePath: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(
            uriFromFile(filePath),
            "application/vnd.android.package-archive"
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            context.startActivity(intent)
            // Notify the callback that the installation has started
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("TAG", "Error in opening the file!")
            // Notify the callback that the installation has failed
        }
    }

    private fun uriFromFile(filePath: String): Uri? {
        return context.let {
            FileProvider.getUriForFile(it, it.packageName + ".provider", File(filePath))
        }
    }
}
