package com.devusercode.upchat.utils;

import android.app.Application;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GetTimeAgo extends Application {
    /*
     * THIS CLASS IS FOR GETTING LAST SEEN TIME IN CHATS
     * IT CONVERTS TIME_STAMP INTO TIME_AGO
     */

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final int WEEK_MILLIS = 7 * DAY_MILLIS;

    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();

        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;

        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else if (diff < WEEK_MILLIS) {
            return diff / DAY_MILLIS + " days ago";
        } else if (diff < 4L * WEEK_MILLIS) {
            if ((diff / WEEK_MILLIS) == 1) {
                return "a week ago";
            } else {
                return diff / WEEK_MILLIS + " weeks ago";
            }
        } else {
            // Display date and time if more than 1 month
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(new Date(time));
        }
    }

    public static String parse(String time) {
        return getTimeAgo(Long.parseLong(time));
    }
}
