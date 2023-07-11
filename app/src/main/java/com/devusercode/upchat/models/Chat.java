package com.devusercode.upchat.models;

public class Chat {
    private String name;
    private String lastMessage;
    private String profileImage;

    public Chat() {
        // Required empty public constructor for Firebase
    }

    public String getName() {
        return name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getProfileImage() {
        return profileImage;
    }
}
