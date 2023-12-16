package com.devusercode.upchat.adapter.viewholder

import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.devusercode.upchat.R
import com.devusercode.upchat.models.Message
import com.devusercode.upchat.models.User
import com.devusercode.upchat.security.AES
import com.devusercode.upchat.security.MAC
import com.devusercode.upchat.utils.GetTimeAgo

class ReceivedMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val TAG = "MessageAdapter@ReceivedMessageViewHolder"
    var message: TextView
    var time: TextView
    var cardview: LinearLayout
    var root_layout: LinearLayout
    var verified: ImageView

    init {
        root_layout = view.findViewById(R.id.root_layout)
        cardview = view.findViewById(R.id.materialcardview1)
        message = view.findViewById(R.id.message_content)
        time = view.findViewById(R.id.message_time)
        verified = view.findViewById(R.id.message_verified)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun bind(model: Message, cid: String, uid: String) {
        val aes = AES(uid)
        val mac = MAC(cid)

        val _message = aes.decrypt(model.message!!)

        if (model.mac != null) {
            val mac_generated = mac.generate(_message)
            val verify = mac.verifyMAC(model.mac!!, mac_generated)

            if (verify) {
                verified.visibility = View.VISIBLE
            } else {
                verified.visibility = View.GONE
            }
        }

        message.text = _message
        time.text = GetTimeAgo.parse(model.timestamp!!)
    }
}