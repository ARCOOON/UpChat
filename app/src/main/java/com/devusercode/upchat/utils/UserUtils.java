package com.devusercode.upchat.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.devusercode.upchat.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class UserUtils {
    private static final String STAG = "UserUtils";
    private static final String REF = "users";

    public static class Result {
        public User user;
        public Error error;

        public Result(User user, Error error) {
            this.user = user;
            this.error = error;
        }

        public Result(User user) {
            this.user = user;
            this.error = null;
        }

        public User getUser() {
            return user;
        }

        public Error getError() {
            return error;
        }

        public boolean isSuccessful() {
            return error == null && user != null;
        }
    }

    public interface UserCallback {
        void onUserReceived(User user);

        void onUserNotFound();

        void onUserError(String errorMessage);
    }

    public static void getUserByUid(String uid, final UserCallback callback) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference(REF).child(uid);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);

                    if (user != null) {
                        callback.onUserReceived(user);
                    } else {
                        callback.onUserError("Unknown error: error while retrieving the user!");
                    }
                } else {
                    callback.onUserNotFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onUserError(databaseError.getMessage());
            }
        });
    }

    public static void getUserByUid(String uid, @NonNull Consumer<Result> onFinish) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference(REF).child(uid);

        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();

                if (dataSnapshot != null && dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);

                    if (user != null) {
                        onFinish.accept(new Result(user));
                    } else {
                        Error error = new Error("Unknown error: error while retrieving the user!");
                        Log.e(STAG, error.getMessage());

                        onFinish.accept(new Result(null, error));
                    }
                } else {
                    Error error = new Error("UserNotFound");
                    Log.e(STAG, error.getMessage());

                    onFinish.accept(new Result(null, error));
                }
            } else {
                Error error = new Error(task.getException().getMessage());
                Log.e(STAG, error.getMessage());

                onFinish.accept(new Result(null, error));
            }
        });
    }

    public static void update(String field, Object value) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(uid);

        ref.child(field).setValue(value).addOnFailureListener(error -> Log.e(STAG, error.getMessage()));
    }
}
