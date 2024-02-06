package com.oreo.ui.stress

import android.graphics.Color
import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.common.maxWithInvalidMovementValues
import com.noisefit_commans.common.maxWithoutInvalidMovementValues
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.ScreenUtils
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OStressActivitiesDataModel
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.Stress
import com.oreo.data.model.health.OreoSleepModel
import com.oreo.ui.custom.Item
import com.oreo.ui.custom.Section
import com.oreo.ui.custom.StressCombineModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.floor

@HiltViewModel
class OStressDetailViewModel @Inject
constructor(
    val screenUtils: ScreenUtils
) : BaseViewModel() {

    var date: String? = null


    fun getCombinedMovementData(
        originalList: List<Int>?,
        includeInvalid: Boolean = false
    ): List<Int> {
        if (originalList.isNullOrEmpty()) {
            return MutableList(96) { 255 }
        }
        val combinedList = ArrayList<Int>()
        for (i in originalList.indices step 3) {
            val endIndex = i + 3
            if (endIndex <= originalList.size) {
                val max = if (includeInvalid) {
                    originalList.subList(i, endIndex).maxWithInvalidMovementValues()
                } else {
                    originalList.subList(i, endIndex).maxWithoutInvalidMovementValues()
                }
                combinedList.add(max)
            }
        }
        return combinedList
    }



    var stressActivityData: ArrayList<OStressActivitiesDataModel>? = null
    fun prepareStressActivityData(dayData: ServerUserHealthData) {
        val workouts = dayData.activity?.workout
        val sleep = dayData.sleep
        val dataList = ArrayList<OStressActivitiesDataModel>()
        workouts?.forEach {
            dataList.add(
                OStressActivitiesDataModel(
                    type = "Workout",
                    startTime = it.startTime,
                    endTime = it.endTime,
                    id = it.id
                )
            )
        }
        if (sleep != null) {
            if (sleep.hourly_breakup != null)
                dataList.add(
                    OStressActivitiesDataModel(
                        type = "Sleep",
                        startTime = sleep.hourly_breakup?.firstOrNull()?.start_time,
                        endTime = sleep.hourly_breakup?.lastOrNull()?.end_time,
                        id = ""
                    )
                )
        }
        stressActivityData = dataList
    }

    fun getStressCombinedData(dayData: ServerUserHealthData): StressCombineModel {

        val stressBreakup =
            Gson().fromJson<List<Int>>("[10,12,13,15,17,19,26,55,77,88,22,44,33,44,10,12,13,15,17,19,26,55,77,88,22,44,33,44,10,12,13,15,17,19,26,55,77,88,22,44,33,44,4,10,12,10,12,13,15,17,19,26,55,77,88,22,44,33,44,10,12,13,15,17,55,44,33,44,10,12,13,15,17,19,26,55,77,88,22,44,33,44,10,12,13,15,17,19,26,55,77,88,22,44,33,44]")

        val workouts = dayData.activity?.workout

        val sections: MutableList<Section> = ArrayList()
        workouts?.forEach {
            getWorkoutSections(it)?.let { pos ->
                sections.add(
                    Section(
                        pos.first,
                        pos.second,
                        Color.parseColor("#00BCD4"),
                        R.drawable.icon_stress_sport
                    )
                )
            }
        }

        getSleepSection(dayData.sleep)?.let { pos ->
            sections.add(
                Section(
                    pos.first,
                    pos.second,
                    Color.parseColor("#C4A9F5"),
                    R.drawable.icon_stress_sleep
                )
            )
        }

        val items: MutableList<Item> = ArrayList<Item>()

        stressBreakup?.forEachIndexed { index, i ->
            items.add(Item(i, index))
        }


        return StressCombineModel(
            sections = sections,
            items = items,
            high = 70,
            medium = 30
        )
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

    private fun getWorkoutSections(it: OActivityListModal): Pair<Int, Int>? {
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

        return Pair(startPos, calculatedDuration.toInt())
    }


    fun getStressMinutes(stress: Stress?): Triple<Int, Int, Int> {
        //return Triple(32, 21, 10)
        if (stress?.breakup.isNullOrEmpty()) return Triple(0, 0, 0)

        var calmCount = 0
        var focusedCount = 0
        var stressedCount = 0

        stress?.breakup?.forEach { stressValue ->
            when (stressValue) {
                0 -> {}
                in 1..34 -> {
                    calmCount++
                }

                in 35..69 -> {
                    focusedCount++
                }

                else -> {
                    stressedCount++
                }
            }

        }
        return Triple(calmCount * 15, focusedCount * 15, stressedCount * 15)
    }


}