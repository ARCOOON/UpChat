package com.devusercode.upchat.utils

import android.app.Application
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GetTimeAgo : Application() {
    private const val SECOND_MILLIS = 1000L
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS
    private const val WEEK_MILLIS = 7 * DAY_MILLIS
    private const val MONTH_MILLIS = 30 * DAY_MILLIS

    fun getTimeAgo(givenTime: Long): String? {
        var time = givenTime

        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000
        }

        val now = System.currentTimeMillis()

        if (time > now || time <= 0) {
            return null
        }

        val diff = now - time

        return when {
            diff < MINUTE_MILLIS -> "now"
            diff < HOUR_MILLIS -> "${diff / MINUTE_MILLIS}m"
            diff < DAY_MILLIS -> "${diff / HOUR_MILLIS}h"
            diff < WEEK_MILLIS -> "${diff / DAY_MILLIS}d"
            diff < MONTH_MILLIS -> "${diff / WEEK_MILLIS}w"

            else -> {
                // Display date and time if more than 1 month
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                sdf.format(Date(time))
            }
        }
    }

    fun parse(time: String): String? {
        return getTimeAgo(time.toLong())
    }
}