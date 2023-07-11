package com.devusercode.upchat;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devusercode.upchat.adapter.HomeAdapter;
import com.devusercode.upchat.models.User;
import com.devusercode.upchat.utils.ConversationUtil;
import com.devusercode.upchat.utils.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private User user;
    private List<User> open_conversations;

    private RecyclerView recyclerview;
    private DatabaseReference conversationsRef;
    private HomeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getUser();

        /*
         * Get current user
         * then get conversation ids from user
         * then get both users from conversation id
         * then from there show the chat on the home
         * activity with the participant user.
         * */
    }

    private void initialize() {
        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));

        open_conversations = new ArrayList<>();

        for (String cid : user.getConversationIds()) {
            ConversationUtil.getConversationById(cid, task -> {
                if (!task.isSuccessful()) {
                    Log.e(TAG, task.getError().getMessage());
                    return;
                }

                task.getConversation().getParticipant(result -> {
                    if (!result.isSuccessful()) {
                        Log.e(TAG, result.getError().getMessage());
                        return;
                    }

                    open_conversations.add(result.getUser());
                    adapter.notifyDataSetChanged();
                });
            });
        }

        adapter = new HomeAdapter(open_conversations);
        recyclerview.setAdapter(adapter);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void getUser() {
        UserUtils.getUserByUid(auth.getCurrentUser().getUid(), result -> {
            if (result.getUser() != null) {
                user = result.getUser();
                initialize();
            } else {
                user = null;
                Log.e(TAG, result.getError().getMessage());
            }
        });
    }
}
