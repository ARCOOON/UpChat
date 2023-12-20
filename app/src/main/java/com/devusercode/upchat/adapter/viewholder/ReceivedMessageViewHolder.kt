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
import com.devusercode.upchat.utils.GetTimeAgo

@RequiresApi(Build.VERSION_CODES.O)
class ReceivedMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val TAG = "MessageAdapter@ReceivedMessageViewHolder"

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
    }
}