package com.noisefit.receiver.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.noisefit.session.SessionManager
import com.noisefit.util.notif.NotificationEventsClass
import com.noisefit.util.notif.NotificationUtil
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.PhoneRinger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class StopFindMyPhoneBroadcast : BroadcastReceiver() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onReceive(context: Context?, intent: Intent?) {
        LOGS.d("${intent?.action}")
        if (intent?.action == ACTION_STOP) {
            PhoneRinger.enableRing(false)
            NotificationUtil.removeNotification(
                context!!,
                NotificationEventsClass.FIND_PHONE_NOTIFICATION_ID
            )
            sessionManager.sendUpdateQueryAction(UpdateDeviceAction.CloseFindPhoneFromWatch(true))
        }

    }

    companion object {
        const val ACTION_STOP = "ACTION_STOP"
    }

}