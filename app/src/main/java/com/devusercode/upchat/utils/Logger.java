package com.devusercode.upchat.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

@SuppressLint("StaticFieldLeak")
public class Logger {
    private static String TAG;
    private static Context context;
    private static Boolean save;
    private static Logger instance = null;

    Logger(String t, Context c, Boolean s) {
        TAG = t;
        context = c;
        save = s;
    }

    @NonNull
    public static Logger getInstance(String t, Context c, Boolean s) {
        if (instance == null) {
            instance = new Logger(t, c, s);
        }

        return instance;
    }

    public void i(String msg) {
        Log.i(TAG, msg);
    }
}
