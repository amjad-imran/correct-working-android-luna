package com.oreo.util

import android.content.Context
import android.graphics.PointF
import com.google.gson.Gson
import com.hookedonplay.decoviewlib.charts.SeriesItem
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import java.lang.Math.abs
import java.lang.Math.floor
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

object UtilClass {

    fun getDetectedWorkoutMovement(data: OreoAutoSportData): HashMap<Int, String> {

        val hm = HashMap<Int, String>()

        tryCatch {
            var start = 0
            var end = 0

            val startTime =
                DateFormats.convertTimestampToDate(data.startTime, DateFormats.timeFormat())
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
                DateFormats.convertTimestampToDate(endTime, DateFormats.timeFormat())
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
                    DateFormats.convertTimestampToDate(data.startTime, DateFormats.timeFormat())
                if (startTime.isNotEmpty()) {
                    val startArray = startTime.split(":")
                    val startHour = startArray[0].toInt()
                    val startMinute = startArray[1].toInt()
                    LOGS.d("getDetectedWorkoutMovementList start:: $startHour $startMinute ")
                    start = (startHour * 12) + (startMinute / 5)
                }
                val endTime = DateFormats.addMinuteToTimeStamp(
                    data.startTime,
                    TimeUnit.SECONDS.toMinutes(data.duration.toLong()).toInt()
                )
                val endTimeText =
                    DateFormats.convertTimestampToDate(endTime, DateFormats.timeFormat())
                if (endTimeText.isNotEmpty()) {
                    val endArray = endTimeText.split(":")
                    val endHour = endArray[0].toInt()
                    val endMinute = endArray[1].toInt()
                    end = (endHour * 12) + (endMinute / 5)
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

            71 -> {
                "6 am"
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

            215 -> {
                "6 pm"
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

    fun getXAxisPoints15Mins(
        startTime: String?,
        endTime: String?,
        dataSize: Int
    ): HashMap<Int, String> {

        val hm = HashMap<Int, String>()
        if (startTime == null || endTime == null) {
        /*    hm[0] = getHour(0)
            hm[71] = getHour(71)
            hm[143] = getHour(143)
            hm[215] = getHour(215)
            hm[95] = getHour(287)*/
            return hm
        }

        //3 points
        if (dataSize <= 36) {
            val center = dataSize / 2
            val centerTime = getCenterTime(
                DateFormats.dateTimeFormat5().parse(startTime),
                DateFormats.dateTimeFormat5().parse(endTime)
            )

            hm[0] = DateFormats.formatDate(
                startTime,
                DateFormats.dateTimeFormat5(),
                DateFormats.time12Meridian()
            )
            hm[center] = DateFormats.time12Meridian().format(centerTime).lowercase()
            hm[dataSize - 1] = DateFormats.formatDate(
                endTime,
                DateFormats.dateTimeFormat5(),
                DateFormats.time12Meridian()
            )
        }
        //5 points

        val center = dataSize / 2
        val centerLeft = center / 2

        val centerTime = getCenterTime(
            DateFormats.dateTimeFormat5().parse(startTime),
            DateFormats.dateTimeFormat5().parse(endTime)
        )
        val centerLeftTime = getCenterTime(
            DateFormats.dateTimeFormat5().parse(startTime),
            centerTime
        )
        val centerRightTime = getCenterTime(
            centerTime,
            DateFormats.dateTimeFormat5().parse(endTime)
        )

        hm[0] = DateFormats.formatDate(
            startTime,
            DateFormats.dateTimeFormat5(),
            DateFormats.time12Meridian()
        ).lowercase()
        hm[centerLeft] = DateFormats.time12Meridian().format(centerLeftTime).lowercase()
        hm[center] = DateFormats.time12Meridian().format(centerTime).lowercase()
        hm[center + centerLeft] = DateFormats.time12Meridian().format(centerRightTime).lowercase()
        hm[dataSize - 1] = DateFormats.formatDate(
            endTime,
            DateFormats.dateTimeFormat5(),
            DateFormats.time12Meridian()
        ).lowercase()

        return hm

    }


    fun getXAxisPoints(
        startTime: String?,
        endTime: String?,
        dataSize: Int
    ): HashMap<Int, String> {

        val hm = HashMap<Int, String>()
        if (startTime == null || endTime == null) {
            hm[0] = getHour(0)
            hm[71] = getHour(71)
            hm[143] = getHour(143)
            hm[215] = getHour(215)
            hm[287] = getHour(287)
            return hm
        }

        //3 points
        if (dataSize <= 36) {
            val center = dataSize / 2
            val centerTime = getCenterTime(
                DateFormats.dateTimeFormat5().parse(startTime),
                DateFormats.dateTimeFormat5().parse(endTime)
            )

            hm[0] = DateFormats.formatDate(
                startTime,
                DateFormats.dateTimeFormat5(),
                DateFormats.time12Meridian()
            )
            hm[center] = DateFormats.time12Meridian().format(centerTime).lowercase()
            hm[dataSize - 1] = DateFormats.formatDate(
                endTime,
                DateFormats.dateTimeFormat5(),
                DateFormats.time12Meridian()
            )
        }
        //5 points

        val center = dataSize / 2
        val centerLeft = center / 2

        val centerTime = getCenterTime(
            DateFormats.dateTimeFormat5().parse(startTime),
            DateFormats.dateTimeFormat5().parse(endTime)
        )
        val centerLeftTime = getCenterTime(
            DateFormats.dateTimeFormat5().parse(startTime),
            centerTime
        )
        val centerRightTime = getCenterTime(
            centerTime,
            DateFormats.dateTimeFormat5().parse(endTime)
        )

        hm[0] = DateFormats.formatDate(
            startTime,
            DateFormats.dateTimeFormat5(),
            DateFormats.time12Meridian()
        ).lowercase()
        hm[centerLeft] = DateFormats.time12Meridian().format(centerLeftTime).lowercase()
        hm[center] = DateFormats.time12Meridian().format(centerTime).lowercase()
        hm[center + centerLeft] = DateFormats.time12Meridian().format(centerRightTime).lowercase()
        hm[dataSize - 1] = DateFormats.formatDate(
            endTime,
            DateFormats.dateTimeFormat5(),
            DateFormats.time12Meridian()
        ).lowercase()

        return hm

    }

    private fun getCenterTime(
        startTime: Date,
        endTime: Date
    ): Date {
        val timeRange = endTime.time - startTime.time
        val singleDuration = timeRange / 2

        val centerTime = startTime.time + singleDuration
        val calendar = Calendar.getInstance()
        calendar.time = Date(centerTime)
        return Date(calendar.timeInMillis)
    }

    fun graphTwoHoursInterval(
        startTime: String?,
        endTime: String?,
        endIndex: Int
    ): HashMap<Int, String> {

        var hm = HashMap<Int, String>()
        if (startTime == null || endTime == null) {
            hm[0] = getHour(0)
//            hm[47] = getHour(47)
            hm[71] = getHour(71)
//            hm[95] = getHour(95)
            hm[143] = getHour(143)
//            hm[191] = getHour(191)
            hm[215] = getHour(215)
            hm[287] = getHour(287)
            return hm
        }

        if (DateFormats.checkStartTimeLess(startTime, endTime, DateFormats.timeFormat12())) {
//            LOGS.d("dsakjdsalkjsladjlksdajldsajldsajl startTimeLess")
            hm = graphTwoHourBaseInterval(startTime, endTime, endIndex)
        } else {
//            LOGS.d("dsakjdsalkjsladjlksdajldsajldsajl startTimegreat $startTime $endTime")
            val hm1 = graphTwoHourBaseInterval(startTime, "24:00", endIndex)
            val hm2 = graphTwoHourBaseInterval("00:00", endTime, endIndex)

            var lastIndex = 0
            hm1.forEach { data ->
//                LOGS.d("dsakjdsalkjsladjlksdajldsajldsajl startTimegreat ${data.key} ${data.value}")
                if (data.value.lowercase() == "12:00 am") {
                    lastIndex = data.key
//                    LOGS.d("dsakjdsalkjsladjlksdajldsajldsajl startTimegreat $lastIndex ${data.key}")
                }
                hm[data.key] = data.value
            }

            hm2.forEach { data ->
                hm[data.key + lastIndex] = data.value
            }


//            LOGS.d("saddasdas startTimegreat $lastIndex ${Gson().toJson(hm1)}")
//            LOGS.d("saddasdas startTimegreat $endIndex ${Gson().toJson(hm2)}")
//            LOGS.d("saddasdas startTimegreat final ${Gson().toJson(hm)}")
        }


        return hm
    }

    fun graphTwoHourBaseInterval(
        startTime: String?,
        endTime: String?,
        endIndex: Int
    ): HashMap<Int, String> {
        val hm = HashMap<Int, String>()
        if (startTime == null || endTime == null) {
            hm[0] = getHour(0)
//            hm[47] = getHour(47)
            hm[71] = getHour(71)
            hm[143] = getHour(143)
//            hm[191] = getHour(191)
            hm[215] = getHour(215)
            hm[287] = getHour(287)
            return hm
        }
        var endTimeFormat = endTime
        if (endTimeFormat == "23:59:00" || endTimeFormat == "23:59") {
            endTimeFormat = "24:00:00"
        }


        val startTimeFull =
            if (startTime.lowercase().contains("am") || startTime.lowercase().contains("pm")) {
                DateFormats.convertTimeIntoTime(
                    startTime,
                    DateFormats.timeFormat12(),
                    DateFormats.timeFormat()
                ).split(":")
            } else {
                startTime.split(":")
            }


        println("nearestStart startTimeFull $startTimeFull")
        val startHr = startTimeFull[0].toInt()

        val originalStartMin = startTimeFull[1].toInt()
        var startMin = originalStartMin

        startMin = (5 * (floor(abs(startMin.toDouble() / 5)))).toInt()
        val startTimeOffset = startHr * 12 + (startMin / 5)
        var nearestStart = getNearest2Number(startHr)
        val nearestStartTimeOffset = (nearestStart * 12) - startTimeOffset

//      var startOffset = 0
//      var offsetMinAdjust = 0

        println("nearestStart  $startTimeOffset $nearestStartTimeOffset")


        val endTimeFull =
            if (endTimeFormat.lowercase().contains("am") || endTimeFormat.lowercase()
                    .contains("pm")
            ) {
                DateFormats.convertTimeIntoTime(
                    endTimeFormat,
                    DateFormats.timeFormat12(),
                    DateFormats.timeFormat()
                ).split(":")
            } else {
                endTimeFormat.split(":")
            }

        println("nearestStart endTimeFull $endTimeFull")
        val endHr = endTimeFull[0].toInt()

        val originalEndMin = endTimeFull[1].toInt()
        var endMin = originalEndMin
        endMin = (5 * (ceil(abs(endMin.toDouble() / 5)))).toInt()
        val endTimeOffset = endHr * 12 + (endMin / 5)
        val nearestEnd = getNearest2Number(endHr) - 2
        val nearestEndTimeOffset = nearestEnd * 12

        val nearestEndAdj = endTimeOffset - startTimeOffset
        val nearestEndTimeOffsetAdj = nearestEndTimeOffset - startTimeOffset

        println("nearestStart $nearestEndAdj  $nearestEndTimeOffsetAdj ")

        hm[0] = formatTime(startHr, originalStartMin)
//       println("totalItems $totalItems firstBottomText $firstBottomText lastBottomText $lastBottomText lastIndex $lastIndex" )
        for (i in nearestStartTimeOffset until nearestEndAdj step 24) {

            println("getHourString $i $nearestStart")
            hm[i] = getHourString(nearestStart.toString())
            nearestStart += 2
        }


        hm[nearestEndAdj] = formatTime(endHr, originalEndMin)

        return hm
    }


    fun graphBaseInterval(
        startTime: String?,
        endTime: String?,
        endIndex: Int
    ): HashMap<Int, String> {
        val hm = HashMap<Int, String>()
        if (startTime == null || endTime == null) {
            hm[0] = getHour(0)
            hm[47] = getHour(47)
            //hm[71] = getHour(71)
            hm[95] = getHour(95)
            hm[143] = getHour(143)
            hm[191] = getHour(191)
            //hm[215] = getHour(215)
            hm[239] = getHour(239)
            hm[287] = getHour(287)
            return hm
        }
        var endTimeFormat = endTime
        if (endTimeFormat == "24:00:00") {
            endTimeFormat = "23:60:00"
        }

        val startTimeFull =
            if (startTime.lowercase().contains("am") || startTime.lowercase().contains("pm")) {
                DateFormats.convertTimeIntoTime(
                    startTime,
                    DateFormats.timeFormat12(),
                    DateFormats.timeFormat()
                ).split(":")
            } else {
                startTime.split(":")
            }

        val startHr = startTimeFull[0].toInt()
        val originalStartMin = startTimeFull[1].toInt()
        var startMin = originalStartMin
        startMin = (5 * (floor(abs(startMin.toDouble() / 5)))).toInt()
        val startTimeOffset = startHr * 12 + (startMin / 5)
        var nearestStart = getNearest4Number(startHr)
        val nearestStartTimeOffset = (nearestStart * 12) - startTimeOffset


        println("nearestStart  $startTimeOffset $nearestStartTimeOffset")


        val endTimeFull =
            if (endTimeFormat.lowercase().contains("am") || endTimeFormat.lowercase()
                    .contains("pm")
            ) {
                DateFormats.convertTimeIntoTime(
                    endTimeFormat,
                    DateFormats.timeFormat12(),
                    DateFormats.timeFormat()
                ).split(":")
            } else {
                endTimeFormat.split(":")
            }


        val endHr = endTimeFull[0].toInt()

        val originalEndMin = endTimeFull[1].toInt()
        var endMin = originalEndMin
        endMin = (5 * (ceil(abs(endMin.toDouble() / 5)))).toInt()
        val endTimeOffset = endHr * 12 + (endMin / 5)
        val nearestEnd = getNearest4Number(endHr) - 4
        val nearestEndTimeOffset = nearestEnd * 12

        val nearestEndAdj = endTimeOffset - startTimeOffset
        val nearestEndTimeOffsetAdj = nearestEndTimeOffset - startTimeOffset

        println("nearestStart $nearestEndAdj  $nearestEndTimeOffsetAdj ")


        hm[0] = formatTime(startHr, originalStartMin)
//       println("totalItems $totalItems firstBottomText $firstBottomText lastBottomText $lastBottomText lastIndex $lastIndex" )
        for (i in nearestStartTimeOffset until nearestEndAdj step 48) {

            println("getHourString $i $nearestStart")
            hm[i] = getHourString(nearestStart.toString())
            nearestStart += 4
        }

        hm[endIndex - 1] = formatTime(endHr, originalEndMin)

        return hm
    }

    private fun formatTime(hr: Int, min: Int): String {
        println("formatTime $hr $min")
        return DateFormats.convertTimeIntoTime(
            "$hr:$min",
            DateFormats.timeFormat(),
            DateFormats.timeFormat12()
        ).lowercase()
    }


     fun getNearest4Number(number: Int): Int {
        when (number) {
            in 0..3 -> {
                return 4
            }

            in 4..7 -> {
                return 8
            }

            in 8..11 -> {
                return 12
            }

            in 12..15 -> {
                return 16
            }

            in 16..19 -> {
                return 20
            }

            in 20..23 -> {
                return 24
            }


            else -> {
                return -1
            }
        }
    }

    fun seriesItemWithoutInset(
        context: Context, initialValue: Float, maxValue: Float, color: Int, width: Float
    ): SeriesItem {
        return SeriesItem.Builder(context.resources.getColor(color, null))
            .setShowPointWhenEmpty(true)
            .setRange(0f, maxValue, initialValue).setLineWidth(width).build()
    }
    fun seriesItemWithInset(
        context: Context,
        initialValue: Float,
        maxValue: Float,
        color: Int,
        inset: Float,
        width: Float
    ): SeriesItem {

        return SeriesItem.Builder(context.resources.getColor(color, null))
            .setInset(PointF(inset, inset))
            .setShowPointWhenEmpty(true)
            .setRange(0f, maxValue, initialValue).setLineWidth(width).build()
    }

    private fun getNearest2Number(number: Int): Int {
        when (number) {
            in 0..1 -> {
                return 2
            }

            in 2..3 -> {
                return 4
            }

            in 4..5 -> {
                return 6
            }

            in 6..7 -> {
                return 8
            }

            in 8..9 -> {
                return 10
            }

            in 10..11 -> {
                return 12
            }

            in 12..13 -> {
                return 14
            }

            in 14..15 -> {
                return 16
            }

            in 16..17 -> {
                return 18
            }

            in 18..19 -> {
                return 20
            }

            in 20..21 -> {
                return 22
            }

            in 22..23 -> {
                return 24
            }

            else -> {
                return -1
            }
        }
    }


}