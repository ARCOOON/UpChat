package com.devusercode.upchat.adapter;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devusercode.upchat.ListUsersActivity;

public class WrapLayoutManager extends LinearLayoutManager {
    public WrapLayoutManager(Context context) {
        super(context);
    }

    public WrapLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (Exception e) {
            if (!(e instanceof IndexOutOfBoundsException)) {
                throw e;
            }
        }
    }
}
