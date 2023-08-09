package com.oreo.data.db.implementation

import androidx.room.Transaction
import com.google.gson.Gson
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.model.OreoHeartRate
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.db.abstaction.OreoHeartRateDataSource
import com.oreo.data.db.database.OreoHeartRateDao
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

class OreoHeartRateDataImpl
@Inject
constructor(
    private val heartRateDao: OreoHeartRateDao
) : OreoHeartRateDataSource {

    @Transaction
    override suspend fun insertData(data: OreoHeartRate): Boolean {
        if (data.date == null) {
            return false
        }


        val prevData = getTodayData(data.date!!)

        if (prevData == null) {
            heartRateDao.insert(data)
        } else {
            heartRateDao.updateViaDate(data.breakUp ?: "", data.date!!, false)
        }

        return true
    }


    override suspend fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<OreoHeartRate>? {
        return heartRateDao.getServerUnSyncData(isSync)
    }

    override suspend fun getTodayData(date: String): OreoHeartRate? {
        return heartRateDao.getTodayData(date)
    }

    override suspend fun deleteOldData(timeStamp: Long): Int {
        //return heartRateDao.deleteOlderData(timeStamp)
        return 1
    }

    override suspend fun updateServerSyncData(dataList: List<OreoHeartRate>, timeStamp: Long): Int {
        val ids = ArrayList<Int>()
        dataList.forEach { data ->
            ids.add(data.id)
        }
        return heartRateDao.updateServerUnSyncStatus(ids, true)
    }

    override suspend fun checkHalfSyncData() {

        /*

                val halfSyncList = heartRateDao.checkHalfSyncData(false)
                val heartList = halfSyncList?.chunked(500)

                heartList?.forEach { heartRateList ->
                    val dateList = ArrayList<String>()
                    heartRateList.forEach { data ->
                        dateList.add(data.date!!)
                    }
                    //  LOGS.d("updateServerSyncData ${Gson().toJson(ids)}")
                    heartRateDao.updateHalfSyncData(false, dateList)
                }
        */


    }

    override suspend fun getHeartRateBetweenTimeStamp(
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
            LOGS.d("SLEEP_HR day1Data : $startTimeStamp endTimeStamp: $endTimeStamp")

            val day1Data = getTodayData(startDate)
            val day2Data = getTodayData(endDate)

            LOGS.d("SLEEP_HR startTimeStamp : $day1Data day2Data: $day2Data")

            if (day1Data == null || day2Data == null) return ArrayList()

            val day1Minutes = DateFormats.getDayElapsedMinutesFromTimeStamp(startTimeStamp)
            val day2Minutes = DateFormats.getDayElapsedMinutesFromTimeStamp(endTimeStamp)

            LOGS.d("SLEEP_HR day1Minutes : $day1Minutes day2Minutes: $day2Minutes")


            if (day1Minutes == null || day2Minutes == null) return ArrayList()

            val day1MinutesCeil = 5 * (floor(abs(day1Minutes.toDouble() / 5)))
            val day2MinutesCeil = 5 * (ceil(abs(day2Minutes.toDouble() / 5)))

            LOGS.d("SLEEP_HR day1MinutesCeil : $day1MinutesCeil day2MinutesCeil: $day2MinutesCeil")

            val day1List = extractDataByStartTime(day1Data, day1MinutesCeil)
            val day2List = extractDataByEndTime(day2Data, day2MinutesCeil)

            LOGS.d("SLEEP_HR day1List : $day1List day2List: $day2List")

            return day1List.toMutableList().apply {
                addAll(day2List)
            }
        }
    }

    private fun extractDataByEndTime(day2Data: OreoHeartRate, day2MinutesCeil: Double): List<Int> {
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

    private fun extractDataByStartTime(dayData: OreoHeartRate, dayStartMinutes: Double): List<Int> {
        var startPos = (dayStartMinutes / 5 - 1).toInt()
        val breakupArray = Gson().fromJson<List<Int>>(dayData.breakUp ?: "")
        if (startPos < 0) {
            startPos = 0
        }
        if (breakupArray.size != 288) {
            val currentSize = breakupArray.size
            breakupArray.toMutableList()
            for (i in currentSize until 288) {
                (breakupArray as ArrayList<Int>).add(0)
            }
        }
        return breakupArray.subList(startPos, 288)
    }

    /**
     * 12:03 - 6:17
     */
    private fun extractDataByStartTimeEndTime(
        dayData: OreoHeartRate,
        dayStartMinutes: Double,
        day2MinutesCeil: Double
    ): List<Int> {
        var startPos = (dayStartMinutes / 5 - 1).toInt()
        val endPos = (day2MinutesCeil / 5 - 1).toInt()
        if (startPos < 0) {
            startPos = 0
        }
        val breakupArray = Gson().fromJson<List<Int>>(dayData.breakUp ?: "")
        LOGS.d("extractDataByStartTimeEndTime ${startPos} ${endPos}")
        LOGS.d("extractDataByStartTimeEndTime ${breakupArray.size} ${dayData.breakUp}")
        if (breakupArray.size != 288) {
            val currentSize = breakupArray.size
            breakupArray.toMutableList()
            for (i in currentSize until 288) {
                (breakupArray as ArrayList<Int>).add(0)
            }
        }
        return breakupArray.subList(startPos, endPos)
    }
}
