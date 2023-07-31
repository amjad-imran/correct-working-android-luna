package com.noisefit.ui.reward.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.TaskList
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.NPLRepository
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllTaskViewModel @Inject constructor(
    val rewardsRepository: RewardsRepository,
    val nplRepository: NPLRepository
) :
    BaseViewModel() {
    private val _taskList = MutableLiveData<List<TaskList>>()
    val taskList: LiveData<List<TaskList>>
        get() = _taskList


    private val _userCoins = MutableLiveData<Int>()
    val userCoins: LiveData<Int>
        get() = _userCoins

    fun getTaskListData() {
        viewModelScope.launch {
            rewardsRepository.getTaskList()
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
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        getTaskListData()
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data.let {
                                _taskList.postValue(it?.taskList ?: ArrayList())
                                _userCoins.postValue(it?.points ?: 0)
                            }

                        }
                    }
                }
        }
    }

    fun collectCoupon(transactionId: Int, updateSuccess: (points: Int) -> Unit) {
        viewModelScope.launch {
            val jsonRequest = JsonObject()
            jsonRequest.addProperty("transaction_id", transactionId)
            rewardsRepository.collectPoints(jsonRequest)
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
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        collectCoupon(transactionId, updateSuccess)
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data.let {
                                it?.points?.let { points ->
                                    updateSuccess(points)
                                }
                            }

                        }
                    }
                }
        }
    }

    fun updateCoins(coins: Int) {
        _userCoins.postValue(coins)
    }

    fun collectNplCoupon(predictionId: Int, updateSuccess: (points: Int) -> Unit) {
        viewModelScope.launch {
            val jsonRequest = JsonObject()
            jsonRequest.add("prediction_id_array", JsonArray().apply {
                this.add(predictionId)
            })
            nplRepository.collectNplReward(jsonRequest)
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
                                this.uiComponentType as UIComponentType.RetryApiDialog
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        collectCoupon(predictionId, updateSuccess)
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data.let {
                                it?.points?.let { points ->
                                    updateSuccess(points)
                                }
                            }

                        }
                    }
                }
        }
    }


}