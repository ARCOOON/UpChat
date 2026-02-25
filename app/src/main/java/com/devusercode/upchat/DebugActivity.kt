package com.devusercode.upchat

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView

class DebugActivity : Activity() {
    private val exceptionTypes =
        arrayOf(
            "StringIndexOutOfBoundsException",
            "IndexOutOfBoundsException",
            "ArithmeticException",
            "NumberFormatException",
            "ActivityNotFoundException",
        )

    private val exceptionMessages =
        arrayOf(
            "Invalid string operation\n",
            "Invalid list operation\n",
            "Invalid arithmetical operation\n",
            "Invalid toNumber block operation\n",
            "Invalid intent operation",
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        var madeErrorMessage = ""

        val errorMessage: String = intent?.getStringExtra("error") ?: ""
        val split = errorMessage.split("\n")

        try {
            for (j in exceptionTypes.indices) {
                if (split[0].contains(exceptionTypes[j])) {
                    madeErrorMessage = exceptionMessages[j]

                    val addIndex = split[0].indexOf(exceptionTypes[j]) + exceptionTypes[j].length

                    madeErrorMessage += split[0].substring(addIndex)
                    madeErrorMessage += "\n\nDetailed error message:\n$errorMessage"
                    break
                }
            }

            if (madeErrorMessage.isEmpty()) {
                madeErrorMessage = errorMessage
            }
        } catch (e: Exception) {
            madeErrorMessage =
                "$madeErrorMessage\n\nError while getting error: ${Log.getStackTraceString(e)}"
        }

        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_crash, null)
        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialog_message)

        dialogMessage.text = madeErrorMessage
        dialogMessage.setTextIsSelectable(true)

        val dialog =
            AlertDialog
                .Builder(this)
                .setTitle("An error occurred")
                .setView(dialogView)
                .setPositiveButton("End Application") { _, _ -> finish() }
                .create()

        dialog.show()
    }
}
