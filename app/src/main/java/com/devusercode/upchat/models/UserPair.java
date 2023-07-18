package com.devusercode.upchat.models;

public class UserPair {
    private final User user;
    private final String cid;

    public UserPair(User user, String conversationId) {
        this.user = user;
        this.cid = conversationId;
    }

    public User getUser() {
        return user;
    }

    public String getConversationId() {
        return cid;
    }
}
