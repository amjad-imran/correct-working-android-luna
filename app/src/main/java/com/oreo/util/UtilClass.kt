package com.oreo.util

import com.google.gson.Gson
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import java.util.Arrays
import java.util.concurrent.TimeUnit

object UtilClass {

    fun getDetectedWorkoutMovement(data: OreoAutoSportData): HashMap<Int, String> {

        val hm = HashMap<Int, String>()

        tryCatch {
            var start = 0
            var end = 0

            val startTime =
                DateFormats.convertTimestampToDate(data.startTime, DateFormats.timeFormat)
            if (startTime.isNotEmpty()) {
                val startArray = startTime.split(":")
                val startHour = startArray[0].toInt()
                val startMinute = startArray[1].toInt()

                start = (startHour * 12) + (startMinute / 5)
                LOGS.d("getDetectedWorkoutMovementList start:: $start $startHour $startMinute ")
                hm.put(start, "$startHour:$startMinute")
            }
            val endTime = DateFormats.addMinuteToTimeStamp(
                data.startTime,
                TimeUnit.SECONDS.toMinutes(data.duration.toLong()).toInt()
            )
            val endTimeText =
                DateFormats.convertTimestampToDate(endTime, DateFormats.timeFormat)
            if (endTimeText.isNotEmpty()) {
                val endArray = endTimeText.split(":")
                val endHour = endArray[0].toInt()
                val endMinute = endArray[1].toInt()
                end = (endHour * 12) + (endMinute / 5)
                LOGS.d("getDetectedWorkoutMovementList end:: $end $endHour $endMinute ")
                hm.put(end, "$endHour:$endMinute")

                for (i in start..end) {

                    if (!hm.containsKey(i)) {
                        hm.put(i, "ignore")
                    }
                }
            }


        }
        LOGS.d("getDetectedWorkoutMovementList ${Gson().toJson(hm)}")
        return hm
    }

    fun getDetectedWorkoutMovementList(dataList: ArrayList<OreoAutoSportData>): HashMap<Int, Int> {
        dataList.reverse()
        val hm = HashMap<Int, Int>()
        var index = 0;
        dataList.forEach { data ->

            tryCatch {
                var start = 0
                var end = 0
                index += 1
                val startTime =
                    DateFormats.convertTimestampToDate(data.startTime, DateFormats.timeFormat)
                if (startTime.isNotEmpty()) {
                    val startArray = startTime.split(":")
                    val startHour = startArray[0].toInt()
                    val startMinute = startArray[1].toInt()
                    LOGS.d("getDetectedWorkoutMovementList start:: $startHour $startMinute ")
                    start = (startHour * 12) + (startMinute/5)
                }
                val endTime = DateFormats.addMinuteToTimeStamp(
                    data.startTime,
                    TimeUnit.SECONDS.toMinutes(data.duration.toLong()).toInt()
                )
                val endTimeText =
                    DateFormats.convertTimestampToDate(endTime, DateFormats.timeFormat)
                if (endTimeText.isNotEmpty()) {
                    val endArray = endTimeText.split(":")
                    val endHour = endArray[0].toInt()
                    val endMinute = endArray[1].toInt()
                    end = (endHour * 12) + (endMinute/5)
                    LOGS.d("getDetectedWorkoutMovementList end:: $endHour $endMinute ")
                }

                val avg = (end + start) / 2

                hm.put(avg, index)
            }
        }
        LOGS.d("getDetectedWorkoutMovementList ${Gson().toJson(hm)}")
        return hm
    }

    private fun getHour(index: Int): String {

        return when (index) {
            0 -> {
                "12 am"
            }

            47 -> {
                "4 am"
            }

            95 -> {
                "8 am"
            }

            143 -> {
                "12 pm"
            }

            191 -> {
                "4 pm"
            }

            239 -> {
                "8 pm"
            }

            287 -> {
                "12 am"
            }

            else -> {
                ""
            }

        }

    }

    private fun getHourString(hr: String): String {

        return when (hr) {
            "0" -> {
                "12 am"
            }

            "2" -> {
                "2 am"
            }

            "4" -> {
                "4 am"
            }

            "6" -> {
                "6 am"
            }
            "8" -> {
                "8 am"
            }
            "10" -> {
                "10 am"
            }

            "12" -> {
                "12 pm"
            }

            "14" -> {
                "2 pm"
            }

            "16" -> {
                "4 pm"
            }

            "18" -> {
                "6 pm"
            }

            "20" -> {
                "8 pm"
            }

            "22" -> {
                "10 pm"
            }


            "24" -> {
                "12 am"
            }

            else -> {
                ""
            }

        }

    }


    fun graphTwoHourBaseInterval(
        startTime: String?,
        endTime: String?,
        endIndex: Int
    ): HashMap<Int, String> {
        val hm = HashMap<Int, String>()
        if (startTime == null && endTime == null) {
            hm[0] = getHour(0)
            hm[47] = getHour(47)
            hm[95] = getHour(95)
            hm[143] = getHour(143)
            hm[191] = getHour(191)
            hm[239] = getHour(239)
            hm[287] = getHour(287)
            return hm
        }
        var endTimeFormat = endTime
        if (endTimeFormat == "24:00:00") {
            endTimeFormat = "23:60:00"
        }
        val array = intArrayOf(0,2,4,8,10,12,14,16,18,20,22,24)
        val startTimeFull = startTime!!.split(":")
        val startHr = startTimeFull[0].toInt()
        val startMin = startTimeFull[1].toInt()
        var startOffset = 0
        var nearestStart = usingBinarySearch(startHr, array)
        println("nearestStart $nearestStart")
        if (startHr < nearestStart) {
            val offsetMinAdjust = (60 - startMin) / 5
            val offsetHrAdjust = ((nearestStart - 1) - startHr) * 12
            startOffset = offsetHrAdjust + offsetMinAdjust
            println("nearestStart $offsetMinAdjust $offsetHrAdjust")
        } else {
            nearestStart += 2
            val offsetMinAdjust = (60 - startMin) / 5
            val offsetHrAdjust = ((nearestStart - 1) - startHr) * 12
            startOffset = offsetHrAdjust + offsetMinAdjust
            println("nearestStart $offsetMinAdjust $offsetHrAdjust")
        }


        val endTimeFull = endTimeFormat!!.split(":")
        var endHr = endTimeFull[0].toInt()
        if (endHr < 12) {
            endHr += 12
        }
        val endMin = endTimeFull[1].toInt()
        var endOffset = 0
        var nearestEnd = usingBinarySearch(endHr, array)

        val offsetEndMinAdjust = (endMin) / 5
        if (nearestEnd == endHr) {
            endOffset = offsetEndMinAdjust
            println("offsetEndHrAdjust  $offsetEndMinAdjust")
        } else if (nearestEnd > endHr) {
            nearestEnd -= 2

            val offsetEndHrAdjust = (endHr - nearestEnd) * 12
            endOffset = offsetEndMinAdjust + offsetEndHrAdjust

        } else {
            val offsetEndHrAdjust = ((endHr) - nearestEnd) * 12
            endOffset = offsetEndMinAdjust + offsetEndHrAdjust

        }
        println("endOffset $endOffset")

        val totalItems = (endHr * 12) + (endMin / 5)
        val firstBottomText = startOffset
        val lastBottomText = totalItems - endOffset - (startHr * 12)
        println("totalItems $totalItems firstBottomText $firstBottomText lastBottomText $lastBottomText lastIndex $endIndex")

        println("startHr $startTime $endTimeFormat")
        hm[0] = formatTime(startHr, startMin)
        for (i in firstBottomText..lastBottomText step 24) {
            println("getHourString $i $nearestStart")
            hm[i] = getHourString(nearestStart.toString())
            nearestStart += 2
        }
        hm[endIndex - 1] = formatTime(endHr, endMin)

        return hm
    }
    fun graphBaseInterval(
        startTime: String?,
        endTime: String?,
        endIndex: Int
    ): HashMap<Int, String> {
        val hm = HashMap<Int, String>()
        if (startTime == null && endTime == null) {
            hm[0] = getHour(0)
            hm[47] = getHour(47)
            hm[95] = getHour(95)
            hm[143] = getHour(143)
            hm[191] = getHour(191)
            hm[239] = getHour(239)
            hm[287] = getHour(287)
            return hm
        }
        var endTimeFormat = endTime
        if (endTimeFormat == "24:00:00") {
            endTimeFormat = "23:60:00"
        }
        val array = intArrayOf(0, 4, 8, 12, 16, 20, 24)
        val startTimeFull = startTime!!.split(":")
        val startHr = startTimeFull[0].toInt()
        val startMin = startTimeFull[1].toInt()
        var startOffset = 0
        var nearestStart = usingBinarySearch(startHr, array)
        println("nearestStart $nearestStart")
        if (startHr < nearestStart) {
            val offsetMinAdjust = (60 - startMin) / 5
            val offsetHrAdjust = ((nearestStart - 1) - startHr) * 12
            startOffset = offsetHrAdjust + offsetMinAdjust
            println("nearestStart $offsetMinAdjust $offsetHrAdjust")
        } else {
            nearestStart += 4;
            val offsetMinAdjust = (60 - startMin) / 5
            val offsetHrAdjust = ((nearestStart - 1) - startHr) * 12
            startOffset = offsetHrAdjust + offsetMinAdjust
            println("nearestStart $offsetMinAdjust $offsetHrAdjust")
        }


        val endTimeFull = endTimeFormat!!.split(":")
        var endHr = endTimeFull[0].toInt()
        if (endHr < 12) {
            endHr += 12
        }
        val endMin = endTimeFull[1].toInt()
        var endOffset = 0
        var nearestEnd = usingBinarySearch(endHr, array)

        val offsetEndMinAdjust = (endMin) / 5
        if (nearestEnd == endHr) {
            endOffset = offsetEndMinAdjust
            println("offsetEndHrAdjust  $offsetEndMinAdjust")
        } else if (nearestEnd > endHr) {
            nearestEnd -= 4;

            val offsetEndHrAdjust = (endHr - nearestEnd) * 12
            endOffset = offsetEndMinAdjust + offsetEndHrAdjust

        } else {
            val offsetEndHrAdjust = ((endHr) - nearestEnd) * 12
            endOffset = offsetEndMinAdjust + offsetEndHrAdjust

        }
        println("endOffset $endOffset")

        val totalItems = (endHr * 12) + (endMin / 5)
        val firstBottomText = startOffset
        val lastBottomText = totalItems - endOffset - (startHr * 12)
        println("totalItems $totalItems firstBottomText $firstBottomText lastBottomText $lastBottomText lastIndex $endIndex")

        println("startHr $startTime $endTimeFormat")
        hm[0] = formatTime(startHr, startMin)
        for (i in firstBottomText..lastBottomText step 48) {
            println("startHr $i $nearestStart")
            hm[i] = getHourString(nearestStart.toString())
            nearestStart += 4
        }
        hm[endIndex - 1] = formatTime(endHr, endMin)

        return hm
    }

    private fun formatTime(hr: Int, min: Int): String {
        println("formatTime $hr $min")
        return if (hr == 0 && min == 0) {
            "12 am"
        } else if ((hr == 23 && min == 59) || (hr == 23 && min == 60) || (hr == 24 && min == 0)) {
            "12 am"
        } else if (hr < 12) {
            "$hr:$min am"
        } else {
            var hrIn12 = hr - 12
            if (hrIn12 == 0) {
                hrIn12 = 12
            }
            "$hrIn12:$min pm"
        }
    }

    private fun usingBinarySearch(value: Int, a: IntArray): Int {
        if (value <= a[0]) {
            return a[0]
        }
        if (value >= a[a.size - 1]) {
            return a[a.size - 1]
        }
        val result = Arrays.binarySearch(a, value)
        if (result >= 0) {
            return a[result]
        }
        val insertionPoint = -result - 1
        return if (a[insertionPoint] - value < value - a[insertionPoint - 1]) a[insertionPoint] else a[insertionPoint - 1]
    }

}