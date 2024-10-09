package com.noisefit.ui.profile

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.model.referral.ReferralInfoResponse
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.AuthenticationRepository
import com.noisefit.data.repository.abstraction.ReferralRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit.watch.ConnectionHandler
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.User
import com.noisefit_commans.models.HeightUnitSystem
import com.noisefit_commans.models.Units
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.BuildUtils
import com.noisefit_commans.utils.Event
import com.oreo.data.model.femaleh.FemaleCycleTrackInfoModel
import com.oreo.data.repository.abstraction.FemaleHealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel
@Inject
constructor(
    var connectionHandler: ConnectionHandler,
    var sessionManager: SessionManager,
    private val repository: AuthenticationRepository,
    val localDataStore: DataStoredInterface,
    val ringDataStore: RingDataStore,
    private val userRepository: UserRepository,
    private val referralRepository: ReferralRepository,
    private val femaleHealthRepository: FemaleHealthRepository,
) : BaseViewModel() {

    private var _user = MutableLiveData<User>()
    private var _logoutSuccess = MutableLiveData<Boolean>()
    var numberAvailable = MutableLiveData<Boolean>()

    var unit = Units.METRIC


    val referralRunningState = MutableLiveData<ReferralRunningState>(ReferralRunningState.Default)
    var referralResponse: ReferralInfoResponse? = null


    fun getUser(): LiveData<User> = _user
    fun logoutSuccess(): LiveData<Boolean> = _logoutSuccess

    fun getEndGameValue(): String {
        val endGameKey = localDataStore.getUser()?.endGame
        val endGameList = localDataStore.getEndGameList()
        if (endGameKey.isNullOrEmpty()) {
            return "Not Set"
        }
        try {
            endGameList.forEach {
                if (it.id == endGameKey.toInt()) {
                    return it.title
                }
            }
        } catch (exp: Exception) {
            return "Not Set"
        }
        return "Not Set"
    }

    fun getUnitValue(): String {
        val unitName = localDataStore.getUnit().name
        return if (unitName.lowercase() == HeightUnitSystem.METRIC.name.lowercase())
            METRIC
        else
            IMPERIAL
    }

    fun isProfileSetupPending(): Boolean {
        val localUser = localDataStore.getUser()

        return localUser?.firstName.isNullOrEmpty() ||
                localUser?.userInfo?.dob.isNullOrEmpty()
    }

    init {
        numberAvailable.value = !localDataStore.getUser()?.mobile.isNullOrEmpty()
        getReferralInfo()
    }


    fun logoutUser() {
        viewModelScope.launch {
            repository.logoutUser().collect { resource ->
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
                                        logoutUser()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.let {
                            repository.logoutUserLocally().collect {
                                if (it) {
                                    localDataStore.setEndGameValue("")
                                    localDataStore.deleteYearlyGoal()
                                    localDataStore.setGoogleFitStatus(false)
                                    ringDataStore.setGoogleFitCrossed(false)
                                    _logoutSuccess.value = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun deleteUser() {
        viewModelScope.launch {
            repository.deleteUser().collect { resource ->
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
                                        deleteUser()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.let {
                            repository.logoutUserLocally().collect {
                                if (it) {
                                    localDataStore.setGoogleFitStatus(false)
                                    localDataStore.setEndGameValue("")
                                    ringDataStore.setGoogleFitCrossed(false)
                                    _logoutSuccess.value = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    fun getUserData() {
        _user.value = (userRepository.getUser())
    }

    fun getDeviceName(): String {

        val manufacturer: String = BuildUtils.getDeviceManufacturer()
        val model: String = BuildUtils.getDeviceModel()
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else capitalize(manufacturer) + " " + model
    }

    private fun capitalize(str: String): String {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capitalizeNext = true
        val phrase = StringBuilder()
        for (c in arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(c.uppercaseChar())
                capitalizeNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true
            }
            phrase.append(c)
        }
        return phrase.toString()
    }


    private val _cycleTrackInfo = MutableLiveData<Event<FemaleCycleTrackInfoModel?>>()
    val cycleTrackInfo: LiveData<Event<FemaleCycleTrackInfoModel?>?> = _cycleTrackInfo

    private val _showFemaleHealthSplash = MutableLiveData<Event<Boolean?>>()
    val showFemaleHealthSplash: LiveData<Event<Boolean?>?> = _showFemaleHealthSplash

    fun getCycleTrackerInfo() {
        viewModelScope.launch {
            femaleHealthRepository.getCycleTrackerInfo().collect { resource ->
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
                                object :
                                    BinaryActionCallback {
                                    override fun yes() {
                                        getCycleTrackerInfo()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            if (it == null) {
                                _showFemaleHealthSplash.postValue(Event(true))
                            } else {
                                if (it.periodDate.isNullOrEmpty()) {
                                    _showFemaleHealthSplash.postValue(Event(true))
                                } else {
                                    _cycleTrackInfo.postValue(Event(it))
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    private fun getReferralInfo() {
        viewModelScope.launch {
            referralRepository.getReferralInfo().collect { resource ->
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
                                object :
                                    BinaryActionCallback {
                                    override fun yes() {
                                        getReferralInfo()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {

                            referralResponse = it
                            referralRunningState.postValue(getReferralRunningState(it))

                        }
                    }
                }
            }
        }
    }

    private fun getReferralRunningState(referralInfoResponse: ReferralInfoResponse?): ReferralRunningState {
        if (referralInfoResponse == null) return ReferralRunningState.Default

        return if (referralInfoResponse.banner.isNullOrEmpty()) {
            if(referralInfoResponse.prize!=null){
                ReferralRunningState.Available(
                    referralInfoResponse.referralImage,
                    referralInfoResponse.referralText
                )
            }else{
                ReferralRunningState.NotAvailable
            }
        } else {
            ReferralRunningState.Available(
                referralInfoResponse.referralImage,
                referralInfoResponse.referralText
            )
        }
    }


}

sealed class ReferralRunningState {
    data class Available(val prizeImageUrl: String? = null, val prizeTitle: String? = null) :
        ReferralRunningState()

    data object NotAvailable : ReferralRunningState()
    data object Default : ReferralRunningState()
}