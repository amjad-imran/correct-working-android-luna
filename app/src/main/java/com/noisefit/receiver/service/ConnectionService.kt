package com.noisefit.receiver.service

import android.Manifest
import android.app.AlarmManager
import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.noisefit.luna.R
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.local.db.DataBase
import com.noisefit.data.repository.LastSyncItems
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.SportEventRepository
import com.noisefit.data.repository.abstraction.SyncRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.receiver.broadcastReceiver.AudioSettingReceiver
import com.noisefit.receiver.workManager.HealthOverviewDataType
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.tryCatch
import com.noisefit.ui.myDevice.camera.CameraShutterActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit.util.FirebaseCrashlyticsUtils
import com.noisefit.util.SportUtils
import com.noisefit.util.moveToServer.BatteryNotificationUtils
import com.noisefit.util.notif.NotificationEventsClass
import com.noisefit.util.notif.NotificationUtil
import com.noisefit.util.notif.NotificationUtil.NOTIFICATION_ID_MAIN
import com.noisefit.watch.ApplicationHandler
import com.noisefit.watch.CommonGlobals
import com.noisefit.watch.ConnectionHandler
import com.noisefit.watch.DeviceQueryHandler
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.UpdateDeviceHandler
import com.noisefit.watch.UserActivityHandler
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.constants.EventConstants
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.enums.Actions
import com.noisefit_commans.data.enums.Device
import com.noisefit_commans.data.enums.ServiceState
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.data.model.User
import com.noisefit_commans.handler.MusicPlayerControlsHandler
import com.noisefit_commans.interfaces.IQueryDataCallback
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.connection.BindState
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.connection.ConnectionCallbacks
import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.interfaces.data.IUserActivityDataCallback
import com.noisefit_commans.interfaces.data.UserActivityAction
import com.noisefit_commans.interfaces.data.UserActivityCallback
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.utils.*
import com.noisefit_commans.interfaces.device_data.IUpdateDeviceDataCallback
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceFirmware
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.DeviceUnits
import com.noisefit_commans.models.StepsData
import com.noisefit_commans.models.TimeFormat
import com.noisefit_commans.models.TimeFormats
import com.noisefit_commans.models.UpdateStatus
import com.noisefit_commans.models.WatchFirmwareDetails
import com.noisefit_commans.models.WatchUpdateStatus
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.PhoneRinger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ScheduledFuture
import javax.inject.Inject


//https://robertohuertas.com/2019/06/29/android_foreground_services/
@AndroidEntryPoint
class ConnectionService
@Inject
constructor() : LifecycleService() {

    private var timer: Timer? = null
    private val executor: Executor = Executor()

    private val TAG = ConnectionService::class.java.simpleName

    @Inject
    lateinit var syncRepository: SyncRepository

    @Inject
    lateinit var batteryNotificationUtils: BatteryNotificationUtils

    @Inject
    lateinit var dataUnitConverter: DataUnitConverter

    @Inject
    lateinit var database: DataBase

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var sportUtils: SportUtils

    @Inject
    lateinit var firebaseCrashlyticsUtils: FirebaseCrashlyticsUtils

    @Inject
    lateinit var watchDataStore: WatchDataStore

    @Inject
    lateinit var lastSyncProvider: LastSyncProvider

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var connectionHandler: ConnectionHandler

    @Inject
    lateinit var queryHandler: DeviceQueryHandler

    @Inject
    lateinit var updateDeviceHandler: UpdateDeviceHandler

    @Inject
    lateinit var userActivityHandler: UserActivityHandler

    @Inject
    lateinit var sportEventRepository: SportEventRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var callHandler: CallHandler

    @Inject
    lateinit var vibrationUtils: VibrationUtils

    @Inject
    lateinit var watchesSDK: WatchesSDK

    private var isStopServiceCalled = false
    // private var isDateSynced = false

    //    private var connectionMode = ConnectionMode.BLUETOOTH
    private var isServiceStarted = false
    private var initDefaultValues = false
    private var connectionDataAction: ConnectionDataActions? = null
    private var queryDataAction: QueryDeviceDataActions? = null
    private var updateDeviceDataAction: UpdateDeviceDataActions? = null
    private var userActivityDataActions: UserActivityDataActions? = null
    private val handler = Handler()
    private val handlerActivity = Handler()

    var mLastNotification: Notification? = null

    private val runnableCode: Runnable = object : Runnable {
        override fun run() {
            val request = sessionManager.sportsModeRequest.value
            request?.let { sportsReq ->
                sportsReq?.duration = sportsReq?.duration?.plus(1)!!
                sessionManager.postSportsModeRequest(sportsReq)
                sessionManager.sendUserActivityAction(UserActivityAction.Refresh(sportsReq))
            }
            handlerActivity.sendEmptyMessage(100)
            handlerActivity.postDelayed(this, 1000)
        }
    }

    @Inject
    lateinit var applicationHandler: ApplicationHandler

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        LOGS.i(TAG, "Some component want to bind with the service")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        LOGS.i(TAG, "onStartCommand executed with startId: $startId")
        try {
            mLastNotification?.let {
                LOGS.i(TAG, "onStartCommand executed posting last notification")
                startForeground(NOTIFICATION_ID_MAIN, it)
            }


            if (!hasRequiredBluetoothPermission()) {
                stopServiceAndShowNotification()
            }


            if (intent != null) {
                val action = intent.action
                LOGS.i(TAG, "using an intent with action $action")
                when (action) {
                    Actions.START.name -> startService(false)
                    Actions.INIT_DEFAULT.name -> startService(true)
                    Actions.STOP.name -> {
                        val device = localDataStore.getConnectedDevice()
                        isStopServiceCalled = true
                        /**
                         * Added to stop issue
                         * Context.startForegroundService() did not then call Service.startForeground()
                         * need testing
                         */
                        mLastNotification = NotificationUtil.getNotification(this)
                        startForeground(NOTIFICATION_ID_MAIN, mLastNotification)
                        stopService(device)
                    }

                    else -> {
                        LOGS.i(TAG, "This should never happen. No action in the received intent")
                        startService(false)
                    }
                }
            } else {
                LOGS.i(
                    TAG,
                    "with a null intent. It has been probably restarted by the system."
                )
                startService(false)
            }
            ApplicationUtils.startNotificationListenerService(localDataStore, applicationContext)
            // by returning this we make sure the service is restarted if the system kills the service
        } catch (e: ForegroundServiceStartNotAllowedException) {
            e.printStackTrace()
            LOGS.d("NEEDBACK_PERMISSION_1")

        }

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        LOGS.i(TAG, "The service has been created")
        try {

            firebaseCrashlyticsUtils.setCrashlyticsUserProperty()
            if (localDataStore.getConnectedDevice() == null) {
                mLastNotification = NotificationUtil.getNotification(this)
                mLastNotification?.let {
                    startForeground(NOTIFICATION_ID_MAIN, it)
                }
                isStopServiceCalled = true
                stopSelf()
            } else {
                /**
                 * Added to stop issue
                 * Context.startForegroundService() did not then call Service.startForeground()
                 * need testing
                 */
                mLastNotification = NotificationUtil.getNotification(this)

                mLastNotification?.let {
                    startForeground(NOTIFICATION_ID_MAIN, it)
                }
            }

            if (!hasRequiredBluetoothPermission()) {

                stopServiceAndShowNotification()
            }
            showNotification(getString(R.string.text_connecting))
            updateAlarmManager()
        } catch (e: ForegroundServiceStartNotAllowedException) {
            e.printStackTrace()
            LOGS.d("NEEDBACK_PERMISSION_2")
            ApplicationUtils.setRescueWorkManager(this)
        }
    }

    fun hasRequiredBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun updateAlarmManager() {
        try {
            val restartServiceIntent = Intent(this, this.javaClass)
            val pendingIntent = PendingIntent.getService(
                this,
                8285,
                restartServiceIntent,
                PendingIntent.FLAG_MUTABLE
            )
            val alarm = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm.cancel(pendingIntent)
            alarm.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                5000,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteDB() {
        GlobalScope.launch(Dispatchers.IO) {
            database.clearAllTables()
        }
    }

    private fun showNotification(title: String) {
        updateNotification()
    }

    override fun onDestroy() {
        LOGS.d(TAG, "On Destroy Called $isStopServiceCalled")
        AppLogs.sendAppLogs("ConnectionService : onDestroy(), isStopServiceCalled : $isStopServiceCalled")
        if (!isStopServiceCalled) {
            if (hasRequiredBluetoothPermission()) {
                val restartServiceIntent = Intent(this, this.javaClass)
                restartServiceIntent.setPackage(packageName)
                val restartServicePendingIntent = PendingIntent.getService(
                    applicationContext,
                    8285,
                    restartServiceIntent,
                    PendingIntent.FLAG_MUTABLE
                )
                val alarmService =
                    applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmService.cancel(restartServicePendingIntent)
                alarmService[AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 3000] =
                    restartServicePendingIntent
            }
        } else {
            val serviceIntent = Intent(this, this.javaClass)
            val pendingIntent =
                PendingIntent.getService(
                    this,
                    8285,
                    serviceIntent,
                    PendingIntent.FLAG_MUTABLE
                )
            val am = getSystemService(ALARM_SERVICE) as AlarmManager
            am.cancel(pendingIntent)
        }
        unregisterReceivers()

        super.onDestroy()
        LOGS.i(TAG, "The service has been destroyed")
        localDataStore.setServiceState(ServiceState.STOPPED)
    }

    private fun startService(initDefault: Boolean) {
        initDefaultValues = initDefault
        LOGS.i(TAG, "Before setConnetionMode")
//        setConnectionMode()
        if (isServiceStarted) return
        LOGS.i(TAG, "Starting the foreground service task")
        isServiceStarted = true
        localDataStore.setServiceState(ServiceState.STARTED)
        registerBluetoothReceivers()
        registerTimeChangeReceiver()
        setConnection()
        setQueryObserver()
        updateNotification()
        ApplicationUtils.startNotificationListenerService(localDataStore, applicationContext)


        contentResolver.registerContentObserver(
            android.provider.Settings.System.CONTENT_URI, true, AudioSettingReceiver(
                this,
                Handler(Looper.getMainLooper()),
                sessionManager
            )
        )

//        sessionManager.checkSport.observe(this) {
//            it.getContent()?.let {
//                checkSportWork()
//            }
//        }
    }

    private fun startWeatherScheduler() {
        if (localDataStore.getWeatherSwitch() == true) {
            GlobalScope.launch {
                LOGS.d(
                    TAG,
                    "Weather startWeatherScheduler isWorkSchedulerScheduled : ${sessionManager.isWorkSchedulerScheduled}"
                )

                if (!sessionManager.isWorkSchedulerScheduled) {
                    sessionManager.isWorkSchedulerScheduled = true
                    ApplicationUtils.startWeatherScheduler(applicationContext)
                } else {
                    val lastSyncTime = localDataStore.getLastWeatherSyncTimeStamp()
                    val difference = kotlin.math.abs(DateFormats.getTimeStamp() - lastSyncTime)
                    LOGS.d(TAG, "Weather sync difference $difference")
                    if (difference > 3600 * 1000L) {
                        ApplicationUtils.startWeatherScheduler(applicationContext)
                    }
                }
            }
        }
    }


    private fun getUser(): User? {
        return localDataStore.getUser()
    }


    private fun setConnection() {
        if (hasRequiredBluetoothPermission()) {
            checkValidateConnection()

            LOGS.d(TAG, "Inside setConnection")
            startForceConnectionTimer(20000)
        } else {

            stopServiceAndShowNotification()
        }
        //if (connectionMode == ConnectionMode.FORCED) {
        //30 seconds

        //}
    }


    private fun stopServiceAndShowNotification() {
        stopSelf()
        NotificationUtil.pushNotification(
            this,
            getString(R.string.text_permission_required),
            getString(R.string.text_permission_noisefit_permission_requires),
            NotificationEventsClass.APP_RESCUE_NOTIFICATION_KEY,
            ""
        )
    }

//    private fun setConnectionMode() {
//        localDataStore.getConnectedDevice()?.deviceType?.let {
//            if (it == DeviceType.COLORFIT_VISION.deviceType ||
//                it == DeviceType.COLORFIT_NAV.deviceType ||
//                it == DeviceType.NOISEFIT_AGILE.deviceType
//            ) {
//
//
//                connectionMode = ConnectionMode.FORCED
//            }
//        }
//    }


    /**
     * Stop service
     * clear watch related data in session manager
     */
    private fun hibernateService() {
        isStopServiceCalled = true
        val device = localDataStore.getConnectedDevice()
        LOGS.i(TAG, "Hibernating service started for ${device?.bluetoothName}")

        sessionManager.forceOtaFlowRunning = false
        sessionManager.forceOtaResponse = null


        device?.let { colorFitDevice ->
            applicationHandler.unInitSdks(colorFitDevice)
            connectionHandler.getConnectionActions(colorFitDevice)?.let { connectionDataActions ->
                connectionDataActions.removeCallbacks()
            }
            stopNotificationListenerService()
            ApplicationUtils.clearJobs(this)
            timer?.cancel()
            applicationContext.cacheDir?.deleteRecursively()
            LOGS.i(TAG, "Hibernating service device data cleared ${device?.bluetoothName}")
        }
        unregisterReceivers()
        reconnectHandler.removeCallbacks(bluetoothReconnectRunnable)
        isServiceStarted = false
        localDataStore.setServiceState(ServiceState.STOPPED)
        WatchInfoGlobals.resetData()
        sessionManager.clearSessionManagerHibernate()
        sessionManager.setConnectState(ConnectState.Hibernate())
        LOGS.i(TAG, "Hibernating service stopSelf() for ${device?.bluetoothName}")
        stopSelf()
    }

    private fun stopService(device: ColorFitDevice?) {
        LOGS.i(TAG, "Stopping the foreground service $isServiceStarted")

        sessionManager.forceOtaFlowRunning = false
        sessionManager.forceOtaResponse = null

        if (!isServiceStarted) {
            LOGS.i(TAG, "Service has been stopped -- Ignoring data")
            return
        }

        device?.let { colorFitDevice ->
            LOGS.i(TAG, "Stopping the foreground service - inside")
            applicationHandler.unInitSdks(colorFitDevice)
            connectionHandler.getConnectionActions(colorFitDevice)?.let { connectionDataActions ->
                connectionDataActions.removeCallbacks()
            }
            stopNotificationListenerService()
            deleteDB()
            vibrationUtils.vibrate(LOW_VIBRATION)
            ApplicationUtils.clearJobs(this)
            timer?.cancel()
            applicationContext.cacheDir?.deleteRecursively()
        } ?: LOGS.d(TAG, "getConnectedDevice is null")
        unregisterReceivers()
        reconnectHandler.removeCallbacks(bluetoothReconnectRunnable)
        isServiceStarted = false
        localDataStore.setServiceState(ServiceState.STOPPED)
        WatchInfoGlobals.resetData()
        sessionManager.clearSessionManager()
        sessionManager.setConnectState(ConnectState.UnPaired())
        stopSelf()
    }

    private fun stopNotificationListenerService() {
        try {
            val pm = packageManager
            pm.setComponentEnabledSetting(
                ComponentName(
                    this,
                    NotificationAlertService::class.java
                ),
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP
            )
        } catch (exp: Exception) {
        }
    }

    private fun registerBluetoothReceivers() {
        try {
            val filter = IntentFilter()
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            filter.addAction(BluetoothDevice.ACTION_FOUND)
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            registerReceiver(bluetoothConnectionStatusChangeReceiver, filter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun unregisterReceivers() {
        try {
            unregisterReceiver(bluetoothConnectionStatusChangeReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

        try {

            applicationContext.contentResolver.unregisterContentObserver(
                AudioSettingReceiver(
                    this,
                    Handler(Looper.getMainLooper()),
                    sessionManager
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            unregisterReceiver(timeChangedReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private var statusFailedConnection = false


    private fun checkSportWork() {
        AppLogs.sendAppLogs("$TAG checkSportWork")
        if (sportUtils.checkWhetherSportLiveOrNot()) {
            GlobalScope.launch {
                ApplicationUtils.startSportScheduler(this@ConnectionService)
            }
            localDataStore.saveLastSportApiTimeStamp(DateFormats.getTimeStamp())
        }
    }

    private fun checkValidateConnection() {
        LOGS.d(TAG, "checkValidateConnection called")
        //checkSportWork()


        val lastSyncTime = localDataStore.getLastPeriodicDataSyncTime()
        val difference = kotlin.math.abs(DateFormats.getTimeStamp() - lastSyncTime)
        LOGS.d(TAG, "Periodic sync difference $difference")
        if (difference > 21600 * 1000L) {
            setPeriodicInfo()
            localDataStore.setLastPeriodicDataSyncTime(DateFormats.getTimeStamp())
        }

        localDataStore.getConnectedDevice()?.let { colorFitDevice ->

            sessionManager.setConnectedDevice(colorFitDevice)
            if (connectionDataAction == null) {

                connectionDataAction = connectionHandler.getConnectionActions(colorFitDevice)

                if (connectionDataAction == null) {
                    LOGS.d(TAG, "Connection data action is null")
                }
                connectionDataAction?.callbackListener(object : ConnectionCallbacks {
                    override fun onConnect(connectState: ConnectState) {

                        validateAndSetConnectState(connectState, colorFitDevice)


                        when (connectState) {
                            is ConnectState.ConnectFailed -> {

                                if (localDataStore.getConnectedDevice() == null) {
                                    onDisconnectSuccess()
                                    return
                                }
                                showNotification(getString(R.string.text_connecting))
                                var status = "failed"
                                if (!connectState.status.isNullOrEmpty()) {
                                    status = connectState.status!!

                                }

                                if (status != "failed") {
                                    logConnectionEvents(
                                        InsiderAppEvents.ConnectionEvents.wn_connect_reconnect_failed,
                                        status,
                                        colorFitDevice
                                    )

                                }

                                /*if (connectionMode == ConnectionMode.BLUETOOTH) {
                                    statusFailedConnection = true
                                    startBluetoothReconnectTimer()
                                }*/

                                sessionManager.setConnectedDevice(colorFitDevice)
                            }

                            is ConnectState.ConnectSuccess -> {

                                var status = "success"
                                if (!connectState.status.isNullOrEmpty()) {
                                    status = connectState.status!!
                                }
                                logConnectionEvents(
                                    InsiderAppEvents.ConnectionEvents.wn_connect_reconnect_connected,
                                    status,
                                    colorFitDevice
                                )

                                sessionManager.setConnectedDevice(colorFitDevice)
                                stateConnected(colorFitDevice)
                                startWeatherScheduler()

                            }

                            is ConnectState.DisconnectSuccess -> {

                                onDisconnectSuccess()
                                var status = "disconnect success"
                                if (!connectState.status.isNullOrEmpty()) {
                                    status = connectState.status!!
                                }
                                logConnectionEvents(
                                    InsiderAppEvents.ConnectionEvents.wn_connect_reconnect_disconnected,
                                    status,
                                    colorFitDevice
                                )

                            }

                            is ConnectState.DfuMode -> {
                                sessionManager.needDfuUpdate.value =
                                    Event(connectState.needForceOTA)
                            }

                            else -> {}
                        }
                    }

                    override fun onBluetoothConnect(isBluetoothConnected: Boolean) {

                    }

                    override fun onInitCompleted(noiseFitDevice: ColorFitDevice?) {

                    }

                    override fun onBind(
                        noiseFitDevice: ColorFitDevice?,
                        bindState: BindState
                    ) {

                    }

                    override fun onDeviceReady(noiseFitDevice: ColorFitDevice?) {
                    }

                    override fun onFirmwareUpgradeProgress(firmware: DeviceFirmware) {

                    }

                })
            }


            if (queryDataAction == null) {
                queryDataAction = queryHandler.getQueryActions(colorFitDevice)
                queryDataAction?.callbackListenerNew(queryCallback)
                queryDataAction?.setDevice(colorFitDevice)
            }

            if (updateDeviceDataAction == null) {
                updateDeviceDataAction = updateDeviceHandler.getQueryActions(colorFitDevice)
                updateDeviceDataAction?.callbackListenerNew(updateDeviceCallback)
                updateDeviceDataAction?.setDevice(colorFitDevice)

            }

            if (userActivityDataActions == null) {
                userActivityDataActions = userActivityHandler.getUserActivityActions(colorFitDevice)
                userActivityDataActions?.callbackListenerNew(userActivityCallback)
                userActivityDataActions?.setDevice(colorFitDevice)
            }

            setISConnected(colorFitDevice)
        } ?: LOGS.d(TAG, "Connected device is null")
    }

    private fun validateAndSetConnectState(
        connectState: ConnectState,
        colorFitDevice: ColorFitDevice
    ) {
        var sendState = true
        when (connectState) {
            is ConnectState.ConnectFailed -> {
                if (!colorFitDevice.deviceType.equals(
                        connectState.noiseFitDevice?.deviceType,
                        true
                    )
                ) {
                    sendState = false
                }
            }

            is ConnectState.ConnectSuccess -> {
                if (!colorFitDevice.deviceType.equals(
                        connectState.noiseFitDevice?.deviceType,
                        true
                    )
                ) {
                    sendState = false
                }
            }

            is ConnectState.Connecting -> {
                if (!colorFitDevice.deviceType.equals(
                        connectState.noiseFitDevice?.deviceType,
                        true
                    )
                ) {
                    sendState = false
                }
            }

            is ConnectState.DfuMode -> {
                if (!colorFitDevice.deviceType.equals(
                        connectState.noiseFitDevice?.deviceType,
                        true
                    )
                ) {
                    sendState = false
                }
            }

            is ConnectState.DisconnectFailed -> {
                if (!colorFitDevice.deviceType.equals(
                        connectState.noiseFitDevice?.deviceType,
                        true
                    )
                ) {
                    sendState = false
                }
            }

            is ConnectState.DisconnectSuccess -> {
                if (!colorFitDevice.deviceType.equals(
                        connectState.noiseFitDevice?.deviceType,
                        true
                    )
                ) {
                    sendState = false
                }
            }


            is ConnectState.Start -> {
                if (!colorFitDevice.deviceType.equals(
                        connectState.noiseFitDevice?.deviceType,
                        true
                    )
                ) {
                    sendState = false
                }
            }

            else -> {

            }
        }


        if(sendState){
            sessionManager.setConnectState(connectState)
        }
    }

    private fun setPeriodicInfo() {
        updateDeviceDateTime()
        setUserInfo()
    }

    private fun updateDeviceDateTime() {

        val timeFormat = DateFormats.getDefaultUnitFormats(this)
        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetDeviceDateTime(
                Calendar.getInstance(),
                timeFormat
            )
        )

    }

    private fun setUserInfo() {
        val user = getUser()
        if (user?.userInfo != null && user.userGoals != null) {

            val unit = DeviceUnits(
                unitSystem = user.userGoals!!.unitSystem
            )
            sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.SetDeviceUnits(unit)
            )
            sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.SetUserInfo(
                    user.userInfo!!,
                    user.userGoals!!,
                    user.firstName
                )
            )
        } else {
            LOGS.d("$TAG empty user goal or info $user")
        }


    }


    fun onDisconnectSuccess() {
        statusFailedConnection = false
        val device = localDataStore.getConnectedDevice()
        localDataStore.clearConnectedDevice()
        localDataStore.setUserDataSynced(false)
        watchDataStore.clearWatchData()
        lastSyncProvider.removeSyncTimeStamp(
            listOf(
                LastSyncItems.WATCHFACE_CATEGORIES,
                LastSyncItems.WATCHFACE_MAIN_LIST,
                LastSyncItems.FAVOURITES_WATCHFACE
            )
        )
        sessionManager.setConnectedDevice(null)
        isStopServiceCalled = true
        stopService(device)
    }

    private fun logConnectionEvents(
        eventName: String,
        status: String,
        colorFitDevice: ColorFitDevice
    ) {
        val supplier = watchesSDK.getWatchType(colorFitDevice)
        sessionManager.logInsiderAppEvent(
            eventName,
            HashMap<String, Any>().apply {
                this["status"] = status
                this["dName"] = colorFitDevice.bluetoothName ?: ""
                this["dMac"] = colorFitDevice.address ?: ""
                this["supplier"] = supplier.name
            })
    }

    private fun setISConnected(colorFitDevice: ColorFitDevice) {
        logConnectionEvents(
            InsiderAppEvents.ConnectionEvents.wn_connect_ask_reconnect,
            "init",
            colorFitDevice
        )
        connectionDataAction?.let { action ->
            if (!action.isConnected()) {
                logConnectionEvents(
                    InsiderAppEvents.ConnectionEvents.wn_connect_ask_reconnect_failed,
                    "failed",
                    colorFitDevice
                )
                LOGS.d(TAG, "On Device Reconnected")
                showNotification(getString(R.string.text_connecting))
                sessionManager.setConnectState(ConnectState.Connecting(colorFitDevice))
                logConnectionEvents(
                    InsiderAppEvents.ConnectionEvents.wn_connect_reconnect_again,
                    "init",
                    colorFitDevice
                )
                action.reconnect(colorFitDevice, true)
            } else {
                LOGS.d(TAG, "action.isConnected() is true")
                logConnectionEvents(
                    InsiderAppEvents.ConnectionEvents.wn_connect_ask_reconnect_connected,
                    "connected",
                    colorFitDevice
                )
                if (sessionManager.connectState.value !is ConnectState.ConnectSuccess) {
                    LOGS.d(TAG, "action.isConnected Connect Success")
                    sessionManager.setConnectState(ConnectState.ConnectSuccess(noiseFitDevice = colorFitDevice))
                    stateConnected(colorFitDevice)
                }
//                queryWatchInfo(colorFitDevice,1200)
                queryWatchInfo(colorFitDevice, 1800)
                LOGS.d(TAG, "On Device Connected")
            }
        }
    }

    fun stateConnected(colorFitDevice: ColorFitDevice) {
        showNotification(getString(R.string.text_connected))
        queryWatchInfo(colorFitDevice, 300)

        statusFailedConnection = false
    }

    fun queryWatchInfo(colorFitDevice: ColorFitDevice, fetchTime: Int) {
        val lastSyncTime = localDataStore.getLastInfoFetchTime()
        val difference = kotlin.math.abs(DateFormats.getTimeStamp() - lastSyncTime)
        LOGS.d(TAG, "Info Fetch Difference $difference")
        if (difference > fetchTime * 1000L || sessionManager.batterPercent.value == 0) {
            LOGS.d(TAG, "Info Fetch Difference ask for battery")
            queryHandler.getQueryActions(colorFitDevice)?.queryBatteryPower()
            queryHandler.getQueryActions(colorFitDevice)?.queryFirmwareVersion()
            localDataStore.setLastInfoFetchTime(DateFormats.getTimeStamp())
        }
    }

    private var reconnectHandler = Handler(Looper.getMainLooper())


    //--------------------------------------------------------------------------------------------
    // Connection core logic below please don't change these until you are pro :D -- starts
    //----------------------------------------------------------------------------------------------
    // forced connection methods

    private class Executor : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                RUN_RUNNABLE -> if (msg.obj is java.lang.Runnable) {
                    (msg.obj as java.lang.Runnable).run()
                }
            }
        }

        fun execute(task: java.lang.Runnable?) {
            sendMessage(obtainMessage(RUN_RUNNABLE, task))
        }

        fun cancel(task: java.lang.Runnable?) {
            removeMessages(RUN_RUNNABLE, task)
        }

        companion object {
            private const val RUN_RUNNABLE = 0
        }
    }

    private fun startForceConnectionTimer(duration: Long?) {
        duration?.let {
            timer?.cancel()
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    handler.post(forceConnectionRunnable)
                }
            }, it, it)
        }
    }

    private val forceConnectionRunnable = Runnable {
        executor.cancel(forceReconnectTask)
        executor.execute(forceReconnectTask)
    }

    private val forceReconnectTask = Runnable {
        try {
            LOGS.d(TAG, "Checking connection please wait")
            if (!sessionManager.transferInProgress) {
                checkValidateConnection()
            } else {
                LOGS.d(TAG, "Ignoring checking connection> transfer in progress")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    ///// Bluetooth connection methods
    private var runningBluetoothReconnectTask: ScheduledFuture<*>? = null

    private val bluetoothReconnectRunnable: Runnable by lazy {
        Runnable {
            if (statusFailedConnection) {
                LOGS.i(TAG, "Reconnecting to device")
                checkValidateConnection()
            } else {
                LOGS.i(TAG, "Reconnecting to device removing callback")
                runningBluetoothReconnectTask?.cancel(true)
                runningBluetoothReconnectTask = null
            }
        }
    }

    private fun getTimeFormat(): String {
        var savedTimeFormat = localDataStore.getTimeFormat()
        if (savedTimeFormat.isNullOrEmpty()) {
            savedTimeFormat = TimeFormats.HOURS_12.type
            localDataStore.setTimeFormat(savedTimeFormat)
        }
        return savedTimeFormat
    }
    //--------------------------------------------------------------------------------------------
    // Connection core logic above please don't change these until you are pros :D -- ends
    //----------------------------------------------------------------------------------------------


    private fun setQueryObserver() {
        sessionManager.hibernateWatchService.observe(this) {
            it.getContent()?.let {
                hibernateService()
            }
        }


        sessionManager.deviceQueryAction.observe(this) {
            val connectedDevice = localDataStore.getConnectedDevice() ?: return@observe
            val queryAction = queryHandler.getQueryActions(connectedDevice) ?: return@observe
            ServiceUtil.setQueryMethod(it, queryAction)
        }

        sessionManager.updateDeviceQueryAction.observe(this) {
            try {
                val updateAction =
                    updateDeviceHandler.getQueryActions(localDataStore.getConnectedDevice()!!)

                if (updateAction == null) {
                    LOGS.d(TAG, "Update Data Action is null")
                }

                if (updateAction == null) {
                    LOGS.d(
                        TAG,
                        "updateDeviceHandler.getQueryActions(localDataStore.getConnectedDevice()!!) is null"
                    )
                    return@observe
                }
                ServiceUtil.setUpdateMethods(it, updateAction)
                when (it) {
                    is UpdateDeviceAction.SetIncomingCallInfo -> {
                        if (callHandler.isPhoneMuted) {
                            callHandler.isPhoneMuted = false
                            callHandler.silenceRinger(callHandler.isPhoneMuted)
                        }
                        updateAction.setIncomingCallInfo(it.incomingCall)
                        localDataStore.getConnectedDevice()?.let { device ->
                            if (!device.deviceType.equals(DeviceType.COLORFIT_VISION.deviceType)) {
                                sessionManager.sendQueryAction(QueryAction.GetCustomReplies)
                            }
                        }

                    }

                    is UpdateDeviceAction.SetWatchFace -> {
                        sessionManager.transferInProgress = true
                        AppLogs.sendAppLogs("Transferring WatchFace Started ${it.watchFace.id}")
                        updateAction.setWatchFace(it.watchFace)
                    }

                    is UpdateDeviceAction.UpdateFirmware -> {
                        sessionManager.transferInProgress = true
                        AppLogs.sendAppLogs("OTA transfer Started")
                        updateAction.updateFirmware(it.fileUri)
                    }

                    is UpdateDeviceAction.VisionUpdateFirmware -> {
                        sessionManager.transferInProgress = true
                        updateAction.visionUpdateFirmware(it.visionOtaFiles)
                    }

                    is UpdateDeviceAction.SetWatchFaceCustom -> {
                        sessionManager.transferInProgress = true
                        AppLogs.sendAppLogs("Custom Watchface transfer started")
                        updateAction.setWatchFaceCustom(it.watchFace)

                    }

                    is UpdateDeviceAction.SetDiyWatchFaceCustom -> {
                        sessionManager.transferInProgress = true
                        AppLogs.sendAppLogs("Custom Watchface transfer started")
                        updateAction.setDiyWatchFaceCustom(it.watchFace)

                    }

                    else -> {}
                }
            } catch (exp: Exception) {
                exp.printStackTrace()
            }

        }

        sessionManager.userActivityAction.observe(this) {
            val connectedDevice = localDataStore.getConnectedDevice() ?: return@observe
            val activityAction =
                userActivityHandler.getUserActivityActions(connectedDevice) ?: return@observe
            ServiceUtil.setActivityMethods(it, activityAction)
        }

        sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.SyncStock -> {
                    startService(Intent(this, StockUpdateService::class.java))
                }

                else -> {}
            }
        }

        sessionManager.userActivityCallback.observe(this) { event ->
            event.peekContent()?.let {
                when (it) {
                    is UserActivityCallback.SportsModeStatusChange -> {
                        LOGS.d(TAG, "SportsModeStatusChange " + it.syncDataStatus)
                        handleSportsModeStatus(it.syncDataStatus.status)
                    }

                    is UserActivityCallback.RealStepsDataObtained -> {
                        LOGS.d(TAG, "SportsModeStatusChange " + it.stepsData)
                        GlobalScope.launch {
                            syncRepository.saveStepsData(it.stepsData)
                                .collect { resource ->
                                    when (resource) {
                                        is CacheResult.Success -> {
                                            LOGS.d(TAG, "SyncDataWork: steps ${resource.value}")
                                            sessionManager.setShowSyncOfflineData(
                                                Event(
                                                    HealthOverviewDataType.STEPS
                                                )
                                            )
                                        }

                                        is CacheResult.GenericError -> {
                                            LOGS.e(TAG, "SyncDataWork: Error $it")

                                        }
                                    }
                                }
                        }

                    }

                    is UserActivityCallback.SportsModeDataObtained -> {
                        LOGS.d(TAG, "SportsModeDataObtained ConnectionService")

                        /*if (handlerActivity.hasMessages(100)) {
                            LOGS.d(TAG, "SportsModeDataObtained ConnectionService Running Activity")

                        } else {*/

                        var shouldAdd = false
                        if (!it.sportsModeRequestList.responseType.isNullOrEmpty()) {
                            shouldAdd = !(it.sportsModeRequestList.responseType == "update" ||
                                    it.sportsModeRequestList.responseType == "pause" ||
                                    it.sportsModeRequestList.responseType == "resume")
                        } else {
                            shouldAdd = true
                        }

                        if (shouldAdd) {
                            LOGS.d(
                                TAG,
                                "ActivityFragment Size : ${it.sportsModeRequestList.activities?.size}"
                            )
                            if (!it.sportsModeRequestList.activities.isNullOrEmpty()) {

                                sessionManager.saveSportsActivities(it.sportsModeRequestList.activities)




                                it.sportsModeRequestList.activities?.forEach { act ->

                                    val startTime = DateFormats.formatActivityTime6(act.time)
                                    var endTime = "0"
                                    if (act.duration != null && act.time != null) {
                                        endTime = DateFormats.addMinutes2(
                                            act.time!!,
                                            act.duration!!.toInt()
                                        ).toString()


                                    }
                                    val count = localDataStore.getActivitySyncCount()
                                    val newCount = count + 1
                                    localDataStore.setActivitySyncCount(newCount)
                                    val activityName: String =
                                        if (act.activityType.isNullOrEmpty()) {
                                            act.type.toString()
                                        } else
                                            act.activityType.toString()
                                    sessionManager.logInsiderAppEvent(
                                        InsiderAppEvents.ACTIVITY_SYNC,
                                        HashMap<String, Any>().apply {
                                            this["activity_name"] = activityName
                                            this["activity_starttime"] = startTime
                                            this["activity_endtime"] = endTime
                                            this["activity_duration"] = act.duration.toString()
                                            this["activity_caloriesburnt"] = act.calories.toString()
                                        }
                                    )
                                }
                            }
                        }

                        /*}*/

                    }

                    is UserActivityCallback.SportsModeDataObtainedGPS -> {
                        LOGS.d(TAG, "SportsModeDataObtainedGPS ConnectionService")
                        LOGS.d(
                            TAG,
                            "ActivityFragment",
                            "Size : ${it.sportsModeResponse.activities?.size}"
                        )
                        if (!it.sportsModeResponse.activities.isNullOrEmpty()) {
                            sessionManager.saveSportsActivities(it.sportsModeResponse.activities)

                            it.sportsModeResponse.activities?.forEach { act ->
                                val startTime = DateFormats.formatActivityTime6(act.time)
                                var endTime = "0"
                                if (act.duration != null && act.time != null) {
                                    endTime = DateFormats.addMinutes2(
                                        act.time!!,
                                        act.duration!!.toInt()
                                    ).toString()


                                }
                                val count = localDataStore.getActivitySyncCount()
                                val newCount = count + 1
                                localDataStore.setActivitySyncCount(newCount)
                                val activityName = if (act.activityType.isNullOrEmpty()) {
                                    act.type.toString()
                                } else
                                    act.activityType.toString()

                                sessionManager.logInsiderAppEvent(
                                    InsiderAppEvents.ACTIVITY_SYNC,
                                    HashMap<String, Any>().apply {
                                        this["activity_name"] = activityName
                                        this["activity_starttime"] = startTime
                                        this["activity_endtime"] = endTime
                                        this["activity_duration"] = act.duration.toString()
                                        this["activity_caloriesburnt"] = act.calories.toString()
                                    }
                                )
                            }
                        }
                    }

                    else -> {}
                }
            }

        }

        sessionManager.syncCompleted.observe(this) {
            it?.peekContent()?.let {
                val status = it.status
                if (status == EventConstants.UPDATE_STATUS_SUCCESS) {
                    updateNotification()
                }
            }

        }

        sessionManager.reloadNotification.observe(this) {
            it.getContent()?.let {
                updateNotification()
            }
        }

        sessionManager.forceDisconnect.observe(this) {
            it.getContent()?.let {

                onDisconnectSuccess()
            }
        }
    }


    private fun updateNotification() {
        GlobalScope.launch(Dispatchers.IO) {
            val stepsData = userRepository.getTodayStepsData()
            stepsData?.let {
                checkGoalNotification(it)
            }
            val lastSyncTime = sessionManager.getLastSyncTime()?.let {
                DateFormats.convertTimestampToDate(
                    it,
                    SimpleDateFormat("h:mm a", Locale.ENGLISH).apply {
                        timeZone = TimeZone.getDefault()
                    })
            } ?: ""


            withContext(Dispatchers.Main) {
                val notification = NotificationUtil.changeNotificationContent(
                    Device.SMARTWATCH,
                    this@ConnectionService,
                    time = lastSyncTime
                )
                mLastNotification = notification
                mLastNotification?.let {
                    startForeground(NOTIFICATION_ID_MAIN, it)
                }
            }

        }
    }

    private fun checkGoalNotification(stepsData: StepsData) {
        try {
            //val lastGoalCompletePostedTimeStamp = localDataStore.getNotificationCompleteTimeStamp()
            val lastGoal80PostedTimeStamp = localDataStore.get80NotificationTimeStamp()

            val goal80Date = DateFormats.convertTimestampToDate(
                lastGoal80PostedTimeStamp, SimpleDateFormat(
                    "dd/MM/yyyy",
                    DateFormats.defaultLocale
                )
            )
//            val goalCompleteDate = DateFormats.convertTimestampToDate(
//                lastGoalCompletePostedTimeStamp, SimpleDateFormat(
//                    "dd/MM/yyyy",
//                    DateFormats.defaultLocale
//                )
//            )
            val currentDate = DateFormats.convertTimestampToDate(
                System.currentTimeMillis(), SimpleDateFormat(
                    "dd/MM/yyyy",
                    DateFormats.defaultLocale
                )
            )

            if (goal80Date != currentDate && lastGoal80PostedTimeStamp != 0L) {
                localDataStore.set80NotificationStatus(false)
                // localDataStore.setGoalCompleteNotificationStatus(false)
                localDataStore.clearNotificationGoalTimeStamp()
            }

//            if (goalCompleteDate != currentDate && lastGoalCompletePostedTimeStamp != 0L) {
//                localDataStore.set80NotificationStatus(false)
//                //localDataStore.setGoalCompleteNotificationStatus(false)
//                localDataStore.clearNotificationGoalTimeStamp()
//            }

            val stepGoal = getUser()?.userGoals?.stepGoal?.toString() ?: "5000"
            val steps80 = (stepGoal.toInt() * 90) / 100

            val remainingSteps = stepGoal.toInt() - stepsData.totalSteps

//            val isGoalNotificationPosted = localDataStore.getGoalCompleteNotificationStatus()
//            if (isGoalNotificationPosted) {
//                return
//            }

            sessionManager.logInsiderAppEvent(
                InsiderAppEvents.REWARD_STEPS_GOAL,
                HashMap<String, Any>().apply {
                    this["steps_goal"] = stepGoal
                })
            sessionManager.logInsiderAppEvent(
                InsiderAppEvents.REWARD_STEPS_REMAINING,
                HashMap<String, Any>().apply {
                    this["steps_remaining"] = remainingSteps
                })
            sessionManager.logInsiderAppEvent(
                InsiderAppEvents.REWARD_STEPS_CURRENT,
                HashMap<String, Any>().apply {
                    this["steps_current"] = stepsData.totalSteps
                })

            if (stepsData.totalSteps >= steps80 && stepsData.totalSteps < stepGoal.toInt()) {
                val is80NotificationPosted = localDataStore.get80NotificationStatus()
                if (is80NotificationPosted) {
                    return
                }
                localDataStore.set80NotificationStatus(true)
                NotificationUtil.showLocalNotification(
                    this@ConnectionService, "You are so close to crushing your daily goal",
                    "Only $remainingSteps steps left to complete your daily step goal. You can do it."
                )
            }
//            else if (stepsData.totalSteps > stepGoal.toInt()) {
//                localDataStore.setGoalCompleteNotificationStatus(true)
//                NotificationUtil.showLocalNotification(
//                    this@ConnectionService,
//                    "Wohoo! Daily step goal achieved",
//                    "You have completed your daily step goal of $stepGoal. Excellent work."
//                )
//                localDataStore.setGoalCompletionCount(localDataStore.getGoalCompletionCount() + 1)
//            }
        } catch (exp: Exception) {
            //issue in conversion
        }

    }

    private fun getBluetoothAdapter() =
        (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    private val bluetoothConnectionStatusChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            var btStatus: String = ""
            action?.let {
                when (it) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        //  LOGS.d(TAG, "On Device Found ${device?.name}")
                        localDataStore.getConnectedDevice()?.let { colorFitDevice ->
                            if (device != null && device.address == colorFitDevice.address) {
                                if (getBluetoothAdapter().isDiscovering) {
                                    getBluetoothAdapter().cancelDiscovery()
                                }
                                checkValidateConnection()
                            }
                        }
                    }

                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        val state =
                            intent.getIntExtra(
                                BluetoothAdapter.EXTRA_STATE,
                                BluetoothAdapter.ERROR
                            )
                        LOGS.d(TAG, "Bluetooth State Change", state.toString())

                        if (state == BluetoothAdapter.STATE_ON) {
                            AppLogs.sendAppLogs("ConnectionService : Bluetooth turned ON")
                            LOGS.i(TAG, "STATE_ON")
                            sessionManager.setBluetoothState(true)
                            btStatus = "bt on"
                        }
                        if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_TURNING_OFF) {
                            LOGS.i(TAG, "STATE_OFF")
                            AppLogs.sendAppLogs("ConnectionService : Bluetooth turned OFF")
                            statusFailedConnection = true


                            val connectedDevice = localDataStore.getConnectedDevice()

                            connectedDevice?.let { device ->
                                if (watchesSDK.getWatchType(device) == SDKWatchType.SDK_QUBE) {
                                    connectionDataAction?.let { connectionAction ->
                                        if (!CommonGlobals.isWatchDataUpdating) {
                                            connectionAction.disconnectFromService()
                                        }

                                    }
                                } else {
                                    if (sessionManager.transferInProgress) {
                                        failTransfer()
                                    } else {
                                    }
                                    if (PhoneRinger.isRinging()) {
                                        PhoneRinger.enableRing(false)
                                    } else {
                                    }
                                }
                            }

                            sessionManager.setBluetoothState(false)

                            btStatus = "bt off"
                        } else {
                            LOGS.d(TAG, "Bluetooth State Change", state.toString())
                        }
                        checkValidateConnection()
                    }

                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        tryCatch {
                            if (intent.hasExtra(BluetoothDevice.EXTRA_DEVICE)) {
                                statusFailedConnection = true
                                val device: BluetoothDevice? =
                                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                                localDataStore.getConnectedDevice()?.let { colorFitDevice ->

                                    LOGS.d(TAG, "On Device Disconnected")
                                    if (device != null && device.address == colorFitDevice.address) {
                                        AppLogs.sendAppLogs("ConnectionService: Device Disconnected")
                                        checkValidateConnection()
                                    }
                                }
                            }

                        }

                    }

                    else -> {

                    }
                }
            }

            if (btStatus.isNotEmpty()) {
                sessionManager.logEvent(
                    InsiderAppEvents.ConnectionEvents.wconnect_bt_status,
                    btStatus
                )
            }


        }
    }

    private fun failTransfer() {
        updateDeviceCallback.onUpdateDataReceived(
            UpdateDeviceDataCallback.FirmwareUpgradeProgress(
                WatchUpdateStatus(status = UpdateStatus.ERROR)
            )
        )
        updateDeviceCallback.onUpdateDataReceived(
            UpdateDeviceDataCallback.CustomizeWatchFaceProgress(
                WatchUpdateStatus(status = UpdateStatus.ERROR)
            )
        )
    }


    override fun onTaskRemoved(rootIntent: Intent) {
        LOGS.d("$TAG onTaskRemoved")
        val restartServiceIntent = Intent(this, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        val restartServicePendingIntent: PendingIntent =
            PendingIntent.getService(
                this,
                8285,
                restartServiceIntent,
                PendingIntent.FLAG_MUTABLE
            )
        val alarmService: AlarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.cancel(restartServicePendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            when {
                alarmService.canScheduleExactAlarms() -> {
                    LOGS.d("$TAG  permission granted")
                    // Use to showcase the UX improvements in Android12
                    alarmService.setExact(
                        AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime() + 1000,
                        restartServicePendingIntent
                    )
                }

                else -> {
                    sessionManager.logCustomCrashlyticsEvents("Alarm", "no permission")
                    // go to exact alarm settings
                    LOGS.d("$TAG no permission ")
                }
            }
        } else {
            alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent
            )
        }


    }

    private val queryCallback = object : IQueryDataCallback {
        override fun onQueryDataReceived(queryCallback: QueryCallback) {

            when (queryCallback) {
                is QueryCallback.BatteryDataObtained -> {
                    val percent = queryCallback.batteryData.percentage ?: 0
                    sessionManager.batterPercent.postValue(percent)
                    LOGS.d(TAG, "battery Data testing : $percent")
                    val watchType = watchesSDK.getWatchType()
                    if (watchType == SDKWatchType.SDK_ZH || watchType == SDKWatchType.SDK_EVOLVE) {
                        batteryNotificationUtils.handleNotification(
                            percent,
                            queryCallback.batteryData.isCharging,
                            Device.SMARTWATCH
                        )
                    }

                    watchDataStore.updateBatteryPercent(percent)
                }

                is QueryCallback.OpenCameraActivity -> {
                    if (!sessionManager.isCameraShutterActivityOpened) {
                        startActivity(CameraShutterActivity.getStartIntent(this@ConnectionService))
                    }
                }

                is QueryCallback.OpenFindMyPhoneActivity -> {
                    val isFindMyPhoneNotificationOn =
                        localDataStore.getExperimentalSettings().findMyPhoneNotification
                    if (queryCallback.isRinging) {
                        PhoneRinger.enableRing(true)
                        if (isFindMyPhoneNotificationOn) {
                            NotificationUtil.showFindPhoneNotification(
                                this@ConnectionService
                            )
                        }
                        //startActivity(FindMyPhoneActivity.getStartIntent(this@ConnectionService))

                    } else {
                        PhoneRinger.enableRing(false)
                        if (isFindMyPhoneNotificationOn) {
                            NotificationUtil.removeNotification(
                                this@ConnectionService,
                                NotificationEventsClass.FIND_PHONE_NOTIFICATION_ID
                            )
                        }

                    }

                }

                is QueryCallback.FirmwareVersionObtained -> {
                    val version = queryCallback.deviceFirmware.version ?: ""
                    LOGS.d(
                        TAG,
                        "FirmwareVersionObtained : ${WatchInfoGlobals.firmwareVersionNumber} ${WatchInfoGlobals.firmwareDeviceId}"
                    )
                    sessionManager.firmwareVersion = version
                    watchDataStore.updateFirmwareVersion(version)
                    watchDataStore.saveDeviceFirmwareDetails(
                        WatchFirmwareDetails(
                            WatchInfoGlobals.firmwareVersionNumber,
                            WatchInfoGlobals.firmwareDeviceId
                        )
                    )

                    /*if (initDefaultValues) {
                        LOGS.d(TAG, "Settings Default values")
                        initDefaultValue()
                    }*/
                }

                is QueryCallback.UpdateCallStatus -> {
                    val isCallAlertEnabled = localDataStore.isCallAlertEnabled()
                    if (isCallAlertEnabled || queryCallback.forceCallDisconnect) {
                        callHandler.updateCallStatus(queryCallback.status)
                    }

                }

                is QueryCallback.OnMusicEventChanged -> {
                    MusicPlayerControlsHandler.onEvent(queryCallback.event)
                }

                is QueryCallback.MuteDevice -> {
                    if (callHandler.checkRingerIsOn()) {
                        callHandler.isPhoneMuted = true
                        callHandler.silenceRinger(callHandler.isPhoneMuted)
                    }
                }

                is QueryCallback.AgpsRequestState -> {
                    LOGS.d(
                        TAG,
                        "${TAG}: AgpsRequestState  ${queryCallback.shouldUpdate}"
                    )
                    sessionManager.setAGPSState(queryCallback.shouldUpdate)

                }

                else -> {}
            }
            sessionManager.setQueryCallback(queryCallback)

        }
    }

    private val updateDeviceCallback = object : IUpdateDeviceDataCallback {
        override fun onUpdateDataReceived(dataCallback: UpdateDeviceDataCallback) {
            when (dataCallback) {
                is UpdateDeviceDataCallback.UpdateCallStatus -> {
                    callHandler.updateCallStatus(dataCallback.success)
                }

                is UpdateDeviceDataCallback.MuteDevice -> {
                    if (callHandler.checkRingerIsOn()) {
                        callHandler.isPhoneMuted = true
                        callHandler.silenceRinger(callHandler.isPhoneMuted)
                    }
                }

                is UpdateDeviceDataCallback.WeatherUpdateRequest -> {

                    LOGS.d(TAG, "WeatherWork pending")
                }

                is UpdateDeviceDataCallback.OnMusicEventChanged -> {
                    MusicPlayerControlsHandler.onEvent(dataCallback.event)
                }

                is UpdateDeviceDataCallback.CustomizeWatchFaceProgress -> {
                    if (dataCallback.watchUpdateStatus.status == UpdateStatus.COMPLETED ||
                        dataCallback.watchUpdateStatus.status == UpdateStatus.ERROR ||
                        dataCallback.watchUpdateStatus.status == UpdateStatus.BATTERY_LOW
                    ) {
                        sessionManager.transferInProgress = false
                    }
                    if (dataCallback.watchUpdateStatus.status == UpdateStatus.COMPLETED) {
                        AppLogs.sendAppLogs("WatchFace Transfer Completed")
                        val count = localDataStore.getCustomWatchFaceTransferCount()
                        val newCount = count + 1
                        localDataStore.setCustomWatchFaceTransferCount(newCount)
                    }
                }

                is UpdateDeviceDataCallback.FirmwareUpgradeProgress -> {
                    if (dataCallback.watchUpdateStatus.status == UpdateStatus.COMPLETED ||
                        dataCallback.watchUpdateStatus.status == UpdateStatus.ERROR ||
                        dataCallback.watchUpdateStatus.status == UpdateStatus.BATTERY_LOW
                    ) {
                        sessionManager.transferInProgress = false
                    }
                    if (dataCallback.watchUpdateStatus.status == UpdateStatus.COMPLETED) {
                        AppLogs.sendAppLogs("OTA Transfer Success")
                    }
                }

                else -> {}
            }
            sessionManager.setUpdateDeviceCallback(dataCallback)
        }
        /*override fun onQueryDataReceived(queryCallback: QueryCallback) {

            when(queryCallback){
                is QueryCallback.BatteryDataObtained -> {
                    LOGS.d("battery Data testing : ${queryCallback.batteryData.percentage}")
                }
            }
            sessionManager.setQueryCallback(queryCallback)

        }*/
    }

    private val userActivityCallback = object : IUserActivityDataCallback {
        override fun onUserActivityDataReceived(userActivityCallback: UserActivityCallback) {
            LOGS.d(TAG, "Received data $userActivityCallback")

            when (userActivityCallback) {
                is UserActivityCallback.StepsDataObtained -> {
                    LOGS.d(TAG, "steps Data testing : ${userActivityCallback.stepsData.totalSteps}")
                }

                is UserActivityCallback.SportsModeStatusChange -> {
                    //LOGS.d("SportsModeStatusChange " + userActivityCallback.syncDataStatus)
                    //handleSportsModeStatus(userActivityCallback.syncDataStatus.status)
                }

                is UserActivityCallback.SportsModeDataObtained -> {
                    //LOGS.d("SportsModeDataObtained6 " + userActivityCallback.sportsModeRequestList)
                    //sessionManager.saveSportsActivities(userActivityCallback.sportsModeRequestList.activities)
                }

                else -> {}
            }
            sessionManager.setUserActivityCallback(userActivityCallback)


        }
    }

    private fun handleSportsModeStatus(status: String?) {
        when (status) {
            "start" -> {
                startTimer()
            }

            "pause" -> {
                stopTimer()
            }

            "resume" -> {
                startTimer()
            }

            "stop" -> {
                stopTimer()
                sessionManager.setSportsModeRequest(null)
            }
        }
    }

    private fun startTimer() {
        stopTimer()
        handlerActivity.postDelayed(runnableCode, 1000)
    }

    private fun stopTimer() {
        handlerActivity.removeMessages(100)
        handlerActivity.removeCallbacks(runnableCode)
    }

    private fun registerTimeChangeReceiver() {
        try {
            val timeIntentFilter = IntentFilter().apply {
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
                addAction(Intent.ACTION_TIME_CHANGED)
            }

            registerReceiver(timeChangedReceiver, timeIntentFilter)
        } catch (exp: Exception) {
            exp.printStackTrace()
        }
    }

    private val timeChangedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == Intent.ACTION_TIME_CHANGED || action == Intent.ACTION_TIMEZONE_CHANGED) {
                LOGS.d(TAG, "ontimeChangedReceiver time changed $action")
//                sessionManager.setDateChanged(Event(true))
                sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.SetDeviceDateTime(
                        Calendar.getInstance(), TimeFormat(timeFormat = getTimeFormat())
                    )
                )

            }
        }
    }
}

//private enum class ConnectionMode {
//    FORCED,
//    BLUETOOTH
//}