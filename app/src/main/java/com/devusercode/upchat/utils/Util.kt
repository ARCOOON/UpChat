package com.devusercode.upchat.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.core.util.size
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Random

object Util {
    private const val TAG = "Util"

    @JvmStatic
    fun formatTime(
        joined: String,
        format: String,
    ): String {
        val date = Date(joined.toLong())
        return SimpleDateFormat(format, Locale.getDefault()).format(date)
    }

    fun formatTime(time: String): String {
        val timeInMillis = time.toLong() * 1000
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.timeInMillis = timeInMillis
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time)
    }

    fun parseTime(joined: String): String = formatTime(joined, "dd/MM/yyyy HH:mm")

    fun setCornerRadius(
        view: View,
        cornerRadius: Float,
    ) {
        view.outlineProvider =
            object : ViewOutlineProvider() {
                override fun getOutline(
                    view: View,
                    outline: Outline,
                ) {
                    outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                }
            }
        view.clipToOutline = true
    }

    fun createOverlay(context: Context): View {
        // Create a semi-transparent view with black background color
        val overlay = View(context)
        overlay.setBackgroundColor("#80000000".toColorInt()) // 50% opacity

        // Set the layout parameters to match the parent's size
        overlay.layoutParams =
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )

        return overlay
    }

    fun restartApplication(requestContext: Context) {
        val context = requestContext.applicationContext
        val contextPackageManager = context.packageManager
        val intent = contextPackageManager.getLaunchIntentForPackage(context.packageName)
        val mainIntent = Intent.makeRestartActivityTask(intent!!.component)

        context.startActivity(mainIntent)

        Runtime.getRuntime().exit(0)
    }

    fun alert(
        context: Context,
        title: String,
        message: String,
        onClick: (() -> Unit)?,
    ) {
        AlertDialog.Builder(context).apply {
            setTitle(title)
            setMessage(message)
            setIcon(android.R.drawable.ic_dialog_alert)
            setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                if (onClick != null) {
                    onClick()
                }
            }
            show()
        }
    }

    /*
     * >>> Sketchware code <<<
     * */

    fun isValidMimeType(mimeType: String): Boolean {
        // Define a list of valid MIME types or file extensions
        val validMimeTypes =
            arrayOf(
                "image/jpeg",
                "image/png",
                "application/pdf",
                "text/plain",
                "application/zip",
                "application/x-zip-compressed",
                "application/x-rar-compressed",
                "application/x-tar",
                "application/x-gzip",
                // Add more valid MIME types or file extensions here
            )
        return validMimeTypes.contains(mimeType)
    }

    fun getFileExtension(
        context: Context,
        uri: Uri,
    ): String? {
        val contentResolver = context.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    fun sortListMap(
        listMap: ArrayList<HashMap<String, Any>>,
        key: String,
        isNumber: Boolean,
        ascending: Boolean,
    ) {
        listMap.sortWith { compareMap1, compareMap2 ->
            val value1 = compareMap1[key]
            val value2 = compareMap2[key]

            if (isNumber) {
                val count1 = value1.toString().toInt()
                val count2 = value2.toString().toInt()
                if (ascending) count1.compareTo(count2) else count2.compareTo(count1)
            } else {
                val str1 = value1.toString()
                val str2 = value2.toString()
                if (ascending) str1.compareTo(str2) else str2.compareTo(str1)
            }
        }
    }

    fun cropImage(
        activity: Activity,
        path: String,
    ) {
        try {
            val cropIntent = Intent("com.android.camera.action.CROP")

            val file = File(path)
            val contentUri = Uri.fromFile(file)

            cropIntent.setDataAndType(contentUri, "image/*")
            cropIntent.putExtra("crop", "true")
            cropIntent.putExtra("aspectX", 1)
            cropIntent.putExtra("aspectY", 1)
            cropIntent.putExtra("outputX", 280)
            cropIntent.putExtra("outputY", 280)
            cropIntent.putExtra("return-data", false)

            activity.startActivityForResult(cropIntent, 1)
        } catch (e: ActivityNotFoundException) {
            Toast
                .makeText(
                    activity,
                    "Your device doesn't support the crop action!",
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    fun isConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        return networkCapabilities != null &&
            (
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    networkCapabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_CELLULAR,
                    ) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            )
    }

    fun copyFromInputStream(inputStream: InputStream): String {
        val outputStream = ByteArrayOutputStream()
        try {
            val buffer = ByteArray(1024)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            return outputStream.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            outputStream.close()
        }
        return ""
    }

    fun toggleKeyboard(
        context: Context,
        show: Boolean,
    ) {
        val activity = context as? Activity ?: return
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val targetView = activity.currentFocus ?: activity.window?.decorView

        if (show) {
            targetView?.let { view ->
                inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        } else {
            inputMethodManager.hideSoftInputFromWindow(targetView?.windowToken, 0)
        }
    }

    fun showMessage(
        context: Context,
        message: String,
    ) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun getLocation(view: View): Point {
        val location = IntArray(2)
        view.getLocationInWindow(location)

        return Point(location[0], location[1])
    }

    fun getRandom(
        min: Int,
        max: Int,
    ): Int {
        val random = SecureRandom()
        require(min <= max) { "Invalid range: min should be less than or equal to max" }
        return random.nextInt(max - min + 1) + min
    }

    fun getCheckedItemPositionsToArray(listView: ListView): List<Double> {
        val result = ArrayList<Double>()
        val checkedPositions = listView.checkedItemPositions

        for (i in 0 until checkedPositions.size) {
            val position = checkedPositions.keyAt(i)
            if (checkedPositions.valueAt(i)) {
                result.add(position.toDouble())
            }
        }

        return result
    }

    fun dpToPixels(
        context: Context,
        dp: Float,
    ): Float =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics,
        )

    fun getDisplayWidthPixels(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.widthPixels
    }

    fun getDisplayHeightPixels(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.heightPixels
    }

    fun getAllKeysFromMap(
        map: Map<String, Any>?,
        output: ArrayList<String>?,
    ) {
        output?.clear()
        map?.keys?.let { output?.addAll(it) }
    }
}
