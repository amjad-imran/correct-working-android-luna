package com.noisefit.data.dataConverter

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.model.GoogleFitWorkoutData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.SleepDataGoogleFit
import com.noisefit_commans.models.WorkoutGoogleFit
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import javax.inject.Inject


class OfflineDataMapper
@Inject constructor(
    val watches: WatchesSDK
) {

    fun convertSleepDataToGoogleFit(sleepData: OreoSleepData?): SleepDataGoogleFit? {
        LOGS.d("DATACONVERTER sleepData ${sleepData?.startTime}  ${sleepData?.endTime}")
        if (sleepData?.startTime == null || sleepData.date == null || sleepData.sleepArray.isNullOrEmpty() || sleepData.sleepArray!![0].startTime == null) {
            return null
        }

        // val sleepDataGoogleFit = SleepDataGoogleFit()
        val googleFitSleepBreakUpList = ArrayList<SleepDataGoogleFit.SleepDataBreakup>()
        var offSet = 0
        val midnightTime = "23:59"
        val startTime = sleepData.startTime!!.split(" ")[1]

        if (DateFormats.isTimeBefore(startTime, midnightTime)) {
            offSet = 1
        }
        LOGS.d("DATACONVERTER time $startTime $midnightTime $offSet")
        val sleepStartDate = DateFormats.subtractDate(sleepData.startTime!!, offSet)!!
        LOGS.d("DATACONVERTER sleepStartDate $sleepStartDate")
        val sleepStartTime = DateFormats.convertDateTimeToTimeStamp(sleepStartDate, startTime)
        LOGS.d("DATACONVERTER sleepStartTime $sleepStartTime")
        sleepData.sleepArray!!.forEach { sleepDataBreakup ->
            var breakUpStartTime =
                DateFormats.convertDateTimeToTimeStamp(sleepDataBreakup.startTime!!)
            var breakupEndTime = 0L
            if (sleepStartTime <= breakUpStartTime) {
                breakupEndTime =
                    DateFormats.addSecondToTimeStamp(breakUpStartTime, sleepDataBreakup.duration)
                LOGS.d(
                    "DATACONVERTER SAME DAY $breakUpStartTime $breakupEndTime ${
                        DateFormats.convertTimestampToDate(
                            breakUpStartTime,
                            DateFormats.dateTimeFormat
                        )
                    }  ${
                        DateFormats.convertTimestampToDate(
                            breakupEndTime,
                            DateFormats.dateTimeFormat
                        )
                    }"
                )
            } else {
                breakUpStartTime =
                    DateFormats.convertDateTimeToTimeStamp(
                        sleepData.date!!,
                        sleepDataBreakup.startTime!!
                    )
                breakupEndTime =
                    DateFormats.addSecondToTimeStamp(breakUpStartTime, sleepDataBreakup.duration)
                LOGS.d(
                    "DATACONVERTER Different DAY ${sleepDataBreakup.endTime} ${sleepDataBreakup.startTime} $breakUpStartTime $breakupEndTime ${
                        DateFormats.convertTimestampToDate(
                            breakUpStartTime,
                            DateFormats.dateTimeFormat
                        )
                    }  ${
                        DateFormats.convertTimestampToDate(
                            breakupEndTime,
                            DateFormats.dateTimeFormat
                        )
                    }"
                )


            }

            googleFitSleepBreakUpList.add(
                SleepDataGoogleFit.SleepDataBreakup(
                    breakUpStartTime,
                    breakupEndTime,
                    sleepDataBreakup.sleepType
                )
            )
        }


        if (googleFitSleepBreakUpList.isNullOrEmpty()) {
            return null
        }
        val startSleep = googleFitSleepBreakUpList[0].startTime
        val endSleep = googleFitSleepBreakUpList[googleFitSleepBreakUpList.size - 1].endTime

        return SleepDataGoogleFit(startSleep, endSleep, googleFitSleepBreakUpList)
    }


    fun convertSleepData(data: List<OreoSleepData>?): OreoSleepData {
        val sleepData = OreoSleepData()
        if (data.isNullOrEmpty()) {
            return sleepData
        }

        if (data.size == 1) {
            return data[0]
        }

        val watchType = watches.getWatchType()
        if (watchType == SDKWatchType.SDK_RYEEX) {
            return data[data.size - 1]
        }


        val sleepArray = ArrayList<OreoSleepData.OreoSleepDataBreakup>()
        val sleepDataSize = data.size - 1
        sleepData.startTime = data[0].startTime
        sleepData.startDate = data[0].startDate
        sleepData.availableSleepTypes = data[0].availableSleepTypes
        sleepData.endTime = data[sleepDataSize].endTime
        sleepData.endDate = data[sleepDataSize].endDate
        sleepData.date = data[sleepDataSize].date
        sleepData.breathQuality = data[sleepDataSize].breathQuality
        sleepData.sleepScore = data[sleepDataSize].sleepScore
        sleepData.sleepLatency = data[sleepDataSize].sleepLatency
        sleepData.sleepEfficiency = data[sleepDataSize].sleepEfficiency
        sleepData.timeInBedTime = data[sleepDataSize].timeInBedTime
        sleepData.nightTimeMovement = data[sleepDataSize].nightTimeMovement


        var totalDeep = 0
        var totalLight = 0
        var totalAwake = 0
        var totalRem = 0

        val startTimeCount = data.filter { it.startTime == sleepData.startTime }.size

        //same start time data so return last element
        if (startTimeCount == data.size) {
            data[sleepDataSize].let { sData ->
                sData.sleepArray?.let { sleepArray.addAll(it) }
                totalDeep += sData.deep
                totalAwake += sData.awake
                totalRem += sData.remCount
                totalLight += sData.light
            }
        } else {
            //watch has been returning data in segments so we need to add up all
            data.forEachIndexed { index, mSleepData ->
                mSleepData.sleepArray?.let { sleepArray.addAll(it) }
                totalDeep += mSleepData.deep
                totalAwake += mSleepData.awake
                totalRem += mSleepData.remCount
                totalLight += mSleepData.light

            }
        }



        sleepData.sleepArray = sleepArray
        sleepData.light = totalLight
        sleepData.remCount = totalRem
        sleepData.deep = totalDeep
        sleepData.awake = totalAwake

        if (watches.getDevice()?.deviceType == DeviceType.COLORFIT_PULSE_2.deviceType || watches.getDevice()?.deviceType == DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType) {
            sleepData.total = totalLight + totalDeep + totalRem
        } else if (watches.getWatchType()?.name == SDKWatchType.SDK_NAV_PLUS.name || watches.getWatchType()?.name == SDKWatchType.SDK_ZH.name) {
            sleepData.total = totalLight + totalDeep + totalRem
        } else {
            sleepData.total = totalLight + totalDeep + totalAwake + totalRem
        }


        return sleepData

    }

    fun convertUnSyncSleepDataListToObjectOreo(data: List<OreoSleepData>?): List<OreoSleepData>? {
        if (data.isNullOrEmpty()) {
            return null
        }

        val hm = HashMap<String, ArrayList<OreoSleepData>>()
        data.forEach { sleepData ->
            val date = sleepData.date!!
            if (hm.containsKey(date)) {
                val dataList = hm[date]
                dataList!!.add(sleepData)
                hm[date] = dataList
            } else {
                val dataList = ArrayList<OreoSleepData>()
                dataList.add(sleepData)
                hm[date] = dataList
            }
        }

        val sleepDateList = ArrayList<OreoSleepData>()
        hm.forEach { (_, value) ->
            sleepDateList.add(convertSleepData(value))
        }
        return sleepDateList
    }

    /*
    "date": "2023-11-02",
        "height": "1.67",
        "weight": 67,
     */

    fun convertGFUserDataIntoJsonObject(data: Pair<Float, Float>): JsonObject {
        val jsonObject = JsonObject().apply {
            if (data.first > 0) {
                this.addProperty("height", data.first.toString())
            }
            if (data.second > 0) {
                this.addProperty("weight", data.second)
            }
            this.addProperty("date", DateFormats.getTodaysDateString(9))
            this.addProperty("source", "google")
        }

        return jsonObject
    }

    fun convertGFWorkoutIntoJsonArray(data: List<GoogleFitWorkoutData>): JsonArray {
        val jsonArray = JsonArray()
        data.forEach {
            val requestObject = JsonObject().apply {
                this.addProperty("duration", it.duration)
                this.addProperty("calories", it.calories?.toInt() ?: 0)
                this.addProperty("activity_type", it.activity)
                this.addProperty("type", "google")
                this.addProperty(
                    "date",
                    DateFormats.convertTimestampToDate(
                        (it.startTime!! * 1000L),
                        DateFormats.dateFormat3
                    )
                )
                this.addProperty(
                    "start_time",
                    DateFormats.convertTimestampToDate(
                        it.startTime!! * 1000L,
                        DateFormats.time24WithoutSecond
                    )
                )
                this.addProperty("steps", it.steps)
                this.addProperty(
                    "end_time",
                    DateFormats.convertTimestampToDate(
                        it.endTime!! * 1000L,
                        DateFormats.time24WithoutSecond
                    )
                )
                this.addProperty("intensity", "Moderate")
            }
            jsonArray.add(requestObject)
        }
        return jsonArray
    }

    fun convertWorkoutGoogleFit(data: List<WorkoutGoogleFit>): List<GoogleFitWorkoutData> {
        val workoutList = ArrayList<GoogleFitWorkoutData>()
        data.forEach {
            workoutList.add(
                GoogleFitWorkoutData(
                    0,
                    false,
                    it.name,
                    it.identifier,
                    it.appPackageName,
                    it.activity,
                    it.startTime,
                    it.endTime,
                    it.distance,
                    it.duration,
                    it.calories,
                    it.heartRate,
                    it.steps,
                    it.type
                )
            )
        }
        return workoutList
    }
}