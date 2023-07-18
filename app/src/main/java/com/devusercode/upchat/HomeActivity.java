package com.devusercode.upchat;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devusercode.upchat.adapter.HomeAdapter;
import com.devusercode.upchat.models.User;
import com.devusercode.upchat.models.UserPair;
import com.devusercode.upchat.utils.ConversationUtil;
import com.devusercode.upchat.utils.UserUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private User user;
    private List<UserPair> open_conversations;

    private RecyclerView recyclerview;
    private HomeAdapter adapter;

    private int conversationsCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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

    private void initialize() {
        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));

        loadOpenConversations();
    }

    private void loadOpenConversations() {
        if (user == null || user.getConversations() == null) {
            return;
        }

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
                    } else {
                        open_conversations.add(new UserPair(result.getUser(), cid));
                        conversationsCounter++;

                        if (conversationsCounter == user.getConversationIds().size()) {
                            adapter = new HomeAdapter(open_conversations);
                            recyclerview.setAdapter(adapter);
                        }
                    }
                });
            });
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (user != null) {
            loadOpenConversations();
        }
    }
}
