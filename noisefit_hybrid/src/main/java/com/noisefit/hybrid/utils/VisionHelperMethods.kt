package com.noisefit.hybrid.utils

import cn.appscomm.bluetoothsdk.app.BluetoothSDK
import cn.appscomm.bluetoothsdk.interfaces.ResultCallBack
import com.noisefit.hybrid.base.VisionCommands
import com.noisefit_commans.models.BloodOxygenStressData
import com.noisefit_commans.models.HeartRate
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.utils.LOGS
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import javax.inject.Inject


class VisionHelperMethods
@Inject
constructor(
    private val bitwiseHelperUtils: BitwiseHelperUtils,
    private val visionCommands: VisionCommands
) {
    private var sleepCount = 0
  //  private var bloodOxygenCount = 0

    fun getHeartRateObservable(): Observable<Pair<List<HeartRate>, Int>> {
        LOGS.d("VisionHelperMethods Heart Rate ---start---")
        return Observable.create { emitter: ObservableEmitter<Pair<List<HeartRate>, Int>> ->
            try {
                visionCommands.sendCommand(visionCommands.HEART_RATE_PRESSURE_QUERY_CMD)
                var bloodOxygenCount =0
                val heartList = ArrayList<HeartRate>()
                var totalHeartRate = 0
                BluetoothSDK.set8002CallBack(object : ResultCallBack {
                    override fun onSuccess(p0: Int, p1: Array<Any>) {
                        val bytesString = bitwiseHelperUtils.bytesArrayResult(p1)

                        if (bytesString.contains(visionCommands.HEART_RATE_PRESSURE_COUNT_RESPONSE)) {
                            totalHeartRate = bitwiseHelperUtils.getHeartRateCount(bytesString)
                            bloodOxygenCount = bitwiseHelperUtils.getBloodPressureCount(bytesString)
                            LOGS.d(
                                "VisionHelperMethods Heart Rate count $totalHeartRate $bloodOxygenCount"
                            )

                            if (totalHeartRate > 0) {
                                visionCommands.sendCommand(visionCommands.UPLOAD_HEART_RATE_CMD)

                            } else {
                                emitter.onNext(Pair(heartList, bloodOxygenCount))
                                emitter.onComplete()
                            }


                        } else if (bytesString.contains(visionCommands.HEART_RATE_PRESSURE_DATA_RESPONSE)) {
                            heartList.add(bitwiseHelperUtils.getParseHeartRate(bytesString))
                            LOGS.d(
                                "VisionHelperMethods Heart Rate list ${heartList.size} $totalHeartRate ${
                                    bitwiseHelperUtils.getHeartNumber(
                                        bytesString
                                    )
                                }"
                            )



                            if (bitwiseHelperUtils.getHeartNumber(bytesString) == totalHeartRate) {
                                emitter.onNext(Pair(heartList, bloodOxygenCount))
                                emitter.onComplete()
                                visionCommands.sendCommand(visionCommands.DELETE_HEART_RATE_CMD)
                                // clear callback
                                BluetoothSDK.set8002CallBack(null)
                            }
                        }
                    }

                    override fun onFail(p0: Int) {
                        LOGS.d("VisionHelperMethods Heart Rate onfail")

                    }
                })

            } catch (e: Exception) {

                emitter.onError(e)
                e.printStackTrace()
            }
        }
    }

    fun getStressDataObservable(bloodOxygenCount: Int): Observable<ArrayList<BloodOxygenStressData>> {

        return Observable.create { emitter: ObservableEmitter<ArrayList<BloodOxygenStressData>> ->
            try {
                LOGS.d("VisionHelperMethods Stress ---start---- $bloodOxygenCount")
                val bloodPressureList = ArrayList<BloodOxygenStressData>()

                if(bloodOxygenCount>0){
                    visionCommands.sendCommand(visionCommands.UPLOAD_BLOOD_PRESSURE_CMD)
                }else{
                    emitter.onNext(bloodPressureList)
                    emitter.onComplete()
                }


                BluetoothSDK.set8002CallBack(object : ResultCallBack {
                    override fun onSuccess(p0: Int, p1: Array<Any>) {
                        val bytesString = bitwiseHelperUtils.bytesArrayResult(p1)

                        if (bytesString.contains(visionCommands.BLOOD_OXYGEN_STRESS_RESPONSE)) {
                            val bloodOxygenStressData = BloodOxygenStressData()
                            bitwiseHelperUtils.getParseStressData(bytesString)?.let {
                                bloodOxygenStressData.stressDataBreakup = it
                            }
                            bitwiseHelperUtils.getParseOxygenBreakup(bytesString)?.let {
                                bloodOxygenStressData.bloodOxygenBreakup = it
                            }

                            bloodPressureList.add(bloodOxygenStressData)


                            LOGS.d(
                                "VisionHelperMethods Stress final list ${bloodPressureList.size} $bloodOxygenCount ${
                                    bitwiseHelperUtils.getBloodOxygenNumber(
                                        bytesString
                                    )
                                }"
                            )
                            if (bitwiseHelperUtils.getBloodOxygenNumber(bytesString) == bloodOxygenCount) {
                                emitter.onNext(bloodPressureList)
                                emitter.onComplete()
                                visionCommands.sendCommand(visionCommands.DELETE_PRESSURE_CMD)

                                // clear callback
                                BluetoothSDK.set8002CallBack(null)
                            }
                        }
                    }

                    override fun onFail(p0: Int) {
                        LOGS.d("VisionHelperMethods Stress onfail")

                    }
                })

            } catch (e: Exception) {

                emitter.onError(e)
                e.printStackTrace()
            }
        }
    }


    fun getSleepDataObservable(): Observable<SleepData> {

        return Observable.create { emitter: ObservableEmitter<SleepData> ->
            try {
                LOGS.d("VisionHelperMethods Sleep ---start---- ")
                val sleepList = ArrayList<String>()
                visionCommands.sendCommand(visionCommands.SLEEP_CMD)

                BluetoothSDK.set8002CallBack(object : ResultCallBack {
                    override fun onSuccess(p0: Int, p1: Array<Any>) {

                        val bytesString = bitwiseHelperUtils.bytesArrayResult(p1)

                        if (bytesString.contains(visionCommands.SLEEP_RESPONSE)) {
                            sleepList.add(bytesString)
                            if (bitwiseHelperUtils.parseSleepTypeEnd(bytesString)) {
                                sleepCount += 1
                                val finalSleepDataBreakup = ArrayList<String>()
                                finalSleepDataBreakup.addAll(sleepList)
                                sleepList.clear()
                                emitter.onNext(
                                    bitwiseHelperUtils.parseSleepData(
                                        finalSleepDataBreakup
                                    )
                                )
                            }
                        }


                    }

                    override fun onFail(p0: Int) {
                        LOGS.d("VisionHelperMethods Sleep onfail")

                    }
                })

            } catch (e: Exception) {

                emitter.onError(e)
                e.printStackTrace()
            }
        }
    }


}