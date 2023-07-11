package com.devusercode.upchat.models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class User {
    private String deviceId, deviceToken, username, email, uid, photoUrl, joined;
    private Map<String, String> conversations;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String deviceId, String deviceToken, String username, String email, String uid, String photoUrl, String joined) {
        this.deviceId = deviceId;
        this.deviceToken = deviceToken;
        this.username = username;
        this.email = email;
        this.uid = uid;
        this.photoUrl = photoUrl;
        this.joined = joined;
    }

    /* * SETTERS * */
    public void setDeviceId(String _deviceId) {
        this.deviceId = _deviceId;
    }

    public void setDeviceToken(String _deviceToken) {
        this.deviceId = _deviceToken;
    }

    public void setUsername(String _username) {
        this.username = _username;
    }

    public void setEmail(String _email) {
        this.email = _email;
    }

    public void setUid(String _uid) {
        this.uid = _uid;
    }

    public void setPhotoUrl(String _photoUrl) {
        this.photoUrl = _photoUrl;
    }

    public void setJoined(String _joined) {
        this.joined = _joined;
    }

    /* * GETTERS * */
    public String getDeviceId() {
        return this.deviceId;
    }

    public String getDeviceToken() {
        return this.deviceToken;
    }

    public String getUsername() {
        return this.username;
    }

    public String getEmail() {
        return this.email;
    }

    public String getUid() {
        return this.uid;
    }

    public String getPhotoUrl() {
        return this.photoUrl;
    }

    public String getJoined() {
        return this.joined;
    }

    public String getFormattedJoined() {
        Date date = new Date(Long.parseLong(this.joined));
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date);
    }
    
    public Map<String, String> getConversations() {
        return this.conversations;
    }

    public List<String> getConversationIds() {
        List<String> conversationIds = new ArrayList<>();

        for (Map.Entry<String, String> entry : this.conversations.entrySet()) {
            conversationIds.add(entry.getValue());
        }

        return conversationIds;
    }

    public void setConversation(Map<String, String> conversations) {
        this.conversations = conversations;
    }

    public void addConversation(String uid, String cid) {
        conversations.put(uid, cid);
    }

    public void removeConversation(String uid) {
        conversations.remove(uid);
    }

    public void clearConversations() {
        conversations.clear();
    }

    public Map<String, Object> getUserInfo() {
        HashMap<String, Object> _userinfo = new HashMap<>();

        _userinfo.put("deviceId", this.deviceId);
        _userinfo.put("deviceToken", this.deviceToken);
        _userinfo.put("username", this.username);
        _userinfo.put("email", this.email);
        _userinfo.put("uid", this.uid);
        _userinfo.put("photoUrl", this.photoUrl);
        _userinfo.put("joined", this.joined);
        _userinfo.put("conversations", this.conversations);

        return _userinfo;
    }
}