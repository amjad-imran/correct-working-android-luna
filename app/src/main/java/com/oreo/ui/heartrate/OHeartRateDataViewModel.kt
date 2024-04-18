package com.oreo.ui.heartrate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
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
) : BaseViewModel() {

    var date: String? = null
    var isToday = false
    val heartRateData = MutableLiveData<OHealthOverview.HeartRateDataModel?>()
    var summaryHealthData: ServerUserHealthData? = null

    fun getNudges(): ArrayList<Nudges> {
        val dataList = ArrayList<Nudges>()
        dataList.add(
            Nudges(
                label = "Rest is productive",
                message = "Your resting hr is good"
            )
        )
        dataList.add(
            Nudges(
                label = "Rest is not productive",
                message = "Your resting hr is good"
            )
        )
        return dataList
    }

    fun getLearnMoreData(): ArrayList<LearnMoreDataModel> {
        val dataList = ArrayList<LearnMoreDataModel>()
        dataList.add(
            LearnMoreDataModel(
                label = "2 min read",
                msg = "Article name is goes off",
                img = "https://images.unsplash.com/photo-1696961305234-c56d9af60e34?q=80&w=1935&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            )
        )
        dataList.add(
            LearnMoreDataModel(
                label = "2 min read",
                msg = "Article name is goes off",
                img = "https://images.unsplash.com/photo-1696961305234-c56d9af60e34?q=80&w=1935&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            )
        )
        dataList.add(
            LearnMoreDataModel(
                label = "2 min read",
                msg = "Article name is goes off",
                img = "https://images.unsplash.com/photo-1696961305234-c56d9af60e34?q=80&w=1935&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            )
        )
        dataList.add(
            LearnMoreDataModel(
                label = "2 min read",
                msg = "Article name is goes off",
                img = "https://images.unsplash.com/photo-1696961305234-c56d9af60e34?q=80&w=1935&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            )
        )
        dataList.add(
            LearnMoreDataModel(
                label = "2 min read",
                msg = "Article name is goes off",
                img = "https://images.unsplash.com/photo-1696961305234-c56d9af60e34?q=80&w=1935&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            )
        )
        return dataList
    }

    fun checkIsToday(date: String?) {
        if (date == null) {
            isToday = false
        }
        val todayDate = DateFormats.getCurrentDate(DateFormats.dateFormat3)
        isToday = todayDate.equals(date, true)
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
        LOGS.d("HeartRateData ${Gson().toJson(data.heart)}")
        var breakupArray = data.heart?.break_up
        if (breakupArray.isNullOrEmpty()) {
            val dummyArray = ArrayList<Int>()
            for (i in 0..287) {
                dummyArray.add(0)
            }
            breakupArray = dummyArray
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
        var lastHrValue: Pair<Int, Long>? = null//HR value,timer

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

            var chunkCumulativeValue = 0
            sortedBreakUpList.forEach { value ->
                chunkCumulativeValue += value
            }

            sortedBreakUpList.forEachIndexed { index2, value ->
                val indexMillis = ((index * 6) + index2) * 5 * 60L * 1000L
                lastHrValue = Pair(value, indexMillis)

            }
            //if any change chunk value then divide 12 by that chunk value to get below correct xlabel list
            if (index % 2 == 0) {
                hrCount += 1

            }
            listData.add(
                HRModel(
                    maxValues = maxValue,
                    minValues = minValue,
                    values = sortedBreakUpList,
                    midValues = (maxValue + minValue) / 2
                )
            )
        }
        val average = avgList.average().toFloat()


        val measureState = TapMeasureState.HIDE
        return OHealthOverview.HeartRateDataModel(
            listData,
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
        if (sleep != null) {
            if (sleep.hourly_breakup != null)
                dataList.add(
                    ODayTimeActivitiesDataModel(
                        type = "Sleep",
                        startTime = sleep.hourly_breakup?.firstOrNull()?.start_time,
                        endTime = sleep.hourly_breakup?.lastOrNull()?.end_time,
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