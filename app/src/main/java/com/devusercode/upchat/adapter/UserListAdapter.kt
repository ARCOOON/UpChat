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

class UserListAdapter(app: AppCompatActivity, options: FirebaseRecyclerOptions<User?>) :
    FirebaseRecyclerAdapter<User, UserListAdapter.UserViewHolder>(options) {
    private val TAG = this.javaClass.simpleName

    private val firebaseUser = FirebaseAuth.getInstance().currentUser
    private val context = app.applicationContext

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: UserViewHolder, position: Int, user: User) {
        // Exclude yourself from the list
        if (firebaseUser != null && user.uid == firebaseUser.uid) {
            holder.root.visibility = View.GONE
            return
        }

        // Bind user data to the view holder
        holder.username.text = user.username
        holder.email.text = user.email

        // Load a profile picture into the view holder
        if (!user.photoUrl.isNullOrBlank()) {
            Glide.with(context)
                .load(Uri.parse(user.photoUrl))
                .placeholder(R.drawable.ic_account_circle_black)
                .circleCrop()
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_account_circle_black)
        }


        // Handle add user button click
        holder.addUserButton.setOnClickListener {
            val intent = Intent(context, ConversationActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("uid", user.uid)
            context.startActivity(intent)
        }

        holder.materialCardView.setOnClickListener {
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
        var profileImage: ImageView
        var root: LinearLayout
        var materialCardView: MaterialCardView
        var addUserButton: Button

        init {
            root = view.findViewById(R.id.linear1)
            materialCardView = view.findViewById(R.id.materialcardview1)
            profileImage = view.findViewById(R.id.profile_image)
            username = view.findViewById(R.id.username)
            email = view.findViewById(R.id.email)
            addUserButton = view.findViewById(R.id.add_user_button)
        }
    }
}