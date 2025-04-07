package com.oreo.data.db.implementation

import androidx.room.Transaction
import com.google.gson.Gson
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.model.OreoBloodOxygenBreakup
import com.noisefit_commans.data.model.OreoBodyStressData
import com.noisefit_commans.data.model.OreoHeartRate
import com.noisefit_commans.data.model.OreoRespiratoryData
import com.noisefit_commans.data.model.OreoStressDataBreakup
import com.noisefit_commans.models.BloodOxygenBreakup
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.db.abstaction.OreoBloodOxygenDataSource
import com.oreo.data.db.abstaction.OreoBodyStressDataSource
import com.oreo.data.db.abstaction.OreoRespiratoryDataSource
import com.oreo.data.db.database.OreoBloodOxygenDao
import com.oreo.data.db.database.OreoBodyStressDao
import com.oreo.data.db.database.OreoRespiratoryDao
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor


class OreoBodyStressDataImpl
@Inject
constructor(
    private val bodyStressDao: OreoBodyStressDao
) : OreoBodyStressDataSource {
    @Transaction
    override suspend fun insertData(data: OreoBodyStressData): Boolean {
        if (data.date == null) {
            return false
        }

        val prevData = getTodayData(data.date!!)

        if (prevData == null) {
            bodyStressDao.insert(data)
        } else {
            val mergedData = getMergedData(prevData,data)

            //val newBreakup = Gson().fromJson<List<Int>>(data.breakUp ?: "")
            val prevBreakup = Gson().fromJson<List<Int>>(prevData.breakUp ?: "")
            if (mergedData.sum() != prevBreakup.sum()) {
                bodyStressDao.updateViaDate(data.breakUp ?: "", data.date!!, false)
            }
        }
        return true
    }

    private fun getMergedData(prevData: OreoBodyStressData, newData: OreoBodyStressData) : List<Int>{
        val prevBreakup = Gson().fromJson<List<Int>>(prevData.breakUp ?: "")
        val newBreakup = Gson().fromJson<List<Int>>(newData.breakUp ?: "")

        val mergedData = ArrayList<Int>()

        newBreakup.forEachIndexed { index, value->
            try {
                if(value == 0){
                    mergedData.add(prevBreakup[index])
                }else{
                    mergedData.add(value)
                }
            }catch (exp: Exception){
                mergedData.add(0)
            }
        }
        return mergedData
    }


    override suspend fun getUnSyncServerData(
        endDate: Long, isSync: Boolean
    ): List<OreoBodyStressData>? {
        return bodyStressDao.getServerUnSyncData(isSync)
    }

    override suspend fun deleteOldData(days: Int): Int {
        return bodyStressDao.deleteOlderData(days)
    }

    override suspend fun updateServerSyncData(
        dataList: List<OreoBodyStressData>,
        timeStamp: Long
    ): Int {

        val ids = ArrayList<Int>()
        dataList.forEach { data ->
            ids.add(data.id)
        }
        return bodyStressDao.updateServerUnSyncStatus(ids, true)

    }


    override suspend fun getTodayData(date: String): OreoBodyStressData? {
        return bodyStressDao.getTodayData(date)
    }

    override suspend fun getDataBetweenTimeStamp(
        startTimeStamp: Long,
        endTimeStamp: Long
    ): List<Int> {
        val startDate = DateFormats.getDateFromTimeStamp(startTimeStamp)
        val endDate = DateFormats.getDateFromTimeStamp(endTimeStamp)

        if (startDate == null || endDate == null) {
            return ArrayList()
        }
        if (startDate.equals(endDate, true)) {
            val day1Data = getTodayData(startDate) ?: return ArrayList()
            val day1Minutes =
                DateFormats.getDayElapsedMinutesFromTimeStamp(startTimeStamp)
            val day2Minutes = DateFormats.getDayElapsedMinutesFromTimeStamp(endTimeStamp)
            if (day1Minutes == null || day2Minutes == null) return ArrayList()
            val day1MinutesCeil = 15 * (floor(abs(day1Minutes.toDouble() / 15)))
            val day2MinutesCeil = 15 * (ceil(abs(day2Minutes.toDouble() / 15)))

            return extractDataByStartTimeEndTime(day1Data, day1MinutesCeil, day2MinutesCeil)
        } else {
            val day1Data = getTodayData(startDate)
            val day2Data = getTodayData(endDate)

            if (day1Data == null || day2Data == null) return ArrayList()

            val day1Minutes = DateFormats.getDayElapsedMinutesFromTimeStamp(startTimeStamp)
            val day2Minutes = DateFormats.getDayElapsedMinutesFromTimeStamp(endTimeStamp)

            if (day1Minutes == null || day2Minutes == null) return ArrayList()

            val day1MinutesCeil = 15 * (floor(abs(day1Minutes.toDouble() / 15)))
            val day2MinutesCeil = 15 * (ceil(abs(day2Minutes.toDouble() / 15)))


            val day1List = extractDataByStartTime(day1Data, day1MinutesCeil)
            val day2List = extractDataByEndTime(day2Data, day2MinutesCeil)
            return day1List.toMutableList().apply {
                addAll(day2List)
            }
        }
    }

    private fun extractDataByStartTime(dayData: OreoBodyStressData, dayStartMinutes: Double): List<Int> {
        var startPos = (dayStartMinutes / 15 - 1).toInt()
        val breakupArray = Gson().fromJson<List<Int>>(dayData.breakUp ?: "")
        if (startPos < 0) {
            startPos = 0
        }
        if (breakupArray.size != 96) {
            val currentSize = breakupArray.size
            breakupArray.toMutableList()
            for (i in currentSize until 96) {
                (breakupArray as ArrayList<Int>).add(0)
            }
        }
        return breakupArray.subList(startPos, 96)
    }
    private fun extractDataByEndTime(day2Data: OreoBodyStressData, day2MinutesCeil: Double): List<Int> {
        val endPos = (day2MinutesCeil / 15).toInt()
        val breakupArray = Gson().fromJson<List<Int>>(day2Data.breakUp ?: "")
        if (breakupArray.size != 96) {
            val currentSize = breakupArray.size
            breakupArray.toMutableList()
            for (i in currentSize until 96) {
                (breakupArray as ArrayList<Int>).add(0)
            }
        }
        return breakupArray.subList(0, endPos)
    }

    private fun extractDataByStartTimeEndTime(
        dayData: OreoBodyStressData,
        dayStartMinutes: Double,
        day2MinutesCeil: Double
    ): List<Int> {
        var startPos = (dayStartMinutes / 15 - 1).toInt()
        val endPos = (day2MinutesCeil / 15).toInt()
        if (startPos < 0) {
            startPos = 0
        }
        val breakupArray = Gson().fromJson<List<Int>>(dayData.breakUp ?: "")
        if (breakupArray.size != 96) {
            val currentSize = breakupArray.size
            breakupArray.toMutableList()
            for (i in currentSize until 96) {
                (breakupArray as ArrayList<Int>).add(0)
            }
        }
        return breakupArray.subList(startPos, endPos)
    }

    override suspend fun checkHalfSyncData() {

/*
        val halfSyncList = bloodOxygenDao.checkHalfSyncData(false)
        val chunkList = halfSyncList?.chunked(500)

        chunkList?.forEach { dataList ->
            val dateList = ArrayList<String>()
            dataList.forEach { data ->
                dateList.add(data.date!!)
            }
            //  LOGS.d("updateServerSyncData ${Gson().toJson(ids)}")
            bloodOxygenDao.updateHalfSyncData(false, dateList)
        }*/
    }
}
