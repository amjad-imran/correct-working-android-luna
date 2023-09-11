package com.noisefit.ui.profile

import android.util.DisplayMetrics
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.User
import com.noisefit_commans.data.model.UserStats
import com.noisefit_commans.data.model.trophies.TrophyBadge
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.*
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.ConnectionUtil
import com.noisefit_commans.utils.Event
import com.noisefit.watch.ConnectionHandler
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.HeightUnitSystem
import com.noisefit_commans.models.Units
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel
@Inject
constructor(
    var connectionHandler: ConnectionHandler,
    var sessionManager: SessionManager,
    private val repository: AuthenticationRepository,
    private val lastSyncProvider: LastSyncProvider,
    val localDataStore: DataStoredInterface,
    private val userRepository: UserRepository,
    private val connectionUtil: ConnectionUtil
) : BaseViewModel() {

    private var _user = MutableLiveData<User>()
    private var _userStats = MutableLiveData<UserStats>()
    private val _orderUrl = MutableLiveData<Event<String>>()
    private var _logoutSuccess = MutableLiveData<Boolean>()
    var numberAvailable = MutableLiveData<Boolean>()
    val defaultInterestSymbol = "-"

    val trophies: MutableLiveData<List<TrophyBadge>> = MutableLiveData<List<TrophyBadge>>()
    var unit = Units.METRIC

    fun getUser(): LiveData<User> = _user
    fun getStats(): LiveData<UserStats> = _userStats
    fun logoutSuccess(): LiveData<Boolean> = _logoutSuccess
    fun orderUrl(): LiveData<Event<String>> = _orderUrl
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

    fun getUnitValueForRecentTrophy(): Units {
        unit = localDataStore.getUnit()
        return unit
    }

    fun getDeviceWidth(context: FragmentActivity): Int {
        val displaymetrics = DisplayMetrics()
        context.windowManager.defaultDisplay.getMetrics(displaymetrics)
        //if you need three fix imageview in width
        //if you need three fix imageview in width
        return displaymetrics.widthPixels / 3

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
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
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
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
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
                                    localDataStore.setEndGameValue("")
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


    fun getOrderToken() {
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            val userEmail = localDataStore.getUser()?.email
            val ipAddress = connectionUtil.getPublicIPAddress()
            val requestObject = JsonObject().apply {
                val customerObject = JsonObject()
                customerObject.addProperty("email", userEmail)
                customerObject.addProperty("remote_ip", ipAddress)
                this.add("customer", customerObject)
            }

            userRepository.getOrderToken(requestObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getOrderToken()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _orderUrl.postValue(Event("https://mansinoise.myshopify.com/account/login/multipass/${it}"))
                        }
                    }
                }
            }
        }

    }

    fun getRecentTrophies() {
        viewModelScope.launch {
            userRepository.getRecentTrophies().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.data?.let { response ->
                            trophies.postValue(response)
                        }
                    }
                    else -> {}
                }
            }
        }

    }


}