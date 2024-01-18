package com.oreo.data.dataConverter

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit_commans.common.averageWithoutZero
import com.noisefit_commans.common.averageWithoutZeroFloat
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.common.minWithoutZero
import com.noisefit_commans.data.model.DayTimeMovementBreakup
import com.noisefit_commans.data.model.OreoBloodOxygenBreakup
import com.noisefit_commans.data.model.OreoBodyTempNetworkEntity
import com.noisefit_commans.data.model.OreoBodyTemperatureBreakup
import com.noisefit_commans.data.model.OreoCommonNetworkEntity
import com.noisefit_commans.data.model.OreoHeartNetworkEntity
import com.noisefit_commans.data.model.OreoHeartRate
import com.noisefit_commans.data.model.OreoNapData
import com.noisefit_commans.data.model.OreoRespiratoryData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.data.model.OreoSleepNetworkEntity
import com.noisefit_commans.data.model.OreoStepsData
import com.noisefit_commans.data.model.OreoStepsNetworkEntity
import com.noisefit_commans.data.model.OreoStressDataBreakup
import com.noisefit_commans.data.model.OreoUserDataPost
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.to12HourFormat
import com.oreo.data.db.abstaction.OreoBodyTemperatureDataSource
import com.oreo.data.db.implementation.OreoBloodOxygenDataImpl
import com.oreo.data.db.implementation.OreoBodyTemperatureDataImpl
import com.oreo.data.db.implementation.OreoHeartRateDataImpl
import com.oreo.data.db.implementation.OreoRespiratoryDataImpl
import com.oreo.data.db.implementation.OreoStressDataImpl
import com.oreo.data.model.OreoUserSyncActivities
import com.oreo.data.model.SleepOverlayData
import javax.inject.Inject
import kotlin.math.roundToInt

class OreoOnlineDataMapper
@Inject
constructor(
    private val stressDataImpl: OreoStressDataImpl,
    private val respiratoryData: OreoRespiratoryDataImpl,
    private val temperatureData: OreoBodyTemperatureDataSource,
    private val bloodOxygenDataImpl: OreoBloodOxygenDataImpl,
    private val oreoHeartRateDataImpl: OreoHeartRateDataImpl,
) {

    suspend fun convertDataToPost(userSyncActivities: OreoUserSyncActivities): OreoUserDataPost? {
        val combinedData = OreoUserDataPost()

        val steps =
            parseStepsDataOreo(userSyncActivities.stepsDataList, userSyncActivities.dayTimeMovement)
        val stress = parseStressData(userSyncActivities.stressData)
        val heartRateHistory = parseHeartHistoryData(userSyncActivities.hrHistoryData)
        val bloodOxygen = parseBloodOxygenData(userSyncActivities.boData)
        val bodyTemperature = parseBodyTemperature(userSyncActivities.bodyTemperature)
        val respiratory = parseRespiratoryData(userSyncActivities.respiratory)

        val sleeps = parseSleepDataOreo(
            userSyncActivities.sleepData
        )

        combinedData.activities = steps
        combinedData.stress = stress
        combinedData.heartRateHistory = heartRateHistory
        combinedData.bloodOxygen = bloodOxygen
        combinedData.bodyTemperature = bodyTemperature
        combinedData.respiratory = respiratory
        combinedData.sleeps = sleeps

        if (steps == null && stress == null && heartRateHistory == null
            && bloodOxygen == null && bodyTemperature == null && respiratory == null
        ) {
            LOGS.d("Hurray!! just saved one api call")
            return null
        }
        return combinedData
    }

    suspend fun getOreoSleepDataToPost(userSyncActivities: OreoUserSyncActivities): OreoUserDataPost {
        val sleeps = parseSleepDataOreo(
            userSyncActivities.sleepData
        )
        val combinedData = OreoUserDataPost()
        combinedData.sleeps = sleeps


        return combinedData
    }

    private fun parseStepsDataOreo(
        stepsDataList: List<OreoStepsData>?,
        dayTimeMovement: List<DayTimeMovementBreakup>?
    ): ArrayList<OreoStepsNetworkEntity>? {
        if (stepsDataList.isNullOrEmpty()) {
            return null
        }
        val stepsList = ArrayList<OreoStepsNetworkEntity>()

        stepsDataList.forEach { stepsData ->

            val date = stepsData.date!!
            val hourlyBreakupList = ArrayList<OreoStepsNetworkEntity.HourlyBreakup>()
            stepsData.stepArray?.forEachIndexed { index, data ->
                var hour = index
                data.hourOfTheDay?.let {
                    hour = it
                }


                if (data.steps != 0) {
                    hourlyBreakupList.add(
                        OreoStepsNetworkEntity.HourlyBreakup(
                            data.steps,
                            data.activeCalories,
                            data.calories,
                            data.distance,
                            hour
                        )
                    )
                }


            }

            val dayTimeData = dayTimeMovement?.firstOrNull {
                it.date.equals(stepsData.date)
            }
            var dayDataBreakup = Gson().fromJson<List<Int>>(dayTimeData?.breakUp ?: "")
            if (dayDataBreakup.isNullOrEmpty()) {
                dayDataBreakup = IntArray(288) { 255 }.toList()
            }


            val dayBreakup =
                OreoStepsNetworkEntity.DayBreakup(
                    stepsData.totalSteps,
                    stepsData.activeCalories ?: 0,
                    stepsData.totalCalories,
                    stepsData.totalDistance,
                    date
                )

            val steps = OreoStepsNetworkEntity(
                hourlyBreakupList, dayBreakup,
                dayTimeMovement = dayDataBreakup
            )
            stepsList.add(steps)
        }

        if (stepsList.isEmpty()) {
            return null
        }
        return stepsList
    }


    private suspend fun getSleepOverlayData(sleepData: OreoSleepData): SleepOverlayData/*SleepOverlayData*/ {
        var offSet = 0
        val midnightTime = "23:59"
        val startTime = sleepData.startTime!!
        val timeIn24Hour =
            DateFormats.formatTimeInto24HoursValue(startTime, DateFormats.dateTimeFormat6).toInt()


        if (timeIn24Hour in 18..23) {
            offSet = 1
        }
        LOGS.d("getSleepOverlayData time $startTime $midnightTime $offSet")
        val sleepStartDate = DateFormats.subtractDateFormat3(sleepData.date!!, offSet)!!
        LOGS.d("getSleepOverlayData sleepStartDate $sleepStartDate")
        val sleepStartTime = DateFormats.convertDateTimeToTimeStamp3(startTime)
        val sleepEndTime = DateFormats.addSecondToTimeStamp(sleepStartTime, sleepData.timeInBedTime)

        LOGS.d("getSleepOverlayData SLEEP_timeSTAMP : $sleepStartTime $sleepEndTime")
        val hrData =
            oreoHeartRateDataImpl.getHeartRateBetweenTimeStamp(sleepStartTime, sleepEndTime)
        val hrv = stressDataImpl.getStressBetweenTimeStamp(sleepStartTime, sleepEndTime)
        val resp = respiratoryData.getDataBetweenTimeStamp(sleepStartTime, sleepEndTime)
        val spo2Breakup = bloodOxygenDataImpl.getDataBetweenTimeStamp(sleepStartTime, sleepEndTime)
        val temp = temperatureData.getDataBetweenTimeStamp(sleepStartTime, sleepEndTime)

        return SleepOverlayData(hrData, hrv, resp, temp, spo2Breakup)
    }


    private suspend fun parseSleepDataOreo(
        sleepDataList: List<OreoSleepData>?
    ): ArrayList<OreoSleepNetworkEntity>? {
        if (sleepDataList.isNullOrEmpty()) {
            return null
        }

        val sleepList = ArrayList<OreoSleepNetworkEntity>()

        sleepDataList.forEach { sleepData ->
            val sleepOverlayData = getSleepOverlayData(sleepData)
            val hourlyList = ArrayList<OreoSleepNetworkEntity.OreoHourlyBreakup>()

            val nightTimeMovement = ArrayList<OreoSleepNetworkEntity.OreoMovementBreakup>()
            sleepData.nightTimeMovement?.forEachIndexed { index, data ->
                nightTimeMovement.add(
                    OreoSleepNetworkEntity.OreoMovementBreakup(
                        movementType = data.movementType,
                        duration = data.duration,
                        startTime = (data.startTime ?: ""),
                        endTime = (data.endTime ?: "")
                    )
                )

            }


            sleepData.sleepArray?.forEachIndexed { i, data ->
                hourlyList.add(
                    OreoSleepNetworkEntity.OreoHourlyBreakup(
                        sleepType = data.sleepType,
                        duration = data.duration,
                        startTime = data.startTime ?: "",
                        endTime = data.endTime ?: ""
                    )
                )
            }
            var dayBreakup: OreoSleepNetworkEntity.OreoDayBreakup? = null
            dayBreakup =
                OreoSleepNetworkEntity.OreoDayBreakup(
                    totalDeep = sleepData.deep,
                    totalLight = sleepData.light,
                    totalDuration = sleepData.total,
                    timeInBed = sleepData.timeInBedTime,
                    startTime = sleepData.startTime ?: "",
                    endtime = sleepData.endTime ?: "",
                    totalAwake = sleepData.awake,
                    date = (sleepData.date ?: ""),
                    totalRem = sleepData.remCount,
                    sleepScore = sleepData.sleepScore,
                    sleepEfficiency = sleepData.sleepEfficiency,
                    restingHr = sleepOverlayData.hrBreakup.averageWithoutZero(),
                    sleepLatency = sleepData.sleepLatency,
                    hrBreakup = sleepOverlayData.hrBreakup,
                    hrvBreakup = sleepOverlayData.stressBreakup,
                    respBreakup = sleepOverlayData.respBreakup,
                    tempBreakup = sleepOverlayData.tempBreakup,
                    oxyBreakup = sleepOverlayData.spo2Breakup,
                    avgTemp = sleepOverlayData.tempBreakup.averageWithoutZeroFloat(),
                    avgOxy = if (sleepOverlayData.spo2Breakup.isEmpty()) 0 else sleepOverlayData.spo2Breakup.averageWithoutZero(),
                    avgResp = if (sleepOverlayData.respBreakup.isEmpty()) 0 else sleepOverlayData.respBreakup.average()
                        .roundToInt() ?: 0,
                    maxTemp = sleepOverlayData.tempBreakup.maxOrNull() ?: 0f,
                    avgHrv = sleepOverlayData.stressBreakup.averageWithoutZero(),
                    readinessScore = sleepData.readinessScore ?: 0
                )

            val sleep = OreoSleepNetworkEntity(hourlyList, nightTimeMovement, dayBreakup)
            sleepList.add(sleep)
        }

        if (sleepList.isEmpty()) {
            return null
        }

        return sleepList

    }

    private fun parseStressData(stressDataList: List<OreoStressDataBreakup>?): ArrayList<OreoCommonNetworkEntity>? {
        if (stressDataList.isNullOrEmpty()) {
            return null
        }
        val commonList = ArrayList<OreoCommonNetworkEntity>()
        stressDataList.forEach {
            val breakUp = Gson().fromJson<List<Int>>(it.breakUp ?: "")
            val networkReq = OreoCommonNetworkEntity()
            networkReq.dayBreakup = OreoCommonNetworkEntity.DayBreakup(
                breakUp = breakUp,
                frequency = 5,
                date = it.date ?: ""
            )
            commonList.add(networkReq)
        }
        return commonList
    }

    private fun parseRespiratoryData(stressDataList: List<OreoRespiratoryData>?): ArrayList<OreoCommonNetworkEntity>? {
        if (stressDataList.isNullOrEmpty()) {
            return null
        }
        val commonList = ArrayList<OreoCommonNetworkEntity>()
        stressDataList.forEach {
            val breakUp = Gson().fromJson<List<Int>>(it.breakUp ?: "")
            val networkReq = OreoCommonNetworkEntity()
            networkReq.dayBreakup = OreoCommonNetworkEntity.DayBreakup(
                breakUp = breakUp,
                frequency = 5,
                date = it.date ?: ""
            )
            commonList.add(networkReq)
        }
        return commonList
    }

    private fun parseBodyTemperature(tempList: List<OreoBodyTemperatureBreakup>?): List<OreoBodyTempNetworkEntity>? {
        if (tempList.isNullOrEmpty()) {
            return null
        }

        val commonList = ArrayList<OreoBodyTempNetworkEntity>()
        tempList.forEach {
            val breakUp = Gson().fromJson<List<Float>>(it.breakUp ?: "")
            val networkReq = OreoBodyTempNetworkEntity()
            networkReq.dayBreakup = OreoBodyTempNetworkEntity.DayBreakup(
                breakUp = breakUp,
                frequency = 5,
                date = it.date ?: ""
            )
            commonList.add(networkReq)
        }
        return commonList
    }


    private fun parseBloodOxygenData(bloodOxygenList: List<OreoBloodOxygenBreakup>?): ArrayList<OreoCommonNetworkEntity>? {

        if (bloodOxygenList.isNullOrEmpty()) {
            return null
        }
        val commonList = ArrayList<OreoCommonNetworkEntity>()
        bloodOxygenList.forEach {
            val breakUp = Gson().fromJson<List<Int>>(it.breakUp ?: "")
            val networkReq = OreoCommonNetworkEntity()
            networkReq.dayBreakup = OreoCommonNetworkEntity.DayBreakup(
                breakUp = breakUp,
                frequency = 15,
                date = it.date ?: ""
            )
            commonList.add(networkReq)
        }
        return commonList
    }

    private fun parseHeartHistoryData(hrHistoryData: List<OreoHeartRate>?): List<OreoHeartNetworkEntity>? {

        if (hrHistoryData.isNullOrEmpty()) {
            return null
        }

        val commonList = ArrayList<OreoHeartNetworkEntity>()
        hrHistoryData.forEach {
            val breakUp = Gson().fromJson<List<Int>>(it.breakUp ?: "")
            val networkReq = OreoHeartNetworkEntity()
            networkReq.dayBreakup = OreoHeartNetworkEntity.DayBreakup(
                breakUp = breakUp,
                frequency = 5,
                date = it.date ?: ""
            )
            commonList.add(networkReq)
        }

        return commonList
    }

    fun getNapRequest(nap: OreoNapData): JsonObject {
        val jsonObject = JsonObject()
        val napsArray = JsonArray()
        val napObj = JsonObject().apply {
              this.addProperty("start_time",nap.startTime)
              this.addProperty("end_time",nap.endTime)
              this.addProperty("duration",nap.duration)
              this.addProperty("date",nap.date)

              /*this.addProperty("temperature",)
              this.addProperty("avg_temp",)
              this.addProperty("max_hrv",)
              this.addProperty("low_hr",)
              this.addProperty("avg_hr",)
              this.addProperty("avg_hrv",)
              this.addProperty("hr",)
              this.addProperty("hrv",)*/
        }
        napsArray.add(napObj)
        jsonObject.add("naps", napsArray)
        return jsonObject
    }

    private suspend fun getNapOverlayData(sleepData: OreoSleepData): SleepOverlayData {
        var offSet = 0
        val midnightTime = "23:59"
        val startTime = sleepData.startTime!!
        val timeIn24Hour =
            DateFormats.formatTimeInto24HoursValue(startTime, DateFormats.dateTimeFormat6).toInt()


        if (timeIn24Hour in 18..23) {
            offSet = 1
        }
        val sleepStartDate = DateFormats.subtractDateFormat3(sleepData.date!!, offSet)!!
        val sleepStartTime = DateFormats.convertDateTimeToTimeStamp3(startTime)
        val sleepEndTime = DateFormats.addSecondToTimeStamp(sleepStartTime, sleepData.timeInBedTime)

        val hrData =
            oreoHeartRateDataImpl.getHeartRateBetweenTimeStamp(sleepStartTime, sleepEndTime)
        val hrv = stressDataImpl.getStressBetweenTimeStamp(sleepStartTime, sleepEndTime)
        val resp = respiratoryData.getDataBetweenTimeStamp(sleepStartTime, sleepEndTime)
        val spo2Breakup = bloodOxygenDataImpl.getDataBetweenTimeStamp(sleepStartTime, sleepEndTime)
        val temp = temperatureData.getDataBetweenTimeStamp(sleepStartTime, sleepEndTime)

        return SleepOverlayData(hrData, hrv, resp, temp, spo2Breakup)
    }



}