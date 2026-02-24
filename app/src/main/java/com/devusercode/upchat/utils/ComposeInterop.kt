package com.devusercode.upchat.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.annotation.LayoutRes
import androidx.compose.runtime.remember
import androidx.activity.compose.setContent
import androidx.compose.ui.viewinterop.AndroidView

fun ComponentActivity.setComposeContent(@LayoutRes layoutId: Int): View {
    val inflater = LayoutInflater.from(this)
    val contentView = inflater.inflate(layoutId, null, false)
    contentView.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    setContent {
        val rememberedRoot = remember { contentView }
        AndroidView(factory = { rememberedRoot })
    }

    return contentView
}
