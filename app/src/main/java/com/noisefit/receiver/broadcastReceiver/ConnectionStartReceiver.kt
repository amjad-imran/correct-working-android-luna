package com.noisefit.receiver.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.enums.ServiceState
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConnectionStartReceiver : BroadcastReceiver() {
    @Inject
    lateinit var localDataStore: DataStoredInterface
    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == Intent.ACTION_BOOT_COMPLETED && localDataStore.getServiceState() == ServiceState.STARTED) {
            ApplicationUtils.setRescueWorkManager(context)
        }
    }
}