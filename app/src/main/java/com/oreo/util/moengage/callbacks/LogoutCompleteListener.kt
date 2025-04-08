package com.oreo.util.moengage.callbacks

import android.util.Log
import com.moengage.core.listeners.OnLogoutCompleteListener
import com.moengage.core.model.LogoutData

class LogoutCompleteListener : OnLogoutCompleteListener {
    override fun logoutComplete(data: LogoutData) {
        Log.d("moengage_test logoutComplete()", "$data")
    }
}