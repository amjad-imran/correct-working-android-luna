package com.noisefit_nav_plus.handler


import com.noisefit_commans.constants.EventConstants
import com.noisefit_commans.interfaces.data.IUserActivityDataCallback
import com.noisefit_commans.interfaces.data.UserActivityCallback
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.*
import com.noisefit_nav_plus.base.NavPlusApplicationHandler
import com.noisefit_nav_plus.handler.dataConversion.DataConverter
import com.zjw.zhbraceletsdk.bean.*
import com.zjw.zhbraceletsdk.linstener.OffMeasureTempListener
import com.zjw.zhbraceletsdk.linstener.SimplePerformerListener
import com.zjw.zhbraceletsdk.linstener.SyncProtoHistoryListener
import com.zjw.zhbraceletsdk.service.ZhBraceletService
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class NavPlusUserActivityHandler
@Inject
constructor(
    private var dataConverter: DataConverter,
    var navPlusApplicationHandler: NavPlusApplicationHandler
) : UserActivityDataActions() {

    private val TAG = NavPlusUserActivityHandler::class.simpleName
    private var userActivityDataCallbacks: IUserActivityDataCallback? = null

    override fun <T> callbackListener(callback: T) {
        //userActivityDataCallbacks = callback as IUserActivityDataCallback
    }

    private var isSyncProtoSportToday = false
    private var isSyncProtoSportSyncing = false
    private var sportModleInfoList = ArrayList<SportModleInfo>()
    private var colorFitDevice: ColorFitDevice? = null

    var mBleService: ZhBraceletService? = null

    init {
        //mBleService = navPlusApplicationHandler.openBleService()
    }

    override fun init() {
        super.init()
        mBleService = navPlusApplicationHandler.getZhBraceletService()
    }


    override fun syncUserActivity(date: String, isRefresh: Boolean) {
        LOGS.d("Activity Sync:: syncUserActivity")
        syncData()
    }

    override fun <T> callbackListenerNew(callback: T) {
        userActivityDataCallbacks = callback as IUserActivityDataCallback
    }


    override fun getBodyTemperatureData() {

        mBleService?.setOffMeasureTempListener(object : OffMeasureTempListener {
            override fun offMeasureTempData(p0: MutableList<MeasureTempInfo>?) {
                LOGS.d("SyncDataWork:::: BodyTemperatureObtained ${p0?.size}")
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.BodyTemperatureObtained(
                        dataConverter.parseBodyTemperature(p0)
                    )
                )
            }

            override fun noData() {
                LOGS.d("SyncDataWork:::: BodyTemperatureObtained No Data")
            }

        })

    }

    override fun getStepsData(date: String) {
        syncData()
    }

    private fun syncData() {
        LOGS.d("Activity Sync:: syncUserActivity2")
        mBleService?.let { zBleService ->
//            if (CommonGlobals.isWatchDataUpdating) {
//                LOGS.d("Watch has been updating... Please wait")
//                return
//            }

            LOGS.d("onResponseComplete -- START")
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.UserDataSyncUpdated(
                    SyncDataStatus(status = EventConstants.UPDATE_STATUS_STARTED)
                )
            )
            LOGS.d("Activity Sync:: syncUserActivity3")
            zBleService.syncTime()
            initBloodOxygen()
            initStressData()
            getBodyTemperatureData()
        }
    }

    override fun getSleepData(date: String) {
        syncData()
    }

    override fun getStressCount() {
        syncData()
    }

    override fun attachCallbacks() {
        LOGS.d("Activity Sync:: syncUserActivity4")
        mBleService?.removeSimplePerformerListenerLis(mPerformerListener)
        mBleService?.addSimplePerformerListenerLis(mPerformerListener)
        mBleService?.setSyncProtoHistoryListener(mSyncProtoHistoryListener)
    }

    override fun removeCallbacks() {
        mBleService?.removeSimplePerformerListenerLis(mPerformerListener)
    }

    override fun getHeartRate() {
        syncData()
    }

    override fun getBloodOxygenLevel() {
        syncData()
    }

    override fun getBloodPressure() {
        syncData()
    }


    override fun syncSportsActivity(date: String) {
        LOGS.d("Activity Sync:: start")
        if (mBleService != null && !isSyncProtoSportSyncing) {
            isSyncProtoSportSyncing = true
            isSyncProtoSportToday = false
            sportModleInfoList.clear()
            mBleService?.startSyncHistoryDeviceSport()
        }
    }

    override fun setDevice(colorFitDevice: ColorFitDevice) {
        this.colorFitDevice = colorFitDevice
    }


    override fun getHeartHistory(calendar: Calendar) {
        syncData()
    }


    private fun initBloodOxygen() {

        mBleService?.setOfflineBloodOxygenListener { data ->
            LOGS.d(data)
            val list = data as ArrayList<OfflineBloodOxygenBean>
//            val oxygenData = BloodOxygen()

            val oxygenArray = ArrayList<BloodOxygenBreakup>()
            list.forEachIndexed { _, item ->

                val dateTime = Calendar.getInstance()

                //if (curDay == day && curMonth + 1 == month && curYear == year) {
                dateTime.set(
                    item.year,
                    item.month - 1,
                    item.day,
                    item.hour,
                    item.minute,
                    item.second
                )
                // oxygenData.date = dateTime.time
                val date = DateFormats.dateFormat.format(dateTime.time)
                val time = DateFormats.timeFormat.format(dateTime.time)
                val syncDate = DateFormats.convertDateTimeToTimeStamp(date, time)
                if (item.bloodOxygenValue > 0) {
                    //oxygenData.bloodOxygen = item.bloodOxygenValue
                    val oxygenItem = BloodOxygenBreakup(
                        value = item.bloodOxygenValue,
                        date = date,
                        time = time,
                        timeStamp = syncDate
                    )
                    oxygenArray.add(oxygenItem)
                }
                //  }

            }
            // oxygenData.bloodOxygenArray = oxygenArray
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.BloodOxygenObtained(
                    oxygenArray
                )
            )
        }

    }

    private fun initStressData() {

        mBleService?.setOfflinePressureDataListener { data ->
            LOGS.d(data)
            val list = data as ArrayList<OfflinePressureData>
//            val stressData = StressData()

            val stressArray = ArrayList<StressDataBreakup>()
            list.forEachIndexed { _, item ->
                val dateTime = Calendar.getInstance()

//                if (curDay == day && curMonth + 1 == month && curYear == year) {
                dateTime.set(
                    item.year,
                    item.month - 1,
                    item.day,
                    item.hour,
                    item.minute,
                    item.second
                )
                //stressData.date = dateTime.time
                val date = DateFormats.dateFormat.format(dateTime.time)
                val time = DateFormats.timeFormat.format(dateTime.time)
                val syncDate = DateFormats.convertDateTimeToTimeStamp(date, time)
                if (item.pressureData > 0) {
                    //  stressData.value = item.pressureData
                    val oxygenItem = StressDataBreakup(
                        value = item.pressureData,
                        date = date, time = time, timeStamp = syncDate
                    )
                    stressArray.add(oxygenItem)
                }
//                }
            }
//            stressData.stressArray = stressArray
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.StressDataObtained(
                    stressArray
                )
            )
            //userActivityDataCallbacks?.onStressDataObtained(stressData)
        }

    }

    private val mSyncProtoHistoryListener: SyncProtoHistoryListener =
        object : SyncProtoHistoryListener {
            override fun syncSuccess() {
                LOGS.i("Activity Sync:: success")
                AppLogs.sendAppLogs("Sync Data Success")
                sportModleInfoList.forEach { sportModleInfo ->
                    sportModleInfo.let {
                        dataConverter.parseSportsDataGPS(it, colorFitDevice)
                    }
                        .let {
                            userActivityDataCallbacks?.onUserActivityDataReceived(
                                UserActivityCallback.SportsModeDataObtainedGPS(it)
                            )
                            //userActivityDataCallbacks?.onSportsModeDataObtainedGPS(it)
                        }
                }

                sportModleInfoList.clear()
                if (!isSyncProtoSportToday) {
                    isSyncProtoSportToday = true
                    mBleService?.startSyncTodayDeviceSport()
                } else {
                    isSyncProtoSportSyncing = false
                    isSyncProtoSportToday = false
                }
                if(!isSyncProtoSportToday){
                    userActivityDataCallbacks?.onUserActivityDataReceived(UserActivityCallback.SportsModeDataSyncSuccess())
                }
            }

            override fun syncFail() {
                isSyncProtoSportSyncing = false
                LOGS.i("Activity Sync:: fail")
                AppLogs.sendAppLogs(LogEvents.SyncData, SyncDataEvents.Other)
            }

            override fun noData() {
                isSyncProtoSportSyncing = false
                LOGS.i("Activity Sync:: noData")
            }

            override fun syncTimeOut() {
                isSyncProtoSportSyncing = false
                LOGS.i("Activity Sync:: timeout")
                AppLogs.sendAppLogs(LogEvents.SyncData, SyncDataEvents.SyncTimeout)
            }

            override fun syncProgress(p0: Int, p1: Int) {
                LOGS.i("Activity Sync:: progress$p0:$p1")
            }

            override fun syncData(p0: SportModleInfo?) {
                LOGS.d(p0)
                p0?.let {
                    sportModleInfoList.add(it)
                }
            }
        }
    private val mPerformerListener: SimplePerformerListener = object : SimplePerformerListener() {
        override fun onResponseDeviceInfo(mDeviceInfo: DeviceInfo) {


        }

        override fun onResponseMotionInfo(mMotionInfo: MotionInfo) {
            LOGS.d("Activity Sync:: syncUserActivity5")
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.StepsDataObtained(
                    dataConverter.getStepsData(
                        mMotionInfo
                    )
                )
            )

            /*userActivityDataCallbacks?.onStepsDataObtained(
                dataConverter.getStepsData(
                    mMotionInfo
                )
            )*/
        }

        override fun onResponseSleepInfo(mSleepInfo: SleepInfo) {

            val sleepData = dataConverter.getSleepData(mSleepInfo)
            LOGS.d(sleepData)
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.SleepDataObtained(
                    sleepData
                )
            )
            //userActivityDataCallbacks?.onSleepDataObtained(sleepData)
        }

        override fun onResponseWoHeartInfo(mWoHeartInfo: WoHeartInfo) {
            LOGS.d(mWoHeartInfo)
            onHeartHistoryObtained(mWoHeartInfo)
        }

        override fun onResponseComplete() {
            LOGS.d("onResponseComplete -- Complete")
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.UserDataSyncUpdated(
                    SyncDataStatus(status = EventConstants.UPDATE_STATUS_SUCCESS)
                )
            )

        }

        override fun onResponsePhoto() {

        }

        override fun onResponseFindPhone() {
            // LoggerHelper.printVerbose("noise_fit_event:rn_command", "Ringerrrr 22")
            // PhoneRinger.enableRing(true)
            // Timer().schedule(timerTask {
            //
            // }, 5000)
        }

        override fun onResponseCloseCall() {
            // LoggerHelper.printVerbose("noise_fit_event:rn_command", "close call:: 2")
            // CallHandler.updateCallStatus(false)
        }

        override fun onResponseHeartInfo(mHeartInfo: HeartInfo) {

        }

        override fun onResponseMusicControlCmd(p0: Int) {

        }
    }


    private fun onHeartHistoryObtained(mWoHeartInfo: WoHeartInfo) {
        val heartRateList = ArrayList<HeartRate>()
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DATE]
        calendar.set(year, month, day, 0, 0, 0)
        mWoHeartInfo.woHeartData?.let { healthHeartRate ->
            healthHeartRate.forEachIndexed { _, item ->
                calendar.add(Calendar.MINUTE, 5)
                if (item as Int != 0) {
                    val time = DateFormats.timeFormat.format(calendar.time)
                    val heartRate = HeartRate()
                    heartRate.resetData = true
                    heartRate.averageHeartRate = item
                    heartRate.date = DateFormats.getConvertToDateFormat(
                        mWoHeartInfo.woHeartDate,
                        DateFormats.dateFormat3,
                        DateFormats.dateFormat
                    )
                    heartRate.time = time
                    heartRate.timeStamp =
                        DateFormats.convertDateTimeToTimeStamp(heartRate.date!!, time)
                    heartRate.highestHeartRate = mWoHeartInfo.woHeartDayMax
                    heartRate.lowestHeartRate = mWoHeartInfo.woHeartDayMin
                    heartRate.restingHeartRate = mWoHeartInfo.woHeartSleepAvg
                    heartRateList.add(heartRate)
                }
            }
        }
        userActivityDataCallbacks?.onUserActivityDataReceived(
            UserActivityCallback.HeartHistoryObtained(
                heartRateList
            )
        )
    }
}
