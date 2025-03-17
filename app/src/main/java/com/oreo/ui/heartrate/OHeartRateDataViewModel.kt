package com.oreo.ui.heartrate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit_commans.common.maxWithoutZero
import com.noisefit_commans.common.minWithoutZero
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.dataConverter.OreoHRDataConvertor
import com.oreo.data.model.HRModel
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.data.model.ODayTimeActivitiesDataModel
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.TapMeasureState
import com.oreo.data.model.health.Nudges
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OHeartRateDataViewModel @Inject constructor(
    val userRepository: OreoUserActivityRepository,
    val hrDataConvertor: OreoHRDataConvertor,
    private val resourcesProvider: ResourcesProvider
) : BaseViewModel() {

    var date: String? = null
    val heartRateData = MutableLiveData<OHealthOverview.HeartRateDataModel?>()
    var summaryHealthData: ServerUserHealthData? = null


    fun getLearnMoreData(): ArrayList<LearnMoreDataModel> {
        val dataList = ArrayList<LearnMoreDataModel>()
        dataList.add(
            LearnMoreDataModel(
                title = resourcesProvider.getString(R.string.text_general_heart_rate_terms),
                msg = resourcesProvider.getString(R.string.text__2_min_read),
                img = R.drawable.img_hr_article_1,
                type = 1
            )
        )
        dataList.add(
            LearnMoreDataModel(
                title = resourcesProvider.getString(R.string.text_normal_heart_rate_for_my_age),
                msg = resourcesProvider.getString(R.string.text__2_min_read),
                img = R.drawable.img_hr_article_2,
                type = 2
            )
        )
        dataList.add(
            LearnMoreDataModel(
                title = resourcesProvider.getString(R.string.text_what_are_heart_rate_zones),
                msg = resourcesProvider.getString(R.string.text__2_min_read),
                img = R.drawable.img_hr_article_3,
                type = 3
            )
        )
        dataList.add(
            LearnMoreDataModel(
                title = resourcesProvider.getString(R.string.text_heart_rate_during_sleep),
                msg = resourcesProvider.getString(R.string.text__2_min_read),
                img = R.drawable.img_hr_article_4,
                type = 4
            )
        )
        return dataList
    }

    fun getTodayHeartRate() {
        viewModelScope.launch(Dispatchers.IO) {
            heartRateData.postValue(userRepository.getSummaryHRHealthOverview().apply {
            })
        }
    }

    fun parseHealthData(healthData: ServerUserHealthData) {
        heartRateData.postValue(parseHrData(healthData))
    }

    private fun parseHrData(data: ServerUserHealthData): OHealthOverview.HeartRateDataModel {
        var breakupArray = data.heart?.break_up
        if (breakupArray.isNullOrEmpty()) {
            val dummyArray = ArrayList<Int>()
            for (i in 0..287) {
                dummyArray.add(0)
            }
            breakupArray = dummyArray
        }
        var lastHrValue: Pair<Int, Long>? = null//HR value,timer
        breakupArray.forEachIndexed { index2, value ->
            if (value != 0 && value != 255)
                lastHrValue = Pair(value, 0)

        }
        val excludeDataList = arrayListOf<Int>()
        breakupArray.forEach { value ->
            if (value == 255) {
                excludeDataList.add(0)
            } else
                excludeDataList.add(value)
        }

        val hRWithIntervalList = excludeDataList.chunked(6)
        val avgList = ArrayList<Int>()
        var overAllMinValue = Int.MAX_VALUE
        var overAllMaxValue = -1
        var hrCount = 0

        val listData = ArrayList<HRModel>()
        hRWithIntervalList.forEachIndexed { index, hrList ->
            val sortedBreakUpList = hrList.sorted()

            val minValue = sortedBreakUpList.minWithoutZero()
            val maxValue = sortedBreakUpList.maxWithoutZero()

            var min = minValue
            var max = maxValue

            if (min == 0 && max != 0) {
                min = max
            }

            if (max == 0 && min != 0) {
                max = min
            }


            val avg = (min + max) / 2
            if (avg != 0) {
                if (min < overAllMinValue) {
                    overAllMinValue = min
                }
                if (max > overAllMaxValue) {
                    overAllMaxValue = max
                }
                avgList.add(avg)
            }


            //if any change chunk value then divide 12 by that chunk value to get below correct xlabel list
            if (index % 2 == 0) {
                hrCount += 1

            }
            listData.add(
                HRModel(
                    maxValues = max,
                    minValues = min,
                    values = sortedBreakUpList,
                    midValues = avg
                )
            )
        }
        val average = avgList.average().toFloat()


        val measureState = TapMeasureState.HIDE
        return OHealthOverview.HeartRateDataModel(
            listData,
            rawData = breakupArray,
            average = average,
            "0",
            value = lastHrValue?.first.toString(),
            maxValues = breakupArray.maxWithoutZero(),
            minValues = breakupArray.minWithoutZero(),
            measureState
        )
    }

    var activityData: ArrayList<ODayTimeActivitiesDataModel>? = null
    fun prepareActivityData(dayData: ServerUserHealthData) {
        val workouts = dayData.activity?.workout
        val sleep = dayData.sleep
        val dataList = ArrayList<ODayTimeActivitiesDataModel>()
        workouts?.forEach {
            dataList.add(
                ODayTimeActivitiesDataModel(
                    type = "Workout",
                    workoutData = it
                )
            )
        }

        sleep?.sleeps?.forEach {
            dataList.add(
                ODayTimeActivitiesDataModel(
                    type = "Sleep",
                    startTime = it.startTime,
                    endTime = it.endTime,
                    dateTime = it.startTime
                )
            )
        }

        sleep?.naps?.forEach { nap ->
            if (nap.date.equals(dayData.date) && !nap.isNextDayNap) {
                dataList.add(
                    ODayTimeActivitiesDataModel(
                        type = "Nap",
                        id = nap.id,
                        startTime = nap.startTime,
                        endTime = nap.endTime
                    )
                )
            }
        }
        activityData = dataList
    }


}