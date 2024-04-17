package com.oreo.data.dataConverter

import com.google.gson.Gson
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.ui.common.calculatePercentage
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.common.maxWithoutZero
import com.noisefit_commans.common.minWithoutZero
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.HealthOverview
import com.noisefit_commans.data.model.HealthOverviewData
import com.noisefit_commans.data.model.OreoHeartRate
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.models.BloodOxygen
import com.noisefit_commans.models.BloodOxygenBreakup
import com.noisefit_commans.models.BodyTemperature
import com.noisefit_commans.models.BodyTemperatureBreakup
import com.noisefit_commans.models.HeartRate
import com.noisefit_commans.models.HeartRateHistory
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.models.SleepDataGoogleFit
import com.noisefit_commans.models.StepsData
import com.noisefit_commans.models.StressData
import com.noisefit_commans.models.StressDataBreakup
import com.noisefit_commans.models.Units
import com.noisefit_commans.models.UserGoals
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.db.implementation.OreoHeartRateDataImpl
import com.oreo.data.model.HRModel
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.TapMeasureState
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject


class OreoOfflineDataMapper
@Inject
constructor(
    val watches: WatchesSDK,
    val ringDataStore: RingDataStore,
    private val heartRateDataImpl: OreoHeartRateDataImpl,
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
        data: OreoHeartRate?
    ): OHealthOverview.HeartRateDataModel {
        //LOGS.d("Sdaljhsadjhsadjhjksda ${Gson().toJson(data)}")

        val list = data?.breakUp?.replace("255", "0")
        var breakupArray = Gson().fromJson<List<Int>>(list ?: "")
        if (breakupArray.isNullOrEmpty()) {
            val dummyArray = ArrayList<Int>()
            for (i in 0..287) {
                dummyArray.add(0)
            }
            breakupArray = dummyArray
        }
        val hRWithIntervalList = breakupArray.chunked(6)
        /*val lineChartList: ArrayList<Entry> = ArrayList()
        val candleChartList: ArrayList<CandleEntry> = ArrayList()
        val lineColorList: ArrayList<Int> = ArrayList()
        val xLabelList = ArrayList<String>()*/
        val avgList = ArrayList<Int>()
        var overAllMinValue = Int.MAX_VALUE
        var overAllMaxValue = -1
        var hrCount = 0
        var lastHrValue: Pair<Int, Long>? = null//HR value,timer

        //LOGS.w("convertHeartRateOverviewData ${data?.breakUp}")
        val listData = ArrayList<HRModel>()
        hRWithIntervalList.forEachIndexed { index, hrList ->


            val minValue = hrList.minWithoutZero()

            val maxValue = hrList.maxWithoutZero()

            var min = minValue
            var max = maxValue

            if (min == 0 && max != 0) {
                min = max
            }

            if (max == 0 && min != 0) {
                max = min
            }

            val avg = (min + max) / 2
            if (avg != 0) {
                if (min < overAllMinValue) {
                    overAllMinValue = min;
                }
                if (max > overAllMaxValue) {
                    overAllMaxValue = max;
                }
                avgList.add(avg)

            }

            hrList.forEachIndexed { index2, value ->
                if (value != 0) {

                    val indexMillis = ((index * 6) + index2) * 5 * 60L * 1000L
                    //LOGS.w("convertHeartRateOverviewData $index $indexMillis")

                    lastHrValue = Pair(value, indexMillis)
                }
            }

            //if any change chunk value then divide 12 by that chunk value to get below correct xlabel list
            if (index % 2 == 0) {
                hrCount += 1
            }
            listData.add(
                HRModel(
                    maxValues = overAllMaxValue,
                    minValues = overAllMinValue,
                    values = hrList,
                    midValues = hrList.average().toFloat()
                )
            )
        }

        val average = avgList.average().toFloat()


        var lastHr = "0"
        /*if ((lastHrValue ?: 0) > 0) {
            lastHr = lastHrValue.toString()
        }*/

        var manualMeasureTime = 0L
        /*if (lastHr == "0") {*/
        val lastMeasureValue = ringDataStore.getManualMeasurementValue()
        if (lastMeasureValue != null && (lastMeasureValue.timeStamp) + (60 * 60 * 1000) > System.currentTimeMillis() && lastMeasureValue.value > 0) {
            lastHr = lastMeasureValue.value.toString()
            manualMeasureTime = lastMeasureValue.timeStamp

        }
        //}


        if (lastHrValue != null) {
            val cal = Calendar.getInstance(TimeZone.getDefault())
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            val dayStartTimeStamp = cal.timeInMillis
            val hrTimestamp = dayStartTimeStamp + lastHrValue?.second!!
            //LOGS.w("convertHeartRateOverviewData ${lastHrValue?.first} ${lastHrValue?.second} $hrTimestamp  $manualMeasureTime")

            if (hrTimestamp > manualMeasureTime) {
                lastHr = lastHrValue?.first.toString()
                manualMeasureTime = hrTimestamp
                //LOGS.w("convertHeartRateOverviewData new HR set $dayStartTimeStamp + ${lastHrValue?.second} =  $hrTimestamp")

            }

        }




        if (overAllMinValue == Int.MAX_VALUE) {
            overAllMinValue = 69
        }

        if (overAllMinValue != 0) {
            overAllMinValue -= 9
        }

        //LOGS.d("Sdaljhsadjhsadjhjksda ${Gson().toJson(lineChartList)}")
        var measureState = TapMeasureState.DEFAULT

        val measureText = if (manualMeasureTime == 0L) {
            ""
        } else {
            measureState = TapMeasureState.LAST_MEASURED
            "Last measured ${DateFormats.getRelativeTime(manualMeasureTime).lowercase()}"
        }


        return OHealthOverview.HeartRateDataModel(
            listData,
            average = average,
            lastTime = measureText,
            value = "",
            maxValues = breakupArray.maxWithoutZero(),
            minValues = breakupArray.minWithoutZero(),
            measureState
        )
    }

    private fun handleHrFormat(time: Int): String {

        if (time == 1 || time == 24) {
            return "12 am"
        }


        var hour = time
        var suffix = ""
        if (hour > 11) {
            suffix = "pm"
            if (hour > 12)
                hour -= 12;
        } else {
            suffix = "am"
            if (hour == 0)
                hour = 12;
        }
        return "$hour $suffix"
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


    fun convertSleepData(data: List<SleepData>?): SleepData {
        val sleepData = SleepData()
        if (data.isNullOrEmpty()) {
            return sleepData
        }

        if (data.size == 1) {
            return data[0]
        }

        val watchType = watches.getWatchType()


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

        if (watches.getWatchType()?.name == SDKWatchType.SDK_ZH.name) {
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

        if (watches.getWatchType()?.name == SDKWatchType.SDK_ZH.name) {
            sleepData.total = totalLight + totalDeep + totalRem
        } else {
            sleepData.total = totalLight + totalDeep + totalAwake + totalRem
        }


        return sleepData

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


}