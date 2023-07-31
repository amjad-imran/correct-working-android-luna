package com.oreo.data.db.implementation

import androidx.room.Transaction
import com.noisefit_commans.data.model.OreoBodyTemperatureBreakup
import com.oreo.data.db.abstaction.OreoBodyTemperatureDataSource
import com.oreo.data.db.database.OreoBodyTemperatureDao
import javax.inject.Inject

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