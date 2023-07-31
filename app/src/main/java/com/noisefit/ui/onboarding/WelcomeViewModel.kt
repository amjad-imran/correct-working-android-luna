package com.noisefit.ui.onboarding

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.ConfigResponse
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class WelcomeViewModel
@Inject
constructor(
    private val localDataStore: DataStoredInterface,
    private val deviceRepository: DeviceRepository,
    private val watchDataStore: WatchDataStore,
    var sessionManager: SessionManager
) : BaseViewModel() {
    var images: List<String> = ArrayList()
    var imageCounter = 0
    private val _killApp = MutableLiveData<Event<Boolean>>()
    private val _config = MutableLiveData<ConfigResponse>()
    var config = _config
    var killApp = _killApp

    var displayImage = MutableLiveData<String>()

    var autoContinue = false


    init {
//        getConfig()
    }

    fun getConfig() {
        viewModelScope.launch {
            deviceRepository.getConfig().collect { resource ->
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
                                    getConfig()
                                }

                                override fun no() {
                                    _killApp.postValue(Event(true))
                                }
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            it.images?.let {
                                images = it
                            }
                            it.endGames?.let { endGames ->
                                localDataStore.setEndGameList(endGames)
                            }
                            _config.postValue(it)
                        }/* ?: getConfig()*/
                    }
                }
            }
        }

    }

}