package com.oreo.data.dataConverter

import android.graphics.Color
import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit_commans.common.averageWithoutZero
import com.noisefit_commans.common.averageWithoutZeroFloat
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.model.DayTimeMovementBreakup
import com.noisefit_commans.data.model.OreoBloodOxygenBreakup
import com.noisefit_commans.data.model.OreoBodyStressData
import com.noisefit_commans.data.model.OreoBodyTempNetworkEntity
import com.noisefit_commans.data.model.OreoBodyTemperatureBreakup
import com.noisefit_commans.data.model.OreoCommonNetworkEntity
import com.noisefit_commans.data.model.OreoHeartNetworkEntity
import com.noisefit_commans.data.model.OreoHeartRate
import com.noisefit_commans.data.model.OreoRespiratoryData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.data.model.OreoSleepNetworkEntity
import com.noisefit_commans.data.model.OreoStepsData
import com.noisefit_commans.data.model.OreoStepsNetworkEntity
import com.noisefit_commans.data.model.OreoStressDataBreakup
import com.noisefit_commans.data.model.OreoUserDataPost
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.db.abstaction.OreoBodyTemperatureDataSource
import com.oreo.data.db.implementation.OreoBloodOxygenDataImpl
import com.oreo.data.db.implementation.OreoHeartRateDataImpl
import com.oreo.data.db.implementation.OreoRespiratoryDataImpl
import com.oreo.data.db.implementation.OreoStressDataImpl
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OreoUserSyncActivities
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.SleepOverlayData
import com.oreo.data.model.health.OreoSleepModel
import com.oreo.ui.custom.Item
import com.oreo.ui.custom.Section
import com.oreo.ui.custom.StressCombineModel
import java.text.SimpleDateFormat
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

class OreoStressDataConvertor
@Inject
constructor(
) {

    /**
     * Stress
     */

    fun getStressCombinedData(dayData: ServerUserHealthData): StressCombineModel {

//        val stressBreakup =
//            Gson().fromJson<List<Int>>("[10,12,13,15,17,19,26,55,77,88,22,44,33,44,10,12,13,15,17,19,26,55,77,88,22,44,33,44,10,12,13,15,17,19,26,55,77,88,22,44,33,44,4,10,12,10,12,13,15,17,19,26,55,77,88,22,44,33,44,10,12,13,15,17,55,44,33,44,10,12,13,15,17,19,26,55,77,88,22,44,33,44,10,12,13,15,17,19,26,55,77,88,22,44,33,44]")

        val workouts = dayData.activity?.workout

        val sections: MutableList<Section> = ArrayList()
        workouts?.forEach {
            getWorkoutSections(it)?.let { pos ->
                sections.add(
                    Section(
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
                    pos.first,
                    pos.second,
                    Color.parseColor("#4cc5a8ed"),
                    R.drawable.icon_stress_sleep
                )
            )
        }

        val items: MutableList<Item> = ArrayList<Item>()

        dayData.stress?.breakUp?.forEachIndexed { index, i ->
            items.add(Item(i, index))
        }


        return StressCombineModel(
            sections = sections,
            items = items,
            high = 70,
            medium = 30
        )
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