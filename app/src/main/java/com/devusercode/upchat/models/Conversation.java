package com.devusercode.upchat.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.devusercode.upchat.utils.UserUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class Conversation {
    public static class Result {
        private User user;
        private User participant;
        private Error error;

        public Result() {
            this.user = null;
            this.participant = null;
            this.error = null;
        }

        public Result(User user, User participant, Error error) {
            this.user = user;
            this.participant = participant;
            this.error = error;
        }

        public Result(User user, User participant) {
            this.user = user;
            this.participant = participant;
            this.error = null;
        }

        public User getUser() {
            return this.user;
        }

        public User getParticipant() {
            return this.participant;
        }

        public Error getError() {
            return this.error;
        }

        public boolean isSuccessful() {
            return this.error == null && (this.user != null | this.participant != null);
        }

        public void setUser(User _user) {
            this.user = _user;
        }

        public void setParticipant(User _user) {
            this.user = _user;
        }

        public void setError(Error _error) {
            this.error = _error;
        }

        @NonNull
        @Override
        public String toString() {
            Map<String, Object> _user = user != null ? user.getUserInfo() : null;
            Map<String, Object> _participant = participant != null ? participant.getUserInfo() : null;
            String _error = error != null ? error.getMessage() : null;

            return "Result(" + _user + ", " + _participant + ", " + _error + ")";
        }
    }

    private final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private List<String> members;
    private Map<String, Message> messages;

    public Conversation() {
    }

    public List<String> getMembers() {
        return members;
    }

    public Map<String, Message> getMessages() {
        return messages;
    }

    public Message getMessageById(String mid) {
        return messages.get(mid);
    }

    public void getParticipant(Consumer<UserUtils.Result> onFinish) {
        members.remove(uid);
        UserUtils.getUserByUid(members.get(0), result -> {
            if (result.isSuccessful()) {
                onFinish.accept(result);
            } else {
                onFinish.accept(result);
            }
        });
    }

    public String getParticipantUid() {
        members.remove(uid);
        return members.get(0);
    }
}
