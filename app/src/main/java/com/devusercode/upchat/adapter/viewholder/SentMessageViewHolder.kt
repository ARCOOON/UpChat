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
import com.devusercode.upchat.adapter.MessageAdapter
import com.devusercode.upchat.models.Message
import com.devusercode.upchat.security.AES
import com.devusercode.upchat.security.MAC
import com.devusercode.upchat.utils.GetTimeAgo

class SentMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val TAG = "MessageAdapter@${this.javaClass.simpleName}"

    private var message: TextView
    private var time: TextView
    private var cardview: LinearLayout
    private var rootLayout: LinearLayout
    private var verified: ImageView

    init {
        rootLayout = view.findViewById(R.id.root_layout)
        cardview = view.findViewById(R.id.materialcardview1)
        message = view.findViewById(R.id.message_content)
        time = view.findViewById(R.id.message_time)
        verified = view.findViewById(R.id.message_verified)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun bind(model: Message, cid: String, uid: String) {
        val aes = AES(uid)
        val mac = MAC(cid)

        val messageDecrypted = aes.decrypt(model.message!!)

        if (model.mac != null) {
            val messageMac = mac.generate(messageDecrypted)
            val verify = mac.verifyMAC(model.mac!!, messageMac)

            if (verify) {
                verified.visibility = View.VISIBLE
            } else {
                verified.visibility = View.GONE
            }
        }

        message.text = messageDecrypted
        time.text = GetTimeAgo.parse(model.timestamp!!)

        cardview.setOnLongClickListener { view: View ->
            MessageAdapter.conversationId = cid
            MessageAdapter.showTooltipOverlay(view, model)
            true
        }
    }
}