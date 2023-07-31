package com.noisefit.data.local.db.implementation

import androidx.room.Transaction
import com.noisefit.data.local.db.abstraction.BloodOxygenDataSource
import com.noisefit.data.local.db.database.BloodOxygenDao
import com.noisefit_commans.models.BloodOxygenBreakup
import javax.inject.Inject


class BloodOxygenDataImpl
@Inject
constructor(
    private val bloodOxygenDao: BloodOxygenDao
) : BloodOxygenDataSource {
    @Transaction
    override suspend fun insertData(data: List<BloodOxygenBreakup>): Boolean {
        if (data.isNullOrEmpty() || data[0].date == null) {
            return false
        }
//        if (!data.isNullOrEmpty() && data[0].resetData) {
//            //delete data
//        }
        bloodOxygenDao.insertAll(data)
        return true
    }


    override suspend fun getUnSyncServerData(
        endDate: Long, isSync: Boolean
    ): List<BloodOxygenBreakup>? {
        return bloodOxygenDao.getUnSyncServerData(endDate, isSync)
    }

    override suspend fun deleteOldData(timeStamp: Long): Int {
        return bloodOxygenDao.deleteOlderData(timeStamp)
    }

    override suspend fun updateServerSyncData(
        dataList: List<BloodOxygenBreakup>,
        timeStamp: Long
    ): Int {
        val chunkList = dataList.chunked(500)

        chunkList.forEach { chunk ->
            val ids = ArrayList<Int>()
            chunk.forEach { data ->
                ids.add(data.id)
            }
            //  LOGS.d("updateServerSyncData ${Gson().toJson(ids)}")
            bloodOxygenDao.updateServerUnSyncStatus(ids, true, timeStamp)
        }

        return 1
    }


    override suspend fun getTodayData(date: String): List<BloodOxygenBreakup>? {
        return bloodOxygenDao.getTodayData(date)
    }

    override suspend fun checkHalfSyncData() {


        val halfSyncList = bloodOxygenDao.checkHalfSyncData(false)
        val chunkList = halfSyncList?.chunked(500)

        chunkList?.forEach { dataList ->
            val dateList = ArrayList<String>()
            dataList.forEach { data ->
                dateList.add(data.date!!)
            }
            //  LOGS.d("updateServerSyncData ${Gson().toJson(ids)}")
            bloodOxygenDao.updateHalfSyncData(false, dateList)
        }
    }
}
