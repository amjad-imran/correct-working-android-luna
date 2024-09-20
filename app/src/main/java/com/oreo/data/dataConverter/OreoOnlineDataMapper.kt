package com.oreo.data.dataConverter

import com.google.gson.Gson
import com.noisefit_commans.common.averageWithoutZero
import com.noisefit_commans.common.averageWithoutZeroFloat
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.common.minWithoutZero
import com.noisefit_commans.data.model.DayTimeMovementBreakup
import com.noisefit_commans.data.model.OreoBloodOxygenBreakup
import com.noisefit_commans.data.model.OreoBodyStressData
import com.noisefit_commans.data.model.OreoBodyTempNetworkEntity
import com.noisefit_commans.data.model.OreoBodyTemperatureBreakup
import com.noisefit_commans.data.model.OreoCommonNetworkEntity
import com.noisefit_commans.data.model.OreoHeartNetworkEntity
import com.noisefit_commans.data.model.OreoHeartRate
import com.noisefit_commans.data.model.OreoNapData
import com.noisefit_commans.data.model.OreoNapNetworkEntity
import com.noisefit_commans.data.model.OreoNapNetworkObjEntity
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
import com.oreo.data.model.NapOverlayData
import com.oreo.data.model.OreoUserSyncActivities
import com.oreo.data.model.SleepOverlayData
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

class OreoOnlineDataMapper
@Inject constructor(
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
        val bodyStress = parseBodyStressData(userSyncActivities.bodyStressData)
        val bodyTemperature = parseBodyTemperature(userSyncActivities.bodyTemperature)
        val respiratory = parseRespiratoryData(userSyncActivities.respiratory)

        val sleeps = parseSleepDataOreo(userSyncActivities.sleepData)

        combinedData.activities = steps
        combinedData.stress = stress
        combinedData.bodyStress = bodyStress
        combinedData.heartRateHistory = heartRateHistory
        combinedData.bloodOxygen = bloodOxygen
        combinedData.bodyTemperature = bodyTemperature
        combinedData.respiratory = respiratory
        combinedData.sleeps = sleeps

        if (steps == null && stress == null && heartRateHistory == null
            && bloodOxygen == null && bodyTemperature == null && respiratory == null && bodyStress == null
        ) {
            LOGS.d("Hurray!! just saved one api call")
            return null
        }
        return combinedData
    }

    @Deprecated("not in use")
    suspend fun getOreoSleepDataToPost(userSyncActivities: OreoUserSyncActivities): OreoUserDataPost {
        val sleeps = parseSleepDataOreo(
            userSyncActivities.sleepData
        )
        val combinedData = OreoUserDataPost()
        combinedData.sleeps = sleeps


        return combinedData
    }

    private fun parseStepsDataOreo(
        stepsDataList: List<OreoStepsData>?, dayTimeMovement: List<DayTimeMovementBreakup>?
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
                            data.steps, data.activeCalories, data.calories, data.distance, hour
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


            val dayBreakup = OreoStepsNetworkEntity.DayBreakup(
                stepsData.totalSteps,
                stepsData.activeCalories ?: 0,
                stepsData.totalCalories,
                stepsData.totalDistance,
                date
            )

            val steps = OreoStepsNetworkEntity(
                hourlyBreakupList, dayBreakup, dayTimeMovement = dayDataBreakup
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
            DateFormats.formatTimeInto24HoursValue(startTime, DateFormats.dateTimeFormat6()).toInt()


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
            val tempNewArray = filterTempArray(sleepOverlayData.tempBreakup)
            val avgTemp =
                String.format(
                    locale = Locale.US,
                    "%.1f",
                    tempNewArray.averageWithoutZeroFloat()
                )

            dayBreakup = OreoSleepNetworkEntity.OreoDayBreakup(
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
                avgTemp = avgTemp.toFloat(),
                avgOxy = if (sleepOverlayData.spo2Breakup.isEmpty()) 0 else sleepOverlayData.spo2Breakup.averageWithoutZero(),
                avgResp = if (sleepOverlayData.respBreakup.isEmpty()) 0 else sleepOverlayData.respBreakup.averageWithoutZero(),
                maxTemp = tempNewArray.maxOrNull() ?: 0f,
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

    private fun filterTempArray(tempBreakup: List<Float>): List<Float> {
        return tempBreakup.filter { it != 0f && it != 255f && it in 90f..110f }
    }


    suspend fun getNapRequest(nap: OreoNapData): OreoNapNetworkEntity {
        val overlayData = getNapOverlayData(nap)
        val tempNewArray = filterTempArray(overlayData.tempBreakup)

        val avgTemp = String.format(
            locale = Locale.US,
            "%.1f",
            tempNewArray.averageWithoutZeroFloat()
        )

        val napObject = OreoNapNetworkObjEntity(
            startTime = nap.startTime ?: "",
            endTime = nap.endTime ?: "",
            duration = nap.duration,
            date = nap.date ?: "",
            avgHrv = overlayData.hrvBreakup.minWithoutZero(),
            temperature = overlayData.tempBreakup,
            avgTemp = avgTemp.toFloat(),
            hr = overlayData.hrBreakup,
            hrv = overlayData.hrvBreakup,
            avgHr = overlayData.hrBreakup.averageWithoutZero(),
            lowHr = overlayData.hrBreakup.minWithoutZero(),
            maxHrv = overlayData.hrvBreakup.maxOrNull() ?: 0
        )
        return OreoNapNetworkEntity(
            naps = arrayListOf(napObject)
        )
    }

    private suspend fun getNapOverlayData(napData: OreoNapData): NapOverlayData {
        //val startTime = napData.startTime!!

        val sleepStartTime = DateFormats.convertDateTimeToTimeStamp3(napData.startTime!!)
        val sleepEndTime = DateFormats.addSecondToTimeStamp(sleepStartTime, napData.duration * 60)

        val hrData =
            oreoHeartRateDataImpl.getHeartRateBetweenTimeStamp(sleepStartTime, sleepEndTime)
        val hrv = stressDataImpl.getStressBetweenTimeStamp(sleepStartTime, sleepEndTime)
        val resp = respiratoryData.getDataBetweenTimeStamp(sleepStartTime, sleepEndTime)
        val spo2Breakup = bloodOxygenDataImpl.getDataBetweenTimeStamp(sleepStartTime, sleepEndTime)
        val temp = temperatureData.getDataBetweenTimeStamp(sleepStartTime, sleepEndTime)

        return NapOverlayData(hrData, hrv, resp, temp, spo2Breakup)
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
                breakUp = breakUp, frequency = 5, date = it.date ?: ""
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
                breakUp = breakUp, frequency = 5, date = it.date ?: ""
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
                breakUp = breakUp, frequency = 5, date = it.date ?: ""
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
                breakUp = breakUp, frequency = 15, date = it.date ?: ""
            )
            commonList.add(networkReq)
        }
        return commonList
    }

    private fun parseBodyStressData(bodyStressList: List<OreoBodyStressData>?): ArrayList<OreoCommonNetworkEntity>? {

        if (bodyStressList.isNullOrEmpty()) {
            return null
        }
        val commonList = ArrayList<OreoCommonNetworkEntity>()
        bodyStressList.forEach {
            val breakUp = Gson().fromJson<List<Int>>(it.breakUp ?: "")
            val networkReq = OreoCommonNetworkEntity()
            val lastValue = getLastNonZeroValue(breakUp, it.date)
            networkReq.dayBreakup = OreoCommonNetworkEntity.DayBreakup(
                breakUp = breakUp,
                frequency = 15,
                date = it.date ?: "",
                stressValue = lastValue.first,
                lastUpdated = lastValue.second

            )
            commonList.add(networkReq)
        }
        return commonList
    }

    private fun getLastNonZeroValue(breakUp: List<Int>, date: String?): Pair<Int, Long> {
        if (date == null) return Pair(0, 0)
        val value = breakUp.lastOrNull { it != 0 && it != 255 }
        val index = breakUp.indexOfLast { it != 0 && it != 255 }

        return if (value != null && index != -1) {
            val timestamp =
                (DateFormats.dateFormat3().parse(date)?.time ?: 0L) + ((index * 15) * 60000)
            Pair(value, timestamp)
        } else {
            Pair(0, 0)
        }

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
                breakUp = breakUp, frequency = 5, date = it.date ?: ""
            )
            commonList.add(networkReq)
        }

        return commonList
    }

}