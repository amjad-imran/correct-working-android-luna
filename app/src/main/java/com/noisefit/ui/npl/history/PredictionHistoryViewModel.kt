package com.noisefit.ui.npl.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.model.PredictionHistoryData
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.NPLRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PredictionHistoryViewModel @Inject constructor(val nplRepository: NPLRepository) :
    BaseViewModel() {

    private val _historyList = MutableLiveData<ArrayList<PredictionHistoryData>>()
    val historyList: LiveData<ArrayList<PredictionHistoryData>>
        get() = _historyList

    fun getPredictionHistory() {
        viewModelScope.launch {
            nplRepository.getPredictionHistory()
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
                                        getPredictionHistory()
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data.let {
                                val dataList=ArrayList<PredictionHistoryData>()
                                it?.map {
                                    dataList.add(it)
                                }
                                _historyList.postValue(dataList)
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


}