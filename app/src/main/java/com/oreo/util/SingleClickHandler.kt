package com.oreo.util

import android.os.Handler
import android.os.Looper
import android.view.View

fun View.setSafeOnClickListener(delayMillis: Long = 400, onClick: () -> Unit) {
    var isClicked = false
    setOnClickListener {
        if (!isClicked) {
            isClicked = true
            onClick()
            isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                isClicked = false
                isEnabled = true
            }, delayMillis)
        }
    }
}