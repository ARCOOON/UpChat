package com.devusercode.upchat;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.devusercode.upchat.utils.UpdateHelper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class StartupActivity extends AppCompatActivity implements UpdateHelper.OnUpdateCheckListener {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        UpdateHelper.with(this).onUpdateCheck(this).check();
        FirebaseApp.initializeApp(this);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    private void initialize() {
        if (auth.getCurrentUser() != null) {
            intent.setClass(getApplicationContext(), MyProfileActivity.class);
        } else {
            intent.setClass(getApplicationContext(), LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }

    @Override
    public void onUpdateAvailable(String urlApp) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("New App Version Available")
                .setMessage("Please update to the latest build,\nfor the latest features and bug fixes")
                .setPositiveButton("Update", (dialogInterface, which) -> redirectStore(urlApp))
                .setNegativeButton("Close", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    initialize();
                })
                .setOnCancelListener((dialogInterface) -> {
                    dialogInterface.dismiss();
                    initialize();
                })
                .create();

        alertDialog.show();
    }

    @Override
    public void onUpdateRequired(String urlApp) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("New App Version Required")
                .setMessage("An app update is required, to run this app!")
                .setPositiveButton("Update", (dialogInterface, which) -> redirectStore(urlApp))
                .setCancelable(false)
                .create();

        alertDialog.show();
    }

    @Override
    public void onNoUpdateAvailable() {
        initialize();
    }

    private void redirectStore(String urlApp) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlApp));
        intent.addFlags((Intent.FLAG_ACTIVITY_NEW_TASK));
        startActivity(intent);
    }
}
