package com.noisefit.data.dataConverter

import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.R
import com.noisefit.data.local.db.implementation.HeartRateDataImpl
import com.noisefit.data.local.db.implementation.StressDataImpl
import com.noisefit_commans.data.model.CountCardData
import com.noisefit_commans.data.model.HealthOverview
import com.noisefit_commans.data.model.HealthOverviewData
import com.noisefit_commans.data.model.SleepOfflineOverlayData
import com.noisefit.ui.common.calculatePercentage
import com.noisefit.util.ApplicationUtils.getSleepType
import com.noisefit.util.graph.SleepChartUtils
import com.noisefit.util.graph.StressBarChartUtils
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.models.*
import com.noisefit_commans.response.SleepBreakup
import com.noisefit_commans.response.SleepHeartRate
import com.noisefit_commans.response.SleepHourBreakup
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.data.model.OreoSleepData
import javax.inject.Inject
import kotlin.math.roundToInt


class OfflineDataMapper
@Inject
constructor(
    val watches: WatchesSDK,
    private val stressDataImpl: StressDataImpl,
    private val heartRateDataImpl: HeartRateDataImpl,
) {

    fun convertSleepDataToGoogleFit(sleepData: SleepData?): SleepDataGoogleFit? {
        LOGS.d("DATACONVERTER sleepData ${sleepData?.startTime}  ${sleepData?.endTime}")
        if (sleepData?.startTime == null || sleepData.date == null || sleepData.sleepArray.isNullOrEmpty() || sleepData.sleepArray!![0].startTime == null) {
            return null
        }

        // val sleepDataGoogleFit = SleepDataGoogleFit()
        val googleFitSleepBreakUpList = ArrayList<SleepDataGoogleFit.SleepDataBreakup>()
        var offSet = 0
        val midnightTime = "23:59"
        val startTime = sleepData.sleepArray!![0].startTime!!

        if (DateFormats.isTimeBefore(startTime, midnightTime)) {
            offSet = 1
        }
        LOGS.d("DATACONVERTER time $startTime $midnightTime $offSet")
        val sleepStartDate = DateFormats.subtractDate(sleepData.date!!, offSet)!!
        LOGS.d("DATACONVERTER sleepStartDate $sleepStartDate")
        val sleepStartTime = DateFormats.convertDateTimeToTimeStamp(sleepStartDate, startTime)
        LOGS.d("DATACONVERTER sleepStartTime $sleepStartTime")
        sleepData.sleepArray!!.forEach { sleepDataBreakup ->
            var breakUpStartTime =
                DateFormats.convertDateTimeToTimeStamp(sleepStartDate, sleepDataBreakup.startTime!!)
            var breakupEndTime = 0L
            if (sleepStartTime <= breakUpStartTime) {
                breakupEndTime =
                    DateFormats.addMinuteToTimeStamp(breakUpStartTime, sleepDataBreakup.duration)
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
                    DateFormats.addMinuteToTimeStamp(breakUpStartTime, sleepDataBreakup.duration)
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


    fun convertSportDataToGoogleFit(sportsModeResponse: SportsModeResponse): SportsDataGoogleFit? {
        if (sportsModeResponse.date == null || sportsModeResponse.time == null || sportsModeResponse.duration == null) {
            return null
        }
        val dateWithTime = DateFormats.convertDateTimeToTimeStampWithISO(
            sportsModeResponse.time!!
        )
        val endTime =
            DateFormats.addSecondToTimeStamp(dateWithTime, sportsModeResponse.duration!!.toInt())
        val distance = sportsModeResponse.distance?.toFloat() ?: 0f
        val duration = sportsModeResponse.duration?.toInt() ?: 0
        val calories = sportsModeResponse.calories?.toFloat() ?: 0f
        val heartRate = sportsModeResponse.heartRateCurrent?.toFloat() ?: 0f
        var steps = 0
        if (sportsModeResponse.steps != 0) {
            steps = sportsModeResponse.steps ?: 0
        }
        val type = sportsModeResponse.type ?: ""

        return SportsDataGoogleFit(
            dateWithTime,
            endTime,
            distance,
            duration,
            calories,
            heartRate,
            steps,
            type
        )

    }

    fun convertHealthOverviewData(
        stepsData: StepsData?,
        userGoals: UserGoals?,
        dataUnitConverter: DataUnitConverter
    ): HealthOverviewData {
        val healthOverviewData = HealthOverviewData()

        val unit = userGoals?.getUnit()
        val aMinutes = stepsData?.totalActiveTime ?: 0

        healthOverviewData.activeMinute = aMinutes
        healthOverviewData.activeMinuteGoal = userGoals?.durationInMin

//        val activeMinPercentage = aMinutes.toFloat()
//            .calculatePercentage(healthOverviewData.activeMinuteGoal?.toFloat())

//        healthOverviewData.activeMinuteGoalProgress = if (activeMinPercentage >= 100f) {
//            100f
//        } else {
//            activeMinPercentage
//        }


        val distance = stepsData?.totalDistance ?: 0
        val userDistance = userGoals?.distanceGoal ?: 0


        val distanceUnit = dataUnitConverter.distanceUnit(unit)
        healthOverviewData.distance =
            dataUnitConverter.formatDistance(
                distance,
                unit
            )
        healthOverviewData.distanceGoal = "${
            dataUnitConverter.formatDistanceGoal(
                userDistance,
                unit
            )
        } $distanceUnit"

        val distancePercentage = distance.toFloat()
            .calculatePercentage(userDistance.toFloat())

        healthOverviewData.distanceGoalProgress = if (distancePercentage >= 100) {
            100f
        } else {
            distancePercentage
        }



        healthOverviewData.calories = stepsData?.totalCalories ?: 0
        healthOverviewData.caloriesGoal = userGoals?.caloriesGoal ?: 0

        val caloriesPercentage = healthOverviewData.calories!!.toFloat()
            .calculatePercentage(healthOverviewData.caloriesGoal!!.toFloat())


        healthOverviewData.steps = stepsData?.totalSteps ?: 0
        healthOverviewData.stepsGoal = userGoals?.stepGoal ?: 0

        val stepsPercentage = healthOverviewData.steps!!.toFloat()
            .calculatePercentage(healthOverviewData.stepsGoal!!.toFloat())
        healthOverviewData.stepsGoalProgress = stepsPercentage


        healthOverviewData.caloriesGoalProgress = if (caloriesPercentage >= 100) {
            100f
        } else {
            caloriesPercentage
        }

        var hours = 0
        stepsData?.stepArray?.forEach { breakUp ->
            if (breakUp.steps > 100) {
                hours += 1
            }
        }

        healthOverviewData.standGoal = userGoals?.standingHr
        healthOverviewData.stand = hours

        val standPercentage = hours.toFloat()
            .calculatePercentage(healthOverviewData.standGoal?.toFloat())
        healthOverviewData.standGoalProgress = if (standPercentage >= 100) {
            100f
        } else {
            standPercentage
        }


        return healthOverviewData
    }

    fun convertStepsOverviewData(stepsData: StepsData?, userGoals: UserGoals?): HealthOverview {
        var value = "--"
        // var totalSteps = 0
        var timeAgo = ""
        val goals = userGoals?.stepGoal ?: 0
        if (stepsData != null && stepsData.totalSteps != 0) {
            value = stepsData.totalSteps.toString()
            //  totalSteps = stepsData.totalSteps
        }

//        var percentage = totalSteps.toFloat().calculatePercentage(goals.toFloat())
//
//        if (percentage == 0f) {
//            percentage = 70f
//        }
        timeAgo = stepsData?.timeStamp?.let { DateFormats.getRelativeTime(it) }.toString()
        return HealthOverview.Steps(
            value,
            0f,
            goals,
            timeAgo
        )
    }

    fun convertDistanceOverviewData(
        stepsData: StepsData?,
        userGoals: UserGoals?,
        dataUnitConverter: DataUnitConverter
    ): HealthOverview {
        var value = "--"
        var timeAgo = ""
        var distanceUnit = ""
        if (stepsData != null && stepsData.totalDistance != 0) {
            distanceUnit = dataUnitConverter.distanceUnit(userGoals?.getUnit())
            value = dataUnitConverter.formatDistance(
                stepsData.totalDistance,
                userGoals?.getUnit()
            )
            timeAgo = stepsData.timeStamp?.let { DateFormats.getRelativeTime(it) }.toString()
        }

        return HealthOverview.Distance(
            value,
            timeAgo,
            distanceUnit
        )
    }

    fun convertCaloriesOverviewData(
        stepsData: StepsData?
    ): HealthOverview {
        var value = "--"
        var timeAgo = ""
        if (stepsData != null && stepsData.totalCalories != 0) {
            value = stepsData.totalCalories.toString()
            timeAgo = stepsData.timeStamp?.let { DateFormats.getRelativeTime(it) }.toString()
        }

        return HealthOverview.Calories(
            value,
            timeAgo,
            "kcal"
        )
    }

    fun convertHeartRateOverviewData(
        dataList: List<HeartRate>?
    ): HealthOverview {

        var value = "--"
        var timeAgo = ""
        var average = ""
        if (!dataList.isNullOrEmpty()) {
            val currentValue = dataList.last()
            value = "${currentValue.averageHeartRate}"
            timeAgo = currentValue.timeStamp?.let { DateFormats.getRelativeTime(it) }.toString()
            average = "All day average ${
                dataList.map { it.averageHeartRate }.average().roundToInt()
            } bpm"

        }


        return HealthOverview.HeartRate(
            value,
            timeAgo,
            average
        )
    }

    fun convertStressOverviewData(
        dataList: List<StressDataBreakup>?
    ): HealthOverview {

        var value = "--"
        var timeAgo = ""
        var type = ""
        var average = ""
        if (!dataList.isNullOrEmpty()) {
            val currentValue = dataList.last()
            value =
                "${currentValue.value}"
            timeAgo = currentValue.timeStamp?.let { DateFormats.getRelativeTime(it) }.toString()
            type = StressBarChartUtils.stressType(currentValue.value ?: 0)

            val avg = dataList.map { it.value?:0 }.average().roundToInt()
            average = "All day average $avg (${StressBarChartUtils.stressType(avg)})"


        }

        return HealthOverview.Stress(
            value,
            type,
            timeAgo,
            average
        )
    }


    fun convertBloodOxygenOverviewData(
        dataList: List<BloodOxygenBreakup>?
    ): HealthOverview {
        var value = "--"
//        var valueInInt = 70
        var timeAgo = ""
        var average = ""
        if (!dataList.isNullOrEmpty()) {
            val currentValue = dataList.last()
            value = "${currentValue.value}%"
//            valueInInt = currentValue.value ?: 70
            timeAgo = currentValue.timeStamp?.let { DateFormats.getRelativeTime(it) }.toString()
            val avg = dataList.map { it.value?:0 }.average().roundToInt()
            average = "All day average $avg %"

        }



        return HealthOverview.BloodOxygen(
            value,
            average,
            timeAgo
        )
    }


    fun convertSleepOverviewData(
        dataList: List<SleepData>?
    ): HealthOverview {
        var duration = -1
        var date = ""
        var sleepScore = ""
        if (!dataList.isNullOrEmpty()) {
            val lastData = dataList.last()
            // val currentValue = dataList.last()
            duration = lastData.total
            date = DateFormats.formatDate(
                lastData.date,
                DateFormats.dateFormat,
                DateFormats.monthDateWithoutYear
            )
            if(lastData.sleepScore !=0){
                sleepScore =
                    "${lastData.sleepScore} (${SleepChartUtils.getSleepScoreMessage(lastData.sleepScore).first})"
            }

        }


//        val dummyData = parse24HoursFormatSleep(convertSleepData(dataList))
        return HealthOverview.Sleep(
            duration,
            date,
            sleepScore
        )
    }

    fun convertHeartRate(data: List<HeartRate>?): HeartRateHistory {
        val heartRateHistory = HeartRateHistory()
        if (data.isNullOrEmpty()) {
            return heartRateHistory
        }

        heartRateHistory.heartRateList = data
        val heartRate = data.last()


        if (heartRate.lowestHeartRate == 0 || heartRate.highestHeartRate == 0) {
            val highestHeartRate =
                data.maxByOrNull { item -> item.averageHeartRate }?.averageHeartRate
            val lowestHeartRate =
                data.minByOrNull { item -> item.averageHeartRate }?.averageHeartRate
            heartRate.highestHeartRate = highestHeartRate ?: heartRate.averageHeartRate
            heartRate.lowestHeartRate = lowestHeartRate ?: heartRate.averageHeartRate
        }

        heartRateHistory.lastValue = heartRate.averageHeartRate.toString()
        heartRateHistory.heartRate = heartRate
        return heartRateHistory

    }


    fun convertStressData(data: List<StressDataBreakup>?): StressData {
        val stressData = StressData()
        if (data.isNullOrEmpty()) {
            return stressData
        }
        stressData.stressArray = data
        val lastElement = data.last()
        stressData.date = lastElement.date
        stressData.value = lastElement.value
        stressData.lastValue = lastElement.value
        return stressData

    }

    fun convertBloodOxygenData(data: List<BloodOxygenBreakup>?): BloodOxygen {
        val bloodOxygen = BloodOxygen()
        if (data.isNullOrEmpty()) {
            return bloodOxygen
        }
        bloodOxygen.bloodOxygenArray = data
        val lastElement = data.last()
        bloodOxygen.date = lastElement.date

        bloodOxygen.lastValue = lastElement.value
        bloodOxygen.bloodOxygen = lastElement.value
        return bloodOxygen

    }

    fun convertBodyTempOverviewData(
        dataList: List<BodyTemperatureBreakup>?,
        dataUnitConverter: DataUnitConverter,
        units: Units
    ): HealthOverview {

        var value = "--"
        var timeAgo = ""
        var average = ""
        if (!dataList.isNullOrEmpty()) {
            val unit = dataUnitConverter.bodyTempUnit(units)
            val currentValue = dataList.last()
            value = "${
                dataUnitConverter.formatBodyTemp(
                    currentValue.value ?: 0f,
                    units
                )
            } $unit"
            timeAgo = currentValue.timeStamp?.let { DateFormats.getRelativeTime(it) }.toString()

            var total = 0f
            var count = 0
            dataList.forEach {
                if (it.value != 0f) {
                    count += 1
                    total += it.value!!
                }
            }
            if (count != 0) {
                val avgValue = (total / count).toInt()
                average = "All day average $avgValue $unit"
            }
        }

        return HealthOverview.BodyTemp(
            value,
            timeAgo,
            average
        )
    }

    fun convertBodyTempData(data: List<BodyTemperatureBreakup>?): BodyTemperature {
        val bodyTemperature = BodyTemperature()
        if (data.isNullOrEmpty()) {
            return bodyTemperature
        }

        bodyTemperature.bodyTemperatureBreakup = data
        val lastElement = data.last()
        bodyTemperature.date = lastElement.date

        bodyTemperature.lastValue = lastElement.value ?: 0f
        bodyTemperature.bodyTemperature = lastElement.value ?: 0f
        return bodyTemperature
    }


    private suspend fun getSleepOverlayData(sleepData: SleepData): SleepOfflineOverlayData {
        var minHr = 0
        var maxHr = 0
        var minStress = 0
        var maxStress = 0
        if (sleepData.sleepArray.isNullOrEmpty()) {
            return SleepOfflineOverlayData(
                hrMax = maxHr,
                hrMin = minHr,
                heartRateBreakup = ArrayList(),
                stressMax = maxStress,
                stressMin = minStress,
                stressBreakUp = ArrayList()
            )
        }

        val heartRateList = ArrayList<SleepHeartRate>()
        val stressList = ArrayList<SleepHeartRate>()

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
                SleepHeartRate(
                    avg_value = heartRate.averageHeartRate,
                    time = heartRate.time,
                    date = heartRate.date,
                )
            )
        }
        stressData?.forEach { stress ->
            stressValueList.add(stress.value ?: 0)
            stressList.add(
                SleepHeartRate(
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





        return SleepOfflineOverlayData(
            hrMax = maxHr,
            hrMin = minHr,
            heartRateBreakup = heartRateList,
            stressMax = maxStress,
            stressMin = minStress,
            stressBreakUp = stressList
        )
    }

    private suspend fun getSleepOverlayData(sleepData: OreoSleepData): SleepOfflineOverlayData {
        var minHr = 0
        var maxHr = 0
        var minStress = 0
        var maxStress = 0
        if (sleepData.sleepArray.isNullOrEmpty()) {
            return SleepOfflineOverlayData(
                hrMax = maxHr,
                hrMin = minHr,
                heartRateBreakup = ArrayList(),
                stressMax = maxStress,
                stressMin = minStress,
                stressBreakUp = ArrayList()
            )
        }

        val heartRateList = ArrayList<SleepHeartRate>()
        val stressList = ArrayList<SleepHeartRate>()

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
                SleepHeartRate(
                    avg_value = heartRate.averageHeartRate,
                    time = heartRate.time,
                    date = heartRate.date,
                )
            )
        }
        stressData?.forEach { stress ->
            stressValueList.add(stress.value ?: 0)
            stressList.add(
                SleepHeartRate(
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





        return SleepOfflineOverlayData(
            hrMax = maxHr,
            hrMin = minHr,
            heartRateBreakup = heartRateList,
            stressMax = maxStress,
            stressMin = minStress,
            stressBreakUp = stressList
        )
    }


    suspend fun convertSleepDataForDetailsView(sleepData: SleepData?): SleepBreakup {
        val date = DateFormats.getTodaysDateString(10)
        if (sleepData == null) {
            return SleepBreakup(
                date = date,
                deep = 0,
                light = 0,
                sober = 0,
                hour_of_the_day = 0,
                month = 0,
                awake = 0,
                hourly_breakup = ArrayList(),
                hr_breakup = ArrayList(),
                stress_breakup = ArrayList(),
                hr_max = 0,
                hr_min = 0,
                stress_max = 0,
                stress_min = 0,
                duration = 0,
                avg_duration = 0,
                rem = 0,
                total_duration = 0,
                start_time = null,
                end_time = null,
                sleepScore = 0,
                breathQuality = 0
            )
        }


        val sleepHourBreakup = ArrayList<SleepHourBreakup>()
        sleepData.sleepArray?.forEach { sleepDataBreakup ->
            sleepHourBreakup.add(
                SleepHourBreakup(
                    startTime = sleepDataBreakup.startTime,
                    startDate = sleepDataBreakup.startDate,
                    type = sleepDataBreakup.sleepType,
                    endDate = sleepDataBreakup.endDate,
                    endTime = sleepDataBreakup.endTime,
                    date = sleepDataBreakup.date,
                    duration = sleepDataBreakup.duration,
                    hour_of_the_day = sleepDataBreakup.hourOfTheDay ?: 0
                )
            )
        }

        val sleepOverlayData = getSleepOverlayData(sleepData)
        val parsedHeartRateData = sleepOverlayData.heartRateBreakup
        val minHr = sleepOverlayData.hrMin
        val maxHr = sleepOverlayData.hrMax
        val parsedStressData = sleepOverlayData.stressBreakUp
        val minStress = sleepOverlayData.stressMin
        val maxStress = sleepOverlayData.stressMax

        return SleepBreakup(
            date = date,
            deep = sleepData.deep,
            light = sleepData.light,
            sober = sleepData.sober,
            hour_of_the_day = 0,
            month = 0,
            awake = sleepData.awake,
            hourly_breakup = sleepHourBreakup,
            hr_breakup = parsedHeartRateData,
            stress_breakup = parsedStressData,
            hr_max = maxHr,
            hr_min = minHr,
            stress_max = maxStress,
            stress_min = minStress,
            duration = sleepData.total,
            avg_duration = 0,
            rem = sleepData.remCount,
            total_duration = sleepData.total,
            start_time = sleepData.startTime,
            end_time = sleepData.endTime,
            sleepScore = sleepData.sleepScore,
            breathQuality = sleepData.breathQuality
        )
    }

    suspend fun convertSleepDataForDetailsView(sleepData: OreoSleepData?): SleepBreakup {
        val date = DateFormats.getTodaysDateString(10)
        if (sleepData == null) {
            return SleepBreakup(
                date = date,
                deep = 0,
                light = 0,
                sober = 0,
                hour_of_the_day = 0,
                month = 0,
                awake = 0,
                hourly_breakup = ArrayList(),
                hr_breakup = ArrayList(),
                stress_breakup = ArrayList(),
                hr_max = 0,
                hr_min = 0,
                stress_max = 0,
                stress_min = 0,
                duration = 0,
                avg_duration = 0,
                rem = 0,
                total_duration = 0,
                start_time = null,
                end_time = null,
                sleepScore = 0,
                breathQuality = 0
            )
        }


        val sleepHourBreakup = ArrayList<SleepHourBreakup>()
        sleepData.sleepArray?.forEach { sleepDataBreakup ->
            sleepHourBreakup.add(
                SleepHourBreakup(
                    startTime = sleepDataBreakup.startTime,
                    startDate = sleepDataBreakup.startDate,
                    type = sleepDataBreakup.sleepType,
                    endDate = sleepDataBreakup.endDate,
                    endTime = sleepDataBreakup.endTime,
                    date = sleepDataBreakup.date,
                    duration = sleepDataBreakup.duration,
                    hour_of_the_day = sleepDataBreakup.hourOfTheDay ?: 0
                )
            )
        }

        val sleepOverlayData = getSleepOverlayData(sleepData)
        val parsedHeartRateData = sleepOverlayData.heartRateBreakup
        val minHr = sleepOverlayData.hrMin
        val maxHr = sleepOverlayData.hrMax
        val parsedStressData = sleepOverlayData.stressBreakUp
        val minStress = sleepOverlayData.stressMin
        val maxStress = sleepOverlayData.stressMax

        return SleepBreakup(
            date = date,
            deep = sleepData.deep,
            light = sleepData.light,
            sober = sleepData.sober,
            hour_of_the_day = 0,
            month = 0,
            awake = sleepData.awake,
            hourly_breakup = sleepHourBreakup,
            hr_breakup = parsedHeartRateData,
            stress_breakup = parsedStressData,
            hr_max = maxHr,
            hr_min = minHr,
            stress_max = maxStress,
            stress_min = minStress,
            duration = sleepData.total,
            avg_duration = 0,
            rem = sleepData.remCount,
            total_duration = sleepData.total,
            start_time = sleepData.startTime,
            end_time = sleepData.endTime,
            sleepScore = sleepData.sleepScore,
            breathQuality = sleepData.breathQuality
        )
    }


    fun convertSleepData(data: List<SleepData>?): SleepData {
        val sleepData = SleepData()
        if (data.isNullOrEmpty()) {
            return sleepData
        }

        if (data.size == 1) {
            return data[0]
        }

        val watchType = watches.getWatchType()
        if(watchType == SDKWatchType.SDK_RYEEX){
            return data[data.size - 1]
        }


        val sleepArray = ArrayList<SleepData.SleepDataBreakup>()
        val sleepDataSize = data.size - 1
        sleepData.startTime = data[0].startTime
        sleepData.startDate = data[0].startDate
        sleepData.availableSleepTypes = data[0].availableSleepTypes
        sleepData.endTime = data[sleepDataSize].endTime
        sleepData.endDate = data[sleepDataSize].endDate
        sleepData.date = data[sleepDataSize].date
        sleepData.breathQuality = data[sleepDataSize].breathQuality
        sleepData.sleepScore = data[sleepDataSize].sleepScore

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

    fun convertSleepData(data: List<OreoSleepData>?): OreoSleepData {
        val sleepData = OreoSleepData()
        if (data.isNullOrEmpty()) {
            return sleepData
        }

        if (data.size == 1) {
            return data[0]
        }

        val watchType = watches.getWatchType()
        if(watchType == SDKWatchType.SDK_RYEEX){
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



    fun getTotalSleep(total: Int, awake: Int): Int {
        val device = watches.getDevice()
        if (device?.deviceType == DeviceType.COLORFIT_PULSE_2.deviceType||device?.deviceType == DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType) {
            return (total + awake)
        }

        val watchType = watches.getWatchType()
        return if (watchType?.name == SDKWatchType.SDK_NAV_PLUS.name || watchType?.name == SDKWatchType.SDK_ZH.name) {
            (total + awake)
        } else {
            total
        }
    }


    fun convertUnSyncHeartRateDataListToObject(data: List<HeartRate>?): List<HeartRateHistory>? {
        if (data.isNullOrEmpty()) {
            return null
        }
        val hm = HashMap<String, ArrayList<HeartRate>>()
        data.forEach { mData ->
            val date = mData.date!!
            if (hm.containsKey(date)) {
                val dataList = hm[date]
                dataList!!.add(mData)
                hm[date] = dataList
            } else {
                val dataList = ArrayList<HeartRate>()
                dataList.add(mData)
                hm[date] = dataList
            }
        }
        val arrayList = ArrayList<HeartRateHistory>()
        hm.forEach { (_, value) ->
            arrayList.add(convertHeartRate(value))
        }
        return arrayList
    }

    fun convertUnSyncBodyTempDataListToObject(data: List<BodyTemperatureBreakup>?): List<BodyTemperature>? {
        if (data.isNullOrEmpty()) {
            return null
        }
        val hm = HashMap<String, ArrayList<BodyTemperatureBreakup>>()
        data.forEach { mData ->
            val date = mData.date!!
            if (hm.containsKey(date)) {
                val dataList = hm[date]
                dataList!!.add(mData)
                hm[date] = dataList
            } else {
                val dataList = ArrayList<BodyTemperatureBreakup>()
                dataList.add(mData)
                hm[date] = dataList
            }
        }
        val arrayList = ArrayList<BodyTemperature>()
        hm.forEach { (_, value) ->
            arrayList.add(convertBodyTempData(value))
        }
        return arrayList
    }

    fun convertUnSyncBloodOxygenDataListToObject(data: List<BloodOxygenBreakup>?): List<BloodOxygen>? {
        if (data.isNullOrEmpty()) {
            return null
        }
        val hm = HashMap<String, ArrayList<BloodOxygenBreakup>>()
        data.forEach { mData ->
            val date = mData.date!!
            if (hm.containsKey(date)) {
                val dataList = hm[date]
                dataList!!.add(mData)
                hm[date] = dataList
            } else {
                val dataList = ArrayList<BloodOxygenBreakup>()
                dataList.add(mData)
                hm[date] = dataList
            }
        }
        val arrayList = ArrayList<BloodOxygen>()
        hm.forEach { (_, value) ->
            arrayList.add(convertBloodOxygenData(value))
        }
        return arrayList
    }

    fun convertUnSyncStressDataListToObject(data: List<StressDataBreakup>?): List<StressData>? {
        if (data.isNullOrEmpty()) {
            return null
        }
        val hm = HashMap<String, ArrayList<StressDataBreakup>>()
        data.forEach { mData ->
            val date = mData.date!!
            if (hm.containsKey(date)) {
                val dataList = hm[date]
                dataList!!.add(mData)
                hm[date] = dataList
            } else {
                val dataList = ArrayList<StressDataBreakup>()
                dataList.add(mData)
                hm[date] = dataList
            }
        }
        val arrayList = ArrayList<StressData>()
        hm.forEach { (_, value) ->
            arrayList.add(convertStressData(value))
        }
        return arrayList
    }

    fun convertUnSyncStepsDataListToObject(data: List<StressDataBreakup>?): List<StressData>? {
        if (data.isNullOrEmpty()) {
            return null
        }
        val hm = HashMap<String, ArrayList<StressDataBreakup>>()
        data.forEach { mData ->
            val date = mData.date!!
            if (hm.containsKey(date)) {
                val dataList = hm[date]
                dataList!!.add(mData)
                hm[date] = dataList
            } else {
                val dataList = ArrayList<StressDataBreakup>()
                dataList.add(mData)
                hm[date] = dataList
            }
        }
        val arrayList = ArrayList<StressData>()
        hm.forEach { (_, value) ->
            arrayList.add(convertStressData(value))
        }
        return arrayList
    }


    fun convertUnSyncSleepDataListToObject(data: List<SleepData>?): List<SleepData>? {
        if (data.isNullOrEmpty()) {
            return null
        }

        val hm = HashMap<String, ArrayList<SleepData>>()
        data.forEach { sleepData ->
            val date = sleepData.date!!
            if (hm.containsKey(date)) {
                val dataList = hm[date]
                dataList!!.add(sleepData)
                hm[date] = dataList
            } else {
                val dataList = ArrayList<SleepData>()
                dataList.add(sleepData)
                hm[date] = dataList
            }
        }

        val sleepDateList = ArrayList<SleepData>()
        hm.forEach { (_, value) ->
            sleepDateList.add(convertSleepData(value))
        }
        return sleepDateList
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



    private fun parse24HoursFormatHeartRate(dataList: List<HeartRate>?): ArrayList<Entry> {
        if (dataList.isNullOrEmpty()) {
            return DummyList.getTodayHeartRateDummyData()
        }

        val hmHeartData = HashMap<Int, ArrayList<Int>>()
        dataList.forEach { heartRate ->
            val timeIn24HoursFormat = DateFormats.formatTimeInto24HoursValue(heartRate.time)
            if (timeIn24HoursFormat.isNotEmpty() && heartRate.averageHeartRate != 0) {
                val timeInInt = timeIn24HoursFormat.toInt()
                if (hmHeartData.containsKey(timeInInt)) {
                    val heartValueList = hmHeartData[timeInInt]!!
                    heartValueList.add(heartRate.averageHeartRate)
                    hmHeartData[timeInInt] = heartValueList
                } else {
                    val heartValueList = ArrayList<Int>()
                    heartValueList.add(heartRate.averageHeartRate)
                    hmHeartData[timeInInt] = heartValueList
                }

            }

        }

        val entryList = ArrayList<Entry>()
        for (time in 0..23) {
            var heartValue = 0
            if (hmHeartData.containsKey(time)) {
                heartValue = hmHeartData[time]?.average()?.toInt() ?: 0
            }
            entryList.add(Entry(time.toFloat(), heartValue.toFloat()))
        }
        return entryList

    }

    private fun parse24HoursDistanceData(stepsData: StepsData?): ArrayList<BarEntry> {
        if (stepsData == null || stepsData.totalDistance == 0) {
            return DummyList.getDummyDistanceData()
        }
        val entryList = ArrayList<BarEntry>()
        stepsData.stepArray?.forEachIndexed { index, stepDataBreakup ->
            entryList.add(BarEntry(index.toFloat(), stepDataBreakup.distance.toFloat()))
        }

        return entryList

    }

    private fun parse24HoursFormatStress(dataList: List<StressDataBreakup>?): Pair<ArrayList<BarEntry>, ArrayList<Int>> {
        if (dataList.isNullOrEmpty()) {
            return DummyList.getDummyStressData()
        }

        val hashMap = HashMap<Int, ArrayList<Int>>()
        dataList.forEach { heartRate ->
            val timeIn24HoursFormat = DateFormats.formatTimeInto24HoursValue(heartRate.time)
            if (timeIn24HoursFormat.isNotEmpty() && heartRate.value != null && heartRate.value != 0) {
                val timeInInt = timeIn24HoursFormat.toInt()
                if (hashMap.containsKey(timeInInt)) {
                    val heartValueList = hashMap[timeInInt]!!
                    heartValueList.add(heartRate.value!!)
                    hashMap[timeInInt] = heartValueList
                } else {
                    val heartValueList = ArrayList<Int>()
                    heartValueList.add(heartRate.value!!)
                    hashMap[timeInInt] = heartValueList
                }

            }

        }

        val colorList = ArrayList<Int>()
        val entryList = ArrayList<BarEntry>()
        for (time in 0..23) {
            var dataValue = 0
            if (hashMap.containsKey(time)) {
                dataValue = hashMap[time]?.average()?.roundToInt() ?: 0
            }
            colorList.add(
                NoiseFitApplicationMain.context!!.resources.getColor(
                    StressBarChartUtils.getStressColor(
                        dataValue
                    )
                )
            )
            entryList.add(BarEntry(time.toFloat(), dataValue.toFloat()))
        }
        return Pair(entryList, colorList)

    }

    private fun parse24HoursFormatBodyTemp(
        dataList: List<BodyTemperatureBreakup>?,
        dataUnitConverter: DataUnitConverter,
        units: Units
    ): Pair<ArrayList<BarEntry>, ArrayList<Int>> {
        if (dataList.isNullOrEmpty()) {
            return DummyList.getDummyBodyTempData()
        }

        val hashMap = HashMap<Int, ArrayList<Float>>()
        dataList.forEach { bodyTemp ->
            val bodyTempValue = dataUnitConverter.formatBodyTempInFloat(bodyTemp.value ?: 0f, units)
            val timeIn24HoursFormat = DateFormats.formatTimeInto24HoursValue(bodyTemp.time)
            if (timeIn24HoursFormat.isNotEmpty() && bodyTempValue != 0f) {
                val timeInInt = timeIn24HoursFormat.toInt()
                if (hashMap.containsKey(timeInInt)) {
                    val bodyTempList = hashMap[timeInInt]!!
                    bodyTempList.add(bodyTempValue)
                    hashMap[timeInInt] = bodyTempList
                } else {
                    val bodyTempList = ArrayList<Float>()
                    bodyTempList.add(bodyTempValue)
                    hashMap[timeInInt] = bodyTempList
                }

            }

        }

        val colorList = ArrayList<Int>()
        val entryList = ArrayList<BarEntry>()
        for (time in 0..23) {
            var dataValue = 0
            if (hashMap.containsKey(time)) {
                dataValue = hashMap[time]?.average()?.roundToInt() ?: 0
            }
            colorList.add(
                NoiseFitApplicationMain.context!!.resources.getColor(
                    R.color.body_temp
                )
            )
            entryList.add(
                BarEntry(
                    time.toFloat(),
                    dataValue.toFloat()
                )
            )
        }
        return Pair(entryList, colorList)

    }

    private fun parse24HoursFormatSleep(sleepData: SleepData): Pair<CountCardData, ArrayList<SleepData.SleepDataBreakup>> {

        if (sleepData.total == 0) {
            return DummyList.getDummySleepData()
        }
        val countCData = CountCardData(
            type = "Sleep",
            imageSourceId = 0,
            cardSourceId = 0,
            imageBgSourceId = 0
        )
        countCData.count = "_"
        countCData.countSubText = "sub"
//        countCData.leftValue = sleepData.startTime
//        countCData.rightValue = endTime

        val sleepArray = ArrayList<SleepData.SleepDataBreakup>()
        sleepData.sleepArray?.forEach { sleepHourBreakup ->
            sleepArray.add(
                SleepData.SleepDataBreakup(
                    sleepType = getSleepType(sleepHourBreakup.sleepType).type,
                    duration = sleepHourBreakup.duration,
                    startTime = sleepHourBreakup.startTime,
                    endTime = sleepHourBreakup.endTime
                )
            )
        }
        return Pair(countCData, sleepArray)
    }

}