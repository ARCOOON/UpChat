package com.devusercode.upchat

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.devusercode.upchat.adapter.MessageAdapter
import com.devusercode.upchat.adapter.WrapLayoutManager
import com.devusercode.upchat.models.Message
import com.devusercode.upchat.models.User
import com.devusercode.upchat.utils.ConversationUtil
import com.devusercode.upchat.utils.ErrorCodes
import com.devusercode.upchat.utils.StorageController
import com.devusercode.upchat.utils.UserUtils
import com.devusercode.upchat.utils.Util
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

@RequiresApi(Build.VERSION_CODES.O)
class ConversationActivity : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName
    private val auth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val conversations = firebaseDatabase.getReference("conversations")
    private val users = firebaseDatabase.getReference("users")

    /* Toolbar */
    private lateinit var backButton: Button
    private lateinit var participantName: TextView
    private lateinit var profileImage: ImageView
    private lateinit var statusOnline: TextView

    /* Content */
    private var adapter: MessageAdapter? = null
    private lateinit var recyclerview: RecyclerView
    private lateinit var attachButton: Button
    private lateinit var messageInput: TextInputEditText
    private lateinit var sendButton: Button

    private var participant: User? = null
    private var user: User? = null

    private lateinit var storageController: StorageController
    private lateinit var conversationUtil: ConversationUtil

    private var file: Uri? = null
    private var filePickerLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
            Util.showMessage(this, "Image selected...")
            file = result
        }

    private lateinit var currentConversationId: String
    private var chatExists = false

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        storageController = StorageController.getInstance(this)!!

        // Simplified logic to fetch user and participant data
        if (intent != null && intent.extras != null) {
            // The uid for the participant
            val uid = intent?.getStringExtra("uid") ?: run {
                Log.e(TAG, "No uid for the participant provided in intent")
                return
            }

            // fetch the participant
            UserUtils.getUserByUid(uid) { result ->
                if (result.isSuccessful && result.user != null) {
                    participant = result.user
                } else {
                    Log.e(TAG, result.error?.message ?: "Error fetching participant")
                }
            }
            fetchUser()
        }
    }

    private fun fetchUser() {
        // Check if the current user is already available in storage
        val suser = storageController.getUser("user")

        if (suser != null) {
            user = suser
            Log.d(TAG, "User loaded from Storage: ${suser.info}")
            initialize()
        } else {
            // If not in storage, fetch from Firebase
            UserUtils.getUserByUid(auth.currentUser!!.uid) { result ->
                if (result.isSuccessful && result.user != null) {
                    user = result.user
                    // storageController["user"] = result.user // Save the user for future use
                    initialize()
                } else {
                    Log.e(TAG, result.error?.message!!)
                }
            }
        }
    }

    override fun startActivity(intent: Intent) {
        super.startActivity(intent)
        overridePendingTransition(R.anim.right_in, R.anim.left_out)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.left_in, R.anim.right_out)
    }

    private fun initialize() {
        // val coordinator = findViewById<CoordinatorLayout>(R.id._coordinator)
        // val appBar = findViewById<AppBarLayout>(R.id.app_bar)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        toolbar.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("uid", participant?.uid)
            startActivity(intent)
        }

        backButton = toolbar.findViewById(R.id.back_button)
        participantName = toolbar.findViewById(R.id.participant_name)
        profileImage = toolbar.findViewById(R.id.profile_image)
        statusOnline = toolbar.findViewById(R.id.status_online)

        attachButton = findViewById(R.id.attach_button)
        messageInput = findViewById(R.id.message_input)
        sendButton = findViewById(R.id.send_button)
        recyclerview = findViewById(R.id.chat_recyclerview)

        attachButton.setOnClickListener { filePickerLauncher.launch("image/*") }

        val layoutManager: LinearLayoutManager =
            WrapLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)

        recyclerview.layoutManager = layoutManager
        recyclerview.itemAnimator = DefaultItemAnimator()

        // recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {})
        // view: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
        recyclerview.addOnLayoutChangeListener { _: View?, _: Int, _: Int, _: Int, bottom: Int, _: Int, _: Int, _: Int, oldBottom: Int ->
            if (bottom < oldBottom) {
                recyclerview.scrollBy(0, oldBottom - bottom)
            }
            if (bottom > oldBottom)
                recyclerview.scrollBy(0, bottom - oldBottom)
        }

        // recyclerview.smoothScrollToPosition(recyclerview.adapter!!.itemCount - 1);

        backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        sendButton.setOnClickListener {
            // Checks if a chat is already created
            if (!chatExists) {
                currentConversationId = ConversationUtil.newConversation(user!!, participant!!)
                conversationUtil =
                    ConversationUtil(this, currentConversationId, user!!, participant!!)
                Log.d(TAG, "Conversation ($currentConversationId) created")
                loadConversation(currentConversationId)
            }

            val message: String = messageInput.text.toString()

            // Check if user has selected a image to upload
            if (file != null) {
                // Show a loading indicator or disable input while sending
                sendButton.isEnabled = false

                // Sends message
                conversationUtil.sendImage(
                    file!!,
                    if (message.isNotEmpty() && message.isNotBlank()) message else null
                ).run { message }

                file = null
            } else {
                // Check if user has entered a text
                if (message.isNotEmpty()) {
                    sendButton.isEnabled = false
                    conversationUtil.sendMessage(message)
                }
            }

            // finally clear the input field after sending
            messageInput.setText("")
            sendButton.isEnabled = true
        }

        participantName.text = participant!!.username

        participant?.photoUrl?.let {
            Glide.with(applicationContext)
                .load(Uri.parse(it))
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.drawable.ic_account_circle_white)
                .circleCrop()
                .into(profileImage)
        } ?: run {
            profileImage.setImageResource(R.drawable.ic_account_circle_white)
        }

        val onlineRef = users.child(participant?.uid!!).child("online")

        onlineRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val online = snapshot.value.toString().toBoolean()

                if (online) {
                    statusOnline.visibility = View.VISIBLE
                    statusOnline.text = getString(R.string.conversation__status_online)
                } else {
                    statusOnline.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error reading 'online' value: ${error.message}")
            }
        })

        chatExists = ConversationUtil.conversationExists(user!!, participant!!)

        if (chatExists) {
            currentConversationId =
                ConversationUtil.getConversationId(participant!!, user!!.conversations).toString()
            conversationUtil = ConversationUtil(this, currentConversationId, user!!, participant!!)

            loadConversation(currentConversationId)
        }
    }

    fun scrollToBottom() {
        recyclerview.post {
            recyclerview.smoothScrollToPosition(recyclerview.adapter!!.itemCount - 1)
        }
    }

    private fun loadConversation(cid: String) {
        val messages: Query = conversations.child(cid).child(Key.Conversation.MESSAGES)

        val options = FirebaseRecyclerOptions.Builder<Message>()
            .setQuery(messages, Message::class.java)
            .build()

        adapter = MessageAdapter(applicationContext, options)

        adapter?.setConversationId(cid)
        adapter?.setParticipant(participant)

        recyclerview.adapter = adapter
        adapter?.startListening()

        adapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                // Automatically scroll to the bottom when messages are first loaded
                if (positionStart == 0) {
                    recyclerview.smoothScrollToPosition(adapter!!.itemCount - 1)
                } else {
                    // Handle scrolling to the bottom when new messages are inserted
                    val lastVisiblePosition =
                        (recyclerview.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()

                    // If the last message is visible or the user is at the end of the list, scroll down
                    if (lastVisiblePosition == -1 || (positionStart >= (adapter!!.itemCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                        recyclerview.smoothScrollToPosition(positionStart)
                    }
                }
            }
        })
    }


    override fun onStart() {
        adapter?.startListening()
        super.onStart()
    }

    override fun onStop() {
        adapter?.stopListening()
        super.onStop()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        UserUtils.update("online", "true")
        adapter?.startListening()
        adapter?.notifyDataSetChanged()
        super.onResume()
    }
}
