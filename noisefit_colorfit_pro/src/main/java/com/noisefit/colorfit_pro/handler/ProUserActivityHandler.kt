package com.noisefit.colorfit_pro.handler

import com.crrepa.ble.conn.bean.*
import com.crrepa.ble.conn.listener.*
import com.crrepa.ble.conn.type.*
import com.google.gson.Gson
import com.noisefit.colorfit_pro.base.ProApplicationHandler
import com.noisefit.colorfit_pro.dataConversion.DataConverter
import com.noisefit.colorfit_pro.handler.connect.ProConnectHandler.Companion.bleConnection
import com.noisefit.colorfit_pro.utils.SportDataListenerWrapper
import com.noisefit_commans.constants.EventConstants
import com.noisefit_commans.interfaces.data.IUserActivityDataCallback
import com.noisefit_commans.interfaces.data.UserActivityCallback
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import java.util.*
import javax.inject.Inject


class ProUserActivityHandler
@Inject
constructor(
    private var dataConverter: DataConverter,
    private var navPlusApplicationHandler: ProApplicationHandler
) : UserActivityDataActions() {
    private var lastCRPStepInfo: CRPStepInfo? = null
    private var userActivityDataCallbacks: IUserActivityDataCallback? = null
    private var sportsModeRequest: SportsModeRequest? = null
    private var crpStepInfo: CRPStepInfo? = null
    private var sportsModeThreshold: CRPStepInfo? = null
    private var sportsModeResponse: SportsModeResponse? = null
    private val heartRateList = ArrayList<Int>()

    private var colorFitDevice: ColorFitDevice? = null


    override fun setDevice(device: ColorFitDevice) {
        colorFitDevice = device
    }


    override fun <T> callbackListener(callback: T) {

    }

    override fun <T> callbackListenerNew(callback: T) {
        userActivityDataCallbacks = callback as IUserActivityDataCallback
    }

    override fun getBodyTemperatureData() {

    }

    override fun syncUserActivity(date: String, isRefresh: Boolean) {
        getStepsData(date)
        getSleepData(date)
        getBloodOxygenLevel()
        getHeartRate()
        getStressCount()


        userActivityDataCallbacks?.onUserActivityDataReceived(
            UserActivityCallback.UserDataSyncUpdated(
                SyncDataStatus(status = EventConstants.UPDATE_STATUS_STARTED)
            )
        )
    }

    override fun getStepsData(date: String) {
        bleConnection?.setStepChangeListener(stepChangeListener)
        bleConnection?.setStepsCategoryListener(stepsCategoryChangeListener)
        bleConnection?.syncStep()


    }

    override fun getSleepData(date: String) {
        bleConnection?.setSleepChangeListener(sleepChangeListener)
        bleConnection?.syncSleep()

    }

    override fun getHeartRate() {
        heartRateList.clear()
        bleConnection?.enableTimingMeasureHeartRate(6)
        bleConnection?.setHeartRateChangeListener(heartRateChangListener)
//       bleConnection?.startMeasureOnceHeartRate()
        bleConnection?.queryTodayHeartRate(CRPHeartRateType.TIMING_MEASURE_HEART_RATE)
        bleConnection?.queryTodayHeartRate(CRPHeartRateType.ALL_DAY_HEART_RATE)
        bleConnection?.queryHistoryHeartRate()
        bleConnection?.queryLastDynamicRate(CRPHistoryDynamicRateType.FIRST_HEART_RATE)
        bleConnection?.queryLastDynamicRate(CRPHistoryDynamicRateType.SECOND_HEART_RATE)
        bleConnection?.queryLastDynamicRate(CRPHistoryDynamicRateType.THIRD_HEART_RATE)
    }

    override fun getBloodOxygenLevel() {
        bleConnection?.setBloodOxygenChangeListener(bloodOxygenChangeListener)
        // bleConnection?.enableContinueBloodOxygen()
        //    bleConnection?.enableTimingMeasureBloodOxygen(1)
        bleConnection?.queryTimingBloodOxygen(CRPBloodOxygenTimeType.TODAY)
        bleConnection?.queryTimingBloodOxygen(CRPBloodOxygenTimeType.YESTERDAY)
        bleConnection?.queryLast24HourBloodOxygen()
        bleConnection?.queryHistoryBloodOxygen()

    }

    override fun getBloodPressure() {

    }

    override fun getStressCount() {
        LOGS.d("SFDSDFSFDSFDS stress count")
        bleConnection?.queryHistoryStress()
        bleConnection?.queryTimingStress(CRPStressDate.TODAY)
        bleConnection?.setStressListener(stressChangeListener)
    }

    override fun attachCallbacks() {
        bleConnection?.setStepChangeListener(stepChangeListener)
        bleConnection?.setSleepChangeListener(sleepChangeListener)
        bleConnection?.setHeartRateChangeListener(heartRateChangListener)

        bleConnection?.setBloodOxygenChangeListener(bloodOxygenChangeListener)
//       bleConnection?.startMeasureBloodOxygen()
    }

    private var stepsCategoryChangeListener = object : CRPStepsCategoryChangeListener {
        override fun onStepsCategoryChange(p0: CRPStepsCategoryInfo?) {

            lastCRPStepInfo?.let { crpStepInfo ->

                val stepArray = ArrayList<StepsData.StepDataBreakup>()
                p0?.stepsList?.let { stepsList ->
                    for (i in 0..47 step 2) {
                        val hour = i / 2
                        val steps = stepsList[i] + stepsList[i + 1]
                        //  LOGS.d("QUBE_STEPS_PREVIOUS $hour $steps")
                        stepArray.add(StepsData.StepDataBreakup(steps, 0, 0, 0, hour))
                    }
                }

                val stepsData = StepsData(
                    totalSteps = crpStepInfo.steps,
                    totalCalories = crpStepInfo.calories,
                    totalDistance = crpStepInfo.distance,
                    totalActiveTime = 0,
                    date = DateFormats.getDateFormat()

                )
                stepsData.stepArray = stepArray
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.StepsDataObtained(
                        stepsData
                    )
                )
            }


        }

    }


    private var stepChangeListener: CRPStepChangeListener = object : CRPStepChangeListener {
        override fun onStepChange(info: CRPStepInfo) {
            // LOGS.d("QUBE_STEPS ${Gson().toJson(info)}")
            lastCRPStepInfo = info
            colorFitDevice?.deviceType?.let { deviceType ->
                if (deviceType == DeviceType.NOISEFIT_ENDURE.deviceType) {

                    val stepsData = StepsData(
                        totalSteps = info.steps,
                        totalCalories = info.calories,
                        totalDistance = info.distance,
                        totalActiveTime = 0,
                        date = DateFormats.getDateFormat()
                    )
                    val stepArray = ArrayList<StepsData.StepDataBreakup>()
                    for (i in 0..23) {
                        stepArray.add(StepsData.StepDataBreakup(0, 0, 0, 0, i))
                    }
                    stepsData.stepArray = stepArray
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.StepsDataObtained(
                            stepsData
                        )
                    )
                } else {
                    bleConnection?.queryStepsCategory(CRPStepsCategoryDateType.TODAY_STEPS_CATEGORY)
                }
            }


        }

        override fun onHistoryStepChange(p0: CRPHistoryDay?, p1: CRPStepInfo?) {

        }


    }

    private var sleepChangeListener: CRPSleepChangeListener = object : CRPSleepChangeListener {
        override fun onSleepChange(info: CRPSleepInfo) {
            if (info.details != null) {
                val sleepData = dataConverter.getSleepData(info,colorFitDevice)
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.SleepDataObtained(sleepData)
                )

            }
        }

        override fun onHistorySleepChange(p0: CRPHistoryDay?, p1: CRPSleepInfo?) {

        }


    }

    private var stressChangeListener: CRPStressListener = object : CRPStressListener {
        override fun onSupportStress(p0: Boolean) {

        }

        override fun onStressChange(p0: Int) {

        }

        override fun onHistoryStressChange(p0: MutableList<CRPHistoryStressInfo>?) {
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.StressDataObtained(
                    dataConverter.parseStressData(p0)
                )
            )

        }

        override fun onTimingStressStateChange(p0: Boolean) {

        }

        override fun onTimingStressChange(p0: CRPTimingStressInfo?) {

        }


    }

    private var heartRateChangListener: CRPHeartRateChangeListener =
        object : CRPHeartRateChangeListener {
            override fun onMeasuring(rate: Int) {

                heartRateList.add(rate)
            }

            override fun onOnceMeasureComplete(rate: Int) {
                heartRateList.add(rate)
                LOGS.d("On Once Measure Complete ${rate}")


            }

            override fun onHistoryHeartRate(p0: MutableList<CRPHistoryHeartRateInfo>?) {

            }

            override fun onMeasureComplete(
                p0: CRPHistoryDynamicRateType?,
                info: CRPHeartRateInfo?
            ) {
//                syncComplete()
                LOGS.d("On Once onMeasureComplete ")
                bleConnection?.queryMovementHeartRate()
            }

            override fun on24HourMeasureResult(info: CRPHeartRateInfo) {

                LOGS.d(info)

                if (info.historyDay.value == CRPHistoryDay.TODAY.value) {
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.HeartHistoryObtained(
                            dataConverter.parseHeartHistory(
                                info
                            )
                        )
                    )
                } else if (info.historyDay.value == CRPHistoryDay.YESTERDAY.value) {
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.HeartHistoryObtained(
                            dataConverter.parseHeartHistory(
                                info
                            )
                        )
                    )
                }
            }

            override fun onMovementMeasureResult(list: List<CRPMovementHeartRateInfo>) {
                onSportActivitiesObtained(list)
            }
        }


    private fun onSportActivitiesObtained(list: List<CRPMovementHeartRateInfo>) {
        LOGS.d("GET Activity")
        userActivityDataCallbacks?.onUserActivityDataReceived(
            UserActivityCallback.SportsModeDataObtainedGPS(dataConverter.parseSportsMode(list))
        )
        userActivityDataCallbacks?.onUserActivityDataReceived(
            UserActivityCallback.SportsModeDataSyncSuccess()
        )

    }

//    private fun syncComplete() {
//        userActivityDataCallbacks?.onUserActivityDataReceived(
//            UserActivityCallback.UserDataSyncUpdated(
//                SyncDataStatus(status = EventConstants.UPDATE_STATUS_SUCCESS)
//            )
//        )
//    }


    override fun setSpo2MeasurementData(status: Boolean) {
        if (status) {
            bleConnection?.startMeasureBloodOxygen()
            bleConnection?.setBloodOxygenChangeListener(bloodOxygenChangeListener)
        } else {
            bleConnection?.stopMeasureBloodOxygen()
        }
    }

    private var bloodOxygenChangeListener: CRPBloodOxygenChangeListener =
        object : CRPBloodOxygenChangeListener {
            override fun onContinueState(p0: Boolean) {
                LOGS.d(
                    "Qube",
                    "CRPBloodOxygenChangeListener onContinueState $p0"
                )
            }

            override fun onTimingMeasure(p0: Int) {
                LOGS.d(
                    "Qube",
                    "CRPBloodOxygenChangeListener onTimingMeasure $p0"
                )
            }

            override fun onBloodOxygen(p0: Int) {
                LOGS.d("Qube", "CRPBloodOxygenChangeListener onBloodOxygen $p0")
                if (p0 > 100) {
                    LOGS.d(
                        "Qube",
                        "CRPBloodOxygenChangeListener value is greater than 100: $p0"
                    )
                    return
                }
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.BloodOxygenObtained(getBloodOxygenData(p0))
                )

            }

            override fun onHistoryBloodOxygen(p0: MutableList<CRPHistoryBloodOxygenInfo>?) {
                LOGS.d(
                    "Qube",
                    "CRPBloodOxygenChangeListener onHistoryBloodOxygen $p0"
                )
            }

            override fun onContinueBloodOxygen(info: CRPBloodOxygenInfo?) {
                LOGS.d(
                    "Qube",
                    "CRPBloodOxygenChangeListener onContinueBloodOxygen $info"
                )
                if (info?.type?.value == CRPBloodOxygenTimeType.TODAY.value) {
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.BloodOxygenObtained(
                            dataConverter.parseBloodOxygenHistory(
                                info
                            )
                        )
                    )
                } else if (info?.type?.value == CRPBloodOxygenTimeType.YESTERDAY.value) {
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.BloodOxygenObtained(
                            dataConverter.parseBloodOxygenHistory(
                                info
                            )
                        )
                    )
                }


            }


        }


    private fun getBloodOxygenData(p0: Int): ArrayList<BloodOxygenBreakup> {
        val bloodOxygenBreakupList = ArrayList<BloodOxygenBreakup>()
        val bloodOxygenBreakup = BloodOxygenBreakup()
        val date = DateFormats.getTodaysDateString(7)
        val time = DateFormats.getTimeFormat()
        val timeStamp = DateFormats.convertDateTimeToTimeStamp(date, time)
        bloodOxygenBreakup.value = p0
        bloodOxygenBreakup.date = date
        bloodOxygenBreakup.time = time
        bloodOxygenBreakup.timeStamp = timeStamp
        bloodOxygenBreakup.resetData = false
        bloodOxygenBreakupList.add(bloodOxygenBreakup)
        return bloodOxygenBreakupList
    }


    override fun updateSportsMode(sportsModeRequest: SportsModeRequest) {
        this.sportsModeRequest = sportsModeRequest
        when (sportsModeRequest.status) {
            "start" -> {
                bleConnection?.startMeasureDynamicRate()
                sportsModeThreshold = crpStepInfo
                sportsModeResponse =
                    SportsModeResponse(type = "running", steps = 0, calories = 0, duration = 0)
            }
            "pause" -> {
                bleConnection?.stopMeasureDynamicRtae()
            }
            "resume" -> {
                sportsModeThreshold = crpStepInfo
                bleConnection?.startMeasureDynamicRate()
            }
            "stop" -> {
//                bleConnection?.stopMeasureDynamicRtae()
//                sportsModeResponse?.let {
//                    val list = ArrayList<SportsModeResponse>()
//                    list.add(it)
//                    userActivityDataCallbacks.onSportsModeDataObtained(
//                        SportsModeList(
//                            activities = list,
//                            responseType = "final"
//                        )
//                    )
//                    sportsModeResponse = null
//                }
            }
        }
    }

    override fun refresh(sportsModeRequest: SportsModeRequest) {
        sportsModeResponse?.let {
            val list = ArrayList<SportsModeResponse>()
            list.add(it)
//            userActivityDataCallbacks.onSportsModeDataObtained(
//                SportsModeList(
//                    activities = list,
//                    responseType = "update"
//                )
//            )
        }
    }

    override fun getHeartHistory(calendar: Calendar) {
        bleConnection?.setHeartRateChangeListener(heartRateChangListener)
        bleConnection?.queryTodayHeartRate(CRPHeartRateType.TIMING_MEASURE_HEART_RATE)
    }


    override fun syncSportsActivity(date: String) {
        bleConnection?.queryMovementHeartRate()


        when (colorFitDevice?.deviceType) {
            DeviceType.COLORFIT_ICON_2_VISTA.deviceType -> {
                bleConnection?.setTrainingListener(object :
                    SportDataListenerWrapper(object : Callback {


                        override fun onSyncSportData(records: List<CRPTrainingInfo>) {
                            userActivityDataCallbacks?.onUserActivityDataReceived(
                                UserActivityCallback.SportsModeDataObtainedGPS(
                                    dataConverter.parseCRPTrainingSportsMode(
                                        records
                                    )
                                )
                            )
                            userActivityDataCallbacks?.onUserActivityDataReceived(
                                UserActivityCallback.SportsModeDataSyncSuccess()
                            )
                        }

                    }) {})
                bleConnection?.queryHistoryTraining()
            }
        }

    }


}