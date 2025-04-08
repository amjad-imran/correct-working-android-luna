package com.oreo.util.moengage.inapp

import android.util.Log
import com.moengage.inapp.listeners.SelfHandledAvailableListener
import com.moengage.inapp.model.SelfHandledCampaignData

class SelfHandledCallback: SelfHandledAvailableListener {

    override fun onSelfHandledAvailable(data: SelfHandledCampaignData?) {
        Log.d( "moengage_test onSelfHandledAvailable()"," $data" )
    }
}