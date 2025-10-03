package com.noisefit.ui.onboarding.auth

import android.annotation.SuppressLint
import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.remote.request.LoginRequest
import com.noisefit.data.repository.abstraction.AuthenticationRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.User
import com.noisefit_commans.models.Gender
import com.noisefit_commans.models.Units
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MiscUtil
import com.noisefit_commans.utils.MoEngageAppEventAttributes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthenticationRepository,
    private val userRepository: UserRepository,
    private val localDataStore: DataStoredInterface,
    private val ringDataStore: RingDataStore,
    private val resourcesProvider: ResourcesProvider,
    val sessionManager: SessionManager
) : BaseViewModel() {

    var googleImageUrl: String? = null
    var uuidType: String? = null
    var email: String? = null
    var uuid: String? = null
    var useOtp = true
    var isNewUser = false
    var mobile: String? = null
    var countryCode: String? = null
    var user: User? = null
    var loginMethod: String? = null

    val emailOtpGenerated = MutableLiveData<Event<Boolean>>()

    private var _enteredOtp = MutableLiveData<String>()
    var enteredOtp = _enteredOtp
    var authSuccess = MutableLiveData<Event<Boolean>>()
    var createUser = MutableLiveData<Event<Boolean>>()
    var verifyMobile = MutableLiveData<Event<Boolean>>()
    var verifyEmail = MutableLiveData<Event<Boolean>>()
    var accountCreated = MutableLiveData<Event<Boolean>>()

    private var _enteredPass = MutableLiveData<String>()
    var enteredPass = _enteredPass


    var enteredValue: String? = null
    var usePassword = false


    fun resetData() {
        uuidType = null
        email = null
        isNewUser = false
        useOtp = true
        enteredValue = null
        mobile = null
        _enteredPass.postValue("")
        verifyEmail.postValue(Event(false))
        verifyMobile.postValue(Event(false))
        authSuccess.postValue(Event(false))
        accountCreated.postValue(Event(false))
        emailOtpGenerated.postValue(Event(false))
        createUser.postValue(Event(false))
        _enteredOtp.postValue("")
    }


    fun sendOtp(value: String, type: AuthMode) {
        enteredValue = value
        viewModelScope.launch {
            val requestObject = JsonObject()
            requestObject.addProperty("type", type.name.lowercase())
            requestObject.addProperty("value", value)
            authRepo.sendOtp(requestObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        val errTimer = resource.errorBody?.errors?.timer
                        if (errTimer != null) {
                            sessionManager.otpResendTimerSeconds = errTimer

                            if (type == AuthMode.email) {
                                email = value
                                usePassword = false
                            }
                            emailOtpGenerated.postValue(Event(true))
                            sendMessage(resource.errorBody.errors.message)
                        }else{
                            sendMessage(resource.message)
                        }
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        LOGS.d("Network error")
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        sendOtp(value, type)
                                    }

                                    override fun no() {}
                                }
                        })

                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            sessionManager.otpResendTimerSeconds = it.timer?:AppConstants.OTP_RESEND_SECONDS

                            if (type == AuthMode.email) {
                                email = value
                                usePassword = it.is_pwd ?: false
                            }
                            emailOtpGenerated.postValue(Event(true))
                        }

                    }
                }
            }

        }
    }

    fun verifyOtp(value: String, type: AuthMode, otp: String) {
        viewModelScope.launch {
            val requestObject = JsonObject()
            requestObject.addProperty("type", type.name.lowercase())
            requestObject.addProperty("value", value)
            requestObject.addProperty("otp", otp)

            val isOutSideIndia = ApplicationUtils.isOutSideIndia()


            if (type == AuthMode.email) {
                if (isOutSideIndia) {
                    requestObject.addProperty("is_international", isOutSideIndia)
                }
            }

            authRepo.verifyOtp(requestObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        LOGS.d("Network error")
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        verifyOtp(value, type, otp)
                                    }

                                    override fun no() {}
                                }
                        })

                    }

                    is Resource.Success -> {

                        resource.data?.data?.let {

                            ringDataStore.setUpdateUserDeviceStatus(false)
                            if (it.user == null) {
                                //New user case
                                isNewUser = true
                                verifyMobile.postValue(Event(true))
                                return@let
                            }

                            localDataStore.saveUserInfo(it.user!!)
                            sessionManager.updateUnit(it.user?.userGoals?.getUnit() ?: Units.METRIC)
                            sessionManager.updateGender(it.user?.userInfo?.gender)
                            sessionManager.updateNotificationSettings(
                                it.user?.notificationsEnabledLuna ?: 1
                            )

                            localDataStore.updateUserToken(it.token)
                            localDataStore.setIsInDemoMode(it.token?.multiLogin?:false)
                            authSuccess.postValue(Event(true))
                            loginSuccessEvent()

                            /* if (isOutSideIndia) {
                                 localDataStore.saveUserInfo(it.user!!)
                                 localDataStore.updateUserToken(it.token)
                                 ringDataStore.setUpdateUserDeviceStatus(false)
                                 authSuccess.postValue(Event(true))
                                 loginSuccessForOutSideIndiaUserEvent()
                                 return@let
                             }


                             if (it.user!!.mobile.isNullOrEmpty()) {
                                 user = it.user
                                 email = it.user!!.email
                                 verifyMobile.postValue(Event(true))
                             } else {
                                 localDataStore.saveUserInfo(it.user!!)
                                 localDataStore.updateUserToken(it.token)
                                 ringDataStore.setUpdateUserDeviceStatus(false)
                                 authSuccess.postValue(Event(true))
                                 loginSuccessEvent()
                             }*/
                            sendMessage(resourcesProvider.getString(R.string.text_otp_verified))
                            //emailOtpGenerated.postValue(Event(true))
                        }

                    }
                }
            }

        }
    }

    @SuppressLint("Range")
    private fun loginSuccessForOutSideIndiaUserEvent() {
        val user = localDataStore.getUser()
        val userInfo = localDataStore.getUser()?.userInfo
        val userGoals = localDataStore.getUser()?.userGoals
        val gender: String = if (userInfo?.gender?.lowercase() == Gender.MALE.name.lowercase()) {
            "Male"
        } else if (userInfo?.gender?.lowercase() == Gender.FEMALE.name.lowercase()) {
            "Female"
        } else {
            "Other"
        }
        sessionManager.addUserAttributeToMoEngage(true, HashMap<String, Any>().apply {
            this[MoEngageAppEventAttributes.name] = user?.firstName ?: ""
            this[MoEngageAppEventAttributes.gender] = gender
            this[MoEngageAppEventAttributes.age] = userInfo?.age ?: 0
            this[MoEngageAppEventAttributes.dob] = userInfo?.dob.toString()
            this[MoEngageAppEventAttributes.height] = userInfo?.height ?: 0
            this[MoEngageAppEventAttributes.weight] = userInfo?.weight ?: 0
            val connectedDeviceData = ringDataStore.getRingDevice()
            try {
                if (connectedDeviceData != null) {
                    val arr = arrayOf(connectedDeviceData.bluetoothName)
                    this[MoEngageAppEventAttributes.pair_device_name] =
                        connectedDeviceData.bluetoothName ?: ""

                    this[MoEngageAppEventAttributes.pair_device_mac_address] =
                        connectedDeviceData.address ?: ""
                    this[MoEngageAppEventAttributes.pair_device_color] = ""
                    this[MoEngageAppEventAttributes.mobile_device] = "Android"
                    this[MoEngageAppEventAttributes.mobile_device_manufacturer] =
                        MiscUtil.getDeviceName()
                    this[MoEngageAppEventAttributes.pair_device_firmware_number] = ""
                    this[MoEngageAppEventAttributes.paired_devices_list] = arr
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


        })


        if (userInfo == null) sessionManager.logInsiderAppEvent(InsiderAppEvents.REGISTER_SUCCESS,
            HashMap<String, Any>().apply {
                this["user_login_id"] = user?.id ?: ""
                this["method_used"] = loginMethod.toString()
            })
        else sessionManager.logInsiderAppEvent(InsiderAppEvents.LOGIN_SUCCESS,
            HashMap<String, Any>().apply {
                this["user_login_id"] = user?.id ?: ""
                this["method_used"] = loginMethod.toString()
            })
    }

    fun emailLoginPassword(value: String, password: String) {
        viewModelScope.launch {
            val requestObject = LoginRequest(
                login_type = AuthMode.email.name.lowercase(), value = value, password = password
            )

            authRepo.loginUser(requestObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        LOGS.d("Network error")
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        emailLoginPassword(value, password)
                                    }

                                    override fun no() {}
                                }
                        })

                    }

                    is Resource.Success -> {

                        resource.data?.data?.let {
                            it.user?.let { user ->
                                val isOutSideIndia = ApplicationUtils.isOutSideIndia()

                                ringDataStore.setUpdateUserDeviceStatus(false)
                                if (isOutSideIndia) {
                                    if (it.user != null) {
                                        localDataStore.saveUserInfo(it.user!!)
                                        sessionManager.updateUnit(
                                            it.user?.userGoals?.getUnit() ?: Units.METRIC
                                        )
                                        sessionManager.updateGender(it.user?.userInfo?.gender)
                                        sessionManager.updateNotificationSettings(
                                            it.user?.notificationsEnabledLuna ?: 1
                                        )
                                        localDataStore.updateUserToken(it.token)
                                        authSuccess.postValue(Event(true))
                                        loginSuccessEvent()
                                    }
                                } else {
                                    if (user.mobile.isNullOrEmpty()) {
                                        it.user = user
                                        email = it.user?.email
                                        verifyMobile.postValue(Event(true))
                                    } else {
                                        localDataStore.saveUserInfo(user)
                                        sessionManager.updateUnit(
                                            it.user?.userGoals?.getUnit() ?: Units.METRIC
                                        )
                                        sessionManager.updateGender(it.user?.userInfo?.gender)
                                        sessionManager.updateNotificationSettings(
                                            it.user?.notificationsEnabledLuna ?: 1
                                        )
                                        localDataStore.updateUserToken(it.token)
                                        authSuccess.postValue(Event(true))
                                        loginSuccessEvent()
                                    }
                                }
                                sendMessage("Login Success ")
                            }
                        }

                    }
                }
            }

        }
    }


    var timer: CountDownTimer? = null
    val timerRunning = MutableLiveData<Boolean>()
    val tickerTime = MutableLiveData<String>()

    fun startOtpResendTimer(startSeconds: Int? = null) {
        val totalMillis = if (startSeconds != null) startSeconds.toLong() * 1000L else AppConstants.OTP_RESEND_TIMER
        timer?.cancel()
        timerRunning.postValue(true)
        timer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSecs = millisUntilFinished / 1000
                val seconds = totalSecs % 60
                val minutes = (totalSecs / 60) % 60
                val hours = (totalSecs / 3600) % 24
                val tempSec: String = if (seconds < 10)
                    "0$seconds"
                else
                    "$seconds"
                val tempMin: String = if (minutes < 10)
                    "0$minutes"
                else
                    "$minutes"
                val tempHour: String = if (hours < 10)
                    "0$hours"
                else
                    "$hours"
                tickerTime.postValue(if (hours > 0) "$tempHour:$tempMin:$tempSec" else "$tempMin:$tempSec")

            }

            override fun onFinish() {
                timerRunning.postValue(false)
            }
        }
        timer?.start()
    }

    fun setEnteredOtp(number: String) {
        _enteredOtp.postValue(number)
    }

    fun setEnteredPassword(pass: String) {
        _enteredPass.postValue(pass)
    }

    @SuppressLint("Range")
    private fun loginSuccessEvent() {


        val user = localDataStore.getUser()
        val userInfo = localDataStore.getUser()?.userInfo
        val userGoals = localDataStore.getUser()?.userGoals
        val gender: String = if (userInfo?.gender?.lowercase() == Gender.MALE.name.lowercase()) {
            "Male"
        } else if (userInfo?.gender?.lowercase() == Gender.FEMALE.name.lowercase()) {
            "Female"
        } else {
            "Other"
        }

        sessionManager.addUserAttributeToMoEngage(true, HashMap<String, Any>().apply {
            this[MoEngageAppEventAttributes.name] = user?.firstName ?: ""
            this[MoEngageAppEventAttributes.gender] = gender
            this[MoEngageAppEventAttributes.age] = userInfo?.age ?: 0
            this[MoEngageAppEventAttributes.dob] = userInfo?.dob.toString()
            this[MoEngageAppEventAttributes.height] = userInfo?.height ?: 0
            this[MoEngageAppEventAttributes.weight] = userInfo?.weight ?: 0
            val connectedDeviceData = ringDataStore.getRingDevice()
            try {
                if (connectedDeviceData != null) {
                    val arr = arrayOf(connectedDeviceData.bluetoothName)
                    this[MoEngageAppEventAttributes.pair_device_name] =
                        connectedDeviceData.bluetoothName ?: ""

                    this[MoEngageAppEventAttributes.pair_device_mac_address] =
                        connectedDeviceData.address ?: ""
                    this[MoEngageAppEventAttributes.pair_device_color] = ""
                    this[MoEngageAppEventAttributes.mobile_device] = "Android"
                    this[MoEngageAppEventAttributes.mobile_device_manufacturer] =
                        MiscUtil.getDeviceName()
                    this[MoEngageAppEventAttributes.pair_device_firmware_number] = ""
                    this[MoEngageAppEventAttributes.paired_devices_list] = arr
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


        })

        sessionManager.logInsiderAppEvent(InsiderAppEvents.LOGIN_SUCCESS,
            HashMap<String, Any>().apply {
                this["user_login_id"] = user?.id ?: ""
                this["method_used"] = loginMethod.toString()
            })


    }


    fun getEndGameValue(id: String?): String {
        if (id.isNullOrEmpty()) {
            return "Not Set"
        }
        val endGameList = localDataStore.getEndGameList()

        endGameList.forEach {
            if (it.id == id.toInt()) {
                return it.title
            }
        }
        return "Not Set"
    }

    /**
     * Use for Social Login
     * @param token Token
     * @param authMode Fb/Google auth mode
     */
    fun handleSocialLogin(
        token: String, authMode: AuthMode, photoUrl: String?
    ) {
        val request = LoginRequest(
            login_type = authMode.name.lowercase(), image_url = photoUrl, token = token
        )

        viewModelScope.launch {
            authRepo.loginUser(request).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }


                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        LOGS.d("Network error")
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        handleSocialLogin(token, authMode, photoUrl)
                                    }

                                    override fun no() {}
                                }
                        })

                    }

                    is Resource.Success -> {

                        resource.data?.data?.let {
                            val isOutSideIndia = ApplicationUtils.isOutSideIndia()
                            ringDataStore.setUpdateUserDeviceStatus(false)

                            if (it.user == null) {
                                //CASE New User
                                isNewUser = true
                                if (authMode == AuthMode.google) {
                                    uuidType = "google"
                                    googleImageUrl = photoUrl
                                    uuid = it.social_data?.sub ?: ""
                                    email = it.social_data?.email ?: ""

                                    if (isOutSideIndia) {
                                        createUser.postValue(Event(true))
                                        return@let
                                    }

                                    verifyMobile.postValue(Event(true))
                                } else if (authMode == AuthMode.facebook) {
                                    uuidType = "facebook"
                                    uuid = it.social_data?.id ?: ""
                                    verifyEmail.postValue(Event(true))
                                } else {
                                    verifyEmail.postValue(Event(true))
                                }
                                return@let
                            }


                            localDataStore.saveUserInfo(it.user!!)
                            sessionManager.updateUnit(it.user?.userGoals?.getUnit() ?: Units.METRIC)
                            sessionManager.updateGender(it.user?.userInfo?.gender)
                            sessionManager.updateNotificationSettings(
                                it.user?.notificationsEnabledLuna ?: 1
                            )
                            localDataStore.updateUserToken(it.token)
                            authSuccess.postValue(Event(true))
                            loginSuccessEvent()


                            /* if (isOutSideIndia) {
                                 localDataStore.saveUserInfo(it.user!!)
                                 localDataStore.updateUserToken(it.token)
                                 ringDataStore.setUpdateUserDeviceStatus(false)
                                 authSuccess.postValue(Event(true))
                                 loginSuccessForOutSideIndiaUserEvent()
                                 return@let
                             }
                             if (it.user!!.mobile.isNullOrEmpty()) {
                                 email = it.user?.email
                                 user = it.user
                                 verifyMobile.postValue(Event(true))
                                 //CASE No mobile, ask mobile auth flow
                             } else {
                                 localDataStore.saveUserInfo(it.user!!)
                                 localDataStore.updateUserToken(it.token)
                                 ringDataStore.setUpdateUserDeviceStatus(false)
                                 authSuccess.postValue(Event(true))
                                 loginSuccessEvent()
                             }*/
                        }

                    }
                }
            }

        }

    }

    fun createInternationalUser() {
        viewModelScope.launch {
            val requestObject = JsonObject()
            requestObject.addProperty("login_type", uuidType)
            requestObject.addProperty("uuid", uuid)
            requestObject.addProperty("email", email)//Should be blank if not available

            authRepo.createInternationalUser(requestObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        LOGS.d("Network error")
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        createInternationalUser()
                                    }

                                    override fun no() {}
                                }
                        })

                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            localDataStore.saveUserInfo(it.user!!)
                            sessionManager.updateUnit(it.user?.userGoals?.getUnit() ?: Units.METRIC)
                            sessionManager.updateGender(it.user?.userInfo?.gender)
                            sessionManager.updateNotificationSettings(
                                it.user?.notificationsEnabledLuna ?: 1
                            )
                            localDataStore.updateUserToken(it.token)
                            ringDataStore.setUpdateUserDeviceStatus(false)
                            authSuccess.postValue(Event(true))
                            registerSuccessForOutSideIndiaUserEvent()
                        }
                    }
                }
            }

        }
    }

    private fun registerSuccessForOutSideIndiaUserEvent() {
        val user = localDataStore.getUser()
        val userInfo = localDataStore.getUser()?.userInfo
        val userGoals = localDataStore.getUser()?.userGoals
        val gender: String = if (userInfo?.gender?.lowercase() == Gender.MALE.name.lowercase()) {
            "Male"
        } else if (userInfo?.gender?.lowercase() == Gender.FEMALE.name.lowercase()) {
            "Female"
        } else {
            "Other"
        }

        sessionManager.addUserAttributeToMoEngage(true, HashMap<String, Any>().apply {
            this[MoEngageAppEventAttributes.name] = user?.firstName ?: ""
            this[MoEngageAppEventAttributes.gender] = gender
            this[MoEngageAppEventAttributes.age] = userInfo?.age ?: 0
            this[MoEngageAppEventAttributes.dob] = userInfo?.dob.toString()
            this[MoEngageAppEventAttributes.height] = userInfo?.height ?: 0
            this[MoEngageAppEventAttributes.weight] = userInfo?.weight ?: 0
            val connectedDeviceData = ringDataStore.getRingDevice()
            try {
                if (connectedDeviceData != null) {
                    val arr = arrayOf(connectedDeviceData.bluetoothName)
                    this[MoEngageAppEventAttributes.pair_device_name] =
                        connectedDeviceData.bluetoothName ?: ""

                    this[MoEngageAppEventAttributes.pair_device_mac_address] =
                        connectedDeviceData.address ?: ""
                    this[MoEngageAppEventAttributes.pair_device_color] = ""
                    this[MoEngageAppEventAttributes.mobile_device] = "Android"
                    this[MoEngageAppEventAttributes.mobile_device_manufacturer] =
                        MiscUtil.getDeviceName()
                    this[MoEngageAppEventAttributes.pair_device_firmware_number] = ""
                    this[MoEngageAppEventAttributes.paired_devices_list] = arr
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


        })


        sessionManager.logInsiderAppEvent(InsiderAppEvents.REGISTER_SUCCESS,
            HashMap<String, Any>().apply {
                this["user_login_id"] = user?.id ?: ""
                this["method_used"] = loginMethod.toString()
            })

    }

    fun isDevicePaired(): Boolean {
        return ringDataStore.getRingDevice() != null
    }


    fun isProfileSetupComplete(): Boolean {
        val user = localDataStore.getUser() ?: return false
        if (user.userInfo?.dob.isNullOrEmpty() || (user.userInfo?.height
                ?: 0) == 0 || (user.userInfo?.weight ?: 0) == 0 || (user.userGoals?.caloriesGoal
                ?: 0) == 0
        ) {
            return false
        }
        return true
    }

    fun hasUserSelectedLanguage() = localDataStore.hasUserSelectedLanguage()
}

enum class AuthMode {
    email, mobile, google, facebook
}

enum class RegistrationType {
    email, google, facebook
}
