package com.oreo.ui.heartrate

import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.noisefit.data.local.db.fromJson
import com.noisefit.luna.R
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.health.Nap
import com.oreo.data.model.health.Nudges
import com.oreo.data.model.health.OreoSleepModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import com.oreo.ui.custom.HRCombineModel
import com.oreo.ui.custom.Item
import com.oreo.ui.custom.Section
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.floor

@HiltViewModel
class OHeartRateDataViewModel @Inject constructor(
    val userRepository: OreoUserActivityRepository
) : BaseViewModel() {

    var date: String? = null
    var isToday = false
    val heartRateData = MutableLiveData<OHealthOverview.HeartRate?>()

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

    fun getStressCombinedData(dayData: ServerUserHealthData): HRCombineModel {

        val stressBreakup =
            Gson().fromJson<List<Int>>("[10,12,13,15,17,19,26,55,77,88,22,44,33,44,10,12,13,15,17,19,26,55,77,88,22,44,33,44,10,12,13,15,17,19,26,55,77,88,22,44,33,44,4,10,12,10,12,13,15,17,19,26,55,77,88,22,44,33,44,10,12,13,15,17,55,44,33,44,10,12,13,15,17,19,26,55,77,88,22,44,33,44,10,12,13,15,17,19,26,55,77,88,22,44,33,44]")

        val workouts = dayData.activity?.workout

        val sections: MutableList<Section> = ArrayList()
        workouts?.forEach {
            getWorkoutSections(it)?.let { pos ->
                sections.add(
                    Section(
                        "workout",
                        pos.first,
                        pos.second,
                        Color.parseColor("#4c8ed3f1"),
                        R.drawable.icon_stress_sport,
                        pos.third
                    )
                )
            }
        }

        getSleepSection(dayData.sleep)?.let { pos ->
            sections.add(
                Section(
                    "sleep",
                    pos.first,
                    pos.second,
                    Color.parseColor("#4cc5a8ed"),
                    R.drawable.icon_stress_sleep
                )
            )
        }

        dayData.sleep?.naps?.forEach { nap ->
            getNapSection(nap)?.let {
                Section(
                    "nap",
                    it.first,
                    it.second,
                    Color.parseColor("#4cc5a8ed"),
                    R.drawable.icon_stress_sleep
                )
            }
        }

        val items: MutableList<Item> = ArrayList<Item>()
        stressBreakup.forEachIndexed { index, i ->
            items.add(Item(i, index))
        }
        val combinedSection = combineSections(sections)


        return HRCombineModel(
            sections = combinedSection,
            items = items,
            high = 70,
            medium = 35
        )
    }

    private fun combineSections(sections: List<Section>): List<Section>? {

        val sortedSection = sections.sortedBy {
            it.start
        }

        LOGS.d("SECTIONS___ Sorted $sortedSection")


        val combinedSection = ArrayList<Section>()

        var current = 0
        var innerLoop = 0
        while (current < sortedSection.size) {
            if (sortedSection[current].type.equals("workout", true)) {
                innerLoop = current
                var count = 1

                val sectionStart = sortedSection[current].start
                var sectionEnd = sortedSection[current].end
                while (innerLoop < sortedSection.size) {
                    val nextItemPos = innerLoop + 1

                    if (nextItemPos == sortedSection.size) break

                    if (sortedSection[innerLoop].end + 1 == sortedSection[innerLoop + 1].start ||
                        sortedSection[innerLoop].start == sortedSection[innerLoop + 1].start
                    ) {
                        sectionEnd = sortedSection[innerLoop + 1].end
                        count++
                    } else {
                        break
                    }

                    innerLoop++
                }
                current = innerLoop

                val combinedSec = sortedSection[current].copy()

                if (count > 1) {
                    combinedSec.start = sectionStart
                    combinedSec.end = sectionEnd
                    combinedSec.type = "combined"
                    combinedSec.count = count
                }



                combinedSection.add(combinedSec)
            } else {
                combinedSection.add(sortedSection[current])
            }

            current++
        }

        LOGS.d("SECTIONS___ $sortedSection \n $combinedSection")

        return combinedSection


    }

    private fun getWorkoutSections(it: OActivityListModal): Triple<Int, Int, String?>? {
        val startTimeStamp = DateFormats.convertDateTimeToTimeStamp(
            "${it.date} ${it.startTime}",
            DateFormats.dateTimeFormat5
        )
        if (startTimeStamp == null || startTimeStamp == 0L) return null

        val day1Minutes =
            DateFormats.getDayElapsedMinutesFromTimeStamp(startTimeStamp) ?: return null

        val day1MinutesCeil = 15 * (floor(abs(day1Minutes.toDouble() / 15)))

        val startPos = (day1MinutesCeil / 15 - 1).toInt()

        var calculatedDuration = startPos + (it.duration ?: 0) / 15
        if (calculatedDuration > 95) {
            calculatedDuration = 95
        }

        return Triple(startPos, calculatedDuration.toInt(), it.iconUrl)
    }

    private fun getNapSection(nap: Nap): Pair<Int, Int>? {

        val startTime = nap.startTime
        val endTime = nap.endTime

        val sleepStartDate = startTime.split(" ")[0]
        val sleepEndDate = endTime.split(" ")[0]

        if (sleepStartDate.equals(sleepEndDate)) {
            //Same day Sleep
            val startTimeStamp = DateFormats.convertDateTimeToTimeStamp(
                startTime,
                DateFormats.dateTimeFormat5
            ) ?: return null
            val day1Minutes =
                DateFormats.getDayElapsedMinutesFromTimeStamp(startTimeStamp) ?: return null
            val day1MinutesCeil = 15 * (floor(abs(day1Minutes.toDouble() / 15)))
            val startPos = (day1MinutesCeil / 15 - 1).toInt()


            val endTimeStamp = DateFormats.convertDateTimeToTimeStamp(
                endTime,
                DateFormats.dateTimeFormat5
            ) ?: return null
            val day1EndMinutes =
                DateFormats.getDayElapsedMinutesFromTimeStamp(endTimeStamp) ?: return null
            val day1EndMinutesCeil = 15 * (floor(abs(day1EndMinutes.toDouble() / 15)))
            var endPos = (day1EndMinutesCeil / 15 - 1).toInt()
            if (endPos > 95) {
                endPos = 95
            }

            return Pair(startPos, endPos)


        } else {
            //Multi day sleep

            val endTimeStamp = DateFormats.convertDateTimeToTimeStamp(
                endTime,
                DateFormats.dateTimeFormat5
            ) ?: return null
            val day1EndMinutes =
                DateFormats.getDayElapsedMinutesFromTimeStamp(endTimeStamp) ?: return null
            val day1EndMinutesCeil = 15 * (floor(abs(day1EndMinutes.toDouble() / 15)))
            var endPos = (day1EndMinutesCeil / 15 - 1).toInt()
            if (endPos > 95) {
                endPos = 95
            }

            return Pair(0, endPos)
        }


    }

    /**
     * "start_time":"2024-01-29 23:34:00",
     * "end_time":"2024-01-29 23:43:30",
     */
    private fun getSleepSection(sleep: OreoSleepModel?): Pair<Int, Int>? {
        val startTime = sleep?.hourly_breakup?.firstOrNull()?.start_time
        val endTime = sleep?.hourly_breakup?.lastOrNull()?.end_time
        if (startTime == null || endTime == null) return null

        val sleepStartDate = startTime.split(" ")[0]
        val sleepEndDate = endTime.split(" ")[0]

        if (sleepStartDate.equals(sleepEndDate)) {
            //Same day Sleep
            val startTimeStamp = DateFormats.convertDateTimeToTimeStamp(
                startTime,
                DateFormats.dateTimeFormat5
            ) ?: return null
            val day1Minutes =
                DateFormats.getDayElapsedMinutesFromTimeStamp(startTimeStamp) ?: return null
            val day1MinutesCeil = 15 * (floor(abs(day1Minutes.toDouble() / 15)))
            val startPos = (day1MinutesCeil / 15 - 1).toInt()


            val endTimeStamp = DateFormats.convertDateTimeToTimeStamp(
                endTime,
                DateFormats.dateTimeFormat5
            ) ?: return null
            val day1EndMinutes =
                DateFormats.getDayElapsedMinutesFromTimeStamp(endTimeStamp) ?: return null
            val day1EndMinutesCeil = 15 * (floor(abs(day1EndMinutes.toDouble() / 15)))
            var endPos = (day1EndMinutesCeil / 15 - 1).toInt()
            if (endPos > 95) {
                endPos = 95
            }

            return Pair(startPos, endPos)


        } else {
            //Multi day sleep

            val endTimeStamp = DateFormats.convertDateTimeToTimeStamp(
                endTime,
                DateFormats.dateTimeFormat5
            ) ?: return null
            val day1EndMinutes =
                DateFormats.getDayElapsedMinutesFromTimeStamp(endTimeStamp) ?: return null
            val day1EndMinutesCeil = 15 * (floor(abs(day1EndMinutes.toDouble() / 15)))
            var endPos = (day1EndMinutesCeil / 15 - 1).toInt()
            if (endPos > 95) {
                endPos = 95
            }

            return Pair(0, endPos)
        }
    }


}