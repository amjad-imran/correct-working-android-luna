package com.noisefit.hybrid.handler

import android.os.Looper
import android.util.LongSparseArray
import cn.appscomm.bluetoothsdk.app.BluetoothSDK
import cn.appscomm.bluetoothsdk.interfaces.GPSDataCallback
import cn.appscomm.bluetoothsdk.interfaces.ResultCallBack
import cn.appscomm.bluetoothsdk.model.GPSData
import cn.appscomm.bluetoothsdk.model.HeartRateData
import cn.appscomm.bluetoothsdk.model.RealTimeSportData
import com.noisefit.hybrid.base.NFHybridApplicationHandler
import com.noisefit.hybrid.dataconversions.DataConverter
import com.noisefit.hybrid.utils.VisionHelperMethods
import com.noisefit_commans.constants.EventConstants
import com.noisefit_commans.interfaces.data.IUserActivityDataCallback
import com.noisefit_commans.interfaces.data.UserActivityCallback
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import java.util.logging.Handler
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

//Vision callbacks flow to get data
//1. Steps
//2. Heart rate
//3. check for stress data if yes a. stress data
//                            no  b. sleep data
//4. Sleep data
class NFHUserActivityHandler
@Inject constructor(
    private var nfhApplicationHandler: NFHybridApplicationHandler,
    private val dataConverter: DataConverter,
    private val visionHelperMethods: VisionHelperMethods
) : UserActivityDataActions() {

    private var userActivityDataCallbacks: IUserActivityDataCallback? = null
    private var colorFitDevice: ColorFitDevice? = null

    override fun <T> callbackListenerNew(callback: T) {
        userActivityDataCallbacks = callback as IUserActivityDataCallback
    }


    override fun init() {
        super.init()
        nfhApplicationHandler.initSdk()
    }

    override fun setDevice(colorFitDevice: ColorFitDevice) {
        this.colorFitDevice = colorFitDevice
    }

    private fun sendStartSyncStatus() {

        userActivityDataCallbacks?.onUserActivityDataReceived(
            UserActivityCallback.UserDataSyncUpdated(
                SyncDataStatus(status = EventConstants.UPDATE_STATUS_STARTED)
            )
        )
    }

//    private fun sendCompletedSyncStatus() {
//        userActivityDataCallbacks?.onUserActivityDataReceived(
//            UserActivityCallback.UserDataSyncUpdated(
//                SyncDataStatus(status = EventConstants.UPDATE_STATUS_SUCCESS)
//            )
//        )
//    }

    override fun syncUserActivity(date: String, isRefresh: Boolean) {
        getStepsData(date)
        sendStartSyncStatus()
        if (colorFitDevice?.deviceType != DeviceType.COLORFIT_VISION.deviceType) {
            getHeartRate()
            getSleepData(date)
        }

    }

    override fun getBodyTemperatureData() {

    }
    override fun getStepsData(date: String) {
        BluetoothSDK.getSportData(resultCallBack)
    }

    override fun getSleepData(date: String) {
        if (colorFitDevice?.deviceType == DeviceType.COLORFIT_VISION.deviceType) {
            getSleepDataForVision()
        } else {
            BluetoothSDK.getSleepData(resultCallBack)
        }

    }

    private fun getSleepDataForVision() {
        visionHelperMethods.getSleepDataObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ sleepDataList ->
                LOGS.d("VisionHelperMethods Sleep List finish")
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.SleepDataObtained(
                        sleepDataList
                    )
                )
            }, {
                it.printStackTrace()

            })
    }

    override fun getStressCount() {

    }

    private fun getStressAndBloodData(bloodOxygenCount: Int) {
        visionHelperMethods.getStressDataObservable(bloodOxygenCount)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ bloodOxygenStressList ->
                bloodOxygenStressList?.let {
                    val bloodOxygenList = ArrayList<BloodOxygenBreakup>()
                    val stressList = ArrayList<StressDataBreakup>()
                    it.forEach { data ->
                        data.bloodOxygenBreakup?.let { bloodOxygenBreakup ->
                            bloodOxygenList.add(bloodOxygenBreakup)
                        }
                        data.stressDataBreakup?.let { stressDataBreakup ->
                            stressList.add(stressDataBreakup)
                        }
                    }

                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.BloodOxygenObtained(
                            bloodOxygenList
                        )
                    )

                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.StressDataObtained(
                            stressList
                        )
                    )

                    getSleepData()
//                    sendCompletedSyncStatus()
                }
                LOGS.d("getStressAndBloodData final data $bloodOxygenStressList")
            }, {
                it.printStackTrace()

            })
    }

    private fun getSleepData() {
        getSleepData(DateFormats.getDateFormat())
    }

    override fun attachCallbacks() {
//        if (colorFitDevice?.deviceType != DeviceType.COLORFIT_VISION.deviceType) {
//            if(nfhApplicationHandler.sdkInitStatus){
//                BluetoothSDK.setRealHeartRateCallBack(resultCallBack, true)
//            }
//
//        }

    }

    override fun <T> callbackListener(callback: T) {}

    override fun getHeartRate() {
        when (colorFitDevice?.deviceType) {
            DeviceType.COLORFIT_NAV.deviceType -> {
                BluetoothSDK.getHeartRateData(resultCallBack)
            }
            DeviceType.NOISEFIT_HYBRID.deviceType -> BluetoothSDK.getHeartRateData(resultCallBack)
            DeviceType.COLORFIT_VISION.deviceType -> {
                BluetoothSDK.getHeartRateData(resultCallBack)
                getVisionHeartRate()
            }
        }
    }

    private fun getVisionHeartRate() {
        LOGS.d("getHeartRateObservable ---getVisionHeartRate---")
        visionHelperMethods.getHeartRateObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it?.first?.let { heartList ->
                    userActivityDataCallbacks?.onUserActivityDataReceived(
                        UserActivityCallback.HeartHistoryObtained(
                            heartRateData = heartList
                        )
                    )
                }



                it?.second?.let { bloodOxygenCount ->

                    getStressAndBloodData(bloodOxygenCount)

                }


                LOGS.d("getHeartRateObservable final data $it")
            }, {
                it.printStackTrace()

            })
    }


    override fun getBloodOxygenLevel() {}

    override fun getBloodPressure() {}

    private var resultCallBack: ResultCallBack = object : ResultCallBack {
        override fun onSuccess(p0: Int, p1: Array<out Any>?) {
            when (p0) {
                ResultCallBack.TYPE_GET_SLEEP_DATA -> {
                    onSleepDataObtained(p1)

                }

                ResultCallBack.TYPE_GET_HEART_RATE_DATA -> {
                    onHeartHistoryObtained(p1)

                }
                ResultCallBack.TYPE_GET_SPORT_DATA -> {
                    onStepsDataObtained(p1)

                }
                ResultCallBack.TYPE_GET_REAL_TIME_SPORT_DATA -> {
                    when (colorFitDevice?.deviceType) {
                        DeviceType.COLORFIT_NAV.deviceType, DeviceType.COLORFIT_VISION.deviceType -> {
                            val all_gps_data = ArrayList<ArrayList<GPSDataResponse>>()
                            var totalActivities = 0
                            p1?.let {
                                val act_data = p1[0] as LinkedList<*>
                                (p1[0] as LinkedList<*>).forEach { item ->
                                    val sportsData = item as RealTimeSportData

                                    //LoggerHelper.printVerbose("GPS DATA", "getGPSData size"+act_data.count())
                                    BluetoothSDK.getGPSDataEx(object : ResultCallBack {
                                        override fun onSuccess(p0: Int, gpsData: Array<out Any>?) {
                                            LOGS.d("GPS DATA", "getGPSData" + act_data.count())
                                            val gps_data = ArrayList<GPSDataResponse>()
                                            gpsData?.let {
                                                val data =
                                                    gpsData[0] as LongSparseArray<LinkedList<GPSData>>
                                                if (data.size() > 0) {
                                                    for (i in 0..data.size() - 1) {
                                                        val key = data.keyAt(i)
                                                        (data.get(key) as LinkedList<GPSData>).forEach { item ->
                                                            val gpsData1 = item as GPSData

                                                            val gpsDataResponse = GPSDataResponse(
                                                                latitude = gpsData1.latitude,
                                                                longitude = gpsData1.longitude
                                                            )

                                                            gps_data.add(gpsDataResponse)
                                                        }
                                                        all_gps_data.add(gps_data)
                                                    }
                                                } else {
                                                    val gpsDataResponse = GPSDataResponse(
                                                        latitude = 0.0,
                                                        longitude = 0.0
                                                    )
                                                    gps_data.add(gpsDataResponse)
                                                    all_gps_data.add(gps_data)
                                                }
                                            }
                                            totalActivities = totalActivities + 1
                                            if (totalActivities == act_data.count()) {
                                                //LoggerHelper.printVerbose("GPS DATA", "getGPSData call1"+act_data.count()+":"+totalActivities)
                                                onSportsDataObtainedGPS(p1, all_gps_data)
                                                onDeleteSportsData()
                                            }

                                            android.os.Handler(Looper.getMainLooper()).postDelayed({
                                                userActivityDataCallbacks?.onUserActivityDataReceived(
                                                    UserActivityCallback.SportsModeDataSyncSuccess()
                                                )
                                            },2000)

                                        }

                                        override fun onFail(p0: Int) {
                                            totalActivities = totalActivities + 1
                                            val gpsDataResponse =
                                                GPSDataResponse(latitude = 0.0, longitude = 0.0)
                                            val gps_data = ArrayList<GPSDataResponse>()
                                            gps_data.add(gpsDataResponse)
                                            all_gps_data.add(gps_data)
                                            if (totalActivities == act_data.count()) {
                                                //LoggerHelper.printVerbose("GPS DATA", "getGPSData call1"+act_data.count()+":"+totalActivities)
                                                onSportsDataObtainedGPS(p1, all_gps_data)
                                                onDeleteSportsData()
                                            }
                                            android.os.Handler(Looper.getMainLooper()).postDelayed({
                                                userActivityDataCallbacks?.onUserActivityDataReceived(
                                                    UserActivityCallback.SportsModeDataSyncSuccess()
                                                )
                                            },2000)

                                            //LoggerHelper.printVerbose("GPS DATA", "getGPSData onfail")
                                            //onSportsDataObtained(p1)
                                        }
                                    }, object : GPSDataCallback {
                                        override fun onProgress(max: Int, progress: Int) {
                                            LOGS.d("GPS DATA", "getGPSData" + max + "::" + progress)
                                        }

                                        override fun onComplete() {
                                            LOGS.d("GPS DATA", "getGPSData oncomplete")
                                            //userActivityDataCallbacks?.onUserActivityDataReceived(UserActivityCallback.SportsModeDataSyncSuccess())
                                        }

                                        override fun onError(p0: Int) {
                                            LOGS.d("GPS DATA", "getGPSData onerror")
                                        }
                                    }, sportsData.index)
                                }
                            }
                            //onSportsDataObtainedGPS(p1,all_gps_data)


                        }
                        DeviceType.NOISEFIT_HYBRID.deviceType -> onSportsDataObtained(p1)

                    }
                }
            }
        }

        override fun onFail(p0: Int) {
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.UserDataSyncUpdated(
                    SyncDataStatus(status = EventConstants.UPDATE_STATUS_FAILED)
                )
            )
        }
    }

    private fun onSportsDataObtained(p1: Array<out Any>?) {
        userActivityDataCallbacks?.onUserActivityDataReceived(
            UserActivityCallback.SportsModeDataObtained(
                dataConverter.parseSportsData(p1)
            )
        )


    }

    private fun onSportsDataObtainedGPS(p1: Array<out Any>?, p2: List<List<GPSDataResponse>>) {
        userActivityDataCallbacks?.onUserActivityDataReceived(
            UserActivityCallback.SportsModeDataObtainedGPS(
                dataConverter.parseSportsDataGPS(p1, p2)
            )
        )

    }

    private fun onStepsDataObtained(p1: Array<out Any>?) {
        val stepsData = dataConverter.getStepsData(p1)
        userActivityDataCallbacks?.onUserActivityDataReceived(
            UserActivityCallback.StepsDataObtained(
                stepsData
            )
        )
        if (colorFitDevice?.deviceType == DeviceType.COLORFIT_VISION.deviceType) {
            getHeartRate()
        }
        return
    }

    override fun syncSportsActivity(date: String) {
        LOGS.d("Activity data --start--")
        BluetoothSDK.getRealTimeSportData(resultCallBack)
    }

    private fun onHeartHistoryObtained(p1: Array<out Any>?) {
        p1?.let {
            val heartRateList = p1[0] as LinkedList<*>
            val heartList = ArrayList<HeartRate>()
            heartRateList.forEach { heartRate ->
                val item = heartRate as HeartRateData
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = item.timestamp * 1000
                val date = DateFormats.dateFormat.format(calendar.time)

                if (date == DateFormats.getTodaysDateString(7)) {
                    val time= DateFormats.timeFormat.format(calendar.time)
                    val timeStamp = DateFormats.convertDateTimeToTimeStamp(date, time)
                    val heartRateData = HeartRate(
                        averageHeartRate = heartRate.avg,
                        time = time,
                        timeStamp = timeStamp,
                        date = date
                    )
                    heartList.add(heartRateData)
                }
            }

            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.HeartHistoryObtained(
                    heartRateData = heartList
                )
            )

//            sendCompletedSyncStatus()
        }
    }

    override fun getHeartHistory(calendar: Calendar) {

        if (colorFitDevice?.deviceType != DeviceType.COLORFIT_VISION.deviceType) {
            BluetoothSDK.getHeartRateData(resultCallBack)
        }

    }

    private fun onSleepDataObtained(p1: Array<out Any>?) {
        val sleepData = dataConverter.getSleepData(p1)
        userActivityDataCallbacks?.onUserActivityDataReceived(
            UserActivityCallback.SleepDataObtained(
                sleepData
            )
        )
        //userActivityDataCallbacks.onSleepDataObtained(sleepData)
    }

    private fun onDeleteSportsData() {
        BluetoothSDK.deleteAllGPSData(object : ResultCallBack {
            override fun onSuccess(p0: Int, gpsData: Array<out Any>?) {
                //LoggerHelper.printVerbose("GPS DATA", "getGPSData delete")
            }

            override fun onFail(p0: Int) {
                //LoggerHelper.printVerbose("GPS DATA", "getGPSData deleteonfail")
            }
        })
        BluetoothSDK.deleteRealTimeSportData(object : ResultCallBack {
            override fun onSuccess(p0: Int, gpsData: Array<out Any>?) {
                //LoggerHelper.printVerbose("GPS DATA", "getGPSData datadelete")
            }

            override fun onFail(p0: Int) {
                //LoggerHelper.printVerbose("GPS DATA", "getGPSData datadeleteonfail")
            }
        })
    }
}
