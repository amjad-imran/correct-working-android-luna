package com.oreo.ui.chatGpt.summary

import android.widget.ProgressBar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.model.AiDailySummaryModel
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AiSummaryViewModel @Inject constructor(
    private val deviceRepository: OreoDeviceRepository
) : BaseViewModel() {


    var isPaused = false

    var currentStoryIndex = 0
    val storyDuration = 6000L

    private val _dailySummaryData = MutableLiveData<Event<List<AiDailySummaryModel>>>()
    val dailySummaryData: MutableLiveData<Event<List<AiDailySummaryModel>>> = _dailySummaryData


    val progressIndicators = ArrayList<ProgressBar>()

    fun getSummaryData() {
       /* _dailySummaryData.postValue(
            Event(
                Gson().fromJson<List<AiDailySummaryModel>>(
                    "[ { \"title\":\"Go out in the Sunlight bliss\", \"bg_image\":\"https://images.pexels.com/photos/14747089/pexels-photo-14747089.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1\", \"sub_title\":\"Step into any environment, whether it’s a bustling street or a crowded café, and let the world fade away. Adaptive ANC does the work so you can focus on what matters.\", \"metrics\":[ { \"name\":\"HRV\", \"value\":32, \"unit\":\"BPM\" },{ \"name\":\"HRV 2\", \"value\":32, \"unit\":\"BPM\" },{ \"name\":\"HRV 3\", \"value\":32, \"unit\":\"BPM\" } ] }, { \"title\":\"2 Go out in the Sunlight bliss\", \"bg_image\":\"https://images.pexels.com/photos/29617299/pexels-photo-29617299/free-photo-of-aerial-view-of-a-city-sports-field.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1\",\"sub_title\":\"Step into any environment, whether it’s a bustling street or a crowded café, and let the world fade away. Adaptive ANC does the work so you can focus on what matters.\", \"metrics\":[ { \"name\":\"HRV\", \"value\":32, \"unit\":\"BPM\" },{ \"name\":\"HRV 2\", \"value\":32, \"unit\":\"BPM\" },{ \"name\":\"HRV 3\", \"value\":32, \"unit\":\"BPM\" } ] }, { \"title\":\"3 Go out in the Sunlight bliss\", \"bg_image\":\"https://images.pexels.com/photos/14643689/pexels-photo-14643689.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1\",\"sub_title\":\"Step into any environment, whether it’s a bustling street or a crowded café, and let the world fade away. Adaptive ANC does the work so you can focus on what matters.\", \"metrics\":[ { \"name\":\"HRV\", \"value\":32, \"unit\":\"BPM\" },{ \"name\":\"HRV 2\", \"value\":32, \"unit\":\"BPM\" },{ \"name\":\"HRV 3\", \"value\":32, \"unit\":\"BPM\" } ] } ]"
                )
            )
        )
        return*/


        viewModelScope.launch(Dispatchers.IO) {
            deviceRepository.getDailySummaryData(
            ).collect { resource ->
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
                                        getSummaryData()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _dailySummaryData.postValue(Event(it))
                        }
                    }
                }
            }
        }
    }

    fun getCurrentStoryData(): AiDailySummaryModel? {
        return try {
            dailySummaryData.value?.peekContent()?.get(currentStoryIndex)
        } catch (exp: Exception) {
            null
        }
    }
}