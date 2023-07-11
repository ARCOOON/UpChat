package com.devusercode.upchat.models;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {
    private String senderId, message, timestamp, type, messageId;

    public Message() {
        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
    }

    public Message(String senderId, String message, String timestamp, String type) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
    }

    /* * Setters * */
    public void setSenderId(String _senderId) {
        this.senderId = _senderId;
    }

    public void setMessage(String _message) {
        this.message = _message;
    }

    public void setTimestamp(String _timestamp) {
        this.timestamp = _timestamp;
    }

    public void setType(String _type) {
        this.type = _type;
    }

    public void setMessageId(String _messageId) {
        this.messageId = _messageId;
    }

    /* * Getters * */
    public String getSenderId() {
        return this.senderId;
    }

    public String getMessage() {
        return this.message;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public String getParsedTime() {
        try {
            Date date = new Date(Long.parseLong(this.timestamp));
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

            return sdf.format(date);
        } catch (NumberFormatException e) {
            Log.e("MessageModel", "Failed to parse timestamp: " + this.timestamp);

            return "";
        }
    }

    public String getType() {
        return this.type;
    }

    public String getMessageId() {
        return this.messageId;
    }
}
