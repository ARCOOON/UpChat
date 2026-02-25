package com.devusercode.upchat.adapter

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

class WrapLayoutManager : LinearLayoutManager {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout)

    override fun onLayoutChildren(
        recycler: Recycler,
        state: RecyclerView.State,
    ) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: Exception) {
            if (e !is IndexOutOfBoundsException) {
                throw e
            }
        }
    }
}
