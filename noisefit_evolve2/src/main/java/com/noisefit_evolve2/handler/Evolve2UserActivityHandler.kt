package com.noisefit_evolve2.handler


import com.google.android.gms.common.api.Api
import com.google.gson.Gson
import com.noisefit_commans.constants.EventConstants
import com.noisefit_commans.interfaces.data.IUserActivityDataCallback
import com.noisefit_commans.interfaces.data.UserActivityCallback
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.*
import com.noisefit_evolve2.base.Evolve2ApplicationHandler
import com.noisefit_evolve2.dataConversion.DataConverter
import com.noisefit_evolve2.util.SportDataListenerWrapper
import com.touchgui.sdk.*
import com.touchgui.sdk.bean.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class Evolve2UserActivityHandler
@Inject
constructor(
    private var dataConverter: DataConverter,
    private var evolve2ApplicationHandler: Evolve2ApplicationHandler,
) : UserActivityDataActions() {

    private var colorFitDevice: ColorFitDevice? = null
    private var userActivityDataCallbacks: IUserActivityDataCallback? = null
    override fun <T> callbackListener(callback: T) {
        //userActivityDataCallbacks = callback as IUserActivityDataCallback
    }

    private var mClient: TGClient? = null
    override fun init() {
        super.init()
        mClient = evolve2ApplicationHandler.getTGBleClient()
        removeCallbacks()
        attachCallbacks()
    }

    override fun getBodyTemperatureData() {

    }

    override fun <T> callbackListenerNew(callback: T) {
        userActivityDataCallbacks = callback as IUserActivityDataCallback
    }

    private var isSyncProtoSportToday = false
    private var isSyncProtoSportSyncing = false

    //  private var sportModleInfoList = ArrayList<SportModleInfo>()


    override fun syncUserActivity(date: String, isRefresh: Boolean) {
        mClient?.syncHealthData()
        mClient?.syncWorkoutData()
        //getStressCount()
        // getBloodOxygenLevel()
    }

    override fun getStepsData(date: String) {
        mClient?.syncHealthData()
    }

    override fun getSleepData(date: String) {
        mClient?.syncHealthData()
    }

    override fun getStressCount() {

        LOGS.d("getStressCount getStressCount")
        mClient?.syncHealthData()
    }

    override fun attachCallbacks() {
        if (mClient != null) {
            mClient?.registerHealthDataCallback(healthDataListener)

            mClient?.registerWorkoutDataCallback(sportDataListener)
        }
    }

    private var sportDataListener: TGWorkoutDataCallback = object :TGWorkoutDataCallback{
//        override fun onSyncSportData(records: List<SportDataListenerWrapper.SportRecordMerge>?) {
//
//            LOGS.d("onSyncSportData ${Gson().toJson(records)}")
//            userActivityDataCallbacks?.onUserActivityDataReceived(
//                UserActivityCallback.SportsModeDataObtainedGPS(
//                    dataConverter.parseSportsDataGPS(
//                        records
//                    )
//                )
//            )
//            userActivityDataCallbacks?.onUserActivityDataReceived(
//                UserActivityCallback.SportsModeDataSyncSuccess()
//            )
//            isSportsDataSynching = false
//        }

        override fun onStart() {

        }

        override fun onProgress(p0: Int) {

        }

        override fun onCompleted(records: MutableList<TGWorkoutRecord>) {
          //  LOGS.d("onSyncSportData ${Gson().toJson(records)}")
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.SportsModeDataObtainedGPS(
                    dataConverter.parseSportsDataGPS(
                        records
                    )
                )
            )
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.SportsModeDataSyncSuccess()
            )
        }

        override fun onError(code: Int, message: String) {
            AppLogs.sendAppLogs(LogEvents.SyncData, SyncDataEvents.Other)
        }

    }
    private val healthDataListener: TGHealthDataManager.OnHealthDataListener =
        object : TGHealthDataManager.OnHealthDataListener {
            override fun onStressData(p0: TGStressData, p1: Boolean) {

                LOGS.d("getStressCount inside")


                val stressList = ArrayList<StressDataBreakup>()
                p0.items?.forEach { item ->
                    val date1 = Date(item.timeSeconds.toLong() * 1000)
                    val date = DateFormats.dateFormat.format(date1.time)
                    val time = DateFormats.timeFormat.format(date1.time)
                    val syncDate = DateFormats.convertDateTimeToTimeStamp(date, time)
                    if (item.value > 0) {
                        val stressItem = StressDataBreakup(
                            value = item.value,
                            date = date, time = time, timeStamp = syncDate
                        )
                        stressList.add(stressItem)
                    }
                }

                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.StressDataObtained(
                        stressList
                    )
                )
            }

            override fun onStart() {
                LOGS.d("onCompleted -- start")
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.UserDataSyncUpdated(
                        SyncDataStatus(status = EventConstants.UPDATE_STATUS_STARTED)
                    )
                )
            }

            override fun onHealthData(p0: Any) {

            }

            override fun onHeartRateData(p0: TGHeartRateData, p1: Boolean) {
                onHeartHistoryObtained(p0)
            }

            override fun onStepData(p0: TGStepData, p1: Boolean) {

                val stepsData = dataConverter.getStepsData(p0)
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.StepsDataObtained(
                        stepsData
                    )
                )

            }

            override fun onSleepData(p0: TGSleepData, p1: Boolean) {
                val sleepData = dataConverter.getSleepData(p0)

                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.SleepDataObtained(
                        sleepData
                    )
                )
            }

            override fun onSpo2Data(p0: TGSyncSpo2, p1: Boolean) {

                LOGS.d("Blood_Oxygen onSpo2Data")
                val bloodOxygenList = ArrayList<BloodOxygenBreakup>()
                p0.items?.forEach { item ->
                    val date1 = Date(item.timeSeconds.toLong() * 1000)
                    val date = DateFormats.dateFormat.format(date1.time)
                    val time = DateFormats.timeFormat.format(date1.time)
                    val syncDate = DateFormats.convertDateTimeToTimeStamp(date, time)
                    if (item.value > 0) {

                        val oxygenItem = BloodOxygenBreakup(
                            value = item.value,
                            date = date, time = time, timeStamp = syncDate
                        )
                        bloodOxygenList.add(oxygenItem)
                    }


                }


                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.BloodOxygenObtained(
                        bloodOxygenList
                    )
                )

            }

            override fun onProgress(p0: Int) {
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.UserDataSyncUpdated(
                        SyncDataStatus(
                            status = EventConstants.UPDATE_STATUS_IN_PROGRESS,
                            progress = p0
                        )
                    )
                )

            }

            override fun onCompleted() {
                LOGS.d("onCompleted -- finish")
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.UserDataSyncUpdated(
                        SyncDataStatus(
                            status = EventConstants.UPDATE_STATUS_SUCCESS,
                        )
                    )
                )
                AppLogs.sendAppLogs("Sync Data Success")

            }

            override fun onError(p0: Int, p1: String) {
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.UserDataSyncUpdated(
                        SyncDataStatus(
                            status = EventConstants.UPDATE_STATUS_FAILED,
                        )
                    )
                )
                AppLogs.sendAppLogs(LogEvents.SyncData, SyncDataEvents.Other.apply {
                    comment = "$p0 | $p1"
                })


            }

        }

    override fun removeCallbacks() {
        if (mClient != null) {
            mClient?.unregisterWorkoutDataCallback(sportDataListener)
            mClient?.unregisterHealthDataCallback(healthDataListener)
        }
    }

    override fun getHeartRate() {
        mClient?.syncHealthData()
    }

    override fun getBloodOxygenLevel() {
        LOGS.d("Blood_Oxygen querySpo2Data")
        mClient?.syncHealthData()
    }

    override fun getBloodPressure() {

    }


    override fun syncSportsActivity(date: String) {
        mClient?.syncWorkoutData()
    }

    override fun setDevice(colorFitDevice: ColorFitDevice) {
        this.colorFitDevice = colorFitDevice
    }

    override fun getHeartHistory(calendar: Calendar) {
        mClient?.syncHealthData()
    }





    private fun onHeartHistoryObtained(mWoHeartInfo: TGHeartRateData) {

        var highest = 0
        var lowest = 0
        var totalValue = 0
        var totalCount = 0


        val heartRateHistory = ArrayList<HeartRate>()
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DATE]
        calendar.set(year, month, day, 0, 0, 0)
        calendar.add(Calendar.MINUTE, mWoHeartInfo.minuteOffset)
        mWoHeartInfo.items?.let { healthHeartRate ->
            healthHeartRate.forEachIndexed { i, item ->
                calendar.add(Calendar.MINUTE, item.offset)
                val time = DateFormats.timeFormat.format(calendar.time)
                if (item.data != 0) {
                    if (highest < item.data) highest = item.data

                    if (lowest == 0) {
                        if (lowest < item.data) lowest = item.data
                    } else {
                        if (lowest > item.data) lowest = item.data
                    }

                    totalValue += item.data
                    totalCount++


                    val date = mWoHeartInfo.date?.let { DateFormats.getDateFormat(it) }
                    val syncDate = date?.let { DateFormats.convertDateTimeToTimeStamp(it, time) }
                    heartRateHistory.add(
                        HeartRate(
                            averageHeartRate = item.data,
                            date = date,
                            time = time,
                            timeStamp = syncDate,
                            highestHeartRate = highest,
                            lowestHeartRate = lowest
                        )
                    )
                }
            }
        }

        userActivityDataCallbacks?.onUserActivityDataReceived(
            UserActivityCallback.HeartHistoryObtained(
                heartRateData = heartRateHistory
            )
        )
    }


}
