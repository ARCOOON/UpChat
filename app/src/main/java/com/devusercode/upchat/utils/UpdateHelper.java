package com.devusercode.upchat.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class UpdateHelper {
    private final String TAG = this.getClass().getSimpleName();

    public static String KEY_UPDATE_REQUIRED = "app_force_update_required";
    public static String KEY_UPDATE_VERSION = "app_version";
    public static String KEY_UPDATE_URL = "app_update_url";

    public interface OnUpdateCheckListener {
        void onUpdateAvailable(String urlApp);

        void onUpdateRequired(String urlApp);

        void onNoUpdateAvailable();
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    private final OnUpdateCheckListener onUpdateCheckListener;
    private final Context context;

    public UpdateHelper(OnUpdateCheckListener onUpdateCheckListener, Context context) {
        this.onUpdateCheckListener = onUpdateCheckListener;
        this.context = context;
    }

    public void check() {
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        String currentVersion = remoteConfig.getString(KEY_UPDATE_VERSION);
        String updateUrl = remoteConfig.getString(KEY_UPDATE_URL);
        String appVersion = getAppVersion(context);

        Log.d(TAG, "installed version: " + appVersion);
        Log.d(TAG, "latest version: " + currentVersion);

        if (!TextUtils.equals(currentVersion, appVersion)) {
            if (remoteConfig.getBoolean(KEY_UPDATE_REQUIRED)) {
                onUpdateCheckListener.onUpdateRequired(updateUrl);
            } else {
                onUpdateCheckListener.onUpdateAvailable(updateUrl);
            }
        } else {
            onUpdateCheckListener.onNoUpdateAvailable();
        }
    }

    private String getAppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class Builder {
        private final Context context;
        private OnUpdateCheckListener onUpdateCheckListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder onUpdateCheck(OnUpdateCheckListener onUpdateCheckListener) {
            this.onUpdateCheckListener = onUpdateCheckListener;
            return this;
        }

        public UpdateHelper build() {
            return new UpdateHelper(onUpdateCheckListener, context);
        }

        public UpdateHelper check() {
            UpdateHelper updateHelper = build();
            updateHelper.check();

            return updateHelper;
        }
    }
}
