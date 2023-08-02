package com.noisefit.receiver.service

import android.app.KeyguardManager
import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.data.local.AppStaticData
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.delay

import com.noisefit.util.music.mode.MusicMode
import com.noisefit.util.notif.NotificationUtil
import com.noisefit_commans.constants.CommonGlobals
import com.noisefit_commans.data.enums.Actions
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.AppNotification
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationAlertService : NotificationListenerService() {


    private var notificationQueue = HashMap<Int, Long>()
    private var sendStopState = false

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var dataUnitConverter: DataUnitConverter

    @Inject
    lateinit var sessionManager: SessionManager

    private val OutTime = 15 * 1000
    private var OldSendTime: Long = 0
    private var OldPageName = ""
    private var oldMessage = ""

    private val NOTIFICATION_CATEGORY = "call"
    private val TAG = "NotificationAlertService"

    private var lastMusicNode: MusicMode? = null


    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }


    override fun onCreate() {
        super.onCreate()

        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

        registerActiveMediaControllerCallback(mediaSessionManager)

//        addSessionStateChangeListener()
    }


    override fun onDestroy() {
        unregisterCallback(mediaController)
        super.onDestroy()
    }


    override fun onListenerConnected() {
        super.onListenerConnected()
        LOGS.d(TAG, "NotificationAlert Service Connected")

        initListeners()

//        addSessionStateChangeListener()
        //updateNotification()
        tryReconnectService()


    }

    private fun initListeners() {
        sessionManager.connectState.observeForever() { connectedState ->
            LOGS.d(TAG, "CONNECT_STATE NotificationAlertService > $connectedState")
            when (connectedState) {
                is ConnectState.ConnectSuccess -> {
                    lastMusicNode?.let {
                        syncMusicData(null, null)
                    }

//                    musicEventObservable?.requestMusicInfo()

                }
                else -> {}
            }
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        LOGS.d(TAG, "onListenerDisconnected")
        tryReconnectService()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            if (intent.action == null)
                return@let

            when (intent.action) {
                Actions.START.name -> tryReconnectService()
                Actions.STOP.name -> stopSelf()
                else -> {
                    LOGS.i(
                        TAG,
                        "NotificationAlertService > This should never happen. No action in the received intent"
                    )
                    tryReconnectService()
                }
            }
        }
        if (sessionManager.isDeviceConnected()) {
            LOGS.d(TAG, "onStartCommand in notification alert service")
        }
        return START_STICKY
    }

    private val whatsappNotificationBlockArray = arrayListOf(
        /*"silent_notifications",*///Not receiving video call notification so commented
        "chat_history_backup", "other_notifications", "sending_media"
    )


    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (!localDataStore.isNotificationAlertEnabled()) {
            // LOGS.i(TAG, "Notification not enabled")
            return
        }
        if (!sessionManager.isDeviceConnected()) {
            return
        }

        if (isSmartNotificationOn()) {
            LOGS.d("$TAG smart_notification_on device_locked")
            return
        }


        if (sbn == null) return

        if (notificationQueue.containsKey(sbn.id)) {
            if (notificationQueue[sbn.id] == sbn.notification.`when`) {
                LOGS.d("$TAG repeated notification ${sbn.notification.`when`}")
                return
            }
        }
        notificationQueue[sbn.id] = sbn.notification.`when`

        if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) {
            if (sbn.notification.category != null && !sbn.notification.category.equals(
                    NOTIFICATION_CATEGORY,
                    ignoreCase = true
                )
            ) {
                return
            }
        }

        val notification = sbn.notification

        if (notification?.extras == null) return


        var packageName: String? = ""
        var notificationTitle: CharSequence? = null
        var notificationText: CharSequence? = null
        var tickerText: CharSequence? = null
        try {
            packageName = sbn.packageName
            notificationTitle = notification.extras.getCharSequence(Notification.EXTRA_TITLE)
            notificationText = notification.extras.getCharSequence(Notification.EXTRA_TEXT)
            tickerText = notification.tickerText
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return
        }



        if (packageName.isNullOrEmpty() || notificationText.isNullOrEmpty() || notificationTitle.isNullOrEmpty()) {
            return
        }

        if (notificationTitle.toString()
                .contains(AppStaticData.NOTIFICATION_TITLE, ignoreCase = true) ||
            notificationTitle.toString()
                .contains(AppStaticData.FINDING_YOUR_PHONE, ignoreCase = true)
        ) {
            return
        }

        val blockPackageFound = blockPackageList.filter {
            it == packageName
        }

        if (blockPackageFound.isNotEmpty()) {
            return
        }

        if ((packageName == "com.whatsapp.w4b" || packageName == "com.whatsapp") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val blockedChannel =
                whatsappNotificationBlockArray.firstOrNull { notification.channelId.contains(it) }
            if (blockedChannel != null) return

        } else if (packageName == "com.facebook.orca" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notification.channelId.contains("chathead_active")) {
                return
            }
        } else if (packageName == "com.snapchat.android" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notification.channelId.contains("general_group_silent") || notification.channelId.contains(
                    "general_group_generic_push"
                )
            ) {
                return
            }
        }


        if (TextUtils.isEmpty(notificationText) && !TextUtils.isEmpty(tickerText) && !TextUtils.isEmpty(
                notificationTitle
            ) &&
            tickerText != notificationTitle
        ) {
            notificationText = tickerText
        }


        val curTime = System.currentTimeMillis()

        if (OldPageName.equals(packageName, ignoreCase = true) &&
            oldMessage.equals(notificationText.toString(), ignoreCase = true)
        ) {
            if (curTime - OldSendTime < OutTime) {
                Log.i(TAG, "retuen 15 S repeat message=" + (curTime - OldSendTime) + "ms")
                return
            }
        }


        handleNotification(packageName, notificationTitle.toString(), notificationText.toString())

    }


    private val blockMessageList = arrayListOf(
        "whatsapp web is currently active",
        "whatsapp",
        "calling…",
        "ongoing voice call",
        "ongoing video call",
        "ringing…"
    )

    private val blockPackageList = arrayListOf(
        "com.android.mms",
        "com.android.mms.service",
        "com.oneplus.mms",
        "com.samsung.android.messaging",
        "cn.nubia.mms",
        "com.google.android.apps.messaging",
        "com.android.systemui",
    )

    private fun isDeviceLocked(context: Context): Boolean {
        var isLocked = false

        // First we check the locked state
        val keyguardManager = context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        val inKeyguardRestrictedInputMode = keyguardManager.isKeyguardLocked
        if (inKeyguardRestrictedInputMode) {
            isLocked = true
        } else {
            // If password is not set in the settings, the inKeyguardRestrictedInputMode() returns false,
            // so we need to check if screen on for this case
            val powerManager: PowerManager = context.getSystemService(POWER_SERVICE) as PowerManager
            isLocked = !powerManager.isInteractive
        }

       // LOGS.d("$TAG smart_notification_on $isLocked")
        //LOGS.d(TAG, String.format("Now device is %s.", if (isLocked) "locked" else "unlocked"))
        return isLocked
    }

    private fun isSmartNotificationOn(): Boolean {
        if (!localDataStore.getExperimentalSettings().smartNotification) {
          //  LOGS.d("$TAG smart_notification_on toggle_off")
            return false
        }
        return !isDeviceLocked(NoiseFitApplicationMain.context!!)
    }

    private fun handleNotification(appPackageName: String, title: String, text: String) {

        var name = title
        if (title.isEmpty()) {
            name = "New Message"
        }

        val blockMessageFound = blockMessageList.filter {
            it.equals(text.lowercase(), false)
        }

        if (blockMessageFound.isNotEmpty()) {
            return
        }

        val enabledAppList = localDataStore.getNotificationEnabledAppList()
        if (enabledAppList.isNullOrEmpty()) {
            return
        }

        val notificationFoundList = enabledAppList.filter {
            it.appPackageName == appPackageName && it.isEnabled
        }


        if (notificationFoundList.isNotEmpty()) {

            val appCode = AppStaticData.getNotificationTypeName(appPackageName)
            val message = if (text != "") text else ""
            val appNotification = AppNotification(appCode, name, null, message)
            LOGS.d("$TAG post notification $appNotification")
            sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.SendAppNotification(
                    appNotification
                )
            )

            OldPageName = packageName
            oldMessage = message
            OldSendTime = System.currentTimeMillis()

            LOGS.d("$TAG notificationQueue size ${notificationQueue.size}")
            if (notificationQueue.size == 40) {
                notificationQueue.clear()
            }
        }


    }


    private fun tryReconnectService() {

        try {
            delay(2000L) {
                if (NotificationUtil.notificationActionGranted(applicationContext)) {
                    LOGS.d(TAG, "Access granted toggle service")
                    sendStopState = false
                    toggleNotificationListenerService()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val componentName = ComponentName(
                            applicationContext,
                            NotificationAlertService::class.java
                        )

                        //It say to Notification Manager RE-BIND your service to listen notifications again inmediatelly!
                        requestRebind(componentName)
                        LOGS.d(TAG, "Rebinding")
                    }
                    addSessionStateChangeListener()

                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Try deactivate/activate your component service
     */
    private fun toggleNotificationListenerService() {
        try {
            val pm = packageManager
            pm.setComponentEnabledSetting(
                ComponentName(
                    this,
                    NotificationAlertService::class.java
                ),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
            pm.setComponentEnabledSetting(
                ComponentName(
                    this,
                    NotificationAlertService::class.java
                ),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        /**
         * Same as declared in manifest
         */
        const val SERVICE_NAME = "com.noisefit.receiver.service.NotificationAlertService"
    }


    private var mediaController: MediaController? = null

    private lateinit var mediaSessionManager: MediaSessionManager

    private val mediaControllerCallback: MediaController.Callback =
        object : MediaController.Callback() {
            override fun onPlaybackStateChanged(playbackState: PlaybackState?) {
                val title = mediaController?.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
                CommonGlobals.songName = title
                syncMusicData(title, playbackState)
            }

            override fun onMetadataChanged(mediaMetadata: MediaMetadata?) {
                val title = mediaMetadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
                val playbackState = mediaController?.playbackState
                CommonGlobals.songName = title
                syncMusicData(title, playbackState)
            }
        }

    //2 paused, 1 playing, 3 others
    private fun syncMusicData(title: String?, playbackState: PlaybackState?) {
        if (!sessionManager.isDeviceConnected()) {
            return
        }


        var songName = title
        if (songName.isNullOrEmpty()) {
            songName = lastMusicNode?.title ?: ""
        }

        var musicState = playbackState?.state
        if (musicState == null) {
            musicState = lastMusicNode?.playState ?: -1
        }
        if (musicState == -1) {
            LOGS.d("syncMusicData  waiting for music state")
            return
        }
        if (musicState == PlaybackState.STATE_BUFFERING && songName.isNullOrEmpty()) {
            LOGS.d("syncMusicData ignore buffering")
            return
        }

        val musicMode = MusicMode.createMusicMode(songName, "", "", 0, musicState)
        LOGS.d("syncMusicData $songName $musicState")
        if (lastMusicNode == null) {
            lastMusicNode = musicMode
        } else if (lastMusicNode?.title?.equals(musicMode.title) == true && lastMusicNode?.playState == musicMode.playState) {
            return
        }
        lastMusicNode = musicMode

        var songPlaying = 1
        if (musicMode.playState != PlaybackState.STATE_PLAYING &&
            musicMode.playState != PlaybackState.STATE_BUFFERING
        ) {
            songPlaying = 2
        }
        LOGS.d("syncMusicData sent to watch $songName $musicState $songPlaying")
        sessionManager.sendQueryAction(QueryAction.SendSongName(songPlaying, songName))
    }

    private val activeSessionsChangedListener =
        MediaSessionManager.OnActiveSessionsChangedListener {
            registerActiveMediaControllerCallback(mediaSessionManager)
        }

    private fun addSessionStateChangeListener() {
        try {
            mediaSessionManager.addOnActiveSessionsChangedListener(
                activeSessionsChangedListener,
                ComponentName(applicationContext, NotificationAlertService::class.java)
            )
            Log.i(TAG, "Successfully added session change listener")
        } catch (e: SecurityException) {
            e.printStackTrace()
            Log.e(TAG, "Failed to add session change listener ${e.localizedMessage}")
        }
    }

    private fun getActiveMediaController(mediaSessionManager: MediaSessionManager): MediaController? {
        return try {
            val mediaControllers = mediaSessionManager.getActiveSessions(
                ComponentName(
                    applicationContext,
                    NotificationAlertService::class.java
                )
            )
            mediaControllers.firstOrNull()
        } catch (e: SecurityException) {
            null
        }
    }

    private fun registerActiveMediaControllerCallback(mediaSessionManager: MediaSessionManager) {
        getActiveMediaController(mediaSessionManager)?.let { mediaController ->
            registerCallback(mediaController)

            mediaController.metadata?.let { metadata ->
                mediaControllerCallback.onMetadataChanged(metadata)
            }
        }
    }

    private fun registerCallback(mediaController: MediaController) {

        unregisterCallback(this.mediaController)

        Log.i(TAG, "Registering callback for ${mediaController.packageName}")

        this.mediaController = mediaController
        mediaControllerCallback.let { mediaControllerCallback ->
            mediaController.registerCallback(mediaControllerCallback)
        }
    }

    private fun unregisterCallback(mediaController: MediaController?) {

        Log.i(TAG, "Unregistering callback for ${mediaController?.packageName}")

        mediaControllerCallback.let { mediaControllerCallback ->
            mediaController?.unregisterCallback(mediaControllerCallback)
        }
    }

}