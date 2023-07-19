package com.devusercode.upchat;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.devusercode.upchat.utils.DeviceId;
import com.devusercode.upchat.utils.SketchwareUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;


public class RegisterActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");
    private final StorageReference profile_images = FirebaseStorage.getInstance().getReference("profile_images");

    private LinearLayout linear1;
    private EditText username_edit;
    private EditText email_edit;
    private EditText password_edit;
    private Button register_button;
    private ImageButton upload_profile_image_button;
    private AppCompatImageView profile_image;

    private TextView toolbar_title;

    private OnCompleteListener<AuthResult> _auth_create_user_listener;
    private OnCompleteListener<Void> auth_updateProfileListener;
    private OnCompleteListener<Uri> profile_images_upload_success_listener;

    private final Intent intent = new Intent();
    private ActivityResultLauncher<String> image_picker_launcher;

    private String downloadUrl = "";
    private Uri imagePath = Uri.EMPTY;
    private Dialog progressDialog;
    private String token;

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.activity_register);
        initialize(_savedInstanceState);
        FirebaseApp.initializeApp(this);
        initializeLogic();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        token = task.getResult();
                    }
                })
                .addOnFailureListener(error -> {
                    token = null;
                    Log.e(TAG, error.getMessage());
                });
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
        toolbar_title = toolbar.findViewById(R.id.toolbar_title);
        toolbar_title.setText("Register");

        progressDialog = new Dialog(this);
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCancelable(false);
        // Get the progress bar from the dialog layout
        ProgressBar progressBar = progressDialog.findViewById(R.id.progress);
        progressBar.setIndeterminate(true);

        linear1 = findViewById(R.id.linear1);
        username_edit = findViewById(R.id.username_edit);
        email_edit = findViewById(R.id.email_edit);
        password_edit = findViewById(R.id.password_edit);
        register_button = findViewById(R.id.register_button);
        upload_profile_image_button = findViewById(R.id.upload_profile_image_button);
        profile_image = findViewById(R.id.profile_image);

        upload_profile_image_button.setOnClickListener(view -> image_picker_launcher.launch("image/*"));

        image_picker_launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                imagePath = result;
                profile_image.setImageURI(result);
            }
        });

        register_button.setOnClickListener(view -> {
            if (username_edit.getText().toString().isEmpty() || (email_edit.getText().toString().isEmpty() || password_edit.getText().toString().isEmpty())) {
                return;
            }

            if (password_edit.getText().toString().length() < 6) {
                password_edit.setError("Password must be longer than 6");
                return;
            }

            progressDialog.show();

            auth.createUserWithEmailAndPassword(email_edit.getText().toString(), password_edit.getText().toString())
                    .addOnCompleteListener(task -> {
                        uploadProfileImage();
                    }).addOnFailureListener(error -> {
                        progressDialog.dismiss();
                        Log.e(TAG, error.getMessage());
                        SketchwareUtil.showMessage(getApplicationContext(), error.getMessage());
                    });
        });
    }

    private void uploadProfileImage() {
        if (imagePath == null | imagePath == Uri.EMPTY) {
            SketchwareUtil.showMessage(getApplicationContext(), "Continue without profile image");
            // No image selected, continue with user profile update
            updateUserProfile();
        } else {
            StorageReference imageRef = profile_images.child(auth.getCurrentUser().getUid() + ".png");

            Log.d(TAG, "imagePath: " + imagePath);
            Log.d(TAG, "imageRef: " + imageRef);

            imageRef.putFile(imagePath).addOnSuccessListener(taskSnapshot -> {
                // Get the download URL of the uploaded image
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    SketchwareUtil.showMessage(getApplicationContext(), "Profile image successfully uploaded");
                    downloadUrl = uri.toString();
                    updateUserProfile();
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get download URL: " + e.getMessage());
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to upload image: " + e.getMessage());
            });
        }
    }

    private void updateUserProfile() {
        UserProfileChangeRequest updates = new UserProfileChangeRequest.Builder().setDisplayName(username_edit.getText().toString()).build();

        auth.getCurrentUser().updateProfile(updates)
                .addOnCompleteListener(task -> {
                    createUserProfile(task);
                })
                .addOnFailureListener(error -> {
                    progressDialog.dismiss();
                    Log.e(TAG, error.getMessage());
                    SketchwareUtil.showMessage(getApplicationContext(), error.getMessage());
                });
    }

    private void createUserProfile(Task<Void> task) {
        final String errorMessage = task.getException() != null ? task.getException().getMessage() : "";

        if (task.isSuccessful()) {
            HashMap<String, Object> userinfo = new HashMap<>();
            FirebaseUser user = auth.getCurrentUser();

            userinfo.put(Key.User.DEVICE_ID, DeviceId.getId());
            userinfo.put(Key.User.DEVICE_TOKEN, (token != null ? token : ""));
            userinfo.put(Key.User.USERNAME, user.getDisplayName());
            userinfo.put(Key.User.EMAIL, user.getEmail());
            userinfo.put(Key.User.UID, user.getUid());
            userinfo.put(Key.User.PHOTO_URL, (!downloadUrl.isEmpty()) ? downloadUrl : "");
            userinfo.put(Key.User.JOINED, String.valueOf(System.currentTimeMillis()));

            users.child(user.getUid()).updateChildren(userinfo);

            progressDialog.dismiss();

            intent.setClass(getApplicationContext(), MyProfileActivity.class);
            startActivity(intent);
            finish();
        } else {
            progressDialog.dismiss();
            Log.e(TAG, errorMessage);
            SketchwareUtil.showMessage(getApplicationContext(), errorMessage);
        }
    }

    private void initializeLogic() {
        /*
         * If -> All fields are initialized
         * Then -> if imagePath is not null upload the profile image
         * Then -> update the user profile
         * Then -> create and save the user to firebase
         */
    }
}
