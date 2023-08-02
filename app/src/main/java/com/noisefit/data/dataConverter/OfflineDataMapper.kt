package com.noisefit.data.dataConverter

import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.models.DeviceType
import javax.inject.Inject


class OfflineDataMapper
@Inject constructor(
    val watches: WatchesSDK
) {

    fun convertSleepData(data: List<OreoSleepData>?): OreoSleepData {
        val sleepData = OreoSleepData()
        if (data.isNullOrEmpty()) {
            return sleepData
        }

        if (data.size == 1) {
            return data[0]
        }

        val watchType = watches.getWatchType()
        if (watchType == SDKWatchType.SDK_RYEEX) {
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

}