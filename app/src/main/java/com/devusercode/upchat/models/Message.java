package com.devusercode.upchat.models;

import android.util.Log;

import com.devusercode.upchat.utils.GetTimeAgo;

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
        return GetTimeAgo.parse(this.timestamp);
    }

    public String getType() {
        return this.type;
    }

    public String getMessageId() {
        return this.messageId;
    }
}
