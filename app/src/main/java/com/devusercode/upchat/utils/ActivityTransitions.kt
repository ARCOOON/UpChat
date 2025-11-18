package com.devusercode.upchat.utils

import android.app.Activity
import android.os.Build
import androidx.annotation.AnimRes

private fun Activity.overrideTransition(
    transitionType: Int,
    @AnimRes enterAnim: Int,
    @AnimRes exitAnim: Int,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(transitionType, enterAnim, exitAnim)
    } else {
        @Suppress("DEPRECATION")
        overridePendingTransition(enterAnim, exitAnim)
    }
}

fun Activity.applyActivityOpenAnimation(
    @AnimRes enterAnim: Int,
    @AnimRes exitAnim: Int,
) {
    overrideTransition(Activity.OVERRIDE_TRANSITION_OPEN, enterAnim, exitAnim)
}

fun Activity.applyActivityCloseAnimation(
    @AnimRes enterAnim: Int,
    @AnimRes exitAnim: Int,
) {
    overrideTransition(Activity.OVERRIDE_TRANSITION_CLOSE, enterAnim, exitAnim)
}
