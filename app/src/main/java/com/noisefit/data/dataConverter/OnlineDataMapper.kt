package com.noisefit.data.dataConverter

import com.noisefit.BuildConfig
import com.noisefit.data.local.db.implementation.HeartRateDataImpl
import com.noisefit.data.local.db.implementation.StressDataImpl
import com.noisefit_commans.data.model.*
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.data.model.OreoStepsData
import com.noisefit_commans.utils.convertToMMDDYYYY
import com.noisefit_commans.utils.to12HourFormat
import com.oreo.data.db.implementation.OreoHeartRateDataImpl
import com.oreo.data.model.OreoUserSyncActivities
import javax.inject.Inject
import kotlin.math.roundToInt

private const val TAG = "OnlineDataMapper"

class OnlineDataMapper
@Inject
constructor(
    private val stressDataImpl: StressDataImpl,
    private val heartRateDataImpl: HeartRateDataImpl,
    private val oreoHeartRateDataImpl: OreoHeartRateDataImpl,
) {

    fun convertDataToPost(userSyncActivities: UserSyncActivities): UserDataPost? {
        val combinedData = UserDataPost(
            version = BuildConfig.VERSION_CODE,
            platform = "android",
            versionName = BuildConfig.VERSION_NAME
        )
        val steps = parseStepsData(userSyncActivities.stepsDataList)
//        LOGS.d("STEPS __ ${Gson().toJson(steps)}")

        //val sleeps = parseSleepData(userSyncActivities.sleepData)
//        LOGS.d("SLEEPS __ ${Gson().toJson(sleeps)}")
        val stress = parseStressData(userSyncActivities.stressData)
//        LOGS.d("STRESS __ ${Gson().toJson(stress)}")
        val heartRateHistory = parseHeartHistoryData(userSyncActivities.hrHistoryData)
//        LOGS.d("HEART_RATE_HISTORY __ ${Gson().toJson(heartRateHistory)}")
        val bloodOxygen = parseBloodOxygenData(userSyncActivities.boData)
//        LOGS.d("BLOOD_OXYGEN __ ${Gson().toJson(bloodOxygen)}")
        val bodyTemperature = parseBodyTemperature(userSyncActivities.bodyTemperature)
//        LOGS.d("BODY_TEMPERATURE __ ${Gson().toJson(bodyTemperature)}")
        combinedData.steps = steps
        //combinedData.sleeps = sleeps
        combinedData.stress = stress
        combinedData.heartRateHistory = heartRateHistory
        combinedData.bloodOxygen = bloodOxygen
        combinedData.bodyTemperature = bodyTemperature

        if (steps == null && stress == null && heartRateHistory == null && bloodOxygen == null && bodyTemperature == null /*&& sleeps == null*/) {
            LOGS.d("Hurray!! just saved one api call")
            return null
        }
        return combinedData
    }

    suspend fun getSleepDataToPost(userSyncActivities: UserSyncActivities): UserDataPost {
        val sleeps = parseSleepData(
            userSyncActivities.sleepData
        )
        val combinedData = UserDataPost(
            version = BuildConfig.VERSION_CODE,
            platform = "android",
            versionName = BuildConfig.VERSION_NAME
        )
        combinedData.sleeps = sleeps


        return combinedData
    }


    private fun parseStepsData(stepsDataList: List<StepsData>?): ArrayList<StepsNetworkEntity>? {
        if (stepsDataList.isNullOrEmpty()) {
            return null
        }
        val stepsList = ArrayList<StepsNetworkEntity>()

        stepsDataList.forEach { stepsData ->

            val date = stepsData.date!!
            val hourlyBreakupList = ArrayList<StepsNetworkEntity.HourlyBreakup>()
            stepsData.stepArray?.forEachIndexed { index, data ->
                var hour = index
                data.hourOfTheDay?.let {
                    hour = it
                }


                if (data.steps != 0) {
                    hourlyBreakupList.add(
                        StepsNetworkEntity.HourlyBreakup(
                            data.steps,
                            data.calories,
                            data.distance,
                            data.activeTime,
                            date,
                            hour
                        )
                    )
                }


            }
            val dayBreakupList = ArrayList<StepsNetworkEntity.DayBreakup>()
            dayBreakupList.add(
                StepsNetworkEntity.DayBreakup(
                    stepsData.totalSteps,
                    stepsData.totalCalories,
                    stepsData.totalDistance,
                    stepsData.totalActiveTime,
                    date
                )
            )

            val steps = StepsNetworkEntity("STEP COUNT", hourlyBreakupList, dayBreakupList)
            stepsList.add(steps)
        }

        if (stepsList.isEmpty()) {
            return null
        }
        return stepsList
    }


    private suspend fun getSleepOverlayData(sleepData: SleepData): SleepOverlayData {

        var minHr = 0
        var maxHr = 0
        var minStress = 0
        var maxStress = 0
        val heartRateList = ArrayList<SleepHrData>()
        val stressList = ArrayList<SleepStressData>()

        val hrValueList = ArrayList<Int>()
        val stressValueList = ArrayList<Int>()
        var offSet = 0
        val midnightTime = "23:59"
        val startTime = sleepData.sleepArray!![0].startTime!!
        val timeIn24Hour = DateFormats.formatTimeInto24HoursValue(startTime).toInt()


        if (timeIn24Hour in 18..23) {
            offSet = 1
        }
        LOGS.d("SLEEPGRAPH time $startTime $midnightTime $offSet")
        val sleepStartDate = DateFormats.subtractDate(sleepData.date!!, offSet)!!
        LOGS.d("SLEEPGRAPH sleepStartDate $sleepStartDate")
        val sleepStartTime = DateFormats.convertDateTimeToTimeStamp(sleepStartDate, startTime)
        val sleepEndTime = DateFormats.addMinuteToTimeStamp(sleepStartTime, sleepData.total)

        val hrData =
            heartRateDataImpl.getHeartRateBetweenTimeStamp(sleepStartTime, sleepEndTime)
        val stressData = stressDataImpl.getStressBetweenTimeStamp(sleepStartTime, sleepEndTime)

        hrData?.forEach { heartRate ->
            hrValueList.add(heartRate.averageHeartRate)
            heartRateList.add(
                SleepHrData(
                    avg_value = heartRate.averageHeartRate,
                    time = heartRate.time,
                    date = heartRate.date,
                )
            )
        }
        stressData?.forEach { stress ->
            stressValueList.add(stress.value ?: 0)
            stressList.add(
                SleepStressData(
                    time = stress.time,
                    value = stress.value ?: 0,
                    date = stress.date,
                )
            )
        }

        maxHr = hrValueList.maxOrNull() ?: 0
        minHr = hrValueList.minOrNull() ?: 0

        maxStress = stressValueList.maxOrNull() ?: 0
        minStress = stressValueList.minOrNull() ?: 0





        return SleepOverlayData(
            hrMax = maxHr,
            hrMin = minHr,
            heartRateBreakup = heartRateList,
            stressMax = maxStress,
            stressMin = minStress,
            stressBreakUp = stressList
        )
    }

    private suspend fun parseSleepData(
        sleepDataList: List<SleepData>?
    ): ArrayList<SleepNetworkEntity>? {
        if (sleepDataList.isNullOrEmpty()) {
            return null
        }

        val sleepList = ArrayList<SleepNetworkEntity>()


        sleepDataList.forEach { sleepData ->
            LOGS.d("sleepData " + sleepData.serialize())


            val sleepOverlayData = getSleepOverlayData(sleepData)
            val hourlyList = ArrayList<SleepNetworkEntity.HourlyBreakup>()
            sleepData.sleepArray?.forEachIndexed { i, data ->
                var startTime = ""
                data.startTime?.let { it1 ->
                    startTime = it1
                }
                var endTime = ""
                data.endTime?.let { it1 ->
                    endTime = it1
                }
                var startDate = ""
                data.startDate?.let { it1 ->
                    startDate = it1
                }
                var endDate = ""
                data.endDate?.let { it1 ->
                    endDate = it1
                }
                var date = ""
                data.date?.let { it1 ->
                    date = it1
                }
                var hourOfTheDay = 0
                data.hourOfTheDay?.let { it1 ->
                    hourOfTheDay = it1
                }
                hourlyList.add(
                    SleepNetworkEntity.HourlyBreakup(
                        data.sleepType,
                        data.duration,
                        startTime,
                        endTime,
                        startDate,
                        endDate,
                        date,
                        hourOfTheDay
                    )
                )
            }
            val dayBreakupList = ArrayList<SleepNetworkEntity.DayBreakup>()
            var date = ""
            sleepData.date?.let { it1 ->
                date = it1
            }
            var startTime = ""
            sleepData.startTime?.let { it1 ->
                try {
                    startTime = it1
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            var endTime = ""
            sleepData.endTime?.let { it1 ->
                endTime = it1
            }
            var availableSleepTypes = ""
            sleepData.availableSleepTypes?.let { it1 ->
                availableSleepTypes = it1
            }

            val parsedHeartRateData = sleepOverlayData.heartRateBreakup
            val minHr = sleepOverlayData.hrMin
            val maxHr = sleepOverlayData.hrMax
            val parsedStressData = sleepOverlayData.stressBreakUp
            val minStress = sleepOverlayData.stressMin
            val maxStress = sleepOverlayData.stressMax

            dayBreakupList.add(
                SleepNetworkEntity.DayBreakup(
                    sleepData.deep,
                    sleepData.light,
                    sleepData.total,
                    startTime,
                    endTime,
                    sleepData.sober,
                    sleepData.awake,
                    date,
                    sleepData.remCount,
                    sleepData.breathQuality,
                    sleepData.sleepScore,
                    availableSleepTypes,
                    maxHr,
                    minHr,
                    parsedHeartRateData,
                    maxStress,
                    minStress,
                    parsedStressData
                )
            )

            val sleep = SleepNetworkEntity("SLEEP HOURS", hourlyList, dayBreakupList)
            sleepList.add(sleep)
        }

        if (sleepList.isEmpty()) {
            return null
        }

        return sleepList

    }


    private fun parseStressData(stressDataList: List<StressData>?): ArrayList<CommonNetworkEntity>? {
        if (stressDataList.isNullOrEmpty()) {
            return null
        }
        val commonList = ArrayList<CommonNetworkEntity>()
        stressDataList.forEach { stressData ->
            val hourlyBreakupList = ArrayList<CommonNetworkEntity.HourlyBreakup>()
            val dayBreakupList = ArrayList<CommonNetworkEntity.DayBreakup>()
            val date = stressData.date ?: ""

            stressData.stressArray?.forEachIndexed { i, data ->
                var time = ""
                var value = 0
                data.time?.let { it1 ->
                    time = it1
                }
                data.value?.let { it1 ->
                    value = it1
                }
                if (time != "" && value != 0) {
                    hourlyBreakupList.add(CommonNetworkEntity.HourlyBreakup(date, time, value))
                }
            }

            stressData.stressArray?.let { it1 ->
                var min = 0
                var max = 0
                var value = 0
                stressData.value?.let { it2 ->
                    value = it2
                }
                val stressArray = it1.filter { it2 -> it2.value != 0 }
                if (stressArray.isNotEmpty()) {
                    max = stressArray.maxOf { it2 -> it2.value!! }
                    min = stressArray.minOf { it2 -> it2.value!! }
                }
                dayBreakupList.add(CommonNetworkEntity.DayBreakup(value, "", min, max, date))
            }
            if (hourlyBreakupList.size > 0 && dayBreakupList.size > 0) {
                val commonNetworkEntity =
                    CommonNetworkEntity("STRESS COUNT", hourlyBreakupList, dayBreakupList)
                commonList.add(commonNetworkEntity)
            }

        }
        if (commonList.isEmpty()) {
            return null
        }
        return commonList
    }

    private fun parseBodyTemperature(tempList: List<BodyTemperature>?): ArrayList<BodyTempNetworkEntity>? {
        if (tempList.isNullOrEmpty()) {
            return null
        }
        val bodyTempList = ArrayList<BodyTempNetworkEntity>()
        tempList.forEach { bodyTemp ->
            val hourlyBreakupList = ArrayList<BodyTempNetworkEntity.HourlyBreakup>()
            val dayBreakupList = ArrayList<BodyTempNetworkEntity.DayBreakup>()

            val date = bodyTemp.date!!

            bodyTemp.bodyTemperatureBreakup?.forEachIndexed { i, data ->
                var time = ""
                var value = 0f
                data.time?.let { it1 ->
                    time = it1
                }
                data.value?.let { it1 ->
                    value = it1
                }
                if (time != "" && value != 0f) {
                    hourlyBreakupList.add(BodyTempNetworkEntity.HourlyBreakup(date, time, value))
                }
            }

            bodyTemp.bodyTemperatureBreakup?.let { it1 ->
                var min = 0f
                var max = 0f
                var value = 0f
                bodyTemp.bodyTemperature.let { it2 ->
                    value = it2
                }
                val stressArray = it1.filter { it2 -> it2.value != 0f }
                if (stressArray.isNotEmpty()) {
                    max = stressArray.maxOf { it2 -> it2.value!! }
                    min = stressArray.minOf { it2 -> it2.value!! }
                }
                dayBreakupList.add(BodyTempNetworkEntity.DayBreakup(value, "", min, max, date))
            }
            if (dayBreakupList.size > 0 && hourlyBreakupList.size > 0) {
                val bodyTempEntity =
                    BodyTempNetworkEntity("Body Temperature", hourlyBreakupList, dayBreakupList)
                bodyTempList.add(bodyTempEntity)
            }

        }

        if (bodyTempList.isEmpty()) {
            return null
        }

        return bodyTempList
    }


    private fun parseBloodOxygenData(bloodOxygenList: List<BloodOxygen>?): ArrayList<CommonNetworkEntity>? {

        if (bloodOxygenList.isNullOrEmpty()) {
            return null
        }
        val commonList = ArrayList<CommonNetworkEntity>()
        bloodOxygenList.forEach { bloodOxygen ->
            val hourlyBreakupList = ArrayList<CommonNetworkEntity.HourlyBreakup>()
            val dayBreakupList = ArrayList<CommonNetworkEntity.DayBreakup>()
            val date = bloodOxygen.date!!

            bloodOxygen.bloodOxygenArray?.forEachIndexed { i, data ->
                var time = ""
                var value = 0
                data.time?.let { it1 ->
                    time = it1
                }
                data.value?.let { it1 ->
                    value = it1
                }
                if (time != "" && value != 0) {
                    hourlyBreakupList.add(CommonNetworkEntity.HourlyBreakup(date, time, value))
                }
            }

            bloodOxygen.bloodOxygenArray?.let { it1 ->
                var min = 0
                var max = 0
                var value = 0
                bloodOxygen.bloodOxygen?.let { it2 ->
                    value = it2
                }
                val boArray = it1.filter { it2 -> it2.value != 0 }
                if (boArray.isNotEmpty()) {
                    max = boArray.maxOf { it2 -> it2.value!! }
                    min = boArray.minOf { it2 -> it2.value!! }
                }
                dayBreakupList.add(CommonNetworkEntity.DayBreakup(value, "", min, max, date))
            }
            if (dayBreakupList.size > 0 && hourlyBreakupList.size > 0) {
                val commonNetworkEntity =
                    CommonNetworkEntity("BLOOD OXYGEN", hourlyBreakupList, dayBreakupList)
                commonList.add(commonNetworkEntity)
            }
        }
        if (commonList.isEmpty()) {
            return null
        }

        return commonList
    }

    private fun parseHeartHistoryData(hrHistoryData: List<HeartRateHistory>?): ArrayList<HeartNetworkEntity>? {

        if (hrHistoryData.isNullOrEmpty()) {
            return null
        }

        val commonList = ArrayList<HeartNetworkEntity>()

        var date = ""
        // hrPostData.type = AppConstants.HEART_RATE
        hrHistoryData.forEach { heartRateHistory ->
            val dayBreakupList = ArrayList<HeartNetworkEntity.DayBreakup>()
            val hourlyBreakupList = ArrayList<HeartNetworkEntity.HourlyBreakup>()
            heartRateHistory.heartRate?.let { heartRate ->
                date = heartRate.date ?: ""
                dayBreakupList.add(
                    HeartNetworkEntity.DayBreakup(
                        heartRate.lowestHeartRate,
                        heartRate.highestHeartRate,
                        heartRateHistory.getAverageHeartRate(),
                        heartRate.restingHeartRate,
                        date
                    )
                )
            }

            if (!heartRateHistory.heartRateList.isNullOrEmpty()) {
                for (hour in 0..23) {
                    val historyBreakup =
                        heartRateHistory.heartRateList!!.filter { it1 ->
                            it1.time != null && it1.time!!.split(
                                ":"
                            )[0].toInt() == hour
                        }
                    if (historyBreakup.isNotEmpty()) {
                        hourlyBreakupList.add(
                            HeartNetworkEntity.HourlyBreakup(
                                historyBreakup.minOf { it2 -> it2.averageHeartRate },
                                historyBreakup.maxOf { it2 -> it2.averageHeartRate },
                                historyBreakup.map { s -> s.averageHeartRate }.average()
                                    .roundToInt(),
                                hour,
                                date
                            )
                        )
                    }
//                    else {
                    //  hourlyBreakupList.add(HeartNetworkEntity.HourlyBreakup(0, 0, 0, hour, date))
//                    }
                }
            }

            if (dayBreakupList.size > 0 && hourlyBreakupList.size > 0) {
                val commonNetworkEntity =
                    HeartNetworkEntity(AppConstants.HEART_RATE, hourlyBreakupList, dayBreakupList)
                commonList.add(commonNetworkEntity)
            }

        }

        if (commonList.isEmpty()) {
            return null
        }

        return commonList
    }


}


