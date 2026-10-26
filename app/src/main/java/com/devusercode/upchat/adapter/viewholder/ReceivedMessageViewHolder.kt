package com.devusercode.upchat.adapter.viewholder

import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.devusercode.upchat.R
import com.devusercode.upchat.models.Message
import com.devusercode.upchat.security.AES
import com.devusercode.upchat.security.MAC
import com.devusercode.upchat.security.MessageIntegrity
import com.devusercode.upchat.utils.GetTimeAgo

@RequiresApi(Build.VERSION_CODES.O)
class ReceivedMessageViewHolder(
    view: View,
) : RecyclerView.ViewHolder(view) {
    private val TAG = "MessageAdapter@${this.javaClass.simpleName}"

    var message: TextView
    var time: TextView
    var cardView: LinearLayout
    var rootLayout: LinearLayout
    var verified: ImageView

    init {
        rootLayout = view.findViewById(R.id.root_layout)
        cardView = view.findViewById(R.id.materialcardview1)
        message = view.findViewById(R.id.message_content)
        time = view.findViewById(R.id.message_time)
        verified = view.findViewById(R.id.message_verified)
    }

    fun bind(model: Message, cid: String, sharedSecret: String) {
        val aes = AES(sharedSecret, cid)
        val mac = MAC(sharedSecret, cid)

        val decrypted = model.message?.let { aes.decrypt(it) } ?: ""
        val payload = MessageIntegrity.canonicalize(model, cid)
        val hasMac = model.mac != null
        val isVerified = mac.verify(payload, model.mac)

        if (hasMac) {
            verified.visibility = View.VISIBLE
            verified.setImageResource(
                if (isVerified) R.drawable.ic_verified_white else R.drawable.ic_round_error_white
            )
        } else {
            verified.visibility = View.GONE
        }

        message.text = decrypted
        time.text = GetTimeAgo.parse(model.timestamp!!)
    }
}
