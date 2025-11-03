package com.devusercode.upchat

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devusercode.upchat.adapter.HomeAdapter
import com.devusercode.upchat.models.User
import com.devusercode.upchat.models.UserPair
import com.devusercode.upchat.utils.ConversationUtil
import com.devusercode.upchat.utils.ErrorCodes
import com.devusercode.upchat.utils.StorageController
import com.devusercode.upchat.utils.UserUtils
import com.devusercode.upchat.utils.setComposeContent
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@RequiresApi(Build.VERSION_CODES.O)
class HomeActivity : AppCompatActivity() {
    private val TAG = HomeActivity::class.java.simpleName
    private val auth = FirebaseAuth.getInstance()
    private var user: User? = null
    private var adapter: HomeAdapter? = null
    private var openConversations: MutableList<UserPair>? = null
    private val userListeners: MutableMap<String, ValueEventListener> = HashMap()

    private lateinit var contentView: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var storageController: StorageController

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_profile -> startActivity(Intent(this, MyProfileActivity::class.java))
            R.id.menu_item_logout -> logout()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logout() {
        auth.signOut()
        storageController.apply {
            set("save_login_info", false)
            remove("email")
            remove("password")
        }
        startActivity(Intent(this, LoginActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentView = setComposeContent(R.layout.activity_home)

        storageController = StorageController.getInstance(this)!!

        setupToolbar()
        setupViews()
        loadUser()
    }

    private fun setupToolbar() {
        val toolbar = contentView.findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.findViewById<TextView>(R.id.toolbar_profile_name).text =
            getString(R.string.app_name)
    }

    private fun setupViews() {
        contentView.findViewById<FloatingActionButton>(R.id.new_conversation_button).setOnClickListener {
            startActivity(Intent(this, ListUsersActivity::class.java))
        }

        recyclerView = contentView.findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadUser() {
        if (storageController.contains("user")) {
            user = storageController.getUser("user")
                .also {
                    Log.d(TAG, "User from Storage: ${it?.info}")
                }
        } else {
            UserUtils.getUserByUid(auth.currentUser!!.uid) { result ->
                if (result.code == ErrorCodes.SUCCESS) {
                    user = result.user!!
                    loadOpenConversations()
                } else {
                    Log.e(TAG, result.error?.message!!)
                    Toast.makeText(this, "Error: ${result.code}", Toast.LENGTH_SHORT).show()
                    if (result.code == ErrorCodes.USER_NOT_FOUND) {
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                }
            }
        }

        if (user != null) {
            FirebaseDatabase.getInstance().getReference("users")
                .child(user?.uid!!)
                .child(Key.User.ONLINE)
                .onDisconnect()
                .setValue(false.toString())
        }
    }

    private fun loadOpenConversations() {
        if (user == null || user!!.conversations == null) return

        openConversations = mutableListOf()
        val conversationIds = user?.getConversationIds() ?: return

        conversationIds.forEach { cid ->
            ConversationUtil.getConversationById(cid) { task ->
                if (!task!!.isSuccessful) {
                    Log.e(TAG, task.error?.message!!)
                } else {
                    val conversation = task.getConversation()
                    conversation?.getParticipant { result ->
                        if (!result.isSuccessful) {
                            Log.e(TAG, result.error?.message!!)
                        } else {
                            val userPair = UserPair(result.user!!, cid)
                            openConversations?.add(userPair)
                        }
                        createAdapter()
                    }
                }
            }
        }
    }

    private fun createAdapter() {
        if (openConversations.isNullOrEmpty()) return
        adapter = HomeAdapter(openConversations!!)
        recyclerView.adapter = adapter
        openConversations?.forEach { userPair ->
            setupUserOnlineStatusListener(userPair.user.uid!!)
        }
    }

    private fun setupUserOnlineStatusListener(userId: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child("online").exists()) {
                    val isOnline =
                        dataSnapshot.child("online").getValue(String::class.java)?.toBoolean()
                    isOnline?.let { updateOnlineStatus(userId, it) }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, "setupUserOnlineStatusListener -> onCancelled: ${databaseError.message}")
            }
        }
        userRef.addValueEventListener(listener)
        userListeners[userId] = listener
    }

    private fun updateOnlineStatus(userId: String, isOnline: Boolean) {
        openConversations?.forEach { userPair ->
            if (userPair.user.uid == userId) {
                userPair.user.online = isOnline.toString()
            }
        }
        adapter?.update(openConversations!!)
    }

    override fun onStop() {
        super.onStop()
        openConversations?.forEach { userPair ->
            val uid = userPair.user.uid!!
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
            userListeners[uid]?.let { userRef.removeEventListener(it) }
        }
    }

    override fun onStart() {
        super.onStart()
        UserUtils.update("online", "true")
        openConversations?.forEach { userPair ->
            setupUserOnlineStatusListener(userPair.user.uid!!)
        }
    }
}
