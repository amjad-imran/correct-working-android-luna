package com.noisefit.receiver.service

//import com.clevertap.android.sdk.CleverTapAPI

import android.content.Intent
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.noisefit.luna.BuildConfig
import com.noisefit.data.local.AppStaticData
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit.session.SessionManager
import com.noisefit.util.notif.NotificationEventsClass
import com.noisefit.util.notif.NotificationEventsClass.NOTIFICATION_INDEX_EXTRA
import com.noisefit.util.notif.NotificationEventsClass.NOTIFICATION_LINK
import com.noisefit.util.notif.NotificationEventsClass.NOTIFICATION_TYPE_EXTRA
import com.noisefit.util.notif.NotificationUtil
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.AppNotification
import com.noisefit_commans.utils.LOGS
import com.useinsider.insider.Insider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class NotificationService
@Inject
constructor() : FirebaseMessagingService() {

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var keyValueDataSource: KeyValueDataSource

    private val dataScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext =
            Dispatchers.IO // no job added i.e + SupervisorJob()
    }

    lateinit var job: Job

    override fun onNewToken(token: String) {
        LOGS.d(TAG, "onNewToken event received $token")
        val newTokenEvent = Intent(NEW_TOKEN_EVENT)
        LocalBroadcastManager
            .getInstance(this)
            .sendBroadcast(newTokenEvent)
    }

    override fun onMessageReceived(message: RemoteMessage) {
//        LOGS.d(TAG, "onMessageReceived event received ${Gson().toJson(message)}")
        message.data.apply {
            try {
                if (message != null &&
                    message.data.containsKey("source") &&
                    message.data.get("source").equals("Insider")
                ) {
                    launchSendNotificationJob(message.notification?.title ?: "",message.notification?.body ?: "")
                    Insider.Instance.handleFCMNotification(applicationContext, message)
                    return
                }
                if (size > 0) {
                    val extras = Bundle()
                    for ((key, value) in this) {
                        extras.putString(key, value)
                    }

                    NotificationUtil.pushNotification(
                        this@NotificationService,
                        message.notification?.title ?: "",
                        message.notification?.body ?: "",
                        message.data[NOTIFICATION_TYPE_EXTRA] ?: "",
                        message.data[NOTIFICATION_INDEX_EXTRA] ?: "",
                        message.data[NOTIFICATION_LINK] ?: ""
                    )
                    val notificationType = message.data[NOTIFICATION_TYPE_EXTRA] ?: ""
                    job = dataScope.launch(Dispatchers.IO) {
                        deleteCache(notificationType)
                    }
//                        if (/*notificationType.equals(NotificationClass.NOTIFICATION_TYPE_BUDDY,true) ||*/
//                            notificationType.equals(
//                                NotificationEventsClass.NOTIFICATION_TYPE_MANAGE,
//                                true
//                            )
//                        ) {
//                            val broadcastIntent = Intent().apply {
////                                action = ManageFragment.ACTION_BUDDY
//                            }
//                            sendBroadcast(broadcastIntent)
//
//                        }
//                    LOGS.d("NOTIFICATION_TYPE ${Gson().toJson(message)}")
//                    LOGS.d("NOTIFICATION_TYPE $notificationType ${message.notification?.body} ${message.data[NOTIFICATION_INDEX_EXTRA]}")
                    // not from CleverTap handle yourself or pass to another provider

                    launchSendNotificationJob(message.notification?.title ?: "",message.notification?.body ?: "")

                }
//                }
            } catch (t: Throwable) {
                LOGS.e("MYFCMLIST", "Error parsing FCM message ${t.toString()}")
            }
        }
    }

    private suspend fun deleteCache(type: String) {
        when (type.lowercase()) {
            NotificationEventsClass.NOTIFICATION_TYPE_USER_ACCEPT_COMPETE_REQUEST -> {
                keyValueDataSource.removeDataByKey(
                    "0",
                    KeyValueDataType.FRIENDS_COMPETITION
                )
            }

            NotificationEventsClass.NOTIFICATION_TYPE_USER_ACCEPT_REQUEST -> {
                keyValueDataSource.removeDataByType(
                    KeyValueDataType.FRIENDS_ACTIVITY
                )
            }
        }
    }

    private fun launchSendNotificationJob(title: String, body: String) {
        job = dataScope.launch(Dispatchers.IO) {
            sendNotification(title, body)
        }

    }

    private fun sendNotification(title: String, body: String) {

        if (!localDataStore.isNotificationAlertEnabled()) {
            // LOGS.i(TAG, "Notification not enabled")
            return
        }
        if (!sessionManager.isDeviceConnected()) {
            return
        }

        val enabledAppList = localDataStore.getNotificationEnabledAppList()
        if (enabledAppList.isNullOrEmpty()) {
            return
        }

        val notificationFoundList = enabledAppList.filter {
            it.appPackageName == BuildConfig.APPLICATION_ID && it.isEnabled
        }

        if (notificationFoundList.isEmpty()) {
            return
        }

        val appCode = AppStaticData.getNotificationTypeName(BuildConfig.APPLICATION_ID)

        val appNotification = AppNotification(appCode, title, null, body)
        LOGS.d("$TAG post notification $appNotification")
        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SendAppNotification(
                appNotification
            )
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object {
        private const val TAG = "RNFMessagingService"
        const val MESSAGE_EVENT = "messaging-message"
        const val NEW_TOKEN_EVENT = "messaging-token-refresh"
        const val REMOTE_NOTIFICATION_EVENT = "notifications-remote-notification"
    }
}