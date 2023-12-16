package com.devusercode.upchat.adapter

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devusercode.upchat.ConversationActivity
import com.devusercode.upchat.R
import com.devusercode.upchat.UserProfileActivity
import com.devusercode.upchat.models.User
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth

class UserAdapter(app: AppCompatActivity, options: FirebaseRecyclerOptions<User?>) :
    FirebaseRecyclerAdapter<User, UserAdapter.UserViewHolder>(options) {
    private val TAG = this.javaClass.simpleName
    private val fuser = FirebaseAuth.getInstance().currentUser
    private val context = app.applicationContext

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: UserViewHolder, position: Int, user: User) {
        // Exclude yourself from the list
        if (fuser != null && user.uid == fuser.uid) {
            holder.root.visibility = View.GONE
            return
        }

        // Bind user data to the view holder
        holder.username.text = user.username
        holder.email.text = user.email

        // Load a profile picture into the view holder
        if (user.photoUrl!!.isNotEmpty()) {
            Glide.with(context).load(Uri.parse(user.photoUrl))
                .placeholder(R.drawable.ic_account_circle_black).circleCrop()
                .into(holder.profile_image)
        } else {
            holder.profile_image.setImageResource(R.drawable.ic_account_circle_black)
        }


        // Handle add user button click
        holder.add_user_button.setOnClickListener {
            val intent = Intent(context, ConversationActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("uid", user.uid)
            context.startActivity(intent)
        }

        holder.materialcardview1.setOnClickListener {
            val intent = Intent(context, UserProfileActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("uid", user.uid)
            context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var username: TextView
        var email: TextView
        var profile_image: ImageView
        var root: LinearLayout
        var materialcardview1: MaterialCardView
        var add_user_button: Button

        init {
            root = view.findViewById(R.id.linear1)
            materialcardview1 = view.findViewById(R.id.materialcardview1)
            profile_image = view.findViewById(R.id.profile_image)
            username = view.findViewById(R.id.username)
            email = view.findViewById(R.id.email)
            add_user_button = view.findViewById(R.id.add_user_button)
        }
    }
}