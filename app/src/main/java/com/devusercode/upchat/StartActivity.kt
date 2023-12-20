package com.devusercode.upchat

import android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.devusercode.upchat.utils.UpdateHelper
import com.devusercode.upchat.utils.UpdateInstaller
import com.devusercode.upchat.utils.UserUtils
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.koushikdutta.ion.Ion
import java.io.File


@RequiresApi(Build.VERSION_CODES.O)
class StartActivity : AppCompatActivity(), UpdateHelper.OnUpdateCheckListener {
    private val TAG = "StartActivity"
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

    override fun onUpdateAvailable(filename: String, url: String) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("New App Version Available")
            .setMessage("Please update to the latest build,\nfor the latest features and bug fixes.")
            .setPositiveButton("Update") { dialogInterface: DialogInterface, _: Int ->
                downloadUpdate(url, filename)
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

    override fun onUpdateRequired(filename: String, url: String) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("New App Version Required")
            .setMessage("An app update is required, to run this app!")
            .setPositiveButton("Update") { dialogInterface: DialogInterface, _: Int ->
                downloadUpdate(url, filename)
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .create()
        alertDialog.show()
    }

    override fun onNoUpdateAvailable() {
        initialize()
    }

    private fun downloadUpdate(url: String, filename: String) {
        requestStoragePermissions()

        val filePath = "${Environment.getExternalStorageDirectory().path}/${Environment.DIRECTORY_DOWNLOADS}/$filename"

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("New App Version")
        progressDialog.setMessage("Downloading update...")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.setCancelable(false)
        progressDialog.isIndeterminate = false
        progressDialog.show()

        Ion.with(this)
            .load(url)
            // .progressBar(progressBar) for the progress bar
            // .progress { downloaded, total -> /* Do something with the process */ }
            .progressDialog(progressDialog)
            .write(File(filePath))
            .setCallback { error, file ->
                progressDialog.dismiss()

                if (error != null) {
                    Log.e(TAG, error.message.toString())
                    return@setCallback
                }

                val updateInstaller = UpdateInstaller(this)
                updateInstaller.install(file.path)
            }
    }

    private fun redirectRepo(urlApp: String) {
        if (urlApp.isEmpty()) {
            return
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlApp))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun requestStoragePermissions() {
        val permissions = arrayOf(
            READ_EXTERNAL_STORAGE,
            WRITE_EXTERNAL_STORAGE,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissions + MANAGE_EXTERNAL_STORAGE
        }

        ActivityCompat.requestPermissions(this, permissions, PackageManager.PERMISSION_GRANTED)
    }
}
