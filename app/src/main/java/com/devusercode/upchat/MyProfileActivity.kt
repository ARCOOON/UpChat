package com.devusercode.upchat

import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.devusercode.upchat.models.User
import com.devusercode.upchat.utils.DatabaseUtil
import com.devusercode.upchat.utils.QRCode
import com.devusercode.upchat.utils.StorageController
import com.devusercode.upchat.utils.UserUtils
import com.devusercode.upchat.utils.Util
import com.devusercode.upchat.utils.applyActivityCloseAnimation
import com.devusercode.upchat.utils.applyActivityOpenAnimation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage

@RequiresApi(Build.VERSION_CODES.O)
class MyProfileActivity : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val profileImages = FirebaseStorage.getInstance().getReference("profile_images")

    private lateinit var linear1: LinearLayout
    private lateinit var username: TextView
    private lateinit var email: TextView
    private lateinit var uid: TextView
    private lateinit var joined: TextView
    private lateinit var linear3: LinearLayout
    private lateinit var appVersionText: TextView

    private lateinit var authDeleteUserListener: OnCompleteListener<Void>

    private val intent = Intent()
    private val users: DatabaseReference = db.getReference("users")

    private var currentUser: User? = null
    private lateinit var storageController: StorageController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        storageController = StorageController.getInstance(this)!!

        initialize(savedInstanceState)
        FirebaseApp.initializeApp(this)
        initializeLogic()
    }

    override fun startActivity(intent: Intent) {
        super.startActivity(intent)
        applyActivityOpenAnimation(R.anim.right_in, R.anim.left_out)
    }

    override fun finish() {
        super.finish()
        applyActivityCloseAnimation(R.anim.left_in, R.anim.right_out)
    }

    private fun initialize(savedInstanceState: Bundle?) {
        // val coordinator: CoordinatorLayout = findViewById(R.id._coordinator)
        // val appBar: AppBarLayout = findViewById(R.id.app_bar)
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        toolbar.findViewById<TextView>(R.id.toolbar_title).text = getString(R.string.my_profile__toolbar_title)

        linear1 = findViewById(R.id.linear1)
        linear3 = findViewById(R.id.linear3)

        username = findViewById(R.id.textview1)
        email = findViewById(R.id.textview2)
        uid = findViewById(R.id.textview3)
        joined = findViewById(R.id.textview4)
        appVersionText = findViewById(R.id.app_version_text)

        try {
            val appVersion = packageManager.getPackageInfo(packageName, 0).versionName
            appVersionText.text = getString(R.string.my_profile__app_version, appVersion)
        } catch (e: PackageManager.NameNotFoundException) {
            appVersionText.text = getString(R.string.my_profile__unknown_version)
            throw RuntimeException(e)
        }

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        username.setOnLongClickListener {
            val clip = ClipData.newPlainText("Username", username.text)
            clipboard.setPrimaryClip(clip)
            false
        }

        email.setOnLongClickListener {
            val clip = ClipData.newPlainText("Email", email.text)
            clipboard.setPrimaryClip(clip)
            false
        }

        uid.setOnLongClickListener {
            val clip = ClipData.newPlainText("Uid", uid.text)
            clipboard.setPrimaryClip(clip)
            false
        }

        val chatsButton: Button = findViewById(R.id.chats_button)
        val logoutButton: Button = findViewById(R.id.logout_button)
        val deleteButton: Button = findViewById(R.id.delete_button)
        val shareQrcodeButton: Button = findViewById(R.id.share_qrcode_button)

        val viewUsersButton: FloatingActionButton = findViewById(R.id.view_users_button)

        logoutButton.setOnClickListener {
            auth.signOut()

            storageController.remove("save_login_info")
            storageController.remove("email")
            storageController.remove("password")

            intent.setClass(applicationContext, StartActivity::class.java)
            startActivity(intent)
            finish()
        }

        chatsButton.setOnClickListener {
            intent.setClass(applicationContext, HomeActivity::class.java)
            startActivity(intent)
        }

        deleteButton.setOnClickListener {
            if (auth.currentUser != null) {
                auth.currentUser
                    ?.delete()
                    ?.addOnCompleteListener(authDeleteUserListener)
                    ?.addOnFailureListener { error ->
                        Log.e(TAG, error.message!!)
                    }
            }
        }

        viewUsersButton.setOnClickListener {
            intent.setClass(applicationContext, ListUsersActivity::class.java)
            startActivity(intent)
        }

        shareQrcodeButton.setOnClickListener {
            val qrCodeBitmap: Bitmap? = QRCode.create(auth.currentUser?.uid!!, 400, 400)

            // Create dialog with custom layout
            val builder = AlertDialog.Builder(this)
            val dialogView: View = LayoutInflater.from(this).inflate(R.layout.dialog_qr_code, null)

            val qrCodeImageView: ImageView = dialogView.findViewById(R.id.qr_code_image)

            qrCodeImageView.setImageBitmap(qrCodeBitmap)

            builder.setView(dialogView)
            builder.setCancelable(true)

            // Create and show the dialog
            val qrCodeDialog: Dialog = builder.create()
            qrCodeDialog.show()
        }

        val usersChildListener: ChildEventListener =
            object : ChildEventListener {
                override fun onChildAdded(
                    dataSnapshot: DataSnapshot,
                    childKey: String?,
                ) {
                    currentUser = dataSnapshot.getValue(User::class.java)

                    if (auth.currentUser?.uid == currentUser?.uid) {
                        username.text = currentUser?.username
                        email.text = currentUser?.email
                        uid.text = currentUser?.uid
                        joined.text = currentUser?.formattedJoined

                        getFCMToken()
                    }
                }

                override fun onChildChanged(
                    dataSnapshot: DataSnapshot,
                    childKey: String?,
                ) {}

                override fun onChildMoved(
                    dataSnapshot: DataSnapshot,
                    childKey: String?,
                ) {}

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

                override fun onCancelled(error: DatabaseError) {}
            }

        users.addChildEventListener(usersChildListener)

        authDeleteUserListener =
            OnCompleteListener<Void> { task ->
                val errorMessage = task.exception?.message ?: ""

                if (task.isSuccessful) {
                    DatabaseUtil.deleteUser { error ->
                        Log.e(
                            TAG,
                            "Failed to delete user: " + error.message,
                        )
                    }
                    DatabaseUtil.deleteProfileImage { error ->
                        Log.e(
                            TAG,
                            "Failed to delete profile image: " + error.message,
                        )
                    }

                    intent.setClass(applicationContext, StartActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.e(TAG, errorMessage)
                    Util.showMessage(applicationContext, errorMessage)
                }
            }
    }

    private fun initializeLogic() {
        if (auth.currentUser == null || auth.currentUser?.uid.isNullOrEmpty()) {
            intent.setClass(applicationContext, StartActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (currentUser?.deviceToken == null) {
                    UserUtils.update(Key.User.DEVICE_TOKEN, task.result)
                }
            }
        }
    }
}
