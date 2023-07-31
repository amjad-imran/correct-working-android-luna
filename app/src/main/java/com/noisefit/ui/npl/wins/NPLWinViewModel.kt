package com.noisefit.ui.npl.wins

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.NPLRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.response.LiveMatch
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.tryCatch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NPLWinViewModel @Inject constructor(val nplRepository: NPLRepository) : BaseViewModel() {
    private val _rewardCollect = MutableLiveData<Any>()
    val rewardCollect: LiveData<Any>
        get() = _rewardCollect

    private val _userCoins = MutableLiveData<Int>()
    val userCoins: LiveData<Int>
        get() = _userCoins


    private val _nplWinListData = MutableLiveData<ArrayList<LiveMatch>>()
    val nplWinListData: LiveData<ArrayList<LiveMatch>>
        get() = _nplWinListData


    fun getNplWinsData() {
        viewModelScope.launch {
            nplRepository.getNplWins()
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
                                        getNplWinsData()
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data.let { it ->
                                val listData = ArrayList<LiveMatch>()
                                it?.response?.map {
                                    listData.add(it)
                                }
                                _nplWinListData.postValue(listData)
                                _userCoins.postValue(it?.user_points ?: 0)
                            }


                        }
                    }
                }
        }

    }

    fun collectReward(predictionId: ArrayList<Int>, updateSuccess: (points: Int) -> Unit) {
        val jsonRequest = JsonObject()
        jsonRequest.add("prediction_id_array", JsonArray().apply {
            predictionId.forEach {
                this.add(it)
            }
        })
        viewModelScope.launch {
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
                                        collectReward(predictionId, updateSuccess)
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


    fun getPredictionId(): ArrayList<Int> {
        val preList = ArrayList<Int>()
        _nplWinListData.value?.map {
            it.prediction_id?.toInt()?.let { it1 -> preList.add(it1) }
        }
        return preList
    }

    fun removeItem(predictionId: Long) {
        val list = _nplWinListData.value ?: return
        tryCatch {
            val index = list.indexOfFirst {
                it.prediction_id == predictionId
            }
            list.removeAt(index)
            _nplWinListData.postValue(list)
        }
    }
    fun removeAll(){
        _nplWinListData.postValue(ArrayList())
    }
}