package com.devusercode.upchat.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import com.devusercode.upchat.DebugActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlin.system.exitProcess

class Application : android.app.Application() {
    private val TAG = "Application"
    private lateinit var uncaughtExceptionHandler: Thread.UncaughtExceptionHandler

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance()

        // Set custom uncaught exception handler
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()!!

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            FirebaseCrashlytics.getInstance().recordException(throwable)

            val intent = Intent(applicationContext, DebugActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("error", Log.getStackTraceString(throwable))

            val pendingIntent = PendingIntent.getActivity(
                applicationContext, 11111, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, pendingIntent)

            Process.killProcess(Process.myPid())

            uncaughtExceptionHandler.uncaughtException(thread, throwable)

            exitProcess(1)
        }

        // Fetch data from Firebase every 5 seconds (ideally, 1min - 5min, etc.)
        val remoteConfig = FirebaseRemoteConfig.getInstance()

        remoteConfig.fetch(1).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                remoteConfig.activate()
            } else {
                Log.w(TAG, "Error fetching remote config.", task.exception)
            }
        }
    }
}
