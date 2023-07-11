package com.devusercode.upchat.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devusercode.upchat.R;
import com.devusercode.upchat.models.User;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {
    private final List<User> data;

    public HomeAdapter(List<User> openConversations) {
        this.data = openConversations;
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_user, parent, false);
        return new HomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        User user = data.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class HomeViewHolder extends RecyclerView.ViewHolder {
        TextView username;

        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
        }

        public void bind(User user) {
            username.setText(user.getUsername());
        }
    }
}

