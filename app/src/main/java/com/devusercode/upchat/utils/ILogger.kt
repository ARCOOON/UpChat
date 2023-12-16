package com.devusercode.upchat.utils

import android.content.Context
import android.util.Log

@Suppress("all")
class ILogger private constructor(context: Context) {
    companion object {
        private var instance: ILogger? = null

        @Synchronized
        fun getInstance(tag: String, context: Context, save: Boolean = false): ILogger? {
            if (instance == null) {
                instance = ILogger(context.applicationContext)
            }
            return instance
        }
    }

    fun i() {
        Log.i("", "")
    }
}