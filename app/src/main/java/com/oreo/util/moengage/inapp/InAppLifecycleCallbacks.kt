package com.oreo.util.moengage.inapp

import android.util.Log
import com.moengage.inapp.listeners.InAppLifeCycleListener
import com.moengage.inapp.model.InAppData
import com.noisefit_commans.utils.LOGS

class InAppLifecycleCallbacks: InAppLifeCycleListener {

    override fun onDismiss(inAppData: InAppData) {
        LOGS.d("moengage_test onDismiss() Data: $inAppData")
    }

    override fun onShown(inAppData: InAppData) {
        LOGS.d("moengage_test onShown() Data:", "$inAppData")

    }
}