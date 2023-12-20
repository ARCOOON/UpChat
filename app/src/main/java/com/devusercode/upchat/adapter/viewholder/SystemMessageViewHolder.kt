package com.devusercode.upchat.adapter.viewholder

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devusercode.upchat.R
import com.devusercode.upchat.models.Message
import com.google.android.material.card.MaterialCardView

class SystemMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val TAG = "MessageAdapter@SystemMessageViewHolder"
    var message: TextView
    var cardview: MaterialCardView
    var rootLayout: LinearLayout

    init {
        rootLayout = view.findViewById(R.id.root_layout)
        cardview = view.findViewById(R.id.materialcardview1)
        message = view.findViewById(R.id.message_content)
    }

    fun bind(model: Message) {
        // Log.d(TAG, "senderId: system")
        message.text = model.message!!.trim { it <= ' ' }
    }
}