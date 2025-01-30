package com.oreo.ui.sleep2.sleepplanner.planner

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SleepGoalViewModel @Inject constructor(
    private val userActivityRepository: OreoUserActivityRepository,
    private val localDataStore: DataStoredInterface,
) : BaseViewModel() {


    val userSelectedGoal = MutableLiveData<Event<String>>()
    val goalsUpdated = MutableLiveData<Event<Boolean>>()

    var newSelectedGoalKey: String? = null

    fun getSelectedGoal() {
        viewModelScope.launch {

            userActivityRepository.getSleepPlannerDetails().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getSelectedGoal()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            withContext(Dispatchers.IO){
                                userActivityRepository.removeSleepPlannerData()
                                userSelectedGoal.postValue(Event(it?.goal ?: ""))
                                newSelectedGoalKey = it?.goal
                            }
                        }
                    }
                }
            }
        }
    }

    fun updateGoal() {
        viewModelScope.launch {

            val request = JsonObject().apply {
                this.addProperty("goal", newSelectedGoalKey)
            }

            userActivityRepository.updateUserSleepGoal(request).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        updateGoal()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            goalsUpdated.postValue(Event(true))
                        }
                    }
                }
            }
        }

    }
}