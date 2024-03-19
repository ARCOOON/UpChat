package com.devusercode.upchat.adapter.viewholder

import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.RoundedCorner
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.devusercode.upchat.R
import com.devusercode.upchat.adapter.MessageAdapter
import com.devusercode.upchat.models.Message
import com.devusercode.upchat.security.AES
import com.devusercode.upchat.security.MAC
import com.devusercode.upchat.utils.GetTimeAgo

class SentImageViewHolder(private var view: View) : RecyclerView.ViewHolder(view) {
    private val TAG = "MessageAdapter@SentImageViewHolder"

    private var messageView: TextView = view.findViewById(R.id.message_content)
    private var imageView: ImageView = view.findViewById(R.id.image_view)
    private var timeView: TextView = view.findViewById(R.id.message_time)
    private var cardView: LinearLayout = view.findViewById(R.id.materialcardview1)
    private var rootLayout: LinearLayout = view.findViewById(R.id.root_layout)
    private var verified: ImageView = view.findViewById(R.id.message_verified)

    @RequiresApi(Build.VERSION_CODES.S)
    fun bind(model: Message, cid: String, uid: String) {
        val aes = AES(uid)
        val mac = MAC(cid)

        var message = model.message

        if (model.message != null) {
            message = aes.decrypt(model.message!!)
        }

        if (model.mac != null) {
            val messageMac = mac.generate(message!!)
            val verify = mac.verifyMAC(model.mac!!, messageMac)

            if (verify) {
                verified.visibility = View.VISIBLE
            } else {
                verified.visibility = View.GONE
            }
        }

        messageView.text = message
        timeView.text = GetTimeAgo.parse(model.timestamp!!)

        Log.d(TAG, "Url: ${model.url} | Uri: ${Uri.parse(model.url)}")

        Glide.with(view.context)
            .load(Uri.parse(model.url))
            .override(700, 900)
            .fitCenter()
            .dontAnimate()
            .transform(RoundedCorners(14))
            .into(imageView)

        cardView.setOnLongClickListener { view: View ->
            MessageAdapter.conversationId = cid
            MessageAdapter.showTooltipOverlay(view, model)
            true
        }
    }
}