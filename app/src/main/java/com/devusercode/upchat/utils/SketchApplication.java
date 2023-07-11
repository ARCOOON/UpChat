package com.devusercode.upchat.utils;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

public class SketchApplication extends Application {
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    @Override
    public void onCreate() {
        getApplicationContext();
        this.uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Intent intent = new Intent(getApplicationContext(), DebugActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("error", Log.getStackTraceString(throwable));

            PendingIntent pendingIntent =
                    PendingIntent.getActivity(
                            getApplicationContext(),
                            11111,
                            intent,
                            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
                    );

            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, pendingIntent);

            Process.killProcess(Process.myPid());
            System.exit(1);

            uncaughtExceptionHandler.uncaughtException(thread, throwable);
        });
        super.onCreate();
    }
}
