package com.noisefit.ui.settings.options

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    val userRepository: UserRepository,
    var sessionManager: SessionManager
) : BaseViewModel() {

    var valueUpdate = MutableLiveData<Event<Boolean>>()

    var masterToggle = false
    var hydrationToggle = false
    var stepsToggle = false
    var sleepToggle = false

    fun getNotificationToggle() {
        viewModelScope.launch {
            userRepository.getNotificationToggle().collect { resource ->
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
                                        getNotificationToggle()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            masterToggle = it.master_notification

                            if (masterToggle) {
                                hydrationToggle = it.hydrate_notification
                                stepsToggle = it.steps_notification
                                sleepToggle = it.sleep_notification
                            }
                            valueUpdate.postValue(Event(true))
                        }
                    }
                }
            }
        }

    }


    fun updateNotificationToggle() {
        viewModelScope.launch {

            val request = JsonObject().apply {
                this.addProperty("master_notification", masterToggle)
                this.addProperty("hydrate_notification", hydrationToggle)
                this.addProperty("steps_notification", stepsToggle)
                this.addProperty("sleep_notification", sleepToggle)
            }
            userRepository.updateNotificationToggle(request)
                .collect { resource ->
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
                                            updateNotificationToggle()
                                        }

                                        override fun no() {}
                                    }
                            })
                        }

                        is Resource.Success -> {
                            resource.data?.data?.let {


                            }
                        }
                    }
                }
        }
    }
}