package com.oreo.ui.sleep.nap

import android.graphics.LinearGradient
import android.graphics.Shader
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.OreoNapDetailsDataModel
import com.oreo.data.model.SlideUpNapScoreDataModel
import com.oreo.data.model.health.Nudges
import com.oreo.data.model.health.UnitDataModelArray
import com.oreo.data.model.health.UnitDataModelArrayFloat
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ONapDetailsViewModel @Inject constructor(val userActivityRepository: OreoUserActivityRepository) :
    BaseViewModel() {
    private val _napDetailsResponse = MutableLiveData<OreoNapDetailsDataModel>()
    val napDetailsResponse: LiveData<OreoNapDetailsDataModel> = _napDetailsResponse
    fun setTextGradient(
        textView: TextView,
        textColor: Int,
        startGradColor: Int,
        endGradColor: Int
    ) {
        textView.setTextColor(textColor)
        val textShader: Shader = LinearGradient(
            0f,
            textView.paint.measureText(textView.text.toString()),
            0f,
            0f,
            intArrayOf(
                endGradColor,
                startGradColor
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        textView.paint.shader = textShader
    }

    fun getUserNapData(napId:String) {
        viewModelScope.launch {
            userActivityRepository.getUserNapData(
                napId
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
                                        getUserNapData(napId)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _napDetailsResponse.postValue(it)

                        }
                    }
                }
            }
        }
    }



    fun getDummyBreakUpDataForTimeDisplay(): ArrayList<Int> {
        val dummyList = ArrayList<Int>()
        for (i in 0..287) {
            dummyList.add(0)
        }
        return dummyList

    }

    fun getDummyBreakUpDataForTimeDisplayFloat(): ArrayList<Float> {
        val dummyList = ArrayList<Float>()
        for (i in 0..287) {
            dummyList.add(0f)
        }
        return dummyList

    }

    fun getNapArrayData(duration:Int): ArrayList<SleepData.SleepDataBreakup> {
        val child1 = SleepData.SleepDataBreakup(
            sleepType = "deep",
            date = null,
            startDate = null,
            endDate = null,
            duration = duration
        )


        val dataList = ArrayList<SleepData.SleepDataBreakup>()
        dataList.add(child1)
        return dataList

    }
}