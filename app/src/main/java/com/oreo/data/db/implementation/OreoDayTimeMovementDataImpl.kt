package com.oreo.data.db.implementation

import androidx.room.Transaction
import com.noisefit_commans.data.model.DayTimeMovementBreakup
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
            dayTimeDao.updateViaDate(data.breakUp ?: "", data.date!!)
        }
        return true
    }


    override suspend fun getUnSyncServerData(
        endDate: Long, isSync: Boolean
    ): List<DayTimeMovementBreakup>? {
        return dayTimeDao.getServerUnSyncData(isSync)
    }

    override suspend fun deleteOldData(timeStamp: Long): Int {
        //return bloodOxygenDao.deleteOlderData(timeStamp)
        return 1
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


    override suspend fun getTodayData(date: String): List<DayTimeMovementBreakup>? {
        //return bloodOxygenDao.getTodayData(date)
        return null
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
