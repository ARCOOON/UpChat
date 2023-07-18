package com.devusercode.upchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.devusercode.upchat.ConversationActivity;
import com.devusercode.upchat.R;
import com.devusercode.upchat.models.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserAdapter extends FirebaseRecyclerAdapter<User, UserAdapter.UserViewHolder> {
    private final String TAG = this.getClass().getSimpleName();
    private final Context currentContext;
    private final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();

    public UserAdapter(@NonNull Context context, @NonNull FirebaseRecyclerOptions<User> options) {
        super(options);
        this.currentContext = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User user) {
        // Bind user data to the view holder
        if (!fuser.getUid().equals(user.getUid())) {
            holder.username.setText(user.getUsername());
            holder.email.setText(user.getEmail());

            // Load a profile picture into the view holder
            if (!user.getPhotoUrl().isEmpty()) {
                Glide.with(currentContext).load(Uri.parse(user.getPhotoUrl())).placeholder(R.drawable.ic_account_circle_black).circleCrop().into(holder.profile_image);
            } else {
                holder.profile_image.setImageResource(R.drawable.ic_account_circle_black);
            }
        } else {
            // Exclude your own user
            holder.root.setVisibility(View.GONE);
        }

        // Handle add user button click
        holder.add_user_button.setOnClickListener(view -> {
            Intent intent = new Intent(currentContext, ConversationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("uid", user.getUid());
            currentContext.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView username, email;
        ImageView profile_image;
        LinearLayout root;
        MaterialCardView materialcardview1;
        Button add_user_button;

        public UserViewHolder(@NonNull View view) {
            super(view);

            root = view.findViewById(R.id.linear1);
            materialcardview1 = view.findViewById(R.id.materialcardview1);
            profile_image = view.findViewById(R.id.profile_image);
            username = view.findViewById(R.id.username);
            email = view.findViewById(R.id.email);
            add_user_button = view.findViewById(R.id.add_user_button);
        }
    }
}