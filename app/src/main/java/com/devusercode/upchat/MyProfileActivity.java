package com.devusercode.upchat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.devusercode.upchat.models.User;
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

public class MyProfileActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final StorageReference profile_images = FirebaseStorage.getInstance().getReference("profile_images");

    private String current_uid = "";
    private User user;

    private LinearLayout linear1;
    private TextView textview1;
    private TextView textview2;
    private TextView textview3;
    private TextView textview4;
    private LinearLayout linear3;

    private OnCompleteListener<Void> auth_deleteUserListener;

    private final Intent intent = new Intent();
    private final DatabaseReference users = db.getReference("users");

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

        textview1 = findViewById(R.id.textview1);
        textview2 = findViewById(R.id.textview2);
        textview3 = findViewById(R.id.textview3);
        textview4 = findViewById(R.id.textview4);

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        textview1.setOnLongClickListener(view -> {
            ClipData clip = ClipData.newPlainText("Username", textview1.getText());
            clipboard.setPrimaryClip(clip);
            return false;
        });
        textview2.setOnLongClickListener(view -> {
            ClipData clip = ClipData.newPlainText("Email", textview2.getText());
            clipboard.setPrimaryClip(clip);
            return false;
        });
        textview3.setOnLongClickListener(view -> {
            ClipData clip = ClipData.newPlainText("Uid", textview3.getText());
            clipboard.setPrimaryClip(clip);
            return false;
        });

        Button logout_button = findViewById(R.id.logout_button);
        Button delete_button = findViewById(R.id.delete_button);
        Button chats_button = findViewById(R.id.chats_button);

        FloatingActionButton view_users_button = findViewById(R.id.view_users_button);

        logout_button.setOnClickListener(view -> {
            auth.signOut();
            intent.setClass(getApplicationContext(), StartupActivity.class);
            startActivity(intent);
            finish();
        });

        delete_button.setOnClickListener(_view -> {
            if (auth.getCurrentUser() != null) {
                current_uid = auth.getCurrentUser().getUid();
                auth.getCurrentUser().delete().addOnCompleteListener(auth_deleteUserListener);
            }
        });

        view_users_button.setOnClickListener(_view -> {
            intent.setClass(getApplicationContext(), ListUsersActivity.class);
            startActivity(intent);
        });

        chats_button.setOnClickListener(view -> {
            intent.setClass(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
        });

        ChildEventListener _users_child_listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String childKey) {
                user = dataSnapshot.getValue(User.class);

                if (auth.getCurrentUser().getUid().equals(user.getUid())) {
                    textview1.setText(user.getUsername());
                    textview2.setText(user.getEmail());
                    textview3.setText(user.getUid());
                    textview4.setText(user.getFormattedJoined());

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
                users.child(current_uid).removeValue().addOnFailureListener(e -> {
                    // Failed to delete the user from the database
                    Log.e(TAG, "Failed to delete user: " + e.getMessage());
                });

                profile_images.child(current_uid + ".png").delete().addOnFailureListener(e -> {
                    // Failed to delete the image
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

                if (user.getDeviceToken() == null) {
                    UserUtils.update("deviceToken", token);
                }
            }
        });
    }
}
