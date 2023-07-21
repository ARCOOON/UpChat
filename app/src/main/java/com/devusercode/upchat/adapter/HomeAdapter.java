package com.devusercode.upchat.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.devusercode.upchat.ConversationActivity;
import com.devusercode.upchat.R;
import com.devusercode.upchat.models.User;
import com.devusercode.upchat.models.UserPair;
import com.devusercode.upchat.utils.ConversationUtil;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {
    private final String TAG = this.getClass().getSimpleName();
    private final List<UserPair> data;

    public HomeAdapter(List<UserPair> openConversations) {
        this.data = openConversations;
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_home_user, parent, false);
        return new HomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        UserPair pair = data.get(position);

        holder.bind(pair);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class HomeViewHolder extends RecyclerView.ViewHolder {
        TextView username, last_message_text, last_message_time;
        MaterialCardView materialcardview1;
        ImageView profile_image;

        public HomeViewHolder(@NonNull View view) {
            super(view);

            materialcardview1 = view.findViewById(R.id.materialcardview1);
            profile_image = view.findViewById(R.id.profile_image);
            username = view.findViewById(R.id.username);
            last_message_text = view.findViewById(R.id.last_message_text);
            last_message_time = view.findViewById(R.id.last_message_time);
        }

        public void bind(UserPair pair) {
            User user = pair.getUser();
            String cid = pair.getConversationId();

            username.setText(user.getUsername());

            if (!user.getPhotoUrl().isEmpty()) {
                Glide.with(profile_image.getContext())
                        .load(Uri.parse(user.getPhotoUrl()))
                        .placeholder(R.drawable.ic_account_circle_black)
                        .circleCrop()
                        .into(profile_image);
            } else {
                profile_image.setImageResource(R.drawable.ic_account_circle_black);
            }

            ConversationUtil.getLastMessage(cid, last_msg -> {
                if (last_msg == null) {
                    last_message_text.setText("No messages available.");
                    last_message_time.setVisibility(View.GONE);

                } else if (last_msg.getSenderId().equals("system")) {
                    last_message_text.setVisibility(View.GONE);
                    last_message_time.setVisibility(View.GONE);

                } else {
                    last_message_text.setText(last_msg.getMessage());
                    last_message_time.setText(last_msg.getParsedTime());
                }
            });

            materialcardview1.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), ConversationActivity.class);
                intent.putExtra("uid", user.getUid());
                view.getContext().startActivity(intent);
            });
        }
    }
}

