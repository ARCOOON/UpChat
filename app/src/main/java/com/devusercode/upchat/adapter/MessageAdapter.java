package com.devusercode.upchat.adapter;

import static com.devusercode.upchat.utils.SketchwareUtil.showMessage;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.devusercode.upchat.R;
import com.devusercode.upchat.models.Message;
import com.devusercode.upchat.utils.GetTimeAgo;
import com.devusercode.upchat.utils.Util;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MessageAdapter extends FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder> {
    private static final String TAG = MessageAdapter.class.getSimpleName();
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private final Context currentContext;
    private static final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();

    private static String conversationId;

    public MessageAdapter(@NonNull Context context, @NonNull FirebaseRecyclerOptions<Message> options) {
        super(options);
        this.currentContext = context;
    }

    public void setConversationId(@NonNull String conversationId) {
        this.conversationId = conversationId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getSnapshots().get(position);

        if (message.getSenderId().equals(fuser.getUid())) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.item_conversation_sent, parent, false);
            return new SentMessageViewHolder(view);

        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.item_conversation_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }

        return null;
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Message model) {
        if (holder instanceof SentMessageViewHolder sentViewHolder) {
            sentViewHolder.bind(model);

        } else if (holder instanceof ReceivedMessageViewHolder receivedViewHolder) {
            receivedViewHolder.bind(model);
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView message, time;
        MaterialCardView cardview;
        LinearLayout root_layout;

        public SentMessageViewHolder(@NonNull View view) {
            super(view);
            root_layout = view.findViewById(R.id.root_layout);
            cardview = view.findViewById(R.id.materialcardview1);
            message = view.findViewById(R.id.message_content);
            time = view.findViewById(R.id.message_time);

            // Create LayoutParams with desired gravity
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) cardview.getLayoutParams();
            layoutParams.gravity = Gravity.END;
            // Set the LayoutParams for the cardView
            cardview.setLayoutParams(layoutParams);
        }

        public void bind(@NonNull Message model) {
            Log.d(TAG, "senderId: you");

            message.setText(model.getMessage().trim());
            time.setText(GetTimeAgo.parse(model.getTimestamp()));

            cardview.setOnLongClickListener(view -> {
                // showOptionsDialog(view.getContext(), model);
                showTooltipOverlay(view, model);

                return true;
            });

            // Create LayoutParams with desired gravity
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) cardview.getLayoutParams();
            layoutParams.gravity = Gravity.END;
            // Set the LayoutParams for the cardView
            cardview.setLayoutParams(layoutParams);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView message, time;
        MaterialCardView cardview;
        LinearLayout root_layout;

        public ReceivedMessageViewHolder(@NonNull View view) {
            super(view);
            root_layout = view.findViewById(R.id.root_layout);
            cardview = view.findViewById(R.id.materialcardview1);
            message = view.findViewById(R.id.message_content);
            time = view.findViewById(R.id.message_time);
        }

        public void bind(@NonNull Message model) {
            Log.d(TAG, "senderId: participant");

            message.setText(model.getMessage().trim());
            time.setText(GetTimeAgo.parse(model.getTimestamp()));

            // Create LayoutParams with desired gravity
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) cardview.getLayoutParams();
            layoutParams.gravity = Gravity.START;
            // Set the LayoutParams for the cardView
            cardview.setLayoutParams(layoutParams);
        }
    }

    private static void showTooltipOverlay(View anchorView, Message model) {
        // Inflate the tooltip overlay layout
        View tooltipView = LayoutInflater.from(anchorView.getContext()).inflate(R.layout.item_conversation_popup, null);
        LinearLayout root_layout = tooltipView.findViewById(R.id.root_layout);

        Util.setCornerRadius(root_layout, 50);

        // Create a PopupWindow to display the tooltip overlay
        PopupWindow popupWindow = new PopupWindow(tooltipView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        // Set the location of the popup window relative to the anchor view
        // popupWindow.showAsDropDown(anchorView);
        // popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
        popupWindow.setElevation(20);

        // Handle button clicks inside the tooltip overlay
        Button delete_button = tooltipView.findViewById(R.id.delete_button);
        Button reply_button = tooltipView.findViewById(R.id.reply_button);

        delete_button.setOnClickListener(v -> {
            String messageId = model.getMessageId();

            DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference()
                    .child("conversations")
                    .child(conversationId)
                    .child("messages")
                    .child(messageId);

            messageRef.removeValue()
                    .addOnCompleteListener(task -> {
                        popupWindow.dismiss();
                    })
                    .addOnFailureListener(error -> {
                        // Failed to delete the message
                        Log.e(TAG, error.getMessage());
                        popupWindow.dismiss();
                    });
        });

        reply_button.setOnClickListener(v -> {
            showMessage(anchorView.getContext(), "Not implemented yet.");
            popupWindow.dismiss(); // Dismiss the tooltip overlay
        });

        popupWindow.showAsDropDown(anchorView);
        popupWindow.setAnimationStyle(R.style.ConversationPopupWindow);
    }
}
