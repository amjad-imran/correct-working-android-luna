package com.oreo.data.db.implementation

import androidx.room.Transaction
import com.google.gson.Gson
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.model.DayTimeMovementBreakup
import com.noisefit_commans.data.model.OreoHeartRate
import com.oreo.data.db.abstaction.OreoDayTimeMovementDataSource
import com.oreo.data.db.database.OreoDayTimeMovementDao
import com.oreo.data.db.database.OreoRespiratoryDao
import javax.inject.Inject


class OreoDayTimeMovementDataImpl
@Inject
constructor(
    private val dayTimeDao: OreoDayTimeMovementDao
) : OreoDayTimeMovementDataSource {
    @Transaction
    override suspend fun insertData(data: DayTimeMovementBreakup): Boolean {
        if (data.date == null) {
            return false
        }


        val prevData = getTodayData(data.date!!)

        if (prevData == null) {
            dayTimeDao.insert(data)
        } else {

            val mergedData = getMergedData(prevData,data)

            //val newBreakup = Gson().fromJson<List<Int>>(data.breakUp ?: "")
            val prevBreakup = Gson().fromJson<List<Int>>(prevData.breakUp ?: "")
            if (mergedData.sum() != prevBreakup.sum()) {
                dayTimeDao.updateViaDate(data.breakUp ?: "", data.date!!, false)
            }
        }
        return true
    }

    private fun getMergedData(prevData: DayTimeMovementBreakup, newData: DayTimeMovementBreakup) : List<Int>{
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
    ): List<DayTimeMovementBreakup>? {
        return dayTimeDao.getServerUnSyncData(isSync)
    }

    override suspend fun deleteOldData(days: Int): Int {
        return dayTimeDao.deleteOlderData(days)
    }

    override suspend fun updateServerSyncData(
        dataList: List<DayTimeMovementBreakup>,
        timeStamp: Long
    ): Int {
        /*val chunkList = dataList.chunked(500)

        chunkList.forEach { chunk ->
            val ids = ArrayList<Int>()
            chunk.forEach { data ->
                ids.add(data.id)
            }
            //  LOGS.d("updateServerSyncData ${Gson().toJson(ids)}")
            bloodOxygenDao.updateServerUnSyncStatus(ids, true, timeStamp)
        }*/

        return 1
    }


    override suspend fun getTodayData(date: String): DayTimeMovementBreakup? {
        return dayTimeDao.getTodayData(date)
    }

    override suspend fun getTodayDayTimeMovement(date: String): String? {
        return dayTimeDao.getTodayData(date)?.breakUp
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
