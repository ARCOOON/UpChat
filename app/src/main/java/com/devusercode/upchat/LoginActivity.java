package com.devusercode.upchat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.devusercode.upchat.utils.SketchwareUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private LinearLayout linear1;
    private EditText email_edit;
    private EditText password_edit;
    private TextView forgot_password_text;

    private OnCompleteListener<AuthResult> auth_create_user_listener;
    private OnCompleteListener<AuthResult> auth_sign_in_listener;
    private OnCompleteListener<Void> auth_reset_password_listener;
    private OnCompleteListener<Void> auth_updateEmailListener;
    private OnCompleteListener<Void> auth_updatePasswordListener;
    private OnCompleteListener<Void> auth_emailVerificationSentListener;
    private OnCompleteListener<Void> auth_deleteUserListener;
    private OnCompleteListener<Void> auth_updateProfileListener;
    private OnCompleteListener<AuthResult> auth_phoneAuthListener;
    private OnCompleteListener<AuthResult> auth_googleSignInListener;

    private final Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.activity_login);
        initialize(_savedInstanceState);
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
        CoordinatorLayout coordinator = findViewById(R.id._coordinator);
        AppBarLayout app_bar = findViewById(R.id.app_bar);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        ((TextView) toolbar.findViewById(R.id.toolbar_title)).setText("Login");

        linear1 = findViewById(R.id.linear1);
        email_edit = findViewById(R.id.email_edit);
        password_edit = findViewById(R.id.password_edit);
        forgot_password_text = findViewById(R.id.forgot_password_text);

        Button login_button = findViewById(R.id.login_button);
        Button register_button = findViewById(R.id.register_button);

        forgot_password_text.setOnClickListener(view -> {
            if (email_edit.getText().toString().isEmpty() || password_edit.getText().toString().isEmpty()) {
                return;
            }
            auth.sendPasswordResetEmail(email_edit.getText().toString()).addOnCompleteListener(auth_reset_password_listener);
        });

        login_button.setOnClickListener(view -> {
            if (email_edit.getText().toString().isEmpty() || password_edit.getText().toString().isEmpty()) {
                return;
            }
            auth.signInWithEmailAndPassword(email_edit.getText().toString(), password_edit.getText().toString())
                    .addOnCompleteListener(auth_sign_in_listener);
        });

        register_button.setOnClickListener(view -> {
            intent.setClass(getApplicationContext(), RegisterActivity.class);
            startActivity(intent);
        });

        /* *** LISTENERS *** */

        auth_emailVerificationSentListener = task -> {
            final boolean success = task.isSuccessful();
            final String errorMessage = task.getException() != null ? task.getException().getMessage() : "";
        };

        auth_googleSignInListener = task -> {
            final boolean success = task.isSuccessful();
            final String errorMessage = task.getException() != null ? task.getException().getMessage() : "";
        };

        auth_sign_in_listener = task -> {
            if (task.isSuccessful()) {
                intent.setClass(getApplicationContext(), MyProfileActivity.class);
                startActivity(intent);
                finish();
            } else {
                if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                    SketchwareUtil.showMessage(getApplicationContext(), "User not found!");
                }

                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    SketchwareUtil.showMessage(getApplicationContext(), "Invalid credentials!");
                } else {
                    Log.e(TAG, task.getException().getClass().getName());
                    Log.e(TAG, task.getException().getMessage());
                }
            }
        };

        auth_reset_password_listener = task -> {
            final boolean success = task.isSuccessful();

            if (success) {
                SketchwareUtil.showMessage(getApplicationContext(), "Password reset email successfully sent");
            } else {
                Log.e(TAG, task.getException().getMessage());
                SketchwareUtil.showMessage(getApplicationContext(), task.getException().getMessage());
            }
        };
    }

    private void initializeLogic() {
    }

}
