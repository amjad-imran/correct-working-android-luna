package com.noisefit.data.local.db.implementation

import androidx.room.Transaction
import com.noisefit.data.local.db.abstraction.StressDataSource
import com.noisefit.data.local.db.database.StressDao
import com.noisefit_commans.models.StressDataBreakup
import javax.inject.Inject


class StressDataImpl
@Inject
constructor(
    private val stressDao: StressDao
) : StressDataSource {
    @Transaction
    override suspend fun insertData(data: List<StressDataBreakup>): Boolean {
        if (data.isNullOrEmpty() || data[0].date == null) {
            return false
        }

        stressDao.insertAll(data)
        return true
    }

    override suspend fun getTodayData(date: String): List<StressDataBreakup>? {
        return stressDao.getTodayData(date)
    }


    override suspend fun getUnSyncServerData(
        endDate: Long,
        isSync: Boolean
    ): List<StressDataBreakup>? {
        return stressDao.getUnSyncServerData(endDate, isSync)
    }

    override suspend fun deleteOldData(timeStamp: Long): Int {
        return stressDao.deleteOlderData(timeStamp)
    }

    override suspend fun updateServerSyncData(
        dataList: List<StressDataBreakup>,
        timeStamp: Long
    ): Int {

        val chunkList = dataList.chunked(500)

        chunkList.forEach { chunk ->
            val ids = ArrayList<Int>()
            chunk.forEach { data ->
                ids.add(data.id)
            }
            //  LOGS.d("updateServerSyncData ${Gson().toJson(ids)}")
            stressDao.updateServerUnSyncStatus(ids, true, timeStamp)
        }


        return 1
    }

    override suspend fun getStressBetweenTimeStamp(
        startTimeStamp: Long,
        endTimeStamp: Long
    ): List<StressDataBreakup>? {
        return stressDao.getStressBetweenTimeStamp(startTimeStamp, endTimeStamp)
    }

    override suspend fun checkHalfSyncData() {

        val halfSyncList = stressDao.checkHalfSyncData(false)
        val chunkList = halfSyncList?.chunked(500)

        chunkList?.forEach { chunk ->
            val dateList = ArrayList<String>()
            chunk.forEach { data ->
                dateList.add(data.date!!)
            }
            //  LOGS.d("updateServerSyncData ${Gson().toJson(ids)}")
            stressDao.updateHalfSyncData(false, dateList)
        }

    }
}
