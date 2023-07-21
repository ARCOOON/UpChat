package com.devusercode.upchat.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.devusercode.upchat.models.Conversation;
import com.devusercode.upchat.models.Message;
import com.devusercode.upchat.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ConversationUtil {
    private static final String TAG = ConversationUtil.class.getSimpleName();
    private static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private static final String STAG = "ConversationUtil";
    public static final String REF = "conversations";

    public static class ConversationResult {
        public Conversation conversation;
        public Error error;

        public ConversationResult(Conversation conversation, Error error) {
            this.conversation = conversation;
            this.error = error;
        }

        public ConversationResult(Conversation conversation) {
            this.conversation = conversation;
            this.error = null;
        }

        public Conversation getConversation() {
            return conversation;
        }

        public Error getError() {
            return error;
        }

        public boolean isSuccessful() {
            return error == null && conversation != null;
        }
    }

    public static String getConversationId(@NonNull User user, Map<String, String> conversations) {
        if (conversations != null) {
            for (Map.Entry<String, String> entry : conversations.entrySet()) {
                if (entry.getKey().equals(user.getUid())) {
                    return entry.getValue();
                }
            }
        }
        // Return null if no matching user ID found
        return null;
    }

    public static boolean conversationExistsForBoth(User user, User participant) {
        if (user.getConversations() != null && participant.getConversations() != null) {
            boolean chatExists = participant.getConversations().containsKey(user.getUid());
            boolean chatExists2 = user.getConversations().containsKey(participant.getUid());
            return chatExists && chatExists2;
        } else {
            if (user.getConversations() == null) {
                Log.d(STAG, "No conversation found for user");
            } else {
                Log.d(STAG, "No conversation found for participant");
            }
            return false;
        }
    }

    public static boolean conversationExists(String uid, Map<String, String> conversations) {
        return conversations.containsKey(uid);
    }

    public static String newConversation(User user, User participant) {
        DatabaseReference conversationsRef = FirebaseDatabase.getInstance().getReference("conversations");
        // Generate a unique conversation ID
        String conversationId = conversationsRef.push().getKey();

        // Remove hyphen at the beginning, if exists
        if (conversationId != null && conversationId.startsWith("-")) {
            conversationId = conversationId.substring(1);
        }

        // Add the conversation ID to the user's profile
        DatabaseReference userConversationsRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(user.getUid())
                .child("conversations");
        userConversationsRef.child(participant.getUid()).setValue(conversationId);

        // Add the conversation ID to the participant's profile
        DatabaseReference participantConversationsRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(participant.getUid())
                .child("conversations");
        participantConversationsRef.child(user.getUid()).setValue(conversationId);

        // Add both user IDs to the conversation's members
        DatabaseReference membersRef = conversationsRef.child(conversationId).child("members");
        List<String> members = new ArrayList<>();

        members.add(user.getUid());
        members.add(participant.getUid());

        membersRef.setValue(members);

        return conversationId;
    }

    public static void getConversationById(String cid, @NonNull Consumer<ConversationResult> onFinish) {
        DatabaseReference conversationRef = FirebaseDatabase.getInstance().getReference()
                .child("conversations")
                .child(cid);

        conversationRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();

                if (dataSnapshot != null && dataSnapshot.exists()) {
                    Conversation conversation = dataSnapshot.getValue(Conversation.class);

                    if (conversation != null) {
                        onFinish.accept(new ConversationResult(conversation));
                    } else {
                        Error error = new Error("Unknown error while retrieving the conversation!");
                        onFinish.accept(new ConversationResult(null, error));
                    }
                } else {
                    Error error = new Error("ConversationNotFound (" + cid + ")");
                    onFinish.accept(new ConversationResult(null, error));
                }
            } else {
                Error error = new Error(task.getException().getMessage());
                onFinish.accept(new ConversationResult(null, error));
            }
        });
    }

    public static void getLastMessageByTimestamp(String conversationId, Consumer<String> onFinish) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference()
                .child("conversations")
                .child(conversationId)
                .child("messages");

        Query lastMessageQuery = messagesRef.orderByChild("timestamp").limitToLast(1);

        lastMessageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot lastMessageSnapshot = dataSnapshot.getChildren().iterator().next();
                    String lastMessage = lastMessageSnapshot.child("message").getValue(String.class);
                    onFinish.accept(lastMessage);
                } else {
                    onFinish.accept(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        });
    }

    public static void getLastMessage(String conversationId, Consumer<Message> onFinish) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference()
                .child("conversations")
                .child(conversationId)
                .child("messages");

        Query lastMessageQuery = messagesRef.orderByChild("timestamp").limitToLast(1);

        lastMessageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot lastMessageSnapshot = dataSnapshot.getChildren().iterator().next();
                    Message lastMessage = lastMessageSnapshot.getValue(Message.class);
                    onFinish.accept(lastMessage);
                } else {
                    onFinish.accept(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        });
    }

}
