package com.oreo.data.db.implementation

import androidx.room.Transaction
import com.google.gson.Gson
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.model.OreoHeartRate
import com.noisefit_commans.data.model.OreoStressDataBreakup
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.db.abstaction.OreoStressDataSource
import com.oreo.data.db.database.OreoStressDao
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

class OreoStressDataImpl
@Inject
constructor(
    private val stressDao: OreoStressDao
) : OreoStressDataSource {
    @Transaction
    override suspend fun insertData(data: OreoStressDataBreakup): Boolean {

        if (data.date == null) {
            return false
        }

        val prevData = getTodayData(data.date!!)

        if (prevData == null) {
            stressDao.insert(data)
        } else {
            stressDao.updateViaDate(data.breakUp ?: "", data.date!!,false)
        }

        return true
    }

    override suspend fun getTodayData(date: String): OreoStressDataBreakup? {
        return stressDao.getTodayData(date)
    }


    override suspend fun getUnSyncServerData(
        endDate: Long,
        isSync: Boolean
    ): List<OreoStressDataBreakup>? {
        return stressDao.getServerUnSyncData(isSync)
    }

    override suspend fun deleteOldData(timeStamp: Long): Int {
        return 1
        //return stressDao.deleteOlderData(timeStamp)
    }

    override suspend fun updateServerSyncData(
        dataList: List<OreoStressDataBreakup>,
        timeStamp: Long
    ): Int {

        val ids = ArrayList<Int>()
        dataList.forEach { data ->
            ids.add(data.id)
        }
        return stressDao.updateServerUnSyncStatus(ids, true)
    }

    override suspend fun getStressBetweenTimeStamp(
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

    private fun extractDataByEndTime(day2Data: OreoStressDataBreakup, day2MinutesCeil: Double): List<Int> {
        val endPos = (day2MinutesCeil / 5 - 1).toInt()
        val breakupArray = Gson().fromJson<List<Int>>(day2Data.breakUp ?: "")
        if (breakupArray.size != 288) {
            val currentSize = breakupArray.size
            breakupArray.toMutableList()
            for (i in currentSize until 288) {
                (breakupArray as ArrayList<Int>).add(0)
            }
        }
        return breakupArray.subList(0, endPos)
    }

    private fun extractDataByStartTime(dayData: OreoStressDataBreakup, dayStartMinutes: Double): List<Int> {
        val startPos = (dayStartMinutes / 5 - 1).toInt()
        val breakupArray = Gson().fromJson<List<Int>>(dayData.breakUp ?: "")
        if (breakupArray.size != 288) {
            val currentSize = breakupArray.size
            breakupArray.toMutableList()
            for (i in currentSize until 288) {
                (breakupArray as ArrayList<Int>).add(0)
            }
        }
        return breakupArray.subList(startPos, 288)
    }

    private fun extractDataByStartTimeEndTime(
        dayData: OreoStressDataBreakup,
        dayStartMinutes: Double,
        day2MinutesCeil: Double
    ): List<Int> {
        val startPos = (dayStartMinutes / 5 - 1).toInt()
        val endPos = (day2MinutesCeil / 5 - 1).toInt()
        val breakupArray = Gson().fromJson<List<Int>>(dayData.breakUp ?: "")
        if (breakupArray.size != 288) {
            val currentSize = breakupArray.size
            breakupArray.toMutableList()
            for (i in currentSize until 288) {
                (breakupArray as ArrayList<Int>).add(0)
            }
        }
        return breakupArray.subList(startPos, endPos)
    }

    override suspend fun checkHalfSyncData() {

       /* val halfSyncList = stressDao.checkHalfSyncData(false)
        val chunkList = halfSyncList?.chunked(500)

        chunkList?.forEach { chunk ->
            val dateList = ArrayList<String>()
            chunk.forEach { data ->
                dateList.add(data.date!!)
            }
            //  LOGS.d("updateServerSyncData ${Gson().toJson(ids)}")
            stressDao.updateHalfSyncData(false, dateList)
        }*/

    }
}