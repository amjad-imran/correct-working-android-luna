package com.oreo.util.moengage.callbacks

import android.content.Context
import android.util.Log
import com.moengage.core.listeners.AppBackgroundListener
import com.moengage.core.model.AppBackgroundData

class ApplicationBackgroundListener : AppBackgroundListener {
    override fun onAppBackground(context: Context, data: AppBackgroundData) {
        Log.d("moengage_test onAppBackground()", "$data")
    }
}