package com.oreo.data.dataConverter

import android.graphics.Color
import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit_commans.common.maxWithInvalidMovementValues
import com.noisefit_commans.common.maxWithoutInvalidMovementValues
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.DayTimeDataModel
import com.oreo.data.model.Item
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.Section
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.health.Nap
import com.oreo.data.model.health.OreoSleepModel
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.floor

class OreoDayTimeDataConvertor @Inject constructor() {

    private fun getWorkoutIntensity(intensity: String?): Int {
        //"Easy", "Moderate", "Hard"
        when (intensity?.lowercase()) {
            "easy" -> {
                return 1
            }

            "moderate" -> {
                return 2
            }

            "hard" -> {
                return 3
            }

            else -> {
                return 0
            }
        }
    }

    fun getDayTimeCombinedData(
        dayData: ServerUserHealthData
    ): DayTimeDataModel {
//        val stressBreakup =
//            Gson().fromJson<List<Int>>("[10,12,13,15,17,19,26,55,77,88,22,44,33,44,10,12,13,15,17,19,26,55,77,88,22,44,33,44,10,12,13,15,17,19,26,55,77,88,22,44,33,44,4,10,12,10,12,13,15,17,19,26,55,77,88,22,44,33,44,10,12,13,15,17,55,44,33,44,10,12,13,15,17,19,26,55,77,88,22,44,33,44,10,12,13,15,17,19,26,55,77,88,22,44,33,44]")
        val workouts = dayData.activity?.workout
        val workoutSectionIntensity = ArrayList<Triple<Int, Int, Int>>()
        val sections: MutableList<Section> = ArrayList()
        workouts?.forEach {

            getWorkoutSections(it)?.let { pos ->
                if (it.type.equals("manual", true) || it.type.equals(
                        "automanual",
                        true
                    )
                ) {//TODO test
                    workoutSectionIntensity.add(
                        Triple(
                            pos.first, pos.second, getWorkoutIntensity(it.intensity)
                        )
                    )
                }

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

        getSleepSection(dayData.sleep, dayData.date)?.let { pos ->
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
            getNapSection(nap, dayData.date)?.let {
                sections.add(
                    Section(
                        "nap",
                        it.first,
                        it.second,
                        Color.parseColor("#4cc5a8ed"),
                        R.drawable.icon_stress_sleep
                    )
                )
            }
        }

        val items: MutableList<Item> = ArrayList<Item>()
        val combinedMovementData =
            getCombinedMovementData(dayData.activity?.daytimeMovement?.movement, true)

        combinedMovementData.forEachIndexed { index, i ->
            items.add(Item(i, index))
        }
        val combinedSection = combineSections(sections)

        updateInactiveStateForWorkout(items, workoutSectionIntensity)

        return DayTimeDataModel(
            sections = combinedSection,
            items = items
        )
    }


    private fun updateInactiveStateForWorkout(
        itemList: MutableList<Item>,
        workoutSectionIntensity: ArrayList<Triple<Int, Int, Int>>
    ) {

        workoutSectionIntensity.forEach { sectionWithIntensity ->
            var count = 0
            for (i in sectionWithIntensity.first..sectionWithIntensity.second) {
                val item = itemList.getOrNull(i)

                item?.let {
                    if (item.value == 0 || item.value == 255) {
                        count += 1

                    }
                }
            }
            val intervalSize =
                (sectionWithIntensity.second + 1) - sectionWithIntensity.first //last value exclusive
            if (count == intervalSize) {
                for (i in sectionWithIntensity.first..sectionWithIntensity.second) {
                    val item = itemList.getOrNull(i)

                    item?.let {
                        item.value = sectionWithIntensity.third
                    }
                }
            }

        }
    }

    private fun getCombinedMovementData(
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

    private fun getNapSection(nap: Nap, date: String): Pair<Int, Int>? {

        val startTime = nap.startTime
        val endTime = nap.endTime

        val sleepStartDate = startTime.split(" ")[0]
        val sleepEndDate = endTime.split(" ")[0]

        if (!sleepStartDate.equals(date)) return null

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
    private fun getSleepSection(sleep: OreoSleepModel?, date: String): Pair<Int, Int>? {
        val startTime = sleep?.hourly_breakup?.firstOrNull()?.start_time
        val endTime = sleep?.hourly_breakup?.lastOrNull()?.end_time
        if (startTime == null || endTime == null) return null

        val sleepStartDate = startTime.split(" ")[0]
//        val sleepEndDate = endTime.split(" ")[0]

        if (sleepStartDate.equals(date)) {
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