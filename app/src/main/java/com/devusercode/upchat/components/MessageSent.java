package com.devusercode.upchat.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.devusercode.upchat.R;
import com.google.android.material.card.MaterialCardView;

public class MessageSent extends LinearLayout {
    private MaterialCardView cardView;
    private TextView messageContent;
    private TextView messageTime;

    public MessageSent(Context context) {
        super(context);
        init(context);
    }

    public MessageSent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MessageSent(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.item_conversation_sent, this, true);

        cardView = findViewById(R.id.materialcardview1);
        messageContent = findViewById(R.id.message_content);
        messageTime = findViewById(R.id.message_time);
    }

    public void setMessageContent(String content) {
        messageContent.setText(content);
    }

    public void setMessageTime(String time) {
        messageTime.setText(time);
    }
}

