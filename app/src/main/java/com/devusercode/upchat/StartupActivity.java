package com.devusercode.upchat;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class StartupActivity extends AppCompatActivity {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        initialize(savedInstanceState);
        FirebaseApp.initializeApp(this);
        initializeLogic();
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

    private void initialize(Bundle savedInstanceState) {
    }

    private void initializeLogic() {
        if (auth.getCurrentUser() != null) {
            intent.setClass(getApplicationContext(), MyProfileActivity.class);
        } else {
            intent.setClass(getApplicationContext(), LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
