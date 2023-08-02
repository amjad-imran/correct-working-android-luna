package com.noisefit.ui.onboarding.auth

import android.annotation.SuppressLint
import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.remote.request.LoginRequest
import com.noisefit.data.repository.abstraction.AuthenticationRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.User
import com.noisefit_commans.models.Gender
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthenticationRepository,
    private val userRepository: UserRepository,
    private val localDataStore: DataStoredInterface,
    private val ringDataStore: RingDataStore,
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
                                        sendOtp(value, type)
                                    }

                                    override fun no() {}
                                }
                        })

                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
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

                            if (it.user == null) {
                                //New user case
                                isNewUser = true
                                verifyMobile.postValue(Event(true))
                                return@let
                            }

                            if (isOutSideIndia) {
                                localDataStore.saveUserInfo(it.user!!)
                                localDataStore.updateUserToken(it.token)
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
                                authSuccess.postValue(Event(true))
                                loginSuccessEvent()
                            }
                            sendMessage("Otp Verified ")
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
        sessionManager.addUserAttributeToInsider(true, HashMap<String, Any>().apply {
            this["name"] = user?.firstName ?: ""
            this["gender"] = gender
            this["age"] = userInfo?.age ?: 0
            this["dob"] = userInfo?.dob.toString()
            this["step_goal"] = userGoals?.stepGoal ?: 0
            this["sleep_goal"] = userGoals?.sleepGoal ?: 8
            this["distance_goal"] = userGoals?.distanceGoal ?: 0
            this["calories_goal"] = userGoals?.caloriesGoal ?: 0
            this["unit_type"] = userGoals?.unitSystem ?: 0
            this["height"] = userInfo?.height ?: 0
            this["weight"] = userInfo?.weight ?: 0
            this["personality_type"] = getEndGameValue(user?.endGame)
            val connectedDeviceData = ringDataStore.getRingDevice()
            try {
                if (connectedDeviceData != null) {
                    val arr = arrayOf(connectedDeviceData.bluetoothName)
                    this["pair_device_watchname"] = connectedDeviceData.bluetoothName ?: ""

                    this["pair_device_mac_address"] = connectedDeviceData.address ?: ""
                    this["pair_device_firmware_number"] = ""
                    this["paired_devices_list"] = arr
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

                                if (isOutSideIndia) {
                                    if (it.user != null) {
                                        localDataStore.saveUserInfo(it.user!!)
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
    fun startOtpResendTimer() {
        timer?.cancel()
        timerRunning.postValue(true)
        timer = object : CountDownTimer(AppConstants.OTP_RESEND_TIMER, 1000) {
            override fun onTick(millisUntilFinished: Long) {
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

        sessionManager.addUserAttributeToInsider(true, HashMap<String, Any>().apply {
            this["name"] = user?.firstName ?: ""
            this["gender"] = gender
            this["age"] = userInfo?.age ?: 0
            this["dob"] = userInfo?.dob.toString()
            this["step_goal"] = userGoals?.stepGoal ?: 0
            this["sleep_goal"] = userGoals?.sleepGoal ?: 8
            this["distance_goal"] = userGoals?.distanceGoal ?: 0
            this["calories_goal"] = userGoals?.caloriesGoal ?: 0
            this["unit_type"] = userGoals?.unitSystem ?: 0
            this["height"] = userInfo?.height ?: 0
            this["weight"] = userInfo?.weight ?: 0
            this["personality_type"] = getEndGameValue(user?.endGame)
            val connectedDeviceData = ringDataStore.getRingDevice()
            try {
                if (connectedDeviceData != null) {
                    val arr = arrayOf(connectedDeviceData.bluetoothName)
                    this["pair_device_watchname"] = connectedDeviceData.bluetoothName ?: ""

                    this["pair_device_mac_address"] = connectedDeviceData.address ?: ""
                    this["pair_device_firmware_number"] = ""
                    this["paired_devices_list"] = arr
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
                            if (isOutSideIndia) {
                                localDataStore.saveUserInfo(it.user!!)
                                localDataStore.updateUserToken(it.token)
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
                                authSuccess.postValue(Event(true))
                                loginSuccessEvent()
                            }
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
                            localDataStore.updateUserToken(it.token)
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

        sessionManager.addUserAttributeToInsider(true, HashMap<String, Any>().apply {
            this["name"] = user?.firstName ?: ""
            this["gender"] = gender
            this["age"] = userInfo?.age ?: 0
            this["dob"] = userInfo?.dob.toString()
            this["step_goal"] = userGoals?.stepGoal ?: 0
            this["sleep_goal"] = userGoals?.sleepGoal ?: 8
            this["distance_goal"] = userGoals?.distanceGoal ?: 0
            this["calories_goal"] = userGoals?.caloriesGoal ?: 0
            this["unit_type"] = userGoals?.unitSystem ?: 0
            this["height"] = userInfo?.height ?: 0
            this["weight"] = userInfo?.weight ?: 0
            this["personality_type"] = getEndGameValue(user?.endGame)
            val connectedDeviceData = ringDataStore.getRingDevice()
            try {
                if (connectedDeviceData != null) {
                    val arr = arrayOf(connectedDeviceData.bluetoothName)
                    this["pair_device_watchname"] = connectedDeviceData.bluetoothName ?: ""

                    this["pair_device_mac_address"] = connectedDeviceData.address ?: ""
                    this["pair_device_firmware_number"] = ""
                    this["paired_devices_list"] = arr
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
        if (user.userInfo?.dob.isNullOrEmpty() && user.userInfo?.height == 0 && user.userInfo?.weight == 0) {
            return false
        }
        return true
    }
}

enum class AuthMode {
    email, mobile, google, facebook
}

enum class RegistrationType {
    email, google, facebook
}
