package com.noisefit_ryeex_sdk.handler

import com.noisefit_commans.constants.EventConstants
import com.noisefit_commans.interfaces.data.IUserActivityDataCallback
import com.noisefit_commans.interfaces.data.UserActivityCallback
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.SyncDataStatus
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_ryeex_sdk.base.RyeexApplicationHandler
import com.noisefit_ryeex_sdk.dataConversion.DataConverter
import com.noisefit_ryeex_sdk.utils.TempStepDataWrapper
import com.ryeex.ble.common.device.OnDataReadyListener
import com.ryeex.ble.connector.callback.AsyncBleCallback
import com.ryeex.ble.connector.error.BleError
import com.ryeex.watch.adapter.device.WatchDevice
import com.ryeex.watch.adapter.model.entity.*
import javax.inject.Inject

private const val TAG = "RyeexUserActivityHandler"

class RyeexUserActivityHandler @Inject constructor(
    private var dataConverter: DataConverter,
    private var ryeexApplicationHandler: RyeexApplicationHandler
) : UserActivityDataActions() {

    private var userActivityDataCallbacks: IUserActivityDataCallback? = null

    private var noiseFitDevice: ColorFitDevice? = null
    private var watchDevice: WatchDevice? = null
    override fun init() {
        super.init()
        watchDevice = ryeexApplicationHandler.getWatchDevice()
        removeCallbacks()
        attachCallbacks()
    }

    override fun attachCallbacks() {
        watchDevice?.dataReadyListener = object : OnDataReadyListener {
            override fun onDataUploadReady() {
                LOGS.d(TAG, "onDataUploadReady")
                AppLogs.sendAppLogs("$TAG onDataUploadReady")
                syncRealTimeActivity()
            }
        }
    }

    override fun syncUserActivity(date: String, isRefresh: Boolean) {
        syncUserActivity()
//        userActivityDataCallbacks?.onUserActivityDataReceived(
//            UserActivityCallback.UserDataSyncUpdated(
//                SyncDataStatus(status = EventConstants.UPDATE_STATUS_SUCCESS)
//            )
//        )
    }

    override fun syncSportsActivity(date: String) {
        LOGS.d("syncSportsActivity date=$date")
        watchDevice?.fetchResultSportData(object : AsyncBleCallback<LibResultData, BleError>() {
            override fun onSuccess(resultData: LibResultData?) {
                resultData?.list?.forEach { libDataDomain ->
                    when (libDataDomain) {
                        is LibSportDomain -> {
                            userActivityDataCallbacks?.onUserActivityDataReceived(
                                UserActivityCallback.SportsModeDataObtainedGPS(
                                    dataConverter.parseSportsData(libDataDomain)
                                )
                            )
                            userActivityDataCallbacks?.onUserActivityDataReceived(
                                UserActivityCallback.SportsModeDataSyncSuccess()
                            )
//                            LOGS.d("syncSportsActivity :SPORT ${Gson().toJson(libDataDomain)}")
                        }
                    }
                }
            }

            override fun onFailure(p0: BleError?) {

            }
        })
    }

    private fun syncUserActivity() {
        LOGS.d("syncUserActivity inside")
        userActivityDataCallbacks?.onUserActivityDataReceived(
            UserActivityCallback.UserDataSyncUpdated(
                SyncDataStatus(status = EventConstants.UPDATE_STATUS_STARTED)
            )
        )
        watchDevice?.fetchResultHealthData(false, object : AsyncBleCallback<LibResultData, BleError>() {
            override fun onSuccess(p0: LibResultData?) {

                val hmSteps = HashMap<String, TempStepDataWrapper>()
                p0?.list?.forEach { libDataDomain ->

                    when (libDataDomain) {

                        is LibHealthStepDomain -> {
                            val key = libDataDomain.date
                            if (hmSteps.containsKey(key)) {
                                val tempStepDataWrapper = hmSteps[key]!!
                                tempStepDataWrapper.stepHealthDomain = libDataDomain
                                hmSteps[key] = tempStepDataWrapper
                            } else {
                                hmSteps[key] = TempStepDataWrapper(stepHealthDomain = libDataDomain)
                            }
//                            LOGS.d("syncUserActivity :STEP ${Gson().toJson(libDataDomain)}")
                        }
                        is LibHealthDistanceDomain -> {
                            val key = libDataDomain.date
                            if (hmSteps.containsKey(key)) {
                                val tempStepDataWrapper = hmSteps[key]!!
                                tempStepDataWrapper.distanceHealthDomain = libDataDomain
                                hmSteps[key] = tempStepDataWrapper
                            } else {
                                hmSteps[key] =
                                    TempStepDataWrapper(distanceHealthDomain = libDataDomain)
                            }
//                            LOGS.d("syncUserActivity :DISTANCE ${Gson().toJson(libDataDomain)}")
                        }
                        is LibHealthCalorieDomain -> {
                            val key = libDataDomain.date
                            if (hmSteps.containsKey(key)) {
                                val tempStepDataWrapper = hmSteps[key]!!
                                tempStepDataWrapper.calorieHealthDomain = libDataDomain
                                hmSteps[key] = tempStepDataWrapper
                            } else {
                                hmSteps[key] =
                                    TempStepDataWrapper(calorieHealthDomain = libDataDomain)
                            }
//                            LOGS.d("syncUserActivity :CALORIES ${Gson().toJson(libDataDomain)}")
                        }

                        is LibHealthBloodOxygenDomain -> {
                            userActivityDataCallbacks?.onUserActivityDataReceived(
                                UserActivityCallback.BloodOxygenObtained(
                                    dataConverter.parseBloodOxygenData(libDataDomain)
                                )
                            )

//                            LOGS.d("syncUserActivity :BLOOD_OXYGEN ${Gson().toJson(libDataDomain)}")
                        }
                        is LibHealthHeartRateDomain -> {
                            userActivityDataCallbacks?.onUserActivityDataReceived(
                                UserActivityCallback.HeartHistoryObtained(
                                    dataConverter.parseHeartRateData(libDataDomain)
                                )
                            )

//                            LOGS.d("syncUserActivity :HEART_RATE ${Gson().toJson(libDataDomain)}")
                        }
                        is LibHealthSleepDomain -> {
                            userActivityDataCallbacks?.onUserActivityDataReceived(
                                UserActivityCallback.SleepDataObtained(
                                    dataConverter.parseSleepData(libDataDomain)
                                )
                            )
//                            LOGS.d("syncUserActivity :SLEEP ${Gson().toJson(libDataDomain)}")
                        }
                    }
                }

                for (hm in hmSteps) {
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.StepsDataObtained(
                            stepsData = dataConverter.parseStepsData(hm.key, hm.value)
                        )
                    )
                }

                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.UserDataSyncUpdated(
                        SyncDataStatus(status = EventConstants.UPDATE_STATUS_SUCCESS)
                    )
                )
            }

            override fun onFailure(p0: BleError?) {

            }
        })
    }

    private fun syncRealTimeActivity() {
        LOGS.d("syncRealTimeActivity")
        watchDevice?.fetchResultHealthData(true, object : AsyncBleCallback<LibResultData, BleError>() {
            override fun onSuccess(resultData: LibResultData?) {
                val hmSteps = HashMap<String, TempStepDataWrapper>()
                resultData?.list?.forEach { libDataDomain ->
                    libDataDomain as LibHealthDomain
                    if(!DateFormats.isTodayDate(libDataDomain.date, "yyyyMMdd")){
                        return@forEach
                    }
                    when (libDataDomain) {
                        is LibHealthStepDomain -> {
                            val key = libDataDomain.date
                            if (hmSteps.containsKey(key)) {
                                val tempStepDataWrapper = hmSteps[key]!!
                                tempStepDataWrapper.stepHealthDomain = libDataDomain
                                hmSteps[key] = tempStepDataWrapper
                            } else {
                                hmSteps[key] = TempStepDataWrapper(stepHealthDomain = libDataDomain)
                            }
//                            LOGS.d("syncUserActivity :STEP ${Gson().toJson(libDataDomain)}")
                        }
                        is LibHealthDistanceDomain -> {
                            val key = libDataDomain.date
                            if (hmSteps.containsKey(key)) {
                                val tempStepDataWrapper = hmSteps[key]!!
                                tempStepDataWrapper.distanceHealthDomain = libDataDomain
                                hmSteps[key] = tempStepDataWrapper
                            } else {
                                hmSteps[key] =
                                    TempStepDataWrapper(distanceHealthDomain = libDataDomain)
                            }
//                            LOGS.d("syncUserActivity :DISTANCE ${Gson().toJson(libDataDomain)}")
                        }
                        is LibHealthCalorieDomain -> {
                            val key = libDataDomain.date
                            if (hmSteps.containsKey(key)) {
                                val tempStepDataWrapper = hmSteps[key]!!
                                tempStepDataWrapper.calorieHealthDomain = libDataDomain
                                hmSteps[key] = tempStepDataWrapper
                            } else {
                                hmSteps[key] =
                                    TempStepDataWrapper(calorieHealthDomain = libDataDomain)
                            }
//                            LOGS.d("syncUserActivity :CALORIES ${Gson().toJson(libDataDomain)}")
                        }
                    }
                }

                for (hm in hmSteps) {
                    LOGS.d("syncRealTimeActivity hmSteps=$hm")
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.RealStepsDataObtained(
                            stepsData = dataConverter.parseStepsData(hm.key, hm.value)
                        )
                    )
                }
            }

            override fun onFailure(p0: BleError?) {
                LOGS.d("syncRealTimeActivity onFailure $p0")
                AppLogs.sendAppLogs("$TAG syncRealTimeActivity onFailure $p0")
            }
        })
    }

    override fun getStepsData(date: String) {

    }

    override fun getSleepData(date: String) {

    }

    override fun getHeartRate() {

    }

    override fun getBloodOxygenLevel() {

    }

    override fun getBloodPressure() {

    }

    override fun getStressCount() {

    }

    override fun getBodyTemperatureData() {
    }

    override fun setDevice(colorFitDevice: ColorFitDevice) {
        this.noiseFitDevice = colorFitDevice
    }

    override fun <T> callbackListener(callback: T) {

    }

    override fun <T> callbackListenerNew(callback: T) {
        userActivityDataCallbacks = callback as IUserActivityDataCallback
    }
}