package com.devusercode.upchat

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.devusercode.upchat.utils.UpdateHelper
import com.devusercode.upchat.utils.UserUtils
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class StartActivity : AppCompatActivity(), UpdateHelper.OnUpdateCheckListener {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var intent: Intent? = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        FirebaseApp.initializeApp(this)
        UserUtils.update("online", true.toString())
        UpdateHelper.with(this).onUpdateCheck(this).check()
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        overridePendingTransition(R.anim.right_in, R.anim.left_out)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.left_in, R.anim.right_out)
    }

    private fun initialize() {
        val redirectClass: Class<*> =
            if (auth.currentUser != null) HomeActivity::class.java else LoginActivity::class.java
        intent!!.setClass(applicationContext, redirectClass)
        startActivity(intent)
        finish()
    }

    override fun onUpdateAvailable(urlApp: String) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("New App Version Available")
            .setMessage("Please update to the latest build,\nfor the latest features and bug fixes")
            .setPositiveButton("Update") { dialogInterface: DialogInterface, _: Int ->
                redirectStore(urlApp)
                dialogInterface.dismiss()
            }
            .setNegativeButton("Close") { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
                initialize()
            }
            .setOnCancelListener { dialogInterface: DialogInterface ->
                dialogInterface.dismiss()
                initialize()
            }
            .create()
        alertDialog.show()
    }

    override fun onUpdateRequired(urlApp: String) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("New App Version Required")
            .setMessage("An app update is required, to run this app!")
            .setPositiveButton("Update") { dialogInterface: DialogInterface, _: Int ->
                redirectStore(urlApp)
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .create()
        alertDialog.show()
    }

    override fun onNoUpdateAvailable() {
        initialize()
    }

    private fun redirectStore(urlApp: String) {
        if (urlApp.isEmpty()) {
            return
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlApp))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}