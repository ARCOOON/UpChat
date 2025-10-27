package com.devusercode.upchat.adapter.viewholder

import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.devusercode.upchat.R
import com.devusercode.upchat.adapter.MessageAdapter
import com.devusercode.upchat.models.Message
import com.devusercode.upchat.security.AES
import com.devusercode.upchat.security.MAC
import com.devusercode.upchat.security.MessageIntegrity
import com.devusercode.upchat.utils.GetTimeAgo

class SentImageViewHolder(private var view: View) : RecyclerView.ViewHolder(view) {
    private val TAG = "MessageAdapter@${this.javaClass.simpleName}"

    private var messageView: TextView = view.findViewById(R.id.message_content)
    private var imageView: ImageView = view.findViewById(R.id.image_view)
    private var timeView: TextView = view.findViewById(R.id.message_time)
    private var cardView: LinearLayout = view.findViewById(R.id.materialcardview1)
    // private var rootLayout: LinearLayout = view.findViewById(R.id.root_layout)
    private var verified: ImageView = view.findViewById(R.id.message_verified)

    @RequiresApi(Build.VERSION_CODES.S)
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

        messageView.text = decrypted
        timeView.text = GetTimeAgo.parse(model.timestamp!!)

        Log.d(TAG, "Url: ${model.url}")

        Glide.with(view.context)
            .load(Uri.parse(model.url))
            .override(700, 900)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .fitCenter()
            .transform(RoundedCorners(14))
            .into(imageView)

        cardView.setOnLongClickListener { view: View ->
            MessageAdapter.conversationId = cid
            MessageAdapter.showTooltipOverlay(view, model)
            true
        }
    }
}