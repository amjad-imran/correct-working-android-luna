package com.noisefit.ui.onboarding.otp

import android.annotation.SuppressLint
import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.AuthenticationRepository
import com.noisefit.ui.onboarding.auth.AuthMode
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.User
import com.noisefit_commans.data.response.Country
import com.noisefit_commans.data.response.SendOtpResponse
import com.noisefit_commans.models.Gender
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.MiscUtil
import com.noisefit_commans.utils.MoEngageAppEventAttributes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val ringDataStore: RingDataStore,
    private val authenticationRepository: AuthenticationRepository,
    val sessionManager: com.noisefit.session.SessionManager
) : BaseViewModel() {

    var googleImageUrl: String? = null
    var uuidType: String? = null
    var uuid: String? = null
    var email: String? = null
    var user: User? = null
    private var _selectedCountry = MutableLiveData<Country>()
    var selectedCountry = _selectedCountry

    private var _successMessage = MutableLiveData<Event<String>?>()
    var successMessage = _successMessage

    private var _showMobExistingBSheet = MutableLiveData<Event<SendOtpResponse>>()
    var showMobExistingBSheet = _showMobExistingBSheet

    var isMobileExist: Boolean = false

    private var _successOtpMessage = MutableLiveData<String?>()
    var successOtpMessage = _successOtpMessage

    private var _contactNumber = MutableLiveData<String>()
    var contactNumber = _contactNumber

    private var _enteredOtp = MutableLiveData<String>()
    var enteredOtp = _enteredOtp

    var countryList = ArrayList<Country>()

    var authSuccess = MutableLiveData<Event<Boolean>>()


    fun setCountries(data: ArrayList<Country>) {
        countryList = data
        data.forEach { country ->
            if (country.name.equals("india", true)) {
                setSelectedCountry(country)
            }
        }
    }

    /**
     * Returns true if Selected country requires
     * mobile number also
     */
    fun isNumberSupported(): Boolean {
        if (selectedCountry.value == null) return false
        return selectedCountry.value?.name.equals("india", true)
    }

    fun setSelectedCountry(country: Country) {
        _selectedCountry.postValue(country)
    }

    fun setEnteredOtp(number: String) {
        _enteredOtp.postValue(number)
    }

    fun setContactNumber(number: String) {
        _contactNumber.postValue(number)
    }

    fun resetSuccessMessage() {
        _successMessage.value = null
    }

    fun sendOtp() {
        val requestObject = JsonObject()
        requestObject.addProperty("type", AuthMode.mobile.name.lowercase())
        requestObject.addProperty("country_code", _selectedCountry.value?.code)
        requestObject.addProperty("value", _contactNumber.value?.replace(" ", ""))
//        requestObject.addProperty("email", email)
        if (isMobileExist) {
            requestObject.addProperty("login", true)
        }

        if (!uuidType.isNullOrEmpty()) {
            requestObject.addProperty("uuid_type", uuidType)
            requestObject.addProperty("uuid", uuid)
            requestObject.addProperty("email", email)
        }

        viewModelScope.launch {
            authenticationRepository.sendOtp(requestObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        sendOtp()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            if (it.status == 422) {
                                _showMobExistingBSheet.postValue(Event(it))
                            } else
                                _successMessage.postValue(Event(it.message))
                        }
                    }
                }
            }
        }

    }

    fun verifyOtp() {
        val requestObject = JsonObject()
        requestObject.addProperty("type", AuthMode.mobile.name.lowercase())
        requestObject.addProperty("value", _contactNumber.value?.replace(" ", ""))
        requestObject.addProperty("otp", _enteredOtp.value)
        requestObject.addProperty("country_code", _selectedCountry.value?.code)
        if (isMobileExist) {
            requestObject.addProperty("login", true)
        }

        if (!uuidType.isNullOrEmpty()) {
            requestObject.addProperty("uuid_type", uuidType)

            if (uuidType.equals("google")) {
                if (!googleImageUrl.isNullOrEmpty()) {
                    requestObject.addProperty("image_url", googleImageUrl)
                }
            }
        }
        if (!uuid.isNullOrEmpty()) {
            requestObject.addProperty("uuid", uuid)
        }
        if (!email.isNullOrEmpty()) {
            requestObject.addProperty("email", email)
        }

        viewModelScope.launch {
            authenticationRepository.verifyOtp(requestObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        sendOtp()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                            if (it.user == null) {
                                //Should not happen
                            } else {
                                localDataStore.saveUserInfo(it.user!!)
                                localDataStore.updateUserToken(it.token)
                                authSuccess.postValue(Event(true))
                                //TODO log register event
                                logRegisterEvent()
                            }

                            //_successMessage.postValue(it.message)
                        }/* ?: getConfig()*/
                    }
                }
            }
        }

    }

    @SuppressLint("Range")
    private fun logRegisterEvent() {
        val user = localDataStore.getUser()
        val userInfo = localDataStore.getUser()?.userInfo
        val userGoals = localDataStore.getUser()?.userGoals
        val gender: String =
            if (userInfo?.gender?.lowercase() == Gender.MALE.name.lowercase()) {
                "Male"
            } else if (userInfo?.gender?.lowercase() == Gender.FEMALE.name.lowercase()) {
                "Female"
            } else {
                "Other"
            }
        sessionManager.addUserAttributeToMoEngage(true,
            HashMap<String, Any>().apply
            {
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
        sessionManager.logInsiderAppEvent(
            InsiderAppEvents.REGISTER_SUCCESS,
            HashMap<String, Any>().apply {
                this["user_login_id"] = user?.id ?: ""
                this["method_used"] = "email"
            }
        )
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
}