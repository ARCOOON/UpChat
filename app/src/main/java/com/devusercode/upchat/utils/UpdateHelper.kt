package com.devusercode.upchat.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class UpdateHelper private constructor(
    private val onUpdateCheckListener: OnUpdateCheckListener,
    private val context: Context,
) {
    companion object {
        const val TAG = "UpdateHelper"
        const val KEY_UPDATE_REQUIRED = "app_force_update_required"
        const val KEY_UPDATE_VERSION = "app_version"
        const val KEY_UPDATE_URL = "app_update_url"
        const val KEY_REPO_URL = "app_repo_url"

        fun with(context: Context): Builder = Builder(context)
    }

    interface OnUpdateCheckListener {
        fun onUpdateAvailable(
            filename: String,
            url: String,
        )

        fun onUpdateRequired(
            filename: String,
            url: String,
        )

        fun onNoUpdateAvailable()
    }

    fun check() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val appVersion: String = remoteConfig.getString(KEY_UPDATE_VERSION)
                val appRepo: String = remoteConfig.getString(KEY_REPO_URL)
                // val updateUrl = remoteConfig.getString(KEY_UPDATE_URL)
                val installedVersion = getAppVersion(context)
                val baseFilename = "upchat"

                // repo: https://github.com/ARCOOON/UpChat
                // download: repo + /releases/download/$appVersion/$filename.apk

                val newUpdateUrl = "$appRepo/releases/download/$appVersion/$baseFilename-$appVersion.apk"
                val filename = "$baseFilename-$appVersion.apk"

                Log.d(TAG, "local version: $installedVersion")
                Log.d(TAG, "server version: $appVersion")
                // Log.d(TAG, "update url: $newUpdateUrl")

                if (appVersion.isEmpty()) {
                    onUpdateCheckListener.onNoUpdateAvailable()
                    return@addOnCompleteListener
                }

                if (!compareVersions(installedVersion!!, appVersion)) {
                    if (remoteConfig.getBoolean(KEY_UPDATE_REQUIRED)) {
                        onUpdateCheckListener.onUpdateRequired(filename, newUpdateUrl)
                    } else {
                        onUpdateCheckListener.onUpdateAvailable(filename, newUpdateUrl)
                    }
                } else {
                    onUpdateCheckListener.onNoUpdateAvailable()
                }
            } else {
                Log.e(TAG, "Failed to fetch remote config: ${task.exception?.message}")
            }
        }
    }

    private fun compareVersions(
        installedVersion: String,
        appVersion: String,
    ): Boolean = installedVersion.split("-")[0] == appVersion.split("-")[0]

    private fun getAppVersion(context: Context): String? =
        try {
            val packageInfo =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    context.packageManager.getPackageInfo(
                        context.packageName,
                        PackageManager.GET_SIGNING_CERTIFICATES,
                    )
                } else {
                    context.packageManager.getPackageInfo(context.packageName, 0)
                }
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }

    class Builder internal constructor(
        private val context: Context,
    ) {
        private var onUpdateCheckListener: OnUpdateCheckListener? = null

        fun onUpdateCheck(onUpdateCheckListener: OnUpdateCheckListener): Builder {
            this.onUpdateCheckListener = onUpdateCheckListener
            return this
        }

        fun build(): UpdateHelper = UpdateHelper(requireNotNull(onUpdateCheckListener), context)

        fun check(): UpdateHelper {
            val updateHelper = build()
            updateHelper.check()
            return updateHelper
        }
    }
}
