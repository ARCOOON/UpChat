package com.devusercode.upchat

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.devusercode.upchat.utils.DatabaseUtil
import com.devusercode.upchat.utils.DeviceId
import com.devusercode.upchat.utils.Util
import com.google.android.gms.tasks.Task
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage

@RequiresApi(Build.VERSION_CODES.O)
class RegisterActivity : AppCompatActivity() {
    private val TAG = "RegisterActivity"

    private val auth = FirebaseAuth.getInstance()
    private val users = FirebaseDatabase.getInstance().getReference("users")
    private val profileImages = FirebaseStorage.getInstance().getReference("profile_images")

    private lateinit var linear1: LinearLayout
    private lateinit var usernameEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var registerButton: Button
    private lateinit var uploadProfileImageButton: ImageButton
    private lateinit var profileImage: AppCompatImageView

    private lateinit var toolbarTitle: TextView

    private val intent = Intent()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>

    private var downloadUrl = ""
    private var imagePath = Uri.EMPTY
    private lateinit var progressDialog: Dialog
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        initialize()

        FirebaseApp.initializeApp(this)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                token = task.result
            }
        }.addOnFailureListener { error ->
            token = null
            Log.e(TAG, error.message!!)
        }

        /*
         * Activity Logic:
         * On Register Click ->
         * If -> All fields are initialized
         * Then -> if imagePath is not null upload the profile image
         * Then -> update the user profile
         * Then -> create and save the user to firebase
         * Else -> return
         */
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
        val coordinator = findViewById<CoordinatorLayout>(R.id._coordinator)
        val appBar = findViewById<AppBarLayout>(R.id.app_bar)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title)
        toolbarTitle.text = getString(R.string.register__toolbar_title)

        progressDialog = Dialog(this)
        progressDialog.setContentView(R.layout.progress_dialog)
        progressDialog.setCancelable(false)

        // Get the progress bar from the dialog layout
        val progressBar = progressDialog.findViewById<ProgressBar>(R.id.progress)
        progressBar.isIndeterminate = true

        linear1 = findViewById(R.id.linear1)
        usernameEdit = findViewById(R.id.username_edit)
        emailEdit = findViewById(R.id.email_edit)
        passwordEdit = findViewById(R.id.password_edit)
        registerButton = findViewById(R.id.register_button)
        uploadProfileImageButton = findViewById(R.id.upload_profile_image_button)
        profileImage = findViewById(R.id.profile_image)

        uploadProfileImageButton.setOnClickListener { imagePickerLauncher.launch("image/*") }

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
            if (result != null) {
                imagePath = result
                profileImage.setImageURI(result)
            }
        }

        registerButton.setOnClickListener {
            if (usernameEdit.text.toString().isEmpty() || (emailEdit.text.toString()
                    .isEmpty() || passwordEdit.text.toString().isEmpty())) {
                return@setOnClickListener
            }

            if (passwordEdit.text.toString().length < 6) {
                passwordEdit.error = "Password must be longer than 6"
                return@setOnClickListener
            }

            progressDialog.show()

            auth.createUserWithEmailAndPassword(emailEdit.text.toString(), passwordEdit.text.toString())
                .addOnCompleteListener {
                    uploadProfileImage()
                }.addOnFailureListener { error ->
                    progressDialog.dismiss()
                    Log.e(TAG, error.message!!)
                    Util.showMessage(applicationContext, error.message!!)
                }
        }
    }

    private fun uploadProfileImage() {
        if (imagePath == null || imagePath == Uri.EMPTY) {
            updateUserProfile()
        } else {
            DatabaseUtil.uploadProfileImage(imagePath, onSuccess = { url ->
                downloadUrl = url
                updateUserProfile()
            }, onFailure = { error ->
                progressDialog.dismiss()
                Log.e(TAG, error.message!!)
            })
        }
    }

    private fun updateUserProfile() {
        val updates = UserProfileChangeRequest.Builder()
            .setDisplayName(usernameEdit.text.toString()).build()

        auth.currentUser?.updateProfile(updates)
            ?.addOnCompleteListener { task -> run { createUserProfile(task) } }
            ?.addOnFailureListener { error ->
                progressDialog.dismiss()
                Log.e(TAG, error.message!!)
                Util.showMessage(applicationContext, error.message!!)
            }
    }

    private fun createUserProfile(task: Task<Void>) {
        val errorMessage = task.exception?.message ?: ""

        if (task.isSuccessful) {
            val userinfo = HashMap<String, Any>()
            val user = auth.currentUser

            userinfo[Key.User.DEVICE_ID] = DeviceId.getId()
            userinfo[Key.User.DEVICE_TOKEN] = token ?: ""
            userinfo[Key.User.USERNAME] = user?.displayName ?: ""
            userinfo[Key.User.EMAIL] = user?.email ?: ""
            userinfo[Key.User.UID] = user?.uid ?: ""
            userinfo[Key.User.PHOTO_URL] = downloadUrl.ifEmpty { "" }
            userinfo[Key.User.JOINED] = System.currentTimeMillis().toString()

            users.child(user?.uid ?: "").updateChildren(userinfo)

            progressDialog.dismiss()

            intent.setClass(applicationContext, HomeActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            progressDialog.dismiss()
            Log.e(TAG, errorMessage)
            Util.showMessage(applicationContext, errorMessage)
        }
    }
}
