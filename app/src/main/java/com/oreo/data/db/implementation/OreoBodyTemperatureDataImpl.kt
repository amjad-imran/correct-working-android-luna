package com.oreo.data.db.implementation

import androidx.room.Transaction
import com.google.gson.Gson
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.model.OreoBodyTemperatureBreakup
import com.noisefit_commans.data.model.OreoRespiratoryData
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.db.abstaction.OreoBodyTemperatureDataSource
import com.oreo.data.db.database.OreoBodyTemperatureDao
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

class OreoBodyTemperatureDataImpl
@Inject
constructor(
    private val bodyTemperatureDao: OreoBodyTemperatureDao
) : OreoBodyTemperatureDataSource {
    @Transaction
    override suspend fun insertData(data: OreoBodyTemperatureBreakup): Boolean {

        if (data.date == null) {
            return false
        }

        val prevData = getTodayData(data.date!!)

        if (prevData == null) {
            bodyTemperatureDao.insert(data)
        } else {
            bodyTemperatureDao.updateViaDate(data.breakUp ?: "", data.date!!,false)
        }

        return true
    }

    override suspend fun getTodayData(date: String): OreoBodyTemperatureBreakup? {
        return bodyTemperatureDao.getTodayData(date)
    }


    override suspend fun getUnSyncServerData(
        endDate: Long, isSync: Boolean
    ): List<OreoBodyTemperatureBreakup>? {
        return bodyTemperatureDao.getServerUnSyncData(isSync)
    }

    override suspend fun deleteOldData(timeStamp: Long): Int {
        //return bodyTemperatureDao.deleteOlderData(timeStamp)
        return 0
    }

    override suspend fun updateServerSyncData(
        dataList: List<OreoBodyTemperatureBreakup>,
        timeStamp: Long
    ): Int {
        val ids = ArrayList<Int>()
        dataList.forEach { data ->
            ids.add(data.id)
        }
        return bodyTemperatureDao.updateServerUnSyncStatus(ids, true)
    }

    override suspend fun getDataBetweenTimeStamp(
        startTimeStamp: Long,
        endTimeStamp: Long
    ): List<Float> {
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
            val day1MinutesCeil = 5 * (floor(abs(day1Minutes.toDouble() / 5)))
            val day2MinutesCeil = 5 * (ceil(abs(day2Minutes.toDouble() / 5)))

            return extractDataByStartTimeEndTime(day1Data, day1MinutesCeil, day2MinutesCeil)
        } else {
            val day1Data = getTodayData(startDate)
            val day2Data = getTodayData(endDate)

            if (day1Data == null || day2Data == null) return ArrayList()

            val day1Minutes = DateFormats.getDayElapsedMinutesFromTimeStamp(startTimeStamp)
            val day2Minutes = DateFormats.getDayElapsedMinutesFromTimeStamp(endTimeStamp)

            if (day1Minutes == null || day2Minutes == null) return ArrayList()

            val day1MinutesCeil = 5 * (floor(abs(day1Minutes.toDouble() / 5)))
            val day2MinutesCeil = 5 * (ceil(abs(day2Minutes.toDouble() / 5)))


            val day1List = extractDataByStartTime(day1Data, day1MinutesCeil)
            val day2List = extractDataByEndTime(day2Data, day2MinutesCeil)
            return day1List.toMutableList().apply {
                addAll(day2List)
            }
        }
    }

    private fun extractDataByStartTime(dayData: OreoBodyTemperatureBreakup, dayStartMinutes: Double): List<Float> {
        var startPos = (dayStartMinutes / 5 - 1).toInt()
        val breakupArray = Gson().fromJson<List<Float>>(dayData.breakUp ?: "")
        if (startPos < 0) {
            startPos = 0
        }
        if (breakupArray.size != 288) {
            val currentSize = breakupArray.size
            breakupArray.toMutableList()
            for (i in currentSize until 288) {
                (breakupArray as ArrayList<Float>).add(0f)
            }
        }
        return breakupArray.subList(startPos, 288)
    }
    private fun extractDataByEndTime(day2Data: OreoBodyTemperatureBreakup, day2MinutesCeil: Double): List<Float> {
        val endPos = (day2MinutesCeil / 5).toInt()
        val breakupArray = Gson().fromJson<List<Float>>(day2Data.breakUp ?: "")
        if (breakupArray.size != 288) {
            val currentSize = breakupArray.size
            breakupArray.toMutableList()
            for (i in currentSize until 288) {
                (breakupArray as ArrayList<Float>).add(0f)
            }
        }
        return breakupArray.subList(0, endPos)
    }

    private fun extractDataByStartTimeEndTime(
        dayData: OreoBodyTemperatureBreakup,
        dayStartMinutes: Double,
        day2MinutesCeil: Double
    ): List<Float> {
        var startPos = (dayStartMinutes / 5 - 1).toInt()
        val endPos = (day2MinutesCeil / 5).toInt()
        if (startPos < 0) {
            startPos = 0
        }
        val breakupArray = Gson().fromJson<List<Float>>(dayData.breakUp ?: "")
        if (breakupArray.size != 288) {
            val currentSize = breakupArray.size
            breakupArray.toMutableList()
            for (i in currentSize until 288) {
                (breakupArray as ArrayList<Float>).add(0f)
            }
        }
        return breakupArray.subList(startPos, endPos)
    }

    override suspend fun checkHalfSyncData() {

        /*val halfSyncList = bodyTemperatureDao.checkHalfSyncData(false)
        val chunkList = halfSyncList?.chunked(500)

        chunkList?.forEach { dataList ->
            val dateList = ArrayList<String>()
            dataList.forEach { data ->
                dateList.add(data.date!!)
            }
            //  LOGS.d("updateServerSyncData ${Gson().toJson(ids)}")
            bodyTemperatureDao.updateHalfSyncData(false, dateList)
        }*/


    }
}