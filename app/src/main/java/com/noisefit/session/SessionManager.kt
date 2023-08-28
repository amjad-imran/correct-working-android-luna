package com.noisefit.session

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.ui.friends.location.search.SearchStateType
import com.noisefit_commans.constants.SyncEvents
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.response.UpdateResponse
import com.noisefit_commans.data.response.VersionCheckResponse
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.data.UserActivityAction
import com.noisefit_commans.interfaces.data.UserActivityCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.SportsModeRequest
import com.noisefit_commans.models.UserLocation
import com.noisefit_commans.utils.Event
import com.oreo.receiver.workManager.HealthOverviewDataType
import com.useinsider.insider.Insider
import com.useinsider.insider.InsiderEvent
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SessionManager
@Inject
constructor(
    private val localDataStore: DataStoredInterface,
    private val ringDataStore: RingDataStore,
    private val userRepository: UserRepository
) {

    companion object {

        val TAG = "SessionManager"

    }

    var tempUserLocation: UserLocation? = null

    var forceSyncDataWithServer: Boolean = false


    //    var clevertap: CleverTapAPI? = null
    var insiderAppEventWithoutParams: Insider? = null
    var insiderAppEventWithParams: InsiderEvent? = null

    var firmwareVersion: String? = null

    var batterPercent = MutableLiveData(0)
    var batteryPercentRing = MutableLiveData(0)
    var isRingCharging = MutableLiveData(false)

    var forceOtaFlowRunning: Boolean = false
    var forceOtaResponse: UpdateResponse? = null
    var forceOtaResponseRing: UpdateResponse? = null

    var needDfuUpdate = MutableLiveData(Event(false))


    /**
     * Handle App Updates
     */
    val versionCheckData = MutableLiveData<VersionCheckResponse>()


    /**
     * is true if watchface or ota transfer is in progress
     */
    var transferInProgress = false

    private val _connectedDeviceRing = MutableLiveData<ColorFitDevice?>()
    val connectedDeviceRing: LiveData<ColorFitDevice?> = _connectedDeviceRing

    private val _connectState = MutableLiveData<ConnectState>()
    private val _connectStateRing = MutableLiveData<ConnectState>()
    private val _bluetoothOnState = MutableLiveData<Boolean>()
    private val _bluetoothOnStateDash = MutableLiveData<Boolean>()
    private val _syncCompleted = MutableLiveData<Event<SyncEvents>>()
    private val _showSyncOfflineData = MutableLiveData<Event<HealthOverviewDataType>>()
    private val _manualMeasurementValue = MutableLiveData<Event<Boolean>>()
    private val _deviceQueryAction = MutableLiveData<QueryAction>()
    private val _updateDeviceQueryAction = MutableLiveData<UpdateDeviceAction>()
    private val _deviceQueryCallback = MutableLiveData<QueryCallback>()
    private val _updateDeviceCallback = MutableLiveData<Event<UpdateDeviceDataCallback>>()

    private val _userActivityAction = MutableLiveData<UserActivityAction>()
    private val _userActivityCallback = MutableLiveData<Event<UserActivityCallback>>()
    private val _reloadNotification = MutableLiveData<Event<Boolean>>()
//    private val _dateChanged = MutableLiveData<Event<Boolean>>()

    private val _sportsModeRequest = MutableLiveData<SportsModeRequest?>()

    val forceDisconnect = MutableLiveData<Event<Boolean>>()

//    private val _connectWatch = MutableLiveData<ColorFitDevice>()

    val reloadNotification: LiveData<Event<Boolean>>
        get() = _reloadNotification

    val manualMeasurementValue: LiveData<Event<Boolean>>
        get() = _manualMeasurementValue

    val bluetoothState: LiveData<Boolean>
        get() = _bluetoothOnState

    val bluetoothStateDash: LiveData<Boolean>
        get() = _bluetoothOnStateDash

    val syncCompleted: LiveData<Event<SyncEvents>>
        get() = _syncCompleted

//    val dateChanged: LiveData<Event<Boolean>>
//        get() = _dateChanged

    val showSyncOfflineData: LiveData<Event<HealthOverviewDataType>>
        get() = _showSyncOfflineData

    val connectStateRing: LiveData<ConnectState>
        get() = _connectStateRing

    val deviceQueryAction: LiveData<QueryAction>
        get() = _deviceQueryAction
    val deviceQueryCallback: LiveData<QueryCallback>
        get() = _deviceQueryCallback


    val updateDeviceQueryAction: LiveData<UpdateDeviceAction>
        get() = _updateDeviceQueryAction

    val updateDeviceCallback: LiveData<Event<UpdateDeviceDataCallback>>
        get() = _updateDeviceCallback

    val userActivityAction: LiveData<UserActivityAction>
        get() = _userActivityAction

    val userActivityCallback: LiveData<Event<UserActivityCallback>>
        get() = _userActivityCallback

    val sportsModeRequest: LiveData<SportsModeRequest?>
        get() = _sportsModeRequest


//    val connectWatch: LiveData<ColorFitDevice>
//        get() = _connectWatch

    private val _agpsStatus = MutableLiveData<Event<Boolean>>()
    val agpsStatus: LiveData<Event<Boolean>> = _agpsStatus

    fun setAGPSState(state: Boolean) {
        GlobalScope.launch(Main) {
            _agpsStatus.value = Event(state)
        }
    }

    fun clearSessionManager() {
        forceOtaResponseRing = null
        _connectedDeviceRing.postValue(null)
        _connectState.postValue(ConnectState.UnPaired())
        _connectStateRing.postValue(ConnectState.UnPaired())
    }

    fun setManualMeasurementValue(status: Boolean) {
        GlobalScope.launch(Main) {
            _manualMeasurementValue.value = Event(status)
        }
    }


    fun clearSessionManagerHibernate() {
        forceOtaResponseRing = null
        _connectedDeviceRing.value = (null)
    }

    fun getPairedState() {

        val device = ringDataStore.getRingDevice()
        if (device == null) {
            _connectStateRing.postValue(ConnectState.UnPaired())
        }
        setConnectedDeviceRing(device)


    }

    fun setConnectStateRing(connectState: ConnectState) {
        GlobalScope.launch(Main) {
            _connectStateRing.value = connectState
        }
    }

    fun setConnectedDeviceRing(colorFitDevice: ColorFitDevice?) {
        GlobalScope.launch(Main) {
            _connectedDeviceRing.value = colorFitDevice
        }
    }

    fun setSportsModeRequest(sportsModeRequest: SportsModeRequest?) {
        GlobalScope.launch(Main) {
            _sportsModeRequest.value = sportsModeRequest
        }
    }

    fun postSportsModeRequest(sportsModeRequest: SportsModeRequest) {
        GlobalScope.launch(Main) {
            _sportsModeRequest.postValue(sportsModeRequest)
        }
    }


    fun setBluetoothState(boolean: Boolean) {
        GlobalScope.launch(Main) {
            if (_bluetoothOnState.value != boolean) {
                _bluetoothOnState.value = boolean
            }
            _bluetoothOnStateDash.value = boolean
        }
    }

    fun setSyncCompletedState(syncDataStatus: Event<SyncEvents>) {
        GlobalScope.launch(Main) {
            if (_syncCompleted.value != syncDataStatus) {
                _syncCompleted.value = syncDataStatus
            }
        }
    }

    fun setShowSyncOfflineData(syncDataStatus: Event<HealthOverviewDataType>) {
        GlobalScope.launch(Main) {
            if (_showSyncOfflineData.value != syncDataStatus) {
                _showSyncOfflineData.value = syncDataStatus
            }
        }
    }

//    fun setDateChanged(changed: Event<Boolean>) {
//        GlobalScope.launch(Main) {
//            if (_dateChanged.value != changed) {
//                _dateChanged.value = changed
//            }
//        }
//    }

    fun sendQueryAction(action: QueryAction) {
        GlobalScope.launch(Main) {
            _deviceQueryAction.value = action
        }
    }

    fun setQueryCallback(callback: QueryCallback) {
        GlobalScope.launch(Main) {
            _deviceQueryCallback.value = callback
            _deviceQueryCallback.value = QueryCallback.Default()
        }
    }

    fun sendUserActivityAction(action: UserActivityAction) {
        GlobalScope.launch(Main) {
            _userActivityAction.value = action
        }
    }

    fun setUserActivityCallback(callback: UserActivityCallback) {
        GlobalScope.launch(Main) {
            _userActivityCallback.value = Event(callback)
        }
    }

    fun sendUpdateQueryAction(action: UpdateDeviceAction) {
        GlobalScope.launch(Main) {
            _updateDeviceQueryAction.value = action
            _updateDeviceQueryAction.value = UpdateDeviceAction.Default()
        }
    }

    fun setUpdateDeviceCallback(callback: UpdateDeviceDataCallback) {
        GlobalScope.launch(Main) {
            _updateDeviceCallback.value = Event(callback)
        }
    }

//    fun setConnectWatchState(colorFitDevice: ColorFitDevice) {
//        GlobalScope.launch(Main) {
//            if (_connectWatch.value != colorFitDevice) {
//                _connectWatch.value = colorFitDevice
//            }
//        }
//    }


    /**
     * Returns connection status on the bases of _connectState state
     */
    fun isDeviceConnected(): Boolean {
        if (connectStateRing.value == null) return false
        return connectStateRing.value is ConnectState.ConnectSuccess
    }


    private fun logFirebaseEvent(eventName: String, data: HashMap<String, Any>) {
//        val newEventName = eventName.lowercase().replace(" ", "_")
//        Firebase.analytics.logEvent(newEventName, ApplicationUtils.convertMapToBundle(data))
//        LOGS.d("LOGS_FIREBASE_EVENT $newEventName ")
    }

    fun logEvent(eventName: String, status: String) {
//        logInsiderAppEvent(
//            eventName,
//            HashMap<String, Any>().apply {
//                this["status"] = status
//            })
    }

    fun logInsiderAppEvent(eventName: String) {
//        insiderAppEventWithoutParams = Instance
//
//        val newEventName = eventName.lowercase().replace(" ", "_")
//        insiderAppEventWithoutParams?.tagEvent(newEventName)?.build()
//        LOGS.d("LOGS_INSIDER_EVENT $newEventName")
//        Firebase.analytics.logEvent(newEventName, null)
        // LOGS.d("LOGS_FIREBASE_EVENT $newEventName ")
    }

    fun logInsiderAppEvent(eventName: String, data: HashMap<String, Any>) {
//        val newEventName = eventName.lowercase().replace(" ", "_")
//        insiderAppEventWithParams = Instance.tagEvent(newEventName)
//        data.forEach { (key, value) ->
//            val key1 = key.lowercase().replace(" ", "_")
//            when (value) {
//                is String -> {
//                    insiderAppEventWithParams?.addParameterWithString(key1, value)
//                }
//
//                is Double -> {
//                    insiderAppEventWithParams?.addParameterWithDouble(key1, value)
//                }
//
//                is Boolean -> {
//                    insiderAppEventWithParams?.addParameterWithBoolean(key1, value)
//                }
//
//                is Int -> {
//                    insiderAppEventWithParams?.addParameterWithInt(key1, value)
//                }
//
//                is Date -> {
//                    insiderAppEventWithParams?.addParameterWithDate(key1, value)
//                }
//
//
//            }
//        }
////        LOGS.d("LOGS_INSIDER_EVENT_HAS_PARAMS $newEventName ${Gson().toJson(data)}")
//        insiderAppEventWithParams?.build()
//
//        Firebase.analytics.logEvent(newEventName, ApplicationUtils.convertMapToBundle(data))
        //LOGS.d("LOGS_FIREBASE_EVENT_HAS_PARAMS $newEventName ")
    }

    fun addUserAttributeToInsider(isLogin: Boolean, data: HashMap<String, Any>) {
//        val insiderUserData = Instance.currentUser
//        val firebaseInstance = Firebase.analytics
//        data.forEach { (key, value) ->
//            when (value) {
//                is String -> {
//                    if (key.equals("name", true)) {
//                        insiderUserData.setName(value)
//                        firebaseInstance.setUserProperty(key, value)
//
//                    } else if (key.equals("gender", true)) {
//                        if (value.lowercase() == Gender.MALE.name.lowercase())
//                            insiderUserData.setGender(InsiderGender.MALE)
//                        else if (value.lowercase() == Gender.FEMALE.name.lowercase())
//                            insiderUserData.setGender(InsiderGender.FEMALE)
//                        else
//                            insiderUserData.setGender(InsiderGender.OTHER)
//                    } else if (key.equals("dob", true) && !value.equals("null", true)) {
//                        tryCatch {
//                            firebaseInstance.setUserProperty(key, value)
//                            insiderUserData.setBirthday(
//                                DateFormats.getDateFormatFromString2(
//                                    value
//                                )
//                            )
//                            firebaseInstance.setUserProperty(key, value)
//                        }
//
//                    } else if (key.equals("home_page_visit", true)) {
//                        Instance.visitHomePage()
//                        firebaseInstance.logEvent(key, null)
//                    } else {
//                        insiderUserData?.setCustomAttributeWithString(key, value)
//                        firebaseInstance.setUserProperty(key, value)
//                    }
//
//
//                }
//
//                is Int -> {
//                    if (key.equals("age", true)) {
//                        insiderUserData.setAge(value)
//                    } else
//                        insiderUserData?.setCustomAttributeWithInt(key, value)
//                }
//
//                is Double -> {
//                    insiderUserData?.setCustomAttributeWithDouble(key, value)
//                }
//
//                is Boolean -> {
//                    insiderUserData?.setCustomAttributeWithBoolean(key, value)
//                }
//
//            }
//        }
//        if (isLogin) {
//            Instance.setGDPRConsent(true)
//            insiderUserData.setEmailOptin(true)
//            insiderUserData.setSMSOptin(true)
//            //identifiers
//            val user = localDataStore.getUser()
//            val identifiers = InsiderIdentifiers()
//            identifiers.addUserID(user?.id.toString())
//            identifiers.addEmail(user?.email.toString())
//            if (!user?.mobile.isNullOrEmpty()) {
//                val phoneNumber: String = "+91" + user?.mobile.toString()
//                identifiers.addPhoneNumber(phoneNumber.trim())
//            }
//            insiderUserData.login(identifiers)
//        }

    }

    fun reloadNotification(event: Event<Boolean>) {
        _reloadNotification.value = event
    }

    fun saveLastSyncTime(timeStamp: Long) {
        ringDataStore.saveLastSyncTimeStamp(timeStamp)
    }


    fun logCustomCrashlyticsEvents(event: String, log: String, e: Exception? = null) {
        FirebaseCrashlytics.getInstance().setCustomKey(event, log)
        e?.let { FirebaseCrashlytics.getInstance().recordException(it) }
    }

    fun getLastSyncTime(): Long? {
        val timeStamp = ringDataStore.getLastSyncTimeStamp()

        return if (timeStamp == -1L) {
            null
        } else {
            timeStamp
        }
    }

    fun updateUserLocationState(data: String?, id: Int?, type: SearchStateType) {

        if (tempUserLocation == null) {
            tempUserLocation = UserLocation()
        }

        when (type) {
            SearchStateType.State -> {
                tempUserLocation!!.stateChanged = true
                tempUserLocation!!.stateId = id
                tempUserLocation!!.state = data
                tempUserLocation!!.cityId = 0
                tempUserLocation!!.city = null
            }

            SearchStateType.City -> {
                tempUserLocation!!.stateId = tempUserLocation!!.stateId
                tempUserLocation!!.state = tempUserLocation!!.state
                tempUserLocation!!.stateChanged = false
                tempUserLocation!!.cityId = id
                tempUserLocation!!.city = data
            }
        }


    }
}

