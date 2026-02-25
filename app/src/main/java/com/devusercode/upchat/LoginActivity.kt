package com.devusercode.upchat

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.devusercode.upchat.utils.StorageController
import com.devusercode.upchat.utils.applyActivityCloseAnimation
import com.devusercode.upchat.utils.applyActivityOpenAnimation
import com.devusercode.upchat.utils.setComposeContent
import com.devusercode.upchat.utils.Util
import com.devusercode.upchat.utils.applyActivityCloseAnimation
import com.devusercode.upchat.utils.applyActivityOpenAnimation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

@RequiresApi(Build.VERSION_CODES.O)
class LoginActivity : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName
    private val auth = FirebaseAuth.getInstance()

    private lateinit var contentView: View
    private lateinit var emailEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var saveLoginInfoCheckbox: CheckBox
    private lateinit var forgotPasswordText: TextView

    // private lateinit var authCreateUserListener: OnCompleteListener<AuthResult>
    private lateinit var authSignInListener: OnCompleteListener<AuthResult>
    private lateinit var authResetPasswordListener: OnCompleteListener<Void>
    // private lateinit var authUpdateEmailListener: OnCompleteListener<Void>
    // private lateinit var authUpdatePasswordListener: OnCompleteListener<Void>
    // private lateinit var authEmailVerificationSentListener: OnCompleteListener<Void>
    // private lateinit var authDeleteUserListener: OnCompleteListener<Void>
    // private lateinit var authUpdateProfileListener: OnCompleteListener<Void>
    // private lateinit var authPhoneAuthListener: OnCompleteListener<AuthResult>
    // private lateinit var authGoogleSignInListener: OnCompleteListener<AuthResult>

    private val intent = Intent()
    private lateinit var storageController: StorageController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentView = setComposeContent(R.layout.activity_login)

        storageController = StorageController.getInstance(this)!!

        initialize()
        initializeLogic()
        FirebaseApp.initializeApp(this)
    }

    override fun startActivity(intent: Intent) {
        super.startActivity(intent)
        applyActivityOpenAnimation(R.anim.right_in, R.anim.left_out)
    }

    override fun finish() {
        super.finish()
        applyActivityCloseAnimation(R.anim.left_in, R.anim.right_out)
    }

    private fun initialize() {
        emailEdit = contentView.findViewById(R.id.email_edit)
        passwordEdit = contentView.findViewById(R.id.password_edit)
        saveLoginInfoCheckbox = contentView.findViewById(R.id.save_login_info_checkbox)
        forgotPasswordText = contentView.findViewById(R.id.forgot_password_text)

        val loginButton: Button = contentView.findViewById(R.id.login_button)
        val registerButton: Button = contentView.findViewById(R.id.register_button)

        saveLoginInfoCheckbox.isChecked = storageController.getBool("save_login_info")

        forgotPasswordText.setOnClickListener {
            if (emailEdit.text.toString().isEmpty()) {
                emailEdit.error = "You need to fill this field!"
                return@setOnClickListener
            }

            auth
                .sendPasswordResetEmail(emailEdit.text.toString())
                .addOnCompleteListener(authResetPasswordListener)
        }

        loginButton.setOnClickListener {
            if (emailEdit.text.toString().isEmpty() || passwordEdit.text.toString().isEmpty()) {
                return@setOnClickListener
            }

            auth
                .signInWithEmailAndPassword(emailEdit.text.toString(), passwordEdit.text.toString())
                .addOnCompleteListener(authSignInListener)
        }

        registerButton.setOnClickListener {
            intent.setClass(applicationContext, RegisterActivity::class.java)
            startActivity(intent)
        }

        // *** LISTENERS ***

        /*
        authEmailVerificationSentListener = OnCompleteListener { task ->
            val success = task.isSuccessful
            val errorMessage = task.exception?.message ?: ""
        }
        authGoogleSignInListener = OnCompleteListener { task ->
            val success = task.isSuccessful
            val errorMessage = task.exception?.message ?: ""
        }
         */

        authSignInListener =
            OnCompleteListener { task ->
                if (task.isSuccessful) {
                    val saveLoginInfo = saveLoginInfoCheckbox.isChecked
                    storageController["save_login_info"] = saveLoginInfo

                    if (saveLoginInfo) {
                        storageController["email"] = emailEdit.text.toString()
                        storageController["password"] = passwordEdit.text.toString()
                    }

                    intent.setClass(applicationContext, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    if (task.exception is FirebaseAuthInvalidUserException) {
                        Util.showMessage(applicationContext, "User not found!")
                    }

                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Util.showMessage(applicationContext, "Invalid credentials!")
                    }

                    Log.e(TAG, task.exception?.javaClass?.name ?: "")
                    Log.e(TAG, task.exception?.message ?: "")
                }
            }

        authResetPasswordListener =
            OnCompleteListener { task ->
                val success = task.isSuccessful

                if (success) {
                    Util.showMessage(applicationContext, "Password reset email sent")
                } else {
                    Log.e(TAG, task.exception?.message ?: "")
                    Util.showMessage(applicationContext, task.exception?.message ?: "")
                }
            }
    }

    private fun initializeLogic() {
        val saveLoginInfo = storageController.getBool("save_login_info")

        if (saveLoginInfo) {
            val email = storageController.getString("email")
            val password = storageController.getString("password")

            if (email != null && password != null) {
                Log.d(TAG, "login by saved info")
                auth
                    .signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(authSignInListener)
            } else {
                Log.d(TAG, "email or password is not set!")
            }
        }
    }
}
