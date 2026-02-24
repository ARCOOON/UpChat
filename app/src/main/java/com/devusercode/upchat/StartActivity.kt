package com.devusercode.upchat

import android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.AlertDialog
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
import com.devusercode.upchat.utils.setComposeContent
import com.devusercode.upchat.utils.applyActivityCloseAnimation
import com.devusercode.upchat.utils.applyActivityOpenAnimation
import com.devusercode.upchat.utils.UpdateHelper
import com.devusercode.upchat.utils.UpdateInstaller
import com.devusercode.upchat.utils.UserUtils
import com.devusercode.upchat.utils.Util
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.koushikdutta.ion.Ion
import java.io.File


@RequiresApi(Build.VERSION_CODES.O)
class StartActivity : AppCompatActivity(), UpdateHelper.OnUpdateCheckListener {
    private val TAG = "StartActivity"
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var intent: Intent? = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setComposeContent(R.layout.activity_start)

        FirebaseApp.initializeApp(this)
        UserUtils.update("online", true.toString())
        UpdateHelper.with(this).onUpdateCheck(this).check()
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        applyActivityOpenAnimation(R.anim.right_in, R.anim.left_out)
    }

    override fun finish() {
        super.finish()
        applyActivityCloseAnimation(R.anim.left_in, R.anim.right_out)
    }

    private fun initialize() {
        val redirectClass: Class<*> =
            if (auth.currentUser != null) {
                HomeActivity::class.java
            } else {
                LoginActivity::class.java
            }
        intent!!.setClass(applicationContext, redirectClass)
        startActivity(intent)
        finish()
    }

    override fun onUpdateAvailable(filename: String, url: String) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("New App Version Available")
            .setMessage("Please update to the latest build,\nfor the latest features and bug fixes.")
            .setPositiveButton("Download Update") { dialogInterface: DialogInterface, _: Int ->
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
            .setPositiveButton("Download Update") { dialogInterface: DialogInterface, _: Int ->
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
        if (!checkStoragePermissions()) {
            requestStoragePermissions()
        }

        val filePath = "${Environment.getExternalStorageDirectory().path}/${Environment.DIRECTORY_DOWNLOADS}/$filename"
        val outputFile = File(filePath)

        val progressUi = DownloadProgressDialog(this)
        progressUi.show()

        val ion = Ion.with(this).load(url)
        ion.progress { downloaded, total ->
            progressUi.update(downloaded, total)
        }
        ion.setLogging(TAG, Log.DEBUG)
        ion.write(outputFile)
            .fail { error ->
                progressUi.dismiss()
                val errorMessage = error.localizedMessage ?: error.message
                Log.e(TAG, errorMessage.toString())
                Util.alert(this, "Error downloading update!", errorMessage.toString(), null)
                throw error
            }
            .success { file ->
                progressUi.dismiss()
                val updateInstaller = UpdateInstaller(this)
                updateInstaller.install(file.path)
            }
    }

    private class DownloadProgressDialog(activity: StartActivity) {
        private val dialogView = activity.layoutInflater.inflate(R.layout.dialog_download_progress, null)
        private val progressIndicator: com.google.android.material.progressindicator.LinearProgressIndicator =
            dialogView.findViewById(R.id.download_progress_indicator)
        private val progressMessage: android.widget.TextView =
            dialogView.findViewById(R.id.download_progress_message)

        private val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.start__update_dialog_title)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        fun show() {
            dialog.show()
        }

        fun dismiss() {
            dialog.dismiss()
        }

        fun update(downloaded: Long, total: Long) {
            val messageRes = R.string.start__update_downloading
            if (total <= 0) {
                progressIndicator.isIndeterminate = true
                progressMessage.setText(messageRes)
                return
            }

            val progress = ((downloaded.toDouble() / total.toDouble()) * 100).toInt().coerceIn(0, 100)
            progressIndicator.isIndeterminate = false
            progressIndicator.setProgressCompat(progress, true)
            progressMessage.text = dialog.context.getString(
                R.string.start__update_download_progress,
                progress
            )
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

    private fun checkStoragePermissions(): Boolean {
        val rw = ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val we = ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        val me = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30) and higher
            ActivityCompat.checkSelfPermission(this, MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 10 (API 29) and lower
            true
        }

        return rw && we && me
    }
}
