package com.devusercode.upchat

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@RequiresApi(Build.VERSION_CODES.O)
class HomeActivity : AppCompatActivity() {
    @Suppress("PrivatePropertyName")
    private val TAG = HomeActivity::class.java.simpleName
    private val auth = FirebaseAuth.getInstance()
    private var user: User? = null
    private lateinit var recyclerView: RecyclerView
    private var adapter: HomeAdapter? = null
    private var openConversations: MutableList<UserPair>? = null
    private val userListeners: MutableMap<String, ValueEventListener> = HashMap()
    private lateinit var storageController: StorageController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        storageController = StorageController.getInstance(this)!!

        loadUser()
        setupToolbar()
        setupViews()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.findViewById<TextView>(R.id.toolbar_profile_name).text =
            getString(R.string.app_name)
    }

    private fun setupViews() {
        findViewById<FloatingActionButton>(R.id.new_conversation_button).setOnClickListener {
            startActivity(Intent(this, ListUsersActivity::class.java))
        }

        recyclerView = findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadUser() {
        if (storageController.contains("user")) {
            val suser = storageController.getUser("user")

            if (suser != null) {
                user = suser
                Log.d(TAG, "User loaded from Storage: ${user?.info}")
                loadOpenConversations()
            } else {
                Log.e(TAG, "User not found in Storage")
                startActivity(Intent(this, LoginActivity::class.java))
            }
            return
        }

        UserUtils.getUserByUid(auth.currentUser!!.uid) { result ->
            if (result.code == ErrorCodes.SUCCESS) {
                user = result.user!!
                loadOpenConversations()
            } else {
                if (result.user == null) {
                    Log.e(TAG, result.error?.message!!)
                }

                Toast.makeText(this, "Error: ${result.code}", Toast.LENGTH_SHORT).show()
                if (result.code == ErrorCodes.USER_NOT_FOUND) {
                    startActivity(Intent(this, LoginActivity::class.java))
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
        user?.conversations?.let {
            // Continue with conversations
            openConversations = mutableListOf()
            val conversationIds = user?.getConversationIds() ?: run {
                Log.e(TAG, "Conversation IDs not found")
                return
            }

            conversationIds.forEach { cid ->
                Log.d(TAG, "Fetching conversation: $cid")
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
        } ?: return
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
        openConversations?.firstOrNull { it.user.uid == userId }?.let { userPair ->
            if (userPair.user.online.toBoolean() != isOnline) {
                userPair.user.online = isOnline.toString()
                adapter?.update(openConversations!!)
            }
        }
    }

    private fun removeUserOnlineStatusListeners() {
        openConversations?.forEach { userPair ->
            val uid = userPair.user.uid!!
            userListeners[uid]?.let {
                FirebaseDatabase.getInstance().getReference("users").child(uid)
                    .removeEventListener(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_profile -> startActivity(Intent(this, MyProfileActivity::class.java))
            R.id.menu_item_logout -> logout()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun logout() {
        auth.signOut()
        storageController.apply {
            set("save_login_info", false)
            remove("user")
            remove("email")
            remove("password")
        }
        startActivity(Intent(this, LoginActivity::class.java))
    }

    override fun onStop() {
        super.onStop()
        removeUserOnlineStatusListeners()
    }

    override fun onStart() {
        super.onStart()
        UserUtils.update("online", "true")
        openConversations?.forEach { userPair ->
            setupUserOnlineStatusListener(userPair.user.uid!!)
        }
    }
}
