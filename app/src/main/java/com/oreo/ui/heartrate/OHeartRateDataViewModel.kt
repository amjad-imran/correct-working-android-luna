package com.oreo.ui.heartrate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
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
                title = "General heart rate terms",
                msg = "2 min read",
                img = R.drawable.img_hr_article_1,
                type = 1
            )
        )
        dataList.add(
            LearnMoreDataModel(
                title = "Normal heart rate for my age",
                msg = "2 min read",
                img = R.drawable.img_hr_article_2,
                type = 2
            )
        )
        dataList.add(
            LearnMoreDataModel(
                title = "What are heart rate zones ?",
                msg = "2 min read",
                img = R.drawable.img_hr_article_3,
                type = 3
            )
        )
        dataList.add(
            LearnMoreDataModel(
                title = "Heart rate during sleep",
                msg = "2 min read",
                img = R.drawable.img_hr_article_4,
                type = 4
            )
        )
        return dataList
    }

    fun checkIsToday(date: String?) {
        if (date == null) {
            isToday = false
        }
        val todayDate = DateFormats.getCurrentDate(DateFormats.dateFormat3())
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