package com.oreo.ui.sleep.nap

import android.graphics.LinearGradient
import android.graphics.Shader
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.OreoNapDetailsDataModel
import com.oreo.data.model.health.Nudges
import com.oreo.data.model.health.UnitDataModelArray
import com.oreo.data.model.health.UnitDataModelArrayFloat
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun getUserNapData() {
        /*viewModelScope.launch {
            userActivityRepository.getUserNapData(
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
                                        getUserNapData()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            //todo return data
                        }
                    }
                }
            }
        }*/
        val data = prepareDummyData()//todo will remove once actual data receive
        _napDetailsResponse.postValue(data)


    }

    private fun prepareDummyData(): OreoNapDetailsDataModel {
        val data = OreoNapDetailsDataModel()
        data.start_time = "2023-12-12 04:06:30"
        data.end_time = "2023-12-12 04:27:00"
        data.sleep_old_score = 81
        data.sleep_new_score = 84
        data.readiness_old_score = 91
        data.readiness_new_score = 96
        data.nap_duration = 360
        data.nap_end_time = 12000
        data.nap_start_time = 10000
        val nudgeListData = ArrayList<Nudges>()
        val nudgeChild1 = Nudges(label = "Nudge1", message = "Test nudge1")
        val nudgeChild2 = Nudges(label = "Nudge2", message = "Test nudge2")
        nudgeListData.add(nudgeChild1)
        nudgeListData.add(nudgeChild2)
        data.nap_nudges = nudgeListData
        //hr data
        data.hrBreakUp = UnitDataModelArray(
            value = arrayListOf(
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                8,
                9,
                10,
                11,
                12,
                13,
                14,
                15,
                16,
                17,
                18,
                19
            ), avg = 45, low = 12, max = 19
        )
        //hrv data
        data.hrvBreakUp = UnitDataModelArray(
            value = arrayListOf(
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                8,
                9,
                10,
                11,
                12,
                13,
                14,
                15,
                16,
                17,
                18,
                19
            ), avg = 45, low = 12, max = 19
        )
        //temperature
        data.temperatureBreakUp = UnitDataModelArrayFloat(
            value = arrayListOf(
                11.0f,
                21.0f,
                31.0f,
                41.0f,
                15.0f,
                61.0f,
                71.0f,
                81.0f,
                91.0f,
                10.0f,
                11.0f,
                12.0f,
                13.0f,
                14.0f,
                15.0f,
                16.0f,
                17.0f,
                18.0f,
                19.0f
            ), avg = 45.0f, max = 91.0f
        )

        return data
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

    fun getNapArrayData(): ArrayList<SleepData.SleepDataBreakup> {
        val child1 = SleepData.SleepDataBreakup(
            startTime = "2023-12-12 04:06:30",
            endTime = "2023-12-12 04:06:30",
            hourOfTheDay = 5,
            sleepType = "deep",
            date = null,
            startDate = null,
            endDate = null,
            duration = 65
        )


        val dataList = ArrayList<SleepData.SleepDataBreakup>()
        dataList.add(child1)
        return dataList

    }

}