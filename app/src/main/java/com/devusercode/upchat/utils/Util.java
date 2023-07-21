package com.devusercode.upchat.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Util {
    private static final String TAG = "Util";

    public static String FormatTime(String joined, String _format) {
        Date date = new Date(Long.parseLong(joined));
        return new SimpleDateFormat(_format, Locale.getDefault()).format(date);
    }

    public static String formatTime(String _time) {
        long time = Long.parseLong(_time);
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time * 1000);
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal);
    }

    public static String parseTime(String joined) {
        return FormatTime(joined, "dd/MM/yyyy HH:mm");
    }

    public static void setCornerRadius(View view, float cornerRadius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
                }
            });
            view.setClipToOutline(true);
        } else {
            Log.w(TAG, "Cannot set corner radius, minimum supported sdk version is 28");
            // For versions before Lollipop, use a background drawable to achieve rounded corners
            // view.setBackgroundResource(R.drawable.rounded_corner_background);
        }
    }

    public static View createOverlay(Context context) {
        // Create a semi-transparent view with black background color
        View overlay = new View(context);
        overlay.setBackgroundColor(Color.parseColor("#80000000")); // 50% opacity

        // Set the layout parameters to match the parent's size
        overlay.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        return overlay;
    }
}
