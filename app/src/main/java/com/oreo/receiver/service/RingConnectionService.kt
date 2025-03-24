package com.oreo.receiver.service

import android.Manifest
import android.app.AlarmManager
import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.google.gson.JsonObject
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.dataConverter.DataConverter
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.luna.R
import com.noisefit.data.repository.implementation.DELETE_DB_DAYS
import com.noisefit.session.SessionManager
import com.noisefit.util.ApplicationUtils
import com.noisefit.util.FirebaseCrashlyticsUtils
import com.noisefit.util.SportUtils
import com.noisefit.util.moveToServer.BatteryNotificationUtils
import com.noisefit.util.notif.NotificationEventsClass
import com.noisefit.util.notif.NotificationUtil
import com.noisefit.util.notif.NotificationUtil.NOTIFICATION_ID_MAIN_OREO
import com.noisefit.watch.ApplicationHandler
import com.noisefit.watch.ConnectionHandler
import com.noisefit.watch.DeviceQueryHandler
import com.noisefit.watch.UpdateDeviceHandler
import com.noisefit.watch.UserActivityHandler
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.constants.ConnectionEventsConstants
import com.noisefit_commans.constants.SyncEvents
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.db.abstraction.LocationDataSource
import com.noisefit_commans.data.enums.Actions
import com.noisefit_commans.data.enums.ServiceState
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.data.model.RecordedWorkoutData
import com.noisefit_commans.data.model.User
import com.noisefit_commans.interfaces.IQueryDataCallback
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.connection.BindState
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.connection.ConnectionCallbacks
import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.interfaces.data.IUserActivityDataCallback
import com.noisefit_commans.interfaces.data.UserActivityCallback
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.interfaces.device_data.IUpdateDeviceDataCallback
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.location.LocationService2
import com.noisefit_commans.location.LocationUtils2
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceFirmware
import com.noisefit_commans.models.DeviceUnits
import com.noisefit_commans.models.ManualMeasureType
import com.noisefit_commans.models.StepsData
import com.noisefit_commans.models.TimeFormat
import com.noisefit_commans.models.TimeFormats
import com.noisefit_commans.models.UpdateStatus
import com.noisefit_commans.models.WatchFirmwareDetails
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.CallHandler
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LOW_VIBRATION
import com.noisefit_commans.utils.ServiceUtil
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.db.OreoDataBase
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import com.oreo.data.repository.abstraction.OreoSyncRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import com.oreo.receiver.workManager.HealthOverviewDataType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
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

@AndroidEntryPoint
class RingConnectionService
@Inject
constructor() : LifecycleService() {

    private var timer: Timer? = null
    private val executor: Executor = Executor()

    private val TAG = RingConnectionService::class.java.simpleName

    @Inject
    lateinit var deviceRepository: DeviceRepository

    @Inject
    lateinit var batteryNotificationUtils: BatteryNotificationUtils

    @Inject
    lateinit var dataUnitConverter: DataUnitConverter

    @Inject
    lateinit var dataConverter: DataConverter

    @Inject
    lateinit var database: OreoDataBase

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var ringDataStore: RingDataStore

    @Inject
    lateinit var sportUtils: SportUtils

    @Inject
    lateinit var syncRepository: OreoSyncRepository

    @Inject
    lateinit var locationDataSource: LocationDataSource

    @Inject
    lateinit var userActivityRepository: OreoUserActivityRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var userHealthDataDataSource: OreoUserHealthDataDataSource

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
    lateinit var callHandler: CallHandler

    @Inject
    lateinit var vibrationUtils: VibrationUtils

    @Inject
    lateinit var resourcesProvider: ResourcesProvider

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

    var mLastNotification: Notification? = null

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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID_MAIN_OREO,
                        it,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                    )
                } else {
                    startForeground(
                        NOTIFICATION_ID_MAIN_OREO,
                        it
                    )
                }
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
                        val device = ringDataStore.getRingDevice()
                        isStopServiceCalled = true
                        /**
                         * Added to stop issue
                         * Context.startForegroundService() did not then call Service.startForeground()
                         * need testing
                         */
                        mLastNotification = NotificationUtil.getNotification(
                            this,
                            resourcesProvider = resourcesProvider
                        )

                        mLastNotification?.let {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                startForeground(
                                    NOTIFICATION_ID_MAIN_OREO,
                                    it,
                                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                                )
                            } else {
                                startForeground(
                                    NOTIFICATION_ID_MAIN_OREO,
                                    it
                                )
                            }
                        }

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
        } catch (e: ForegroundServiceStartNotAllowedException) {
            e.printStackTrace()

        }

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        LOGS.i(TAG, "The service has been created")
        try {

            firebaseCrashlyticsUtils.setCrashlyticsUserProperty()
            if (ringDataStore.getRingDevice() == null) {
                mLastNotification = NotificationUtil.getNotification(
                    this,
                    resourcesProvider = resourcesProvider
                )
                mLastNotification?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(
                            NOTIFICATION_ID_MAIN_OREO,
                            it,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                        )
                    } else {
                        startForeground(
                            NOTIFICATION_ID_MAIN_OREO,
                            it
                        )
                    }
                }
                isStopServiceCalled = true
                stopSelf()
            } else {
                /**
                 * Added to stop issue
                 * Context.startForegroundService() did not then call Service.startForeground()
                 * need testing
                 */
                mLastNotification = NotificationUtil.getNotification(
                    this,
                    resourcesProvider = resourcesProvider
                )

                mLastNotification?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(
                            NOTIFICATION_ID_MAIN_OREO,
                            it,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                        )
                    } else {
                        startForeground(
                            NOTIFICATION_ID_MAIN_OREO,
                            it
                        )
                    }

                }
            }

            if (!hasRequiredBluetoothPermission()) {

                stopServiceAndShowNotification()
            }
            showNotification(getString(R.string.text_connecting))
            updateAlarmManager()
        } catch (e: ForegroundServiceStartNotAllowedException) {
            e.printStackTrace()
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
                8286,
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
            database.clearAllTables()//TODO delete oreo tables
        }
    }

    private fun showNotification(title: String) {
        updateNotification()
    }

    override fun onDestroy() {
        LOGS.d(TAG, "On Destroy Called $isStopServiceCalled")
        AppLogs.sendAppLogs("RingConnectionService : onDestroy(), isStopServiceCalled : $isStopServiceCalled")
        if (!isStopServiceCalled) {
            if (hasRequiredBluetoothPermission()) {
                val restartServiceIntent = Intent(this, this.javaClass)
                restartServiceIntent.setPackage(packageName)
                val restartServicePendingIntent = PendingIntent.getService(
                    applicationContext,
                    8286,
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
                    8286,
                    serviceIntent,
                    PendingIntent.FLAG_MUTABLE
                )
            val am = getSystemService(ALARM_SERVICE) as AlarmManager
            am.cancel(pendingIntent)
        }
        unregisterReceivers()

        super.onDestroy()
        LOGS.i(TAG, "The service has been destroyed")
        localDataStore.setServiceState(ServiceState.STOPPED)//TODO change for ring
    }

    private fun startService(initDefault: Boolean) {
        initDefaultValues = initDefault
        LOGS.i(TAG, "Before setConnetionMode")
        updateNotification()
        if (isServiceStarted) return
        LOGS.i(TAG, "Starting the foreground service task")
        isServiceStarted = true
        localDataStore.setServiceState(ServiceState.STARTED)//TODO change for ring
        registerBluetoothReceivers()
        registerTimeChangeReceiver()
        setConnection()
        setQueryObserver()
        setLocationObserver()
    }

    private fun setLocationObserver() {
        sessionManager.updateRingLocation.observe(this) {
            it.getContent()?.let {
                LocationUtils2.startLocationService()
            }
        }

        LocationService2.locationBroadCastFindMyRing.observe(this) {
            it.getContent()?.let {
                updateRingLocation(it)
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
            startForceConnectionTimer(10 * 60 * 1000)
        } else {
            stopServiceAndShowNotification()
        }
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

    private fun stopService(device: ColorFitDevice?) {
        LOGS.i(TAG, "Stopping the foreground service $isServiceStarted")

        sessionManager.forceOtaFlowRunning = false
        sessionManager.forceOtaResponseRing = null

        if (!isServiceStarted) {
            LOGS.i(TAG, "Service has been stopped -- Ignoring data")
            return
        }

        device?.let { colorFitDevice ->
            removeWatchTokenFromServer(colorFitDevice.address)
            LOGS.i(TAG, "Stopping the foreground service - inside")
            applicationHandler.unInitSdks(colorFitDevice)
            connectionHandler.getConnectionActions(colorFitDevice)?.let { connectionDataActions ->
                connectionDataActions.removeCallbacks()
            }
            deleteDB()
            vibrationUtils.vibrate(LOW_VIBRATION)
            ApplicationUtils.clearJobs(this)
            timer?.cancel()
            applicationContext.cacheDir?.deleteRecursively()
        } ?: LOGS.d(TAG, "getConnectedDevice is null")
        unregisterReceivers()
        ringDataStore.deleteOngoingRecordWorkout()
        reconnectHandler.removeCallbacks(bluetoothReconnectRunnable)
        isServiceStarted = false
        localDataStore.setServiceState(ServiceState.STOPPED)
        WatchInfoGlobals.resetData()
        sessionManager.clearSessionManager()
        ringDataStore.cleaNewOtaVersion()
        sessionManager.setConnectStateRing(ConnectState.UnPaired())
        stopSelf()
    }

    private fun removeWatchTokenFromServer(macAddress: String?) {


        if (macAddress.isNullOrEmpty()) {
            LOGS.d("removeWatchTokenFromServer macAddress null")
            return
        }

        GlobalScope.launch {
            deviceRepository.removeWatchTokenFromServer(macAddress).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {

                    }

                    is Resource.Loading -> {

                    }

                    is Resource.NetworkError -> {

                    }

                    is Resource.Success -> {

                    }
                }
            }

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
            unregisterReceiver(timeChangedReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private var statusFailedConnection = false


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
        LOGS.d("validateAndSetConnectStateRing SendState : $sendState $connectState $colorFitDevice")


        if (sendState) {
            sessionManager.setConnectStateRing(connectState)
        }
    }

    private fun checkValidateConnection() {
        LOGS.d(TAG, "checkValidateConnection called")
        //checkSportWork()


        val lastSyncTime = ringDataStore.getLastPeriodicDataSyncTime()
        val difference = kotlin.math.abs(DateFormats.getTimeStamp() - lastSyncTime)
        LOGS.d(TAG, "Periodic sync difference $difference")
        if (difference > 1800 * 1000L) {//30 min
            setPeriodicInfo()
            ringDataStore.setLastPeriodicDataSyncTime(DateFormats.getTimeStamp())
        }

        ringDataStore.getRingDevice()?.let { colorFitDevice ->

            sessionManager.setConnectedDeviceRing(colorFitDevice)
            LOGS.d(TAG, "connectionDataAction $connectionDataAction")
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

                                if (ringDataStore.getRingDevice() == null) {
                                    onDisconnectSuccess()
                                    return
                                }
                                showNotification(getString(R.string.text_connecting))
                                var status = "failed"
                                if (!connectState.status.isNullOrEmpty()) {
                                    status = connectState.status!!

                                }

                                LOGS.d("sdfkjsk failed ring connection service - >${connectState.status}")

                                //TODO handling for disconnect case
                                if (connectState.status.equals(
                                        ConnectionEventsConstants.RING_DISCONNECT,
                                        true
                                    )
                                ) {
                                    onRingDisconnected()
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

                                sessionManager.setConnectedDeviceRing(colorFitDevice)
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

                                sessionManager.setConnectedDeviceRing(colorFitDevice)
                                stateConnected(colorFitDevice)

                                setRealTimeDataState()

                            }

                            is ConnectState.DisconnectSuccess -> {


                                LOGS.i("Disconnect success Received")
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

    private fun onRingDisconnected() {
        GlobalScope.launch(Dispatchers.IO) {
            //todo handle is disconnected location already sent
            LOGS.d("sdfkjsk onRingDisconnected")
            sessionManager.updateRingLocation.postValue(Event(true))
        }

    }

    private fun setRealTimeDataState() {
        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetRealTimeDataState(sessionManager.appInForeground)
        )
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
        val device = ringDataStore.getRingDevice()
        ringDataStore.clearConnectedDevice()
        localDataStore.clearConnectedDevice()
        watchDataStore.clearWatchData()
        sessionManager.setConnectedDeviceRing(null)
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
                sessionManager.setConnectStateRing(ConnectState.Connecting(colorFitDevice))
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
                if (sessionManager.connectStateRing.value !is ConnectState.ConnectSuccess) {
                    LOGS.d(TAG, "action.isConnected Connect Success")
                    sessionManager.setConnectStateRing(ConnectState.ConnectSuccess(noiseFitDevice = colorFitDevice))
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
        val lastSyncTime = ringDataStore.getLastInfoFetchTime()
        val difference = kotlin.math.abs(DateFormats.getTimeStamp() - lastSyncTime)
        LOGS.d(TAG, "Info Fetch Difference $difference")
        if (difference > fetchTime * 1000L || sessionManager.batterPercent.value == 0) {
            LOGS.d(TAG, "Info Fetch Difference ask for battery")
            GlobalScope.launch(Dispatchers.IO) {
                val isRunning =
                    ApplicationUtils.isOreoSyncDataWorkerRunning(this@RingConnectionService)

                LOGS.w("queryWatchInfo $fetchTime isOreoSyncDataWorkerRunning $isRunning")
                if (isRunning) {
                    return@launch
                }
                queryHandler.getQueryActions(colorFitDevice)?.queryBatteryPower()
                queryHandler.getQueryActions(colorFitDevice)?.queryFirmwareVersion()
                ringDataStore.setLastInfoFetchTime(DateFormats.getTimeStamp())
            }


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

    private val forceConnectionRunnable = kotlinx.coroutines.Runnable {
        executor.cancel(forceReconnectTask)
        executor.execute(forceReconnectTask)
    }

    private val forceReconnectTask = kotlinx.coroutines.Runnable {
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
        kotlinx.coroutines.Runnable {
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

        sessionManager.deviceQueryAction.observe(this) {
            val connectedDevice = ringDataStore.getRingDevice() ?: return@observe
            val queryAction = queryHandler.getQueryActions(connectedDevice) ?: return@observe
            ServiceUtil.setQueryMethod(it, queryAction)
        }

        sessionManager.updateDeviceQueryAction.observe(this) {
            try {
                val updateAction =
                    updateDeviceHandler.getQueryActions(ringDataStore.getRingDevice()!!)

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
                    is UpdateDeviceAction.UpdateFirmware -> {
                        sessionManager.transferInProgress = true
                        AppLogs.sendAppLogs("OTA transfer Started")
                        updateAction.updateFirmware(it.fileUri)
                    }

                    else -> {}
                }
            } catch (exp: Exception) {
                exp.printStackTrace()
            }

        }

        sessionManager.userActivityAction.observe(this) {
            val connectedDevice = ringDataStore.getRingDevice() ?: return@observe
            val activityAction =
                userActivityHandler.getUserActivityActions(connectedDevice) ?: return@observe
            ServiceUtil.setActivityMethods(it, activityAction)
        }


        sessionManager.userActivityCallback.observe(this) { event ->
            event.peekContent()?.let {
                when (it) {

                    is UserActivityCallback.RealStepsDataObtained -> {
                        LOGS.d(TAG, "SportsModeStatusChange " + it.stepsData)
                    }

                    is UserActivityCallback.AutoSportDataObtained -> {
                        LOGS.d(TAG, "SyncDataWork: onAutoSportData inside")
                        it.data.firstOrNull()?.let {
                            val timeStamp = DateFormats.lastClearDataTimeStamp(DELETE_DB_DAYS)
                            val workoutTimeStamp = it.startTime
                            if (workoutTimeStamp > timeStamp && sessionManager.showLocalNotification()) {
                                NotificationUtil.showWorkoutLocalNotification(
                                    this, it,
                                    resourcesProvider
                                )
                            }
                        }


                        GlobalScope.launch {
                            syncRepository.saveAutoWorkoutData(it.data)
                                .collect { resource ->
                                    when (resource) {
                                        is CacheResult.Success -> {
                                            LOGS.d(
                                                TAG,
                                                "SyncDataWork: onAutoSportData ${resource.value}"
                                            )
                                            sessionManager.setShowSyncOfflineData(
                                                Event(
                                                    HealthOverviewDataType.AUTO_WORKOUT
                                                )
                                            )
                                        }

                                        is CacheResult.GenericError -> {
                                            LOGS.e(TAG, "SyncDataWork: onAutoSportData $it")

                                        }
                                    }
                                }
                        }
                    }

                    is UserActivityCallback.SportsModeDataObtained -> {
                        LOGS.d(TAG, "SportsModeDataObtained RingConnectionService")

                        /*if (handlerActivity.hasMessages(100)) {
                            LOGS.d(TAG, "SportsModeDataObtained RingConnectionService Running Activity")

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

                    is UserActivityCallback.RingUserWorkoutData -> {
                        if (it.data.isNotEmpty()) {
                            saveAndSyncWorkouts(it.data)
                        } else {
                            AppLogs.sendAppLogs("Workouts empty")
                            postWorkout("none")
                        }
                    }

                    is UserActivityCallback.SportsModeDataObtainedGPS -> {
                        LOGS.d(TAG, "SportsModeDataObtainedGPS RingConnectionService")
                        LOGS.d(
                            TAG,
                            "ActivityFragment",
                            "Size : ${it.sportsModeResponse.activities?.size}"
                        )
                        if (!it.sportsModeResponse.activities.isNullOrEmpty()) {


                            /*it.sportsModeResponse.activities?.forEach { act ->
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



                            }*/
                        }
                    }

                    else -> {}
                }
            }

        }

        sessionManager.syncCompleted.observe(this) {
            it?.peekContent()?.let {
                val status = it
                if (status is SyncEvents.Success) {
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

    private fun saveAndSyncWorkouts(workouts: List<RecordedWorkoutData>) {
        GlobalScope.launch(Dispatchers.IO) {
            syncRepository.saveRecordedWorkouts(workouts)
                .collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {

                            syncWorkoutsToServer(workouts)

                        }

                        is CacheResult.GenericError -> {

                        }
                    }
                }
        }
    }

    //TODO convert to worker
    private fun syncWorkoutsToServer(workouts: List<RecordedWorkoutData>) {
        GlobalScope.launch(Dispatchers.IO) {

            val workoutsArray = dataConverter.createRecordedWorkoutArray(workouts)

            if (workoutsArray == null || workoutsArray.isEmpty) {
                val dates = HashSet<String>()
                workouts.forEach { workout ->
                    workout.date?.let { date ->
                        dates.add(date)
                    }
                }
                userHealthDataDataSource.clearDataByDates(dates.toList())
                syncRepository.removeRecordedWorkouts().collect()
                locationDataSource.deleteAll()

                ringDataStore.removeRecordDeleteList()
                AppLogs.sendAppLogs("syncWorkoutsToServer workouts empty")

                postWorkout("none")
                return@launch
            }

            val reqObj = JsonObject()
            reqObj.add("workouts", workoutsArray)



            userActivityRepository.addRecordedWorkout(
                reqObj
            ).collect { resource ->
                when (resource) {

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            val dates = HashSet<String>()
                            workouts.forEach { workout ->
                                workout.date?.let { date ->
                                    dates.add(date)
                                }
                            }

                            val workoutId = dataConverter.getWorkoutId(
                                it,
                                sessionManager.lastOngoingWorkoutTimestamp * 1000L
                            )
                            AppLogs.sendAppLogs("Workout id not found")

                            postWorkout(workoutId ?: "none")

                            sessionManager.saveSportsActivities(
                                dataConverter.getSportModeResponseArray(
                                    workouts
                                )
                            )


                            userHealthDataDataSource.clearDataByDates(dates.toList())
                            syncRepository.removeRecordedWorkouts().collect()
                            locationDataSource.deleteAll()
                            ringDataStore.removeRecordDeleteList()
                            delay(200)
                            sessionManager.reloadTodayData.postValue(
                                Event(true)
                            )
                            sessionManager.forceSyncData.postValue(Event(true))
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    fun postWorkout(workoutId: String) {
        sessionManager.lastOngoingWorkoutTimestamp = 0L
        AppLogs.sendAppLogs("postWorkout $workoutId")

        GlobalScope.launch(Dispatchers.Main) {
            sessionManager.showWorkoutDetails.value = (Event(workoutId))
            sessionManager.showWorkoutDetails.value = Event(null)
        }
    }


    private fun updateNotification() {
        GlobalScope.launch(Dispatchers.IO) {
            val lastSyncTime = sessionManager.getLastSyncTime()?.let {
                DateFormats.convertTimestampToDate(
                    it,
                    SimpleDateFormat("h:mm a", Locale.ENGLISH).apply {
                        timeZone = TimeZone.getDefault()
                    })
            } ?: ""


            withContext(Dispatchers.Main) {
                val notification = NotificationUtil.changeNotificationContent(
                    this@RingConnectionService,
                    resourcesProvider,
                    time = lastSyncTime
                )
                mLastNotification = notification
                mLastNotification?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(
                            NOTIFICATION_ID_MAIN_OREO,
                            it,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                        )
                    } else {
                        startForeground(
                            NOTIFICATION_ID_MAIN_OREO,
                            it
                        )
                    }

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
                    this@RingConnectionService, "You are so close to crushing your daily goal",
                    "Only $remainingSteps steps left to complete your daily step goal. You can do it."
                )
            }
//            else if (stepsData.totalSteps > stepGoal.toInt()) {
//                localDataStore.setGoalCompleteNotificationStatus(true)
//                NotificationUtil.showLocalNotification(
//                    this@RingConnectionService,
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
                        ringDataStore.getRingDevice()?.let { colorFitDevice ->
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
                            AppLogs.sendAppLogs("RingConnectionService : Bluetooth turned ON")
                            LOGS.w(TAG, "STATE_ON")
                            sessionManager.setBluetoothState(true)
                            btStatus = "bt on"
                        }
                        if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_TURNING_OFF) {
                            LOGS.w(TAG, "STATE_OFF")
                            AppLogs.sendAppLogs("RingConnectionService : Bluetooth turned OFF")
                            statusFailedConnection = true

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
                                ringDataStore.getRingDevice()?.let { colorFitDevice ->

                                    LOGS.d(TAG, "On Device Disconnected")
                                    if (device != null && device.address == colorFitDevice.address) {
                                        AppLogs.sendAppLogs("RingConnectionService: Device Disconnected")
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

    override fun onTaskRemoved(rootIntent: Intent) {
        LOGS.d("$TAG onTaskRemoved")
        val restartServiceIntent = Intent(this, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        val restartServicePendingIntent: PendingIntent =
            PendingIntent.getService(
                this,
                8286,
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

                is QueryCallback.UpdateFirmwareLogStatus -> {
                    sessionManager.firmwareLogsStatus.postValue(queryCallback.fwLogStatus)
                }

                is QueryCallback.BatteryAlertObtained -> {
                    LOGS.d("sdfkjhsdkjfhksdjf BatteryAlertObtained ${queryCallback.batteryLevel}")
                    batteryNotificationUtils.handleBatteryNotification(
                        queryCallback.batteryLevel
                    )
                }

                is QueryCallback.BatteryDataObtained -> {
                    val percent = queryCallback.batteryData.percentage ?: 0
                    LOGS.d("sdfkjhsdkjfhksdjf BatteryDataObtained ${percent} - ${queryCallback.batteryData.isCharging}")
                    sessionManager.batteryPercentRing.postValue(percent)
                    sessionManager.isRingCharging.postValue(queryCallback.batteryData.isCharging)

                    LOGS.d(TAG, "battery Level Ring : $percent")

                    batteryNotificationUtils.handleBatteryNotification(
                        percent, queryCallback.batteryData.isCharging
                    )
                    /* val lastBatteryLevel = watchDataStore.getBatteryPercentRing()
                     batteryNotificationUtils.handleBatteryNotification(
                         percent,
                         lastBatteryLevel,
                         queryCallback.batteryData.isCharging
                     )
                     */
                    watchDataStore.updateBatteryPercentRing(percent)
                }

                is QueryCallback.FirmwareVersionObtained -> {
                    val version = queryCallback.deviceFirmware.version ?: ""
                    LOGS.d(
                        TAG,
                        "FirmwareVersionObtained : ${WatchInfoGlobals.firmwareVersionNumberRing} ${WatchInfoGlobals.firmwareDeviceIdRing}"
                    )
                    sessionManager.firmwareVersion = version
                    watchDataStore.updateFirmwareVersion(version)
                    WatchInfoGlobals.serialNumberRing?.let {
                        watchDataStore.updateSerialNo(it)
                    }
                    watchDataStore.saveDeviceFirmwareDetails(
                        WatchFirmwareDetails(
                            WatchInfoGlobals.firmwareVersionNumberRing,
                            WatchInfoGlobals.firmwareDeviceIdRing
                        )
                    )

                    if (sessionManager.postFirmwareDetailsOnDash) {
                        sessionManager.postFirmwareDetailsOnDash = false
                        sessionManager.checkForVersionUpdate.postValue(
                            Event(
                                Pair(
                                    WatchInfoGlobals.firmwareVersionNumberRing,
                                    WatchInfoGlobals.firmwareDeviceIdRing
                                )
                            )
                        )
                    }
                    if (sessionManager.postFirmwareDetailsOnSetup) {
                        sessionManager.postFirmwareDetailsOnSetup = false
                        sessionManager.checkForVersionUpdateSetup.postValue(
                            Event(
                                Pair(
                                    WatchInfoGlobals.firmwareVersionNumberRing,
                                    WatchInfoGlobals.firmwareDeviceIdRing
                                )
                            )
                        )
                    }
                    if (sessionManager.postFirmwareDetailsOnAboutDevice) {
                        sessionManager.postFirmwareDetailsOnAboutDevice = false
                        sessionManager.checkForVersionUpdateAbout.postValue(
                            Event(
                                Pair(
                                    WatchInfoGlobals.firmwareVersionNumberRing,
                                    WatchInfoGlobals.firmwareDeviceIdRing
                                )
                            )
                        )
                    }

                    /*if (initDefaultValues) {
                        LOGS.d(TAG, "Settings Default values")
                        initDefaultValue()
                    }*/
                }

                else -> {}
            }
            sessionManager.setQueryCallback(queryCallback)

        }
    }

    private val updateDeviceCallback = object : IUpdateDeviceDataCallback {
        override fun onUpdateDataReceived(dataCallback: UpdateDeviceDataCallback) {
            when (dataCallback) {
                is UpdateDeviceDataCallback.ManualMeasurementObtained -> {
                    if (dataCallback.manualMeasurement.manualMeasureType == ManualMeasureType.STRESS) {
                        ringDataStore.setManualMeasurementValueStress(dataCallback.manualMeasurement)
                        sessionManager.setManualMeasurementValue(
                            true,
                            dataCallback.manualMeasurement.manualMeasureType
                        )
                    } else {
                        ringDataStore.setManualMeasurementValue(dataCallback.manualMeasurement)
                        sessionManager.setManualMeasurementValue(
                            true,
                            dataCallback.manualMeasurement.manualMeasureType
                        )
                    }
                }

                is UpdateDeviceDataCallback.OngoingWorkoutData -> {
                    sessionManager.onGoingWorkoutDetected(
                        dataCallback.duration,
                        dataCallback.sportStatus,
                        dataCallback.sportType,
                        dataCallback.startTimeStamp
                    )
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
    }

    private val userActivityCallback = object : IUserActivityDataCallback {
        override fun onUserActivityDataReceived(userActivityCallback: UserActivityCallback) {
            LOGS.d(TAG, "Received data $userActivityCallback")

            when (userActivityCallback) {
                is UserActivityCallback.StepsDataObtained -> {
                    LOGS.d(TAG, "steps Data testing : ${userActivityCallback.stepsData.totalSteps}")
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

    private fun updateRingLocation(location: Pair<Double, Double>) {
        GlobalScope.launch {
            val mac = ringDataStore.getRingDevice()?.address
            val batteryPercent = watchDataStore.getBatteryPercentRing()
            val request = JsonObject().apply {
                this.addProperty("latitude", location.first)
                this.addProperty("longitude", location.second)
                this.addProperty("battery_percentage", batteryPercent)
                this.addProperty("mac_address", mac)
            }

            userRepository.setRingLastLocation(
                request
            ).collect { resource ->
                when (resource) {

                    is Resource.Success -> {
                        resource.data?.data?.let {

                        }
                    }

                    else -> {}
                }
            }
        }
    }

}