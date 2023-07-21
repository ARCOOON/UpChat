package com.devusercode.upchat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.devusercode.upchat.adapter.MessageAdapter;
import com.devusercode.upchat.adapter.WrapLayoutManager;
import com.devusercode.upchat.models.Message;
import com.devusercode.upchat.models.User;
import com.devusercode.upchat.utils.ConversationUtil;
import com.devusercode.upchat.utils.UserUtils;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConversationActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final DatabaseReference conversations = firebaseDatabase.getReference("conversations");

    /* Toolbar */
    private ImageView back_button;
    private TextView participant_name;
    private ImageView profile_image;

    /* Content */
    private MessageAdapter adapter;
    private RecyclerView recyclerview;
    private Button attach_button;
    private TextInputEditText message_input;
    private Button send_button;

    private final Intent intent = new Intent();

    private User participant;
    private User user;
    private String currentConversationId;
    private boolean chatExists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        // FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // intent for data
        Intent data_intent = getIntent();

        // get uid and get participant user
        if (data_intent != null && data_intent.getExtras() != null) {
            String uid = data_intent.getStringExtra("uid");
            getParticipant(uid);
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void initialize() {
        CoordinatorLayout coordinator = findViewById(R.id._coordinator);
        AppBarLayout app_bar = findViewById(R.id.app_bar);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        back_button = toolbar.findViewById(R.id.back_button);
        participant_name = toolbar.findViewById(R.id.participant_name);
        profile_image = toolbar.findViewById(R.id.profile_image);

        attach_button = findViewById(R.id.attach_button);
        message_input = findViewById(R.id.message_input);
        send_button = findViewById(R.id.send_button);
        recyclerview = findViewById(R.id.chat_recyclerview);

        LinearLayoutManager layoutManager = new WrapLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);

        recyclerview.setLayoutManager(layoutManager);
        recyclerview.setItemAnimator(new DefaultItemAnimator());

        recyclerview.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                recyclerview.scrollBy(0, oldBottom - bottom);
            }
        });

        back_button.setOnClickListener(view -> {
            super.onBackPressed();
        });

        send_button.setOnClickListener(view -> {
            if (!chatExists) {
                currentConversationId = ConversationUtil.newConversation(user, participant);
                Log.d(TAG, "Conversation (" + currentConversationId + ") created");
                loadConversation(currentConversationId);
            }

            if (!message_input.getText().toString().isEmpty()) {
                sendMessage(message_input.getText().toString());
                message_input.setText("");
            }
        });

        participant_name.setText(participant.getUsername());

        if (!participant.getPhotoUrl().isEmpty()) {
            Glide.with(getApplicationContext())
                    .load(Uri.parse(participant.getPhotoUrl()))
                    .placeholder(R.drawable.ic_account_circle_black)
                    .circleCrop()
                    .into(profile_image);
        } else {
            profile_image.setImageResource(R.drawable.ic_account_circle_black);
        }

        chatExists = ConversationUtil.conversationExistsForBoth(user, participant);

        if (chatExists) {
            currentConversationId = ConversationUtil.getConversationId(participant, user.getConversations());
            loadConversation(currentConversationId);
        }
    }

    private void loadConversation(String cid) {
        Query messages = conversations
                .child(currentConversationId)
                .child(Key.Conversation.MESSAGES);

        FirebaseRecyclerOptions<Message> options = new FirebaseRecyclerOptions.Builder<Message>()
                .setQuery(messages, Message.class)
                .build();

        adapter = new MessageAdapter(getApplicationContext(), options);
        adapter.setConversationId(cid);
        adapter.setParticipant(participant);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerview.smoothScrollToPosition(positionStart);
            }
        });

        recyclerview.setAdapter(adapter);
        adapter.startListening();
    }

    private void sendMessage(String message) {
        Map<String, String> data = new HashMap<>();

        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference()
                .child(Key.Conversation.CONVERSATIONS)
                .child(currentConversationId)
                .child(Key.Conversation.MESSAGES);

        String messageId = messagesRef.push().getKey();

        if (messageId != null && messageId.startsWith("-")) {
            messageId = messageId.substring(1);
        }

        // Save the message data
        data.put(Key.Message.MESSAGE, message.trim());
        data.put(Key.Message.ID, messageId);
        data.put(Key.Message.TYPE, "text");
        data.put(Key.Message.SENDER_ID, user.getUid());
        data.put(Key.Message.TIMESTAMP, String.valueOf(System.currentTimeMillis()));

        messagesRef.child(messageId).setValue(data);

        try {
            sendNotification(message);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void sendNotification(String message) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        JSONObject notificationObj = new JSONObject();
        notificationObj.put("title", user.getUsername());
        notificationObj.put("body", message);

        JSONObject dataObj = new JSONObject();
        dataObj.put("userId", user.getUid());

        jsonObject.put("notification", notificationObj);
        jsonObject.put("data", dataObj);
        jsonObject.put("to", participant.getDeviceToken());

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer AIzaSyBhEaPNNyzZjZryMITa9yhhhALfUoBUWwg")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });
    }

    private void getParticipant(String uid) {
        UserUtils.getUserByUid(uid, result -> {
            if (result.getUser() != null) {
                participant = result.getUser();
                getUser();
            } else {
                Log.e(TAG, result.getError().getMessage());
            }
        });
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

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Remove the following line to prevent stopping adapter listening in onPause()
        // adapter.stopListening();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.startListening();
            adapter.notifyDataSetChanged();
        }
    }
}