package com.devusercode.upchat.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.widget.TextView;

public class DebugActivity extends Activity {
    private final String[] exceptionTypes = {
            "StringIndexOutOfBoundsException",
            "IndexOutOfBoundsException",
            "ArithmeticException",
            "NumberFormatException",
            "ActivityNotFoundException"
    };

    private final String[] exceptionMessages = {
            "Invalid string operation\n",
            "Invalid list operation\n",
            "Invalid arithmetical operation\n",
            "Invalid toNumber block operation\n",
            "Invalid intent operation"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String errorMessage;
        String madeErrorMessage = "";

        if (intent != null) {
            errorMessage = intent.getStringExtra("error");

            String[] split = errorMessage.split("\n");
            //errorMessage = split[0];
            try {
                for (int j = 0; j < exceptionTypes.length; j++) {
                    if (split[0].contains(exceptionTypes[j])) {
                        madeErrorMessage = exceptionMessages[j];

                        int addIndex = split[0].indexOf(exceptionTypes[j]) + exceptionTypes[j].length();

                        madeErrorMessage += split[0].substring(addIndex);
                        madeErrorMessage += "\n\nDetailed error message:\n" + errorMessage;
                        break;
                    }
                }

                if (madeErrorMessage.isEmpty()) {
                    madeErrorMessage = errorMessage;
                }
            } catch (Exception e) {
                madeErrorMessage = madeErrorMessage + "\n\nError while getting error: " + Log.getStackTraceString(e);
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("An error occurred")
                .setMessage(madeErrorMessage)
                .setPositiveButton("End Application", (dialog1, which) -> finish())
                .create();
        dialog.show();
        ((TextView) dialog.findViewById(android.R.id.message)).setTextIsSelectable(true);
    }
}
