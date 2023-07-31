package com.noisefit.receiver.broadcastReceiver

//import com.noisefit_commans.utils.FileLogsUtils
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_NEW_OUTGOING_CALL
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.TelephonyManager
import com.noisefit.session.SessionManager
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.enums.ApplicationType
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.AppNotification
import com.noisefit_commans.models.ColorfitData
import com.noisefit_commans.models.IncomingCall
import com.noisefit_commans.models.SMS
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallAndMessageReceiver : BroadcastReceiver() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var localDataStore: DataStoredInterface

    private val TAG = "CallAndMessageReceiver"

    override fun onReceive(context: Context?, intent: Intent) {
        try {
            when (intent.action) {
                Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> {
                    if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
                        for (smsMessage in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {

                            GlobalScope.launch(Dispatchers.IO) {
                                val messageBody = smsMessage.messageBody
                                val sender = smsMessage.originatingAddress
                                val senderName = getSenderName(sender, context)
                                sendNotification(
                                    SMS(content = messageBody, number = sender, name = senderName),
                                    isMissCall = false
                                )
                            }

                        }
                    }
                }
                ACTION_NEW_OUTGOING_CALL -> {
                    return
                }
                else -> {
                    val incomingNumber =
                        intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                    val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

                    when (state) {
                        TelephonyManager.EXTRA_STATE_RINGING -> {

                            GlobalScope.launch(Dispatchers.IO) {
                                if (SessionManager.incomingNumber == null) {
                                    SessionManager.incomingNumber = incomingNumber
                                    SessionManager.ringing = true
                                    SessionManager.senderName = getSenderName(
                                        incomingNumber,
                                        NoisefitApplication.context
                                    )
                                    LOGS.d("$TAG Incoming Call $incomingNumber ${SessionManager.senderName}")
                                    if (incomingNumber != null)
                                        sendNotification(
                                            IncomingCall(
                                                number = incomingNumber,
                                                name = SessionManager.senderName,
                                                status = true
                                            ),
                                            isMissCall = false
                                        )
                                }
                            }

                        }
                        TelephonyManager.EXTRA_STATE_IDLE -> {


                            if (SessionManager.ringing && !SessionManager.callReceived) {

                                SessionManager.incomingNumber?.let {
                                    LOGS.d("$TAG miss call")
                                    sendNotification(
                                        SMS(
                                            content = "Missed call",
                                            number = SessionManager.incomingNumber,
                                            name = SessionManager.senderName
                                        ),
                                        isMissCall = true
                                    )
                                }

                            }
                            LOGS.d("$TAG Idle call")
                            SessionManager.ringing = false
                            SessionManager.callReceived = false
                            SessionManager.incomingNumber?.let {
                                SessionManager.incomingNumber = null
                                SessionManager.senderName = null
                                sendNotification(
                                    IncomingCall(status = false),
                                    isMissCall = false
                                )
                            }
                        }
                        TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                            LOGS.d("$TAG pickup call")
                            SessionManager.callReceived = true
                            SessionManager.incomingNumber?.let {
                                SessionManager.incomingNumber = null
                                SessionManager.senderName = null
                                sendNotification(
                                    IncomingCall(status = false),
                                    isMissCall = false
                                )
                            }
                        }
                    }
                    LOGS.d(TAG, "$state")
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private fun getSenderName(number: String?, context: Context?): String? {
        try {
            number?.let {
                val uri = Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(number)
                )
                val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
                var contactName: String? = null
                val cursor = context?.contentResolver?.query(uri, projection, null, null, null)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        contactName = cursor.getString(0)
                    }
                    cursor.close()
                }
                return contactName
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return null
    }

    private fun sendNotification(colorFitData: ColorfitData, isMissCall: Boolean) {
        if (!sessionManager.isDeviceConnected()) {
            return
        }


        val isCallEnabled = localDataStore.isCallAlertEnabled()
        val isSmsEnabled = localDataStore.isSMSAlertEnabled()

        when (colorFitData) {
            is IncomingCall -> {
                if (isCallEnabled) {
                    sendCallNotification(colorFitData)
                }
            }
            is SMS -> {
                if (isSmsEnabled) {
                    sendSMSNotification(colorFitData, isMissCall)
                }
            }
        }
    }

    private fun sendCallNotification(
        incomingCall: IncomingCall,
    ) {
        sessionManager.sendUpdateQueryAction(UpdateDeviceAction.SetIncomingCallInfo(incomingCall))
    }


    private fun sendSMSNotification(sms: SMS, isMissCall: Boolean) {
        var appType = ApplicationType.SMS.type
        if (isMissCall) {
            appType = ApplicationType.MISSEDCALL.type
        }
        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SendAppNotification(
                AppNotification(
                    appType = appType,
                    number = sms.number,
                    name = sms.name,
                    message = sms.content
                )
            )
        )
    }
}
