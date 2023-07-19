package com.devusercode.upchat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.devusercode.upchat.models.User;
import com.devusercode.upchat.utils.QRCode;
import com.devusercode.upchat.utils.SketchwareUtil;
import com.devusercode.upchat.utils.UserUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

@SuppressLint("SetTextI18n")
public class MyProfileActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final StorageReference profile_images = FirebaseStorage.getInstance().getReference("profile_images");

    private LinearLayout linear1;
    private TextView username;
    private TextView email;
    private TextView uid;
    private TextView joined;
    private LinearLayout linear3;
    private TextView app_version_text;

    private OnCompleteListener<Void> auth_deleteUserListener;

    private final Intent intent = new Intent();
    private final DatabaseReference users = db.getReference("users");

    private User current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
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
        CoordinatorLayout coordinator = findViewById(R.id._coordinator);
        AppBarLayout app_bar = findViewById(R.id.app_bar);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        ((TextView) toolbar.findViewById(R.id.toolbar_title)).setText("My Profile");

        linear1 = findViewById(R.id.linear1);
        linear3 = findViewById(R.id.linear3);

        username = findViewById(R.id.textview1);
        email = findViewById(R.id.textview2);
        uid = findViewById(R.id.textview3);
        joined = findViewById(R.id.textview4);
        app_version_text = findViewById(R.id.app_version_text);

        try {
            String app_version = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            app_version_text.setText("v" + app_version);
        } catch (PackageManager.NameNotFoundException e) {
            app_version_text.setText("null");
            throw new RuntimeException(e);
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        username.setOnLongClickListener(view -> {
            ClipData clip = ClipData.newPlainText("Username", username.getText());
            clipboard.setPrimaryClip(clip);
            return false;
        });

        email.setOnLongClickListener(view -> {
            ClipData clip = ClipData.newPlainText("Email", email.getText());
            clipboard.setPrimaryClip(clip);
            return false;
        });

        uid.setOnLongClickListener(view -> {
            ClipData clip = ClipData.newPlainText("Uid", uid.getText());
            clipboard.setPrimaryClip(clip);
            return false;
        });

        Button chats_button = findViewById(R.id.chats_button);
        Button logout_button = findViewById(R.id.logout_button);
        Button delete_button = findViewById(R.id.delete_button);
        Button share_qrcode_button = findViewById(R.id.share_qrcode_button);

        FloatingActionButton view_users_button = findViewById(R.id.view_users_button);

        logout_button.setOnClickListener(view -> {
            auth.signOut();
            intent.setClass(getApplicationContext(), StartupActivity.class);
            startActivity(intent);
            finish();
        });

        chats_button.setOnClickListener(view -> {
            intent.setClass(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
        });

        delete_button.setOnClickListener(_view -> {
            if (auth.getCurrentUser() != null) {
                auth.getCurrentUser().delete()
                        .addOnCompleteListener(auth_deleteUserListener)
                        .addOnFailureListener(error -> {
                            Log.e(TAG, error.getMessage());
                        });
            }
        });

        view_users_button.setOnClickListener(_view -> {
            intent.setClass(getApplicationContext(), ListUsersActivity.class);
            startActivity(intent);
        });

        share_qrcode_button.setOnClickListener(_view -> {
            Bitmap qrCodeBitmap = QRCode.create(auth.getCurrentUser().getUid(), 400, 400);

            // Create dialog with custom layout
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_qr_code, null);

            ImageView qrCodeImageView = dialogView.findViewById(R.id.qr_code_image);

            qrCodeImageView.setImageBitmap(qrCodeBitmap);

            builder.setView(dialogView);
            builder.setCancelable(true);

            // Create and show the dialog
            Dialog qrCodeDialog = builder.create();
            qrCodeDialog.show();
        });

        ChildEventListener _users_child_listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String childKey) {
                current_user = dataSnapshot.getValue(User.class);

                if (auth.getCurrentUser().getUid().equals(current_user.getUid())) {
                    username.setText(current_user.getUsername());
                    email.setText(current_user.getEmail());
                    uid.setText(current_user.getUid());
                    joined.setText(current_user.getFormattedJoined());

                    getFCMToken();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String childKey) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String childKey) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        users.addChildEventListener(_users_child_listener);

        auth_deleteUserListener = task -> {
            final String errorMessage = task.getException() != null ? task.getException().getMessage() : "";

            if (task.isSuccessful()) {
                users.child(current_user.getUid()).removeValue().addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete user: " + e.getMessage());
                });

                profile_images.child(current_user.getUid() + ".png").delete().addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete profile image: " + e.getMessage());
                });

                intent.setClass(getApplicationContext(), StartupActivity.class);
                startActivity(intent);
                finish();
            } else {
                Log.e(TAG, errorMessage);
                SketchwareUtil.showMessage(getApplicationContext(), errorMessage);
            }
        };

    }

    private void initializeLogic() {
        if (auth.getCurrentUser() == null | auth.getCurrentUser().getUid().isEmpty()) {
            intent.setClass(getApplicationContext(), StartupActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();

                if (current_user.getDeviceToken() == null) {
                    UserUtils.update(Key.User.DEVICE_TOKEN, token);
                }
            }
        });
    }
}
